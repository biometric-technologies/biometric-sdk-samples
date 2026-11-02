# Autopilot: platform samples

A scheduled task runs every hour. Each run takes the next platform in the rotation and makes one
small step of its plan below, on its own branch.

Focus (decided 2026-11-02): only tests on the phones, Android and iOS, because platform bugs in
the SDK (template memory on iOS, image conversion) show up only there. Rotation: android → ios →
android. Java, web, C and every app screen (A3, I3) wait until the core matchers are fixed; the
samples that exist (jvm, java, web, visual) stay as regression checks.
The next platform is the one after the platform of the last line in `log.md`; skip a platform
whose next step is blocked (waiting for an answer) and say so in the log line.

There are no pull requests in this repository. CI is the check: a platform branch whose samples
all pass is merged into `main` by the workflow and deleted; a red branch stays and is fixed. Only
SDK changes go through review, as pull requests in biometric-sdk.

## Rules

- One step per branch, small and easy to read later: one or two commits, roughly 200 changed
  lines or less (generated project files excluded).
- Branch `auto/<platform>-<topic>` from `main` (platform one of java, web, android, ios, c; the
  platform name first, so it never equals a biometric-sdk branch and CI runs it against `iris-v2`).
  A step that needs an unmerged branch is built on top of it. At most 2 unmerged branches per
  platform.
- Commit messages carry what a reviewer would otherwise read in a PR, in full sentences: what
  the step adds and why, how it works, how to run it locally, anything to check by hand. Code
  comments explain the why of every non-obvious line. Questions for a person go to the log line
  and to `## Questions` at the end of this file.
- First read the results of the platform's unmerged branches: file `<branch>.md` on the branch
  `ci-results` (e.g. `ci-results:auto/java-face-check.md`; the workflow writes it after every
  push). If one is red, fix that on its branch and nothing else. A branch that is gone was merged.
- Follow the platform contract in the main README ("Platform contract"): `<platform>/ci/run.sh`, `results/<platform>.md`
  in the shared table format, non-zero exit on any failed check. Build against the SDK source in
  `sdk/`, never against a release.
- Write the code the way a customer's developer would read it: the shortest path from a photo to
  an answer, comments that say why, no test frameworks or abstractions the sample doesn't need.
  Each platform folder gets a short README (requirements, how to run, what it shows).
- Never commit images or put them into a source folder; copy them into the build at build time.
- Leave `jvm/`, `samples/manifest.json` and `autopilot-runs.md` alone (the SDK autopilot uses them),
  (the workflow never merges a branch that changes them).
- Never change `.github/workflows`, secrets or repository settings. If the workflow needs a change
  (a tool, a runner, a step), write it under "Questions" and stop that platform.
- SDK changes (needed for C, or bugs the samples find) go to biometric-sdk as their own draft PR
  `auto/<platform>-<topic>` into `iris-v2`, following biometric-lab `autopilot/README.md`, with a
  detailed description for Slava. Never push to `main` here (except `autopilot/log.md`) or to
  `iris-v2`/`master` there, never merge there, never force-push, never close PRs.
- Commits: plain English, no AI attribution, no Co-Authored-By, no session links.
- Append to `log.md`: `- <UTC date time> | <platform> | <branch>@<short sha> | <step and what it did> | samples CI: <pending/green, merged/red>`;
  update earlier lines of the platform when you check their results.

## Plan

Steps are in order; take the first one not done. Mark a step done here (with the branch name)
in the same branch that does it.

### java (plain Java console application) — postponed
- J1 (done, #2): `java/` Gradle project (`application` plugin, Java 17), SDK from `../sdk` with `includeBuild`
  as in `jvm/settings.gradle.kts`. `Main.java` reads the manifest, runs the
  check for iris, writes `results/java.md`. `java/ci/run.sh`.
- J2: face in the same check.
- J3: `java/README.md`: using the SDK from Java (Maven Central coordinates, the few calls needed,
  what the Kotlin types look like from Java).

### web (page in the browser, JVM server behind it) — postponed
- W1 (done, #4): `web/server`: Java or Kotlin, JDK `HttpServer`, no framework. `POST /api/encode?modality=iris|face`
  with the image bytes returns the template; `POST /api/compare` two templates returns the score and
  whether they match. `web/ci/run.sh` starts it and runs the check with curl.
- W1b (split from W1 to keep the PRs small): `POST /api/identify?modality=…` returns the best person
  of the sample gallery (the sample photos, encoded at start; `exclude=<id>` leaves one photo out);
  `web/ci/run.sh` also identifies every photo among the other photos.
- W2: `web/static/index.html` (plain HTML and JS, no build step): the sample photos as a grid,
  click one or upload your own, see who it is and the score. The server serves it.
- W3: `web/ci/run.sh` drives the page with Playwright (installed by the script) instead of curl,
  clicks every photo and checks who the page says it is; a screenshot goes to `results/`.

### android (app on an emulator)
- A1 (done, auto/android-iris-check, green on the emulator and merged): `android/` app (Kotlin, one activity), SDK from `../sdk` with `includeBuild`. An instrumented
  test runs the check for iris on images that a Gradle task copies from `../samples/data` into the
  test assets at build time. `android/ci/run.sh` runs `connectedAndroidTest` on the emulator the
  workflow starts and writes `results/android.md` from the test output.
- A2: face in the test.
- A3 (postponed): the activity shows the sample photos and the result of identifying a tapped one.

### ios (SwiftUI app on a simulator)
- I1: build the SDK framework from `sdk/` (its Kotlin CocoaPods setup; TensorFlow Lite comes from
  CocoaPods) and an Xcode project generated from a readable spec (XcodeGen `project.yml`, installed
  by the script) so the PR diff stays small. An XCTest runs the check for iris on images copied
  into the test bundle at build time. `ios/ci/run.sh` writes `results/ios.md`.
- I2: face in the test.
- I3 (postponed): the app shows the sample photos and the result of identifying a tapped one.

### c (console application on a native SDK library) — postponed
- C1: `c/DESIGN.md` only: Kotlin/Native targets to add to the SDK (linuxX64 first, macosArm64
  later), what platform code needs `actual`s there (image decoding, resources, logging), iris only
  at first (face needs TensorFlow Lite for that target), the C API (a handful of functions over
  byte buffers: create, encode, compare, free; how errors and memory ownership work).
- C2: biometric-sdk PR: linuxX64 target that compiles the iris part, with the needed `actual`s.
- C3: biometric-sdk PR: `sharedLib` binary and the exported C API from C1, with a test.
- C4: `c/` console application (plain C, Makefile) and `c/ci/run.sh`: builds the library from
  `sdk/`, builds the app, runs the check for iris.

### visual (step-by-step report, shared by all platforms)
- V1: `visual/`: a small JVM program (Kotlin, built against `sdk/` like `jvm/`) and `visual/run.sh`
  that write `results/report.html`, one self-contained page (images inlined as data URIs): per
  image the photo, what the public SDK API gives for it (iris: unwrapped texture from `extract`
  with the mask shown, quality values and issues from `quality().assess`, encoded or the reason;
  face: the cropped face from the extractor) and the score matrix of all pairs coloured by
  expected and actual result. Never uploaded by CI (iris images may not be published); CI only
  checks that it builds and writes the page, with `--no-images` for the CI run.
- V2 (needs an SDK PR first): the segmentation drawn over the iris photo (pupil and limbus circles,
  eyelid lines, glints). The SDK has no public API for the segmentation geometry yet: add one in a
  small biometric-sdk PR (e.g. `IrisExtractor.segment(image): IrisSegmentation?` with the circles
  and the mask, documented), then use it here. Same for the face box (V3).

### finger (every platform, later)
- When biometric-sdk `iris-v2` has a public fingerprint API (biometric-lab `finger/DESIGN.md`,
  steps F1 to F9), add a step "fingerprints in the check" to every platform. Until then the
  samples ignore `"modality": "finger"` in the manifest.

## Results so far

Nothing yet.

## Questions

- android, from `auto/android-iris-check`: the android job cannot fail on a failed check.
  android-emulator-runner runs each `script:` line with `sh -c`, so
  `bash android/ci/run.sh 2>&1 | tee results/android.log` returns the status of `tee`: a failed
  check, or a build that never ran the test, leaves the job green, and the workflow then merges the
  branch. The table in `ci-results` still shows the failure. Possible fix in
  `.github/workflows/samples.yml`:
  `script: mkdir -p results && bash -o pipefail -c 'bash android/ci/run.sh 2>&1 | tee results/android.log'`.
  The android steps wait for it.
