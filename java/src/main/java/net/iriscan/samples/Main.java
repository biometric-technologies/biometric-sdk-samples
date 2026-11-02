package net.iriscan.samples;

import net.iriscan.sdk.BiometricSdkFactory;
import net.iriscan.sdk.BiometricSdkOperations;
import net.iriscan.sdk.iris.IrisEncodeProperties;
import net.iriscan.sdk.iris.IrisExtractProperties;
import net.iriscan.sdk.iris.IrisMatchProperties;
import net.iriscan.sdk.iris.IrisMatcher;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The samples check from plain Java, iris for now: encodes every iris image of
 * samples/manifest.json, compares every pair and checks that two images of the same person match
 * and images of different people do not. Writes the results table to results/java.md and exits
 * with 1 when an image is not encoded or a comparison is not as expected.
 *
 * Run from the repository root after scripts/fetch-samples.sh: sdk/gradlew -p java run
 */
public class Main {

    /** An image of the manifest: who it shows and where scripts/fetch-samples.sh saved it. */
    record Sample(String id, String person, Path file) {}

    public static void main(String[] args) throws Exception {
        List<Sample> samples = readIrisSamples();

        // BiometricSdkFactory is a Kotlin object, so Java reaches it through INSTANCE. The default
        // properties are the SDK's own; the match threshold is in IrisMatchProperties.
        BiometricSdkFactory factory = BiometricSdkFactory.INSTANCE;
        factory.initialize(factory.configBuilder()
                .withIris(new IrisExtractProperties(), new IrisEncodeProperties(), new IrisMatchProperties())
                .build());
        BiometricSdkOperations sdk = factory.getInstance();

        List<String> failures = new ArrayList<>();

        // Encode. ImageIO reads the photo (CASIA ships JPEG) and convert() turns it into the SDK's
        // Image; the template is a byte[], or null when the SDK finds no iris in the photo.
        List<byte[]> templates = new ArrayList<>();
        for (Sample sample : samples) {
            byte[] template = null;
            try {
                BufferedImage photo = ImageIO.read(sample.file().toFile());
                if (photo == null) throw new IOException("ImageIO cannot read " + sample.file());
                template = sdk.iris().encoder().extractAndEncode(sdk.io().convert(photo));
                if (template == null) failures.add(sample.id() + ": not encoded, no iris found");
            } catch (Exception e) {
                failures.add(sample.id() + ": not encoded, " + e);
            }
            templates.add(template);
        }
        long encoded = templates.stream().filter(Objects::nonNull).count();

        // Compare every pair of encoded images; an image that was not encoded is already a failure.
        IrisMatcher matcher = sdk.iris().matcher();
        int compared = 0;
        int asExpected = 0;
        for (int i = 0; i < samples.size(); i++) {
            for (int j = i + 1; j < samples.size(); j++) {
                byte[] a = templates.get(i);
                byte[] b = templates.get(j);
                if (a == null || b == null) continue;
                boolean samePerson = samples.get(i).person().equals(samples.get(j).person());
                // score() is the similarity from 0 to 1; matches() compares it with the threshold
                boolean matches = matcher.matches(a, b);
                String line = String.format(Locale.ROOT, "%s / %s: score %.3f, %s, %s",
                        samples.get(i).id(), samples.get(j).id(), matcher.score(a, b),
                        matches ? "match" : "no match", samePerson ? "same person" : "different people");
                System.out.println(line);
                compared++;
                if (matches == samePerson) asExpected++;
                else failures.add(line);
            }
        }

        // The table every platform writes (README, "Platform contract"); CI posts it on the pull request.
        String table = String.format(Locale.ROOT, """
                ### java
                | modality | encoded | comparisons as expected |
                |---|---|---|
                | iris | %d / %d | %d / %d |

                Failures:%s
                """, encoded, samples.size(), asExpected, compared,
                failures.isEmpty() ? " none" : "\n- " + String.join("\n- ", failures));
        System.out.print(table);
        Files.createDirectories(Path.of("results"));
        Files.writeString(Path.of("results/java.md"), table);
        System.exit(failures.isEmpty() ? 0 : 1);
    }

    /** The iris images of samples/manifest.json; face and fingerprints are not in the check yet. */
    static List<Sample> readIrisSamples() throws IOException {
        JSONArray entries = new JSONObject(Files.readString(Path.of("samples/manifest.json")))
                .getJSONArray("samples");
        List<Sample> samples = new ArrayList<>();
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            if (entry.getString("modality").equals("iris")) {
                String id = entry.getString("id");
                samples.add(new Sample(id, entry.getString("person"), findImage(id)));
            }
        }
        return samples;
    }

    /** scripts/fetch-samples.sh saves every image as samples/data/<id>.<extension of the original>. */
    static Path findImage(String id) throws IOException {
        try (Stream<Path> files = Files.list(Path.of("samples/data"))) {
            return files.filter(f -> f.getFileName().toString().startsWith(id + "."))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException(
                            id + " is not in samples/data: run scripts/fetch-samples.sh"));
        }
    }
}
