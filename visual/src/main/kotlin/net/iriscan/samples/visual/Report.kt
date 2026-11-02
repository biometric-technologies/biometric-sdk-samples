package net.iriscan.samples.visual

import net.iriscan.sdk.BiometricSdkFactory
import net.iriscan.sdk.BiometricSdkOperations
import net.iriscan.sdk.core.image.Image
import net.iriscan.sdk.core.image.ImageColorType
import net.iriscan.sdk.face.FaceEncodeProperties
import net.iriscan.sdk.face.FaceExtractProperties
import net.iriscan.sdk.face.FaceMatchProperties
import net.iriscan.sdk.face.FaceNetModelConfiguration
import net.iriscan.sdk.iris.IrisBoundaries
import net.iriscan.sdk.iris.IrisEncodeProperties
import net.iriscan.sdk.iris.IrisExtractProperties
import net.iriscan.sdk.iris.IrisMatchProperties
import net.iriscan.sdk.iris.IrisQuality
import org.json.JSONObject
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import java.util.Locale
import javax.imageio.ImageIO
import kotlin.system.exitProcess

/**
 * Step-by-step report of what the SDK does with every sample image, as one HTML page
 * (results/report.html, images inlined, opens in any browser):
 *
 * - iris: the photo with the segmentation drawn over it (pupil, limbus, eyelids, from
 *   IrisExtractor.segment), the unwrapped iris with its mask (IrisExtractor.extract), the ISO
 *   quality values and issues, and whether a template was made;
 * - face: the photo and the face the extractor cropped from it;
 * - per modality, the score of every pair, coloured by whether the SDK's decision was the
 *   expected one (same person: match; different people: no match).
 *
 * The results table goes to results/visual.md (the platform contract in the README), and the
 * program exits with 1 if an image did not encode or a pair was decided wrongly.
 *
 * The iris images may be used for research but not published, so the report is for your own
 * computer. CI runs it with --no-images: the same page with every picture left out, which still
 * shows the segmentation numbers and the score matrix.
 *
 * Usage (from the repository root): sdk/gradlew -p visual run [--args="--no-images"]
 */

private const val FACENET_MODEL_URL =
    "https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true"
private const val FACENET_INPUT_SIZE = 160
private const val FACENET_EMBEDDING_LENGTH = 128
private const val FACE_MATCH_THRESHOLD = 1.0

/** Photos are shown at most this wide, so the page stays a few megabytes. */
private const val PHOTO_WIDTH = 360

/** Value of an occluded sample in the texture extract() returns (eyelids, lashes, reflections). */
private const val MASKED = -1

private class Sample(val id: String, val modality: String, val person: String, val file: File)

/** Everything the report shows about one image. Null fields are steps the SDK did not reach. */
private class Result(
    val sample: Sample,
    val photo: BufferedImage?,
    val boundaries: IrisBoundaries? = null,
    val texture: Image? = null,
    val quality: IrisQuality? = null,
    val faceCrop: BufferedImage? = null,
    val template: ByteArray? = null,
    val error: String? = null,
)

private class Pair(val a: Sample, val b: Sample, val score: Double, val matches: Boolean) {
    val expected get() = a.person == b.person
    val correct get() = matches == expected
}

fun main(args: Array<String>) {
    val withImages = "--no-images" !in args
    val root = File(args.firstOrNull { !it.startsWith("--") } ?: ".").absoluteFile
    val samples = readManifest(root)
    if (samples.isEmpty() || samples.any { !it.file.exists() }) {
        System.err.println("Sample images are missing: run scripts/fetch-samples.sh first.")
        exitProcess(2)
    }
    val (sdk, faceProblem) = initialize()

    val results = samples.map { s ->
        when {
            s.modality == "iris" -> iris(sdk, s)
            faceProblem != null -> Result(s, ImageIO.read(s.file), error = faceProblem)
            else -> face(sdk, s)
        }
    }
    val pairs = compareAll(sdk, results)

    File(root, "results").mkdirs()
    File(root, "results/report.html").writeText(html(results, pairs, withImages))
    val (table, failures) = table(results, pairs)
    File(root, "results/visual.md").writeText(table)
    println(table)
    println("Report: ${File(root, "results/report.html")}")
    exitProcess(if (failures == 0) 0 else 1)
}

/** Iris and face as the JVM sample sets them up; if face's native libraries fail, iris still runs. */
private fun initialize(): kotlin.Pair<BiometricSdkOperations, String?> {
    val iris = BiometricSdkFactory.configBuilder()
        .withIris(IrisExtractProperties(), IrisEncodeProperties(), IrisMatchProperties())
    var faceProblem: String? = null
    try {
        BiometricSdkFactory.initialize(
            iris.withFace(
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
            ).build()
        )
    } catch (e: Throwable) {
        faceProblem = "face did not initialize: ${e::class.simpleName}: ${e.message}"
        BiometricSdkFactory.initialize(
            BiometricSdkFactory.configBuilder()
                .withIris(IrisExtractProperties(), IrisEncodeProperties(), IrisMatchProperties())
                .build()
        )
    }
    return (BiometricSdkFactory.getInstance() ?: error("SDK did not initialize")) to faceProblem
}

/** Each iris step separately, so the report shows how far the SDK got and where it stopped. */
private fun iris(sdk: BiometricSdkOperations, s: Sample): Result {
    val photo = ImageIO.read(s.file)
    return try {
        // PNG and BMP through the SDK's own decoder, JPEG (CASIA) through ImageIO and convert(),
        // as an application would read them
        val image = when (s.file.extension.lowercase()) {
            "png", "bmp" -> sdk.io().readImage(s.file.readBytes())
            else -> sdk.io().convert(photo)
        }
        val extractor = sdk.iris().extractor()
        val boundaries = extractor.segment(image)
        val texture = extractor.extract(image)
        val quality = runCatching { sdk.iris().quality().assess(image) }.getOrNull()
        val template = sdk.iris().encoder().extractAndEncode(image)
        val error = when {
            boundaries == null -> "no iris found (segmentation)"
            texture == null -> "iris found, but too little of it is visible (extract)"
            template == null -> "no template (encode)"
            else -> null
        }
        Result(s, photo, boundaries, texture, quality, template = template, error = error)
    } catch (e: Throwable) {
        Result(s, photo, error = "${e::class.simpleName}: ${e.message}")
    }
}

private fun face(sdk: BiometricSdkOperations, s: Sample): Result {
    val photo = ImageIO.read(s.file)
    return try {
        val crop = sdk.face().extractor().extract(photo)
        val template = sdk.face().encoder().extractAndEncode(photo)
        val error = when {
            crop == null -> "no face found (extractor)"
            template == null -> "no embedding (encoder)"
            else -> null
        }
        Result(s, photo, faceCrop = crop, template = template, error = error)
    } catch (e: Throwable) {
        Result(s, photo, error = "${e::class.simpleName}: ${e.message}")
    }
}

private fun compareAll(sdk: BiometricSdkOperations, results: List<Result>): List<Pair> {
    val out = ArrayList<Pair>()
    for (i in results.indices) for (j in i + 1 until results.size) {
        val a = results[i]
        val b = results[j]
        val ta = a.template ?: continue
        val tb = b.template ?: continue
        if (a.sample.modality != b.sample.modality) continue
        out += when (a.sample.modality) {
            "iris" -> sdk.iris().matcher().let { Pair(a.sample, b.sample, it.score(ta, tb), it.matches(ta, tb)) }
            else -> sdk.face().matcher().let { Pair(a.sample, b.sample, it.matchScore(ta, tb), it.matches(ta, tb)) }
        }
    }
    return out
}

private fun readManifest(root: File): List<Sample> {
    val data = File(root, "samples/data")
    val list = JSONObject(File(root, "samples/manifest.json").readText()).getJSONArray("samples")
    // fingerprints are listed for when the SDK can match them; until then they are skipped
    return (0 until list.length()).map { list.getJSONObject(it) }.filter { it.getString("modality") != "finger" }.map { s ->
        val id = s.getString("id")
        val file = data.listFiles()?.firstOrNull { it.nameWithoutExtension == id } ?: File(data, id)
        Sample(id, s.getString("modality"), s.getString("person"), file)
    }
}

// ---------------------------------------------------------------- results table (contract)

private fun table(results: List<Result>, pairs: List<Pair>): kotlin.Pair<String, Int> {
    val sb = StringBuilder("### visual\n| modality | encoded | comparisons as expected |\n|---|---|---|\n")
    val problems = ArrayList<String>()
    for (m in results.map { it.sample.modality }.distinct()) {
        val r = results.filter { it.sample.modality == m }
        val p = pairs.filter { it.a.modality == m }
        sb.append("| $m | ${r.count { it.error == null }} / ${r.size} | ${p.count { it.correct }} / ${p.size} |\n")
        r.filter { it.error != null }.forEach { problems += "${it.sample.id}: ${it.error}" }
        p.filter { !it.correct }.forEach {
            problems += "${it.a.id} / ${it.b.id}: ${if (it.expected) "same person, no match" else "different people, match"} (${fmt(it.score)})"
        }
    }
    sb.append("\nFailures: ${if (problems.isEmpty()) "none" else ""}\n")
    problems.forEach { sb.append("- $it\n") }
    return sb.toString() to problems.size
}

// ---------------------------------------------------------------- HTML

private fun html(results: List<Result>, pairs: List<Pair>, withImages: Boolean): String {
    val sb = StringBuilder()
    sb.append("""<!doctype html><html><head><meta charset="utf-8"><title>SDK samples, step by step</title><style>
body{font:14px/1.45 system-ui,sans-serif;margin:24px;color:#1b1f24;background:#fff}
h1{font-size:22px}h2{font-size:18px;margin-top:32px}
.card{display:flex;gap:16px;align-items:flex-start;border:1px solid #d8dde3;border-radius:8px;padding:12px;margin:10px 0}
.card.bad{border-color:#d1242f;background:#fff5f5}
.step{display:flex;flex-direction:column;gap:4px;font-size:12px;color:#57606a}
.meta{font-size:13px;min-width:260px}.meta b{color:#1b1f24}
.err{color:#d1242f;font-weight:600}.ok{color:#1a7f37;font-weight:600}
table.m{border-collapse:collapse;font-size:11px}table.m td,table.m th{border:1px solid #fff;padding:3px 5px;text-align:center}
table.m th{background:#f3f5f7;font-weight:500;white-space:nowrap}
.g{background:#c8ecd2}.gm{background:#8fd6a3}.r{background:#f5a3a8;font-weight:700}.d{background:#eceff2}
.legend span{display:inline-block;padding:2px 8px;margin-right:8px;border-radius:4px}
.noimg{width:${PHOTO_WIDTH}px;height:60px;display:flex;align-items:center;justify-content:center;background:#f3f5f7;color:#8c959f}
</style></head><body>""")
    sb.append("<h1>SDK samples, step by step</h1>")
    val (table, failures) = table(results, pairs)
    sb.append("<p>").append(if (failures == 0) "<span class=ok>Everything as expected.</span>" else "<span class=err>$failures problems</span> (marked red below).")
    sb.append(" Iris: the pupil is drawn <span style='color:#e5484d'>red</span>, the limbus <span style='color:#0091ff'>blue</span>, eyelids <span style='color:#f5a000'>orange</span>; in the unwrapped iris, masked samples are yellow.</p>")
    sb.append("<pre>").append(esc(table)).append("</pre>")

    for (m in results.map { it.sample.modality }.distinct()) {
        sb.append("<h2>${esc(m)}</h2>")
        for (r in results.filter { it.sample.modality == m }) sb.append(card(r, withImages))
        sb.append(matrix(results.filter { it.sample.modality == m }.map { it.sample }, pairs.filter { it.a.modality == m }, m))
    }
    return sb.append("</body></html>").toString()
}

private fun card(r: Result, withImages: Boolean): String {
    val sb = StringBuilder("<div class='card${if (r.error != null) " bad" else ""}' id='${esc(r.sample.id)}'>")
    // 1. the photo, with the segmentation drawn over it
    sb.append("<div class=step>")
    val photo = r.photo
    if (photo != null) {
        val scale = PHOTO_WIDTH.toDouble() / photo.width
        val h = (photo.height * scale).toInt()
        sb.append("<svg width='$PHOTO_WIDTH' height='$h' viewBox='0 0 ${photo.width} ${photo.height}'>")
        if (withImages) sb.append("<image width='${photo.width}' height='${photo.height}' href='${png(scaled(photo, PHOTO_WIDTH), "jpeg")}'/>")
        else sb.append("<rect width='${photo.width}' height='${photo.height}' fill='#f3f5f7'/>")
        r.boundaries?.let { sb.append(overlay(it, photo.width)) }
        sb.append("</svg>")
    }
    sb.append("photo${if (r.boundaries != null) " + segmentation" else ""}</div>")
    // 2. the unwrapped iris, or the face crop
    r.texture?.let { t ->
        sb.append("<div class=step>")
        if (withImages) sb.append("<img width='360' src='${png(texture(t), "png")}'>") else sb.append("<div class=noimg>${t.width} × ${t.height}</div>")
        val masked = (0 until t.width * t.height).count { t[it % t.width, it / t.width] == MASKED }
        sb.append("unwrapped iris, ${pct(1.0 - masked.toDouble() / (t.width * t.height))} usable</div>")
    }
    r.faceCrop?.let { c ->
        sb.append("<div class=step>")
        if (withImages) sb.append("<img width='160' src='${png(c, "png")}'>") else sb.append("<div class=noimg>${c.width} × ${c.height}</div>")
        sb.append("face found by the extractor</div>")
    }
    // 3. what happened, in words and numbers
    sb.append("<div class=meta><b>${esc(r.sample.id)}</b><br>person ${esc(r.sample.person)}<br>")
    sb.append(if (r.error == null) "<span class=ok>encoded</span>" else "<span class=err>${esc(r.error)}</span>").append("<br>")
    r.boundaries?.let { b ->
        sb.append("pupil (${fmt(b.pupil.x)}, ${fmt(b.pupil.y)}) r ${fmt(b.pupil.radius)}<br>")
        sb.append("limbus (${fmt(b.limbus.x)}, ${fmt(b.limbus.y)}) r ${fmt(b.limbus.radius)}<br>")
        sb.append("pupil / iris ${pct(b.pupil.radius / b.limbus.radius)}; eyelids: ${listOfNotNull(b.upperEyelid?.let { "upper" }, b.lowerEyelid?.let { "lower" }).ifEmpty { listOf("none") }.joinToString(", ")}<br>")
    }
    r.quality?.let { q ->
        sb.append("quality ${q.score}: usable area ${fmt(q.usableIrisArea)}, sharpness ${fmt(q.sharpness)}, iris–sclera ${fmt(q.irisScleraContrast)}, iris–pupil ${fmt(q.irisPupilContrast)}<br>")
        if (q.issues.isNotEmpty()) sb.append("ISO issues: ${esc(q.issues.joinToString(", "))}<br>")
    }
    r.template?.let { sb.append("template ${it.size} bytes") }
    return sb.append("</div></div>").toString()
}

/** Pupil, limbus and eyelids in image pixels; the SVG viewBox is the photo's own size. */
private fun overlay(b: IrisBoundaries, width: Int): String {
    val stroke = width / 160.0
    val sb = StringBuilder()
    sb.append("<circle cx='${fmt(b.pupil.x)}' cy='${fmt(b.pupil.y)}' r='${fmt(b.pupil.radius)}' fill='none' stroke='#e5484d' stroke-width='$stroke'/>")
    sb.append("<circle cx='${fmt(b.limbus.x)}' cy='${fmt(b.limbus.y)}' r='${fmt(b.limbus.radius)}' fill='none' stroke='#0091ff' stroke-width='$stroke'/>")
    // an eyelid curve only means something across the iris
    for (lid in listOfNotNull(b.upperEyelid, b.lowerEyelid)) {
        val x0 = b.limbus.x - b.limbus.radius
        val pts = (0..40).joinToString(" ") { i -> val x = x0 + 2 * b.limbus.radius * i / 40; "${fmt(x)},${fmt(lid.y(x))}" }
        sb.append("<polyline points='$pts' fill='none' stroke='#f5a000' stroke-width='$stroke'/>")
    }
    return sb.toString()
}

/** Score of every pair of one modality; green = the decision the test expects, red = wrong. */
private fun matrix(samples: List<Sample>, pairs: List<Pair>, modality: String): String {
    if (samples.size < 2) return ""
    val byKey = pairs.associateBy { it.a.id to it.b.id }
    val sb = StringBuilder("<h3>Every pair</h3><p class=legend><span class=gm>same person, match</span><span class=g>different people, no match</span><span class=r>wrong</span> ")
    sb.append(if (modality == "iris") "Iris score: similarity, higher is closer." else "Face score: distance, lower is closer.").append("</p><table class=m><tr><th></th>")
    samples.forEach { sb.append("<th>${esc(it.id)}</th>") }
    sb.append("</tr>")
    for (a in samples) {
        sb.append("<tr><th>${esc(a.id)}</th>")
        for (b in samples) {
            if (a == b) { sb.append("<td class=d></td>"); continue }
            val p = byKey[a.id to b.id] ?: byKey[b.id to a.id]
            if (p == null) sb.append("<td class=d>–</td>")
            else sb.append("<td class=${if (!p.correct) "r" else if (p.expected) "gm" else "g"} title='${esc(a.id)} / ${esc(b.id)}'>${fmt(p.score)}</td>")
        }
        sb.append("</tr>")
    }
    return sb.append("</table>").toString()
}

// ---------------------------------------------------------------- pictures

/** The unwrapped iris as gray pixels, with masked samples in yellow so the mask is visible. */
private fun texture(t: Image): BufferedImage {
    val out = BufferedImage(t.width, t.height, BufferedImage.TYPE_INT_RGB)
    for (y in 0 until t.height) for (x in 0 until t.width) {
        val v = t[x, y]
        out.setRGB(x, y, when {
            v == MASKED -> 0xF5C400
            t.colorType == ImageColorType.GRAY -> (v shl 16) or (v shl 8) or v
            else -> v
        })
    }
    return out
}

private fun scaled(img: BufferedImage, width: Int): BufferedImage {
    if (img.width <= width) return img
    val h = img.height * width / img.width
    val out = BufferedImage(width, h, BufferedImage.TYPE_INT_RGB)
    val g = out.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
    g.drawImage(img, 0, 0, width, h, null)
    g.dispose()
    return out
}

private fun png(img: BufferedImage, format: String): String {
    val rgb = if (img.type == BufferedImage.TYPE_INT_RGB) img else BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB).also {
        it.createGraphics().apply { drawImage(img, 0, 0, null); dispose() }
    }
    val bytes = ByteArrayOutputStream().also { ImageIO.write(rgb, format, it) }.toByteArray()
    return "data:image/$format;base64," + Base64.getEncoder().encodeToString(bytes)
}

private fun fmt(v: Double) = String.format(Locale.ROOT, "%.3f", v).trimEnd('0').trimEnd('.')
private fun pct(v: Double) = String.format(Locale.ROOT, "%.0f%%", v * 100)
private fun esc(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;")
