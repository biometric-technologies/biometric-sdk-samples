# Android sample

An Android app (Kotlin) with the SDK, built from the SDK source in `sdk/`. For now the app only
sets up the SDK; the samples check runs as an instrumented test on an emulator or device: it encodes
every iris image of [samples/manifest.json](../samples/manifest.json), compares every pair and
checks that two images of the same person match and images of different people do not. Face is not
in the check yet.

What an app needs is in [SdkSetup.kt](src/main/kotlin/net/iriscan/samples/android/SdkSetup.kt)
(set up the SDK with the application context) and
[SamplesCheckTest.kt](src/androidTest/kotlin/net/iriscan/samples/android/SamplesCheckTest.kt):
`BitmapFactory` decodes the photo, `sdk.io().convert(bitmap)` turns it into the SDK's image,
`sdk.iris().encoder().extractAndEncode(image)` makes the template (null when it finds no iris) and
`sdk.iris().matcher().matches(a, b)` compares two templates.

Requirements: JDK 17 or newer, the Android SDK with platform 35 (`ANDROID_HOME`, or `sdk.dir` in
`android/local.properties`), one emulator or device on `adb` (or `ANDROID_SERIAL` naming one), the
images in `samples/data/` and a biometric-sdk checkout in `sdk/` (see the main
[README](../README.md)). From the repository root:

```sh
scripts/fetch-samples.sh
git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk
bash android/ci/run.sh                        # the check; the table goes to results/android.md
sdk/gradlew -p android connectedAndroidTest   # or only the test; the table is in logcat
```

Android Studio opens `android/` as a project, with the SDK source in it when `sdk/` is there.

Gradle copies the manifest and the images into the test APK at build time (`copySamples` in
[build.gradle.kts](build.gradle.kts)); they are never committed or put into `src/`. The test prints
the score of every comparison to logcat (tag `System.out`) and logs the results table under the tag
`SamplesCheck`. [ci/run.sh](ci/run.sh) takes the table from there into `results/android.md`, keeps
the whole device log in `results/android-logcat.txt` and exits with 1 when the test fails.
