package net.iriscan.samples.android

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/** ci/run.sh takes the results table from the log lines with this tag. */
private const val TAG = "SamplesCheck"

/**
 * The samples check on the device, iris for now: encodes every iris image of
 * samples/manifest.json, compares every pair and checks that two images of the same person match
 * and images of different people do not. The build copies the manifest and the images into the
 * assets of this test (see build.gradle.kts).
 *
 * Logs the results table under [TAG] and fails when an image is not encoded or a comparison is not
 * as expected.
 */
class SamplesCheckTest {

    /** An image of the manifest: who it shows and where it is in the test assets. */
    private class Sample(val id: String, val person: String, val path: String)

    @Test
    fun check() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // the images are in the test APK, the SDK runs in the app under test
        val assets = instrumentation.context.assets
        val sdk = setUpSdk(instrumentation.targetContext)
        val samples = readIrisSamples(assets)
        val failures = ArrayList<String>()

        // Encode. BitmapFactory decodes the photo (CASIA ships JPEG) and convert() turns the Bitmap
        // into the SDK's Image; the template is a ByteArray, or null when the SDK finds no iris.
        val templates = samples.map { sample ->
            try {
                val photo = assets.open(sample.path).use { BitmapFactory.decodeStream(it) }
                    ?: error("BitmapFactory cannot decode ${sample.path}")
                val template = sdk.iris().encoder().extractAndEncode(sdk.io().convert(photo))
                if (template == null) failures += "${sample.id}: not encoded, no iris found"
                template
            } catch (e: Throwable) {
                // a failure on one image is a result of the check, not the end of it
                failures += "${sample.id}: not encoded, $e"
                null
            }
        }

        // Compare every pair of encoded images; an image that was not encoded is already a failure.
        val matcher = sdk.iris().matcher()
        var compared = 0
        var asExpected = 0
        for (i in samples.indices) for (j in i + 1 until samples.size) {
            val a = templates[i] ?: continue
            val b = templates[j] ?: continue
            val samePerson = samples[i].person == samples[j].person
            // score() is the similarity from 0 to 1; matches() compares it with the threshold
            val matches = matcher.matches(a, b)
            val line = String.format(
                Locale.ROOT, "%s / %s: score %.3f, %s, %s",
                samples[i].id, samples[j].id, matcher.score(a, b),
                if (matches) "match" else "no match", if (samePerson) "same person" else "different people",
            )
            println(line) // in logcat under the tag System.out
            compared++
            if (matches == samePerson) asExpected++ else failures += line
        }

        // The table every platform writes (README, "Platform contract"), one log message per line.
        // Logcat drops empty messages, so the blank line is a single space.
        val table = listOf(
            "### android",
            "| modality | encoded | comparisons as expected |",
            "|---|---|---|",
            "| iris | ${templates.count { it != null }} / ${samples.size} | $asExpected / $compared |",
            " ",
            if (failures.isEmpty()) "Failures: none" else "Failures:",
        ) + failures.map { "- $it" }
        table.forEach { Log.i(TAG, it) }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }

    /** The iris images of samples/manifest.json; face and fingerprints are not in the check yet. */
    private fun readIrisSamples(assets: AssetManager): List<Sample> {
        val manifest = assets.open("samples/manifest.json").bufferedReader().use { it.readText() }
        val entries = JSONObject(manifest).getJSONArray("samples")
        // scripts/fetch-samples.sh saves every image as samples/data/<id>.<extension of the original>
        val files = assets.list("samples/data").orEmpty()
        return (0 until entries.length()).map { entries.getJSONObject(it) }
            .filter { it.getString("modality") == "iris" }
            .map { entry ->
                val id = entry.getString("id")
                val file = files.firstOrNull { it.startsWith("$id.") }
                    ?: error("$id is not in samples/data: run scripts/fetch-samples.sh and build again")
                Sample(id, entry.getString("person"), "samples/data/$file")
            }
    }
}
