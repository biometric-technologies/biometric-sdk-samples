package net.iriscan.samples.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.iriscan.sdk.BiometricSdkFactory;
import net.iriscan.sdk.BiometricSdkOperations;
import net.iriscan.sdk.face.FaceEncodeProperties;
import net.iriscan.sdk.face.FaceExtractProperties;
import net.iriscan.sdk.face.FaceMatchProperties;
import net.iriscan.sdk.face.FaceMatcher;
import net.iriscan.sdk.face.FaceNetModelConfiguration;
import net.iriscan.sdk.iris.IrisEncodeProperties;
import net.iriscan.sdk.iris.IrisExtractProperties;
import net.iriscan.sdk.iris.IrisMatchProperties;
import net.iriscan.sdk.iris.IrisMatcher;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * The SDK behind a small JSON API, for the web sample: the browser sends a photo, the server runs
 * the SDK on it (iris and face both work on the JVM) and answers. The API is in web/README.md.
 *
 * Run from the repository root, with the SDK source in sdk/: sdk/gradlew -p web/server run
 */
public class Server {

    /** The FaceNet model of the example apps, the same as in jvm/: 160x160 pixels in, 128 numbers out. */
    static final String FACENET_MODEL =
            "https://github.com/biometric-technologies/biometric-sdk-android-example/blob/master/app/src/main/assets/facenet.tflite?raw=true";

    /** The SDK found no iris or face in the photo: an answer (422), not a failure of the server. */
    static class NothingFound extends Exception {
        NothingFound(String modality) { super("no " + modality + " found in the photo"); }
    }

    /** One API call: the request body and its modality in, the JSON answer out. */
    interface Endpoint {
        JSONObject answer(String modality, byte[] body) throws Exception;
    }

    static BiometricSdkOperations sdk;

    public static void main(String[] args) throws IOException {
        // BiometricSdkFactory is a Kotlin object, so Java reaches it through INSTANCE. withFace has
        // no Java overloads: all four arguments are given, null for the liveness check not used here.
        BiometricSdkFactory factory = BiometricSdkFactory.INSTANCE;
        factory.initialize(factory.configBuilder()
                .withIris(new IrisExtractProperties(), new IrisEncodeProperties(), new IrisMatchProperties())
                .withFace(new FaceExtractProperties(),
                        new FaceEncodeProperties(new FaceNetModelConfiguration(FACENET_MODEL, 160, 160, 128)),
                        new FaceMatchProperties(1.0), // the distance threshold jvm/ uses
                        null)
                .build());
        sdk = factory.getInstance();

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        // localhost only: this is a sample, not a server to put on a network
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.createContext("/api/encode", exchange -> handle(exchange, Server::encode));
        server.createContext("/api/compare", exchange -> handle(exchange, Server::compare));
        server.start(); // no executor set: one request at a time, enough for a sample
        System.out.println("Listening on http://localhost:" + port);
    }

    /** The photo in the body (JPEG, PNG or BMP) gives {"template": base64}. */
    static JSONObject encode(String modality, byte[] photo) throws IOException, NothingFound {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(photo));
        if (image == null) throw new IllegalArgumentException("the body is not an image ImageIO can read");
        byte[] template = modality.equals("iris")
                // iris works on the SDK's own image type, converted from the decoded photo
                ? sdk.iris().encoder().extractAndEncode(sdk.io().convert(image))
                // face takes the platform's image type, BufferedImage on the JVM
                : sdk.face().encoder().extractAndEncode(image);
        if (template == null) throw new NothingFound(modality);
        return new JSONObject().put("template", Base64.getEncoder().encodeToString(template));
    }

    /**
     * Two templates, {"a": base64, "b": base64}, give {"score": number, "match": true|false}. The score
     * is the SDK's: iris similarity from 0 to 1 (higher is closer), face distance (lower is closer).
     */
    static JSONObject compare(String modality, byte[] body) {
        JSONObject templates = new JSONObject(new String(body, StandardCharsets.UTF_8));
        byte[] a = Base64.getDecoder().decode(templates.getString("a"));
        byte[] b = Base64.getDecoder().decode(templates.getString("b"));
        if (modality.equals("iris")) {
            IrisMatcher matcher = sdk.iris().matcher();
            return new JSONObject().put("score", matcher.score(a, b)).put("match", matcher.matches(a, b));
        }
        FaceMatcher matcher = sdk.face().matcher();
        return new JSONObject().put("score", matcher.matchScore(a, b)).put("match", matcher.matches(a, b));
    }

    /**
     * Runs an endpoint for POST ...?modality=iris|face and sends its answer, or {"error": text} with
     * 422 when the SDK finds no iris or face, 400 for a bad request and 500 when the SDK fails.
     */
    static void handle(HttpExchange exchange, Endpoint endpoint) throws IOException {
        int status = 200;
        JSONObject answer;
        try {
            String modality = query(exchange).getOrDefault("modality", "");
            boolean known = modality.equals("iris") || modality.equals("face");
            if (!exchange.getRequestMethod().equals("POST") || !known) {
                throw new IllegalArgumentException("send a POST with ?modality=iris or ?modality=face");
            }
            answer = endpoint.answer(modality, exchange.getRequestBody().readAllBytes());
        } catch (NothingFound e) {
            status = 422;
            answer = new JSONObject().put("error", e.getMessage());
        } catch (IllegalArgumentException | JSONException e) {
            status = 400;
            answer = new JSONObject().put("error", e.getMessage());
        } catch (Exception e) {
            status = 500;
            answer = new JSONObject().put("error", e.toString());
            e.printStackTrace();
        }
        byte[] json = answer.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, json.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(json);
        }
    }

    /** The query parameters of the request, decoded. */
    static Map<String, String> query(HttpExchange exchange) {
        Map<String, String> query = new HashMap<>();
        String raw = exchange.getRequestURI().getQuery();
        for (String pair : raw == null ? new String[0] : raw.split("&")) {
            String[] keyValue = pair.split("=", 2);
            query.put(keyValue[0], keyValue.length > 1 ? keyValue[1] : "");
        }
        return query;
    }
}
