package net.iriscan.samples

import net.iriscan.sdk.BiometricSdkFactory
import net.iriscan.sdk.BiometricSdkOperations
import net.iriscan.sdk.core.image.ImageColorType
import net.iriscan.sdk.face.FaceEncodeProperties
import net.iriscan.sdk.face.FaceExtractProperties
import net.iriscan.sdk.face.FaceMatchProperties
import net.iriscan.sdk.face.FaceNetModelConfiguration
import net.iriscan.sdk.iris.IrisEncodeProperties
import net.iriscan.sdk.iris.IrisExtractProperties
import net.iriscan.sdk.iris.IrisMatchProperties
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.exitProcess

/**
 * Runs the SDK the way an application does, on the images listed in samples/manifest.json:
 * every image is encoded, every pair of images of the same modality is compared, and each
 * comparison is checked against the expectation (same person: match, different people: no
 * match). Writes results/jvm.json and a Markdown summary, and exits with 1 when an
 * expectation fails, so CI shows what to fix in the SDK.
 *
 * Usage (from the repository root): ../sdk/gradlew -p jvm run   or   gradle -p jvm run
 */

/** FaceNet model used by the example apps: 160x160 input, 128-number embedding. */
private const val FACENET_MODEL_URL =
    "https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true"
private const val FACENET_INPUT_SIZE = 160
private const val FACENET_EMBEDDING_LENGTH = 128

/** Distance threshold for FaceNet embeddings; the same value the README uses. */
private const val FACE_MATCH_THRESHOLD = 1.0

private class Sample(val id: String, val modality: String, val person: String, val file: File)

private class Encoded(val sample: Sample, val template: ByteArray?, val error: String?, val millis: Long)

private class Comparison(val a: Sample, val b: Sample, val score: Double, val matches: Boolean) {
    val samePerson get() = a.person == b.person
    val correct get() = matches == samePerson
}

fun main(args: Array<String>) {
    val root = File(args.getOrElse(0) { "." }).absoluteFile
    val samples = readManifest(root)
    if (samples.isEmpty() || samples.any { !it.file.exists() }) {
        System.err.println("Sample images are missing: run scripts/fetch-samples.sh first.")
        exitProcess(2)
    }

    // Face needs native libraries (OpenCV, TensorFlow Lite) on the JVM. If they do not load, the
    // failure is reported as a face result and the iris part still runs.
    var faceUnavailable: String? = null
    try {
        BiometricSdkFactory.initialize(
            BiometricSdkFactory.configBuilder()
                .withIris(IrisExtractProperties(), IrisEncodeProperties(), IrisMatchProperties())
                .withFace(
                    extractor = FaceExtractProperties(),
                    encoder = FaceEncodeProperties(
                        FaceNetModelConfiguration(
                            path = FACENET_MODEL_URL,
                            inputWidth = FACENET_INPUT_SIZE, inputHeight = FACENET_INPUT_SIZE,
                            outputLength = FACENET_EMBEDDING_LENGTH,
                            modelChecksum = null, modelChecksumMethod = null, overrideCacheOnWrongChecksum = null,
                        )
                    ),
                    matcher = FaceMatchProperties(threshold = FACE_MATCH_THRESHOLD),
                )
                .build()
        )
    } catch (e: Throwable) {
        faceUnavailable = "SDK did not initialize face: ${e::class.simpleName}: ${e.message}"
        System.err.println(faceUnavailable)
        e.printStackTrace()
        BiometricSdkFactory.initialize(
            BiometricSdkFactory.configBuilder()
                .withIris(IrisExtractProperties(), IrisEncodeProperties(), IrisMatchProperties())
                .build()
        )
    }
    val sdk = BiometricSdkFactory.getInstance() ?: error("SDK did not initialize")

    val decoderProblems = checkImageDecoder(sdk, samples)
    val encoded = samples.map { sample ->
        if (sample.modality == "face" && faceUnavailable != null) Encoded(sample, null, faceUnavailable, 0)
        else encode(sdk, sample)
    }
    val comparisons = compareAll(sdk, encoded)
    val failures = report(root, encoded, comparisons, decoderProblems)
    exitProcess(if (failures == 0) 0 else 1)
}

private fun readManifest(root: File): List<Sample> {
    val data = File(root, "samples/data")
    val list = JSONObject(File(root, "samples/manifest.json").readText()).getJSONArray("samples")
    return (0 until list.length()).map { i ->
        val s = list.getJSONObject(i)
        val id = s.getString("id")
        // fetch-samples.sh saves every image as <id>.<original extension>
        val file = data.listFiles()?.firstOrNull { it.nameWithoutExtension == id } ?: File(data, id)
        Sample(id, s.getString("modality"), s.getString("person"), file)
    }
}

/**
 * The SDK decodes PNG and BMP itself, in common code. On the JVM the result can be checked
 * against ImageIO: both must give the same pixels.
 */
private fun checkImageDecoder(sdk: BiometricSdkOperations, samples: List<Sample>): List<String> {
    val problems = ArrayList<String>()
    for (sample in samples.filter { it.file.extension.lowercase() in setOf("png", "bmp") }) {
        try {
            val ours = sdk.io().readImage(sample.file.readBytes())
            val reference = ImageIO.read(sample.file)
            val gray = ours.colorType == ImageColorType.GRAY
            var differing = 0
            for (y in 0 until reference.height) for (x in 0 until reference.width) {
                val rgb = reference.getRGB(x, y) and 0xFFFFFF
                val expected = if (gray) rgb and 0xFF else rgb
                if (ours[x, y] != expected) differing++
            }
            if (ours.width != reference.width || ours.height != reference.height) problems += "${sample.id}: size ${ours.width}x${ours.height}, ImageIO ${reference.width}x${reference.height}"
            else if (differing > 0) problems += "${sample.id}: $differing pixels differ from ImageIO"
        } catch (e: Throwable) {
            problems += "${sample.id}: ${e::class.simpleName}: ${e.message}"
        }
    }
    return problems
}

private fun encode(sdk: BiometricSdkOperations, sample: Sample): Encoded {
    val start = System.nanoTime()
    return try {
        val template = when (sample.modality) {
            // iris: PNG and BMP through the SDK's own decoder (the same on every platform),
            // other formats (CASIA ships JPEG) through the platform decoder and convert()
            "iris" -> {
                val image = when (sample.file.extension.lowercase()) {
                    "png", "bmp" -> sdk.io().readImage(sample.file.readBytes())
                    else -> sdk.io().convert(ImageIO.read(sample.file))
                }
                sdk.iris().encoder().extractAndEncode(image)
            }
            // face: the platform image type (BufferedImage on the JVM)
            "face" -> sdk.face().encoder().extractAndEncode(ImageIO.read(sample.file))
            else -> error("unknown modality ${sample.modality}")
        }
        Encoded(sample, template, if (template == null) "no ${sample.modality} found" else null, (System.nanoTime() - start) / 1_000_000)
    } catch (e: Throwable) {
        Encoded(sample, null, "${e::class.simpleName}: ${e.message}", (System.nanoTime() - start) / 1_000_000)
    }
}

private fun compareAll(sdk: BiometricSdkOperations, encoded: List<Encoded>): List<Comparison> {
    val out = ArrayList<Comparison>()
    for (i in encoded.indices) for (j in i + 1 until encoded.size) {
        val a = encoded[i]
        val b = encoded[j]
        if (a.sample.modality != b.sample.modality || a.template == null || b.template == null) continue
        when (a.sample.modality) {
            "iris" -> {
                val matcher = sdk.iris().matcher()
                out += Comparison(a.sample, b.sample, matcher.score(a.template, b.template), matcher.matches(a.template, b.template))
            }
            "face" -> {
                val matcher = sdk.face().matcher()
                out += Comparison(a.sample, b.sample, matcher.matchScore(a.template, b.template), matcher.matches(a.template, b.template))
            }
        }
    }
    return out
}

/** Writes results/jvm.json and prints a Markdown summary; returns the number of failed expectations. */
private fun report(root: File, encoded: List<Encoded>, comparisons: List<Comparison>, decoderProblems: List<String>): Int {
    val lines = ArrayList<String>()
    var failures = decoderProblems.size
    lines += "## SDK samples on the JVM"
    lines += ""
    val decoded = encoded.count { it.sample.file.extension.lowercase() in setOf("png", "bmp") }
    lines += "### Image decoder: ${decoded - decoderProblems.size} of $decoded PNG/BMP files decoded exactly as ImageIO does"
    lines += ""
    for (p in decoderProblems) lines += "- $p"
    if (decoderProblems.isNotEmpty()) lines += ""
    for (modality in encoded.map { it.sample.modality }.distinct()) {
        val enc = encoded.filter { it.sample.modality == modality }
        val cmp = comparisons.filter { it.a.modality == modality }
        val notEncoded = enc.filter { it.template == null }
        val wrong = cmp.filter { !it.correct }
        failures += notEncoded.size + wrong.size
        val genuine = cmp.filter { it.samePerson }
        val impostor = cmp.filter { !it.samePerson }
        lines += "### $modality: ${enc.size - notEncoded.size} of ${enc.size} images encoded, " +
                "${cmp.size - wrong.size} of ${cmp.size} comparisons as expected"
        lines += ""
        lines += "Same person: ${genuine.count { it.matches }} of ${genuine.size} match" +
                (if (genuine.isNotEmpty()) ", scores ${"%.3f".format(genuine.minOf { it.score })} to ${"%.3f".format(genuine.maxOf { it.score })}" else "") +
                ". Different people: ${impostor.count { !it.matches }} of ${impostor.size} do not match" +
                (if (impostor.isNotEmpty()) ", scores ${"%.3f".format(impostor.minOf { it.score })} to ${"%.3f".format(impostor.maxOf { it.score })}" else "") +
                ". Average encoding time ${enc.map { it.millis }.average().toLong()} ms."
        if (notEncoded.isNotEmpty() || wrong.isNotEmpty()) {
            lines += ""
            lines += "| Problem | Images | Score |"
            lines += "|---|---|---|"
            for (e in notEncoded) lines += "| not encoded: ${e.error} | ${e.sample.id} | |"
            for (c in wrong) lines += "| ${if (c.samePerson) "same person, no match" else "different people, match"} | ${c.a.id}, ${c.b.id} | ${"%.3f".format(c.score)} |"
        }
        lines += ""
    }
    lines += if (failures == 0) "All expectations met." else "$failures expectations failed."
    val summary = lines.joinToString("\n")
    println(summary)
    System.getenv("GITHUB_STEP_SUMMARY")?.let { File(it).appendText(summary + "\n") }

    val json = JSONObject()
        .put("platform", "jvm")
        .put("failures", failures)
        .put("encoded", JSONArray(encoded.map {
            JSONObject().put("id", it.sample.id).put("ok", it.template != null).put("error", it.error ?: JSONObject.NULL)
                .put("templateBytes", it.template?.size ?: 0).put("millis", it.millis)
        }))
        .put("comparisons", JSONArray(comparisons.map {
            JSONObject().put("a", it.a.id).put("b", it.b.id).put("samePerson", it.samePerson)
                .put("score", it.score).put("matches", it.matches)
        }))
    File(root, "results").mkdirs()
    File(root, "results/jvm.json").writeText(json.toString(1))
    return failures
}
