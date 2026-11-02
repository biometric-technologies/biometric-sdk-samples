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

- **Iris:** 12 near-infrared images, 4 people, left eye, 3 images each, from CASIA-Iris-Thousand.
  No openly licensed near-infrared iris set with several images of the same eye exists, so these
  come from the private Iriscan mirror on GitLab. CASIA allows research use but not
  redistribution: the images are downloaded at run time and never committed.
- **Face:** 8 photos, 4 adults, a neutral and a smiling front view each, from the
  [Face Research Lab London Set](https://doi.org/10.6084/m9.figshare.5047666) (DeBruine and Jones,
  CC BY 4.0; the participants consented to research use). Downloaded from figshare.
- **Fingerprint:** 10 images, 5 left index fingers, 2 impressions each, from CASIA-FingerprintV5
  (private Iriscan mirror, same terms as CASIA iris). The SDK cannot match fingerprints yet, so
  every sample skips them; they are listed so all platforms read the same manifest when it can.

[scripts/fetch-samples.sh](scripts/fetch-samples.sh) puts them into `samples/data/` (ignored by git).
For the iris images it needs `GITLAB_USER` and `GITLAB_TOKEN` with read access to
`iriscan/datasets`, or your own GitLab login through git.

To add images, add entries to the manifest: `source` (`gitlab` with `repository` and `path`, or
`zip` with `url` and `member`), and `person`, which decides what must match.

## Platforms

| Platform | Directory | Status |
|---|---|---|
| JVM (Kotlin console) | [jvm](jvm) | Iris and face. Runs in CI on every push and on demand for any SDK branch. |
| Java (plain Java console) | java | Planned: how a Java developer calls the SDK. |
| Web (browser page) | web | Planned: a plain HTML page; a small JVM server behind it runs the SDK, so iris and face both work. A browser-only (Wasm) build stays a later option. |
| Android | android | Planned: app plus instrumented test on an emulator in CI. Starting point: [biometric-sdk-android-example](https://github.com/biometric-technologies/biometric-sdk-android-example). |
| iOS | ios | Planned: SwiftUI app plus XCTest on a simulator (macOS runner). Starting point: [biometric-sdk-ios-example](https://github.com/biometric-technologies/biometric-sdk-ios-example). |
| C (console) | c | Needs a Kotlin/Native shared library of the SDK with a C header, which does not exist yet. Iris first; face needs TensorFlow Lite for that target. |

The scheduled platform autopilot builds the planned ones step by step, one small pull request per
step; its plan and rules are in [autopilot/README.md](autopilot/README.md).

### Platform contract

Every platform except `jvm` exists for CI when `<platform>/ci/run.sh` exists. The workflow runs
it from the repository root after it has put a biometric-sdk checkout in `sdk/` and the images in
`samples/data/`. The script builds the sample against `sdk/` (not a release), runs the same check
as the JVM sample (encode every image, compare every pair of the same modality, same person must
match, different people must not) and writes `results/<platform>.md`:

```
### <platform>
| modality | encoded | comparisons as expected |
|---|---|---|
| iris | 12 / 12 | 66 / 66 |
| face | 8 / 8 | 28 / 28 |

Failures: (one line per image not encoded or comparison not as expected, or "none")
```

It exits with a non-zero status if anything failed. The workflow puts the table into the run
summary and, for a branch `auto/*` with an open pull request, into one comment on that PR that is
updated on every run. A user interface (web page, app screen) may show identification (who is in
this photo) on top of that; the check stays the same everywhere so platforms can be compared.

## Running the JVM sample

```sh
scripts/fetch-samples.sh
git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk   # the SDK source to check
sdk/gradlew -p jvm run
```

Without `sdk/`, the sample uses the SDK release from Maven Central.

In GitHub Actions: **SDK samples → Run workflow**, with the SDK branch, tag or commit to check.
A push to a branch `auto/<name>` of this repository runs the samples against the biometric-sdk
branch with the same name if there is one (the SDK autopilot uses this), otherwise against
`iris-v2` (the platform autopilot's branches). If the branch has an open pull request, the results of every platform
(`results/*.md`, including the JVM summary) are posted on it as one comment, updated on every
push: that is how the scheduled runs, which work only in the cloud, read them.
The summary lists every image that failed to encode and every comparison that went wrong.

## What the samples found

- **JVM, face:** the SDK uses OpenCV 4.6.0-1.5.8 from org.bytedeco; its objdetect module loads
  highgui, which links GTK 2 (`libgtk-x11-2.0.so.0`, `libgdk-x11-2.0.so.0`). Without GTK 2 the SDK
  does not initialize at all (`UnsatisfiedLinkError: no jniopencv_highgui`), including the iris part,
  because face and iris are set up together. Current Ubuntu images and most servers have no GTK 2.
  Fixed in biometric-sdk 327257e (iris-v2): face is set up apart from iris, so iris keeps working
  and `face()` reports the cause. Still to fix: the GTK dependency itself.

  Options considered for the face detector:
  1. Own Haar cascade detector in common Kotlin, reading the same OpenCV cascade XML files.
     Removes OpenCV from the JVM build and works in the planned web and C builds. Chosen; in progress.
  2. A newer org.bytedeco OpenCV build: not checked whether it still links GTK; the dependency stays
     large, and the web and C builds still have no detector.
  3. A TensorFlow Lite face detector (BlazeFace or similar) on the TFLite runtime the SDK already
     uses for FaceNet: more accurate than Haar cascades, but TFLite exists only on the JVM, Android
     and iOS, so the web and C builds would again be without a detector. Open: to be decided later,
     possibly as an optional detector next to the Haar one. CI installs GTK 2 for now;
  with it, face encodes all 8 photos and all 28 comparisons are as expected (same person 0.25 to
  0.42, different people 1.28 to 1.57, threshold 1.0).
- **JVM, iris:** 11 of 12 images encoded, all 55 comparisons as expected on iris-v2 (e0e02e4);
  `iris-000-L-2` is not encoded (no iris found).

## Repository secrets

`GITLAB_DEPLOY_USER` and `GITLAB_DEPLOY_TOKEN`: the same deploy token as in iris-lab.
