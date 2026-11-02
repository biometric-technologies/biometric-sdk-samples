# Web sample

The SDK behind a small HTTP server for a web page: the browser sends a photo, the server runs the
SDK on it and answers in JSON. Iris and face both work, because the SDK runs on the JVM. For now
there is the API, checked with curl; the page comes next (see the [plan](../autopilot/README.md)).

| Request | Body | Answer |
|---|---|---|
| `POST /api/encode?modality=iris` (or `face`) | the photo: JPEG, PNG or BMP | `{"template": base64}` |
| `POST /api/compare?modality=iris` (or `face`) | `{"a": template, "b": template}` | `{"score": number, "match": true/false}` |

The score is the SDK's: iris similarity from 0 to 1 (higher is closer), face distance (lower is
closer); `match` applies the SDK's threshold. Errors come as `{"error": text}`: 422 when the SDK
finds no iris or face in the photo, 400 for a bad request, 500 when the SDK fails.

Requirements: JDK 17 or newer and a biometric-sdk checkout in `sdk/` (see the main
[README](../README.md)); on Linux, face needs GTK 2 (`libgtk2.0-0`) for the SDK's OpenCV.
From the repository root:

```sh
git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk
sdk/gradlew -p web/server run          # listens on http://localhost:8080
curl --data-binary @photo.jpg "http://localhost:8080/api/encode?modality=face"
```

CI runs [ci/run.sh](ci/run.sh) (bash, curl and jq): it builds and starts the server, encodes
every photo of `samples/manifest.json` through the API (`scripts/fetch-samples.sh` downloads them),
compares every pair of the same modality and writes the results table to `results/web.md`.
