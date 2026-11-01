# Biometric SDK samples

Private. Small programs that use [biometric-sdk](https://github.com/biometric-technologies/biometric-sdk)
the way an application does, one per platform, all running on the same sample images.
They show whether the SDK works end to end on each platform and what to fix when it does not.

Each sample:

1. reads the images listed in [samples/manifest.json](samples/manifest.json);
2. encodes every image (iris template or face embedding);
3. compares every pair of images of the same modality;
4. checks the result against the expectation: two images of the same person match, images of
   different people do not;
5. writes `results/<platform>.json` and a summary, and fails when an expectation is not met.

## Sample images

12 iris images (4 people, left eye, 3 images each, CASIA-Iris-Thousand) and 12 face photos
(4 people, 3 photos each, CASIA-FaceV5). The images are **not stored in this repository**:
their licences allow research use but not redistribution, so
[scripts/fetch-samples.sh](scripts/fetch-samples.sh) downloads them from the private Iriscan
dataset mirror on GitLab into `samples/data/` (ignored by git). It needs `GITLAB_USER` and
`GITLAB_TOKEN` with read access to `iriscan/datasets`, or your own GitLab login through git.

To add images, add entries to the manifest: `repository` and `path` inside the GitLab group,
and `person`, which decides what must match.

## Platforms

| Platform | Directory | Status |
|---|---|---|
| JVM (Kotlin/Java console) | [jvm](jvm) | Iris and face. Runs in CI on every push and on demand for any SDK branch. |
| Android | android | Planned: instrumented test on an emulator in CI. Starting point: [biometric-sdk-android-example](https://github.com/biometric-technologies/biometric-sdk-android-example). |
| iOS | ios | Planned: XCTest on a simulator (macOS runner). Starting point: [biometric-sdk-ios-example](https://github.com/biometric-technologies/biometric-sdk-ios-example). |
| Web (browser page) | web | Needs a JS/Wasm build of the SDK, which does not exist yet. Iris can be built for the browser because the whole iris pipeline is common Kotlin; face needs TensorFlow Lite and stays unavailable in the browser for now. |
| C (console) | c | Needs a Kotlin/Native shared library of the SDK with a C header, which does not exist yet. Same split: iris yes, face later. |

## Running the JVM sample

```sh
scripts/fetch-samples.sh
git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk   # the SDK source to check
sdk/gradlew -p jvm run
```

Without `sdk/`, the sample uses the SDK release from Maven Central.

In GitHub Actions: **SDK samples → Run workflow**, with the SDK branch, tag or commit to check.
The summary lists every image that failed to encode and every comparison that went wrong.

## Repository secrets

`GITLAB_DEPLOY_USER` and `GITLAB_DEPLOY_TOKEN`: the same deploy token as in iris-lab.
