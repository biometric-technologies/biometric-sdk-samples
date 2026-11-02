# Autopilot: platform samples

A scheduled task runs every hour. Each run takes the next platform in the rotation and makes one
small step of its plan below, as one pull request. Rotation order: java → web → android → ios → c.
The next platform is the one after the platform of the last line in `log.md`; skip a platform
whose next step is blocked (waiting for a merge or for an answer) and say so in the log line.

## Rules

- Slava reviews every PR, so each one must be small and easy to understand on its own: one step,
  one or two commits, roughly 200 changed lines or less (generated project files excluded).
- Branch `auto/<platform>-<topic>` from `main` (the platform name first, so it never equals a biometric-sdk branch and CI runs it against `iris-v2`), draft PR into `main` titled with what it does.
  If the step needs an unmerged PR, branch from that PR's branch, set the PR base to it and write
  "Depends on #N" at the top. At most 2 open autopilot PRs per platform.
- PR description: what the step adds, how to run it locally, what CI showed, anything to check by
  hand. Questions for a person go under "Questions".
- First look at the open autopilot PRs of the platform: the `SDK samples` checks and the results
  comment the workflow posts on the PR. If one is red, fix that and nothing else.
- Follow the platform contract in the main README ("Platform contract"): `<platform>/ci/run.sh`, `results/<platform>.md`
  in the shared table format, non-zero exit on any failed check. Build against the SDK source in
  `sdk/`, never against a release.
- Write the code the way a customer's developer would read it: the shortest path from a photo to
  an answer, comments that say why, no test frameworks or abstractions the sample doesn't need.
  Each platform folder gets a short README (requirements, how to run, what it shows).
- Never commit images or put them into a source folder; copy them into the build at build time.
- Leave `jvm/`, `samples/manifest.json` and `autopilot-runs.md` alone (the SDK autopilot uses them),
  except to add images through the manifest in a PR of their own.
- Never change `.github/workflows`, secrets or repository settings. If the workflow needs a change
  (a tool, a runner, a step), write it under "Questions" and stop that platform.
- SDK changes (needed for C, or bugs the samples find) go to biometric-sdk as their own small PR
  `auto/<platform>-<topic>` into `iris-v2`, following biometric-lab `autopilot/README.md`. Never push to
  `main` here or to `iris-v2`/`master` there, never merge, never force-push, never close PRs.
- Commits: plain English, no AI attribution, no Co-Authored-By, no session links.
- Append to `log.md`: `- <UTC date time> | <platform> | <branch>@<short sha> (PR #N) | <step and what it did> | samples CI: <pending/green/red>`;
  update earlier lines of the platform when you check their results.

## Plan

Steps are in order; take the first one not done. Mark a step done here (with the PR number) in
the same PR that does it.

### java (plain Java console application)
- J1: `java/` Gradle project (`application` plugin, Java 17), SDK from `../sdk` with `includeBuild`
  as in `jvm/settings.gradle.kts`. `Main.java` reads the manifest, runs the
  check for iris, writes `results/java.md`. `java/ci/run.sh`.
- J2: face in the same check.
- J3: `java/README.md`: using the SDK from Java (Maven Central coordinates, the few calls needed,
  what the Kotlin types look like from Java).

### web (page in the browser, JVM server behind it)
- W1: `web/server`: Java or Kotlin, JDK `HttpServer`, no framework. `POST /api/encode?modality=iris|face`
  with the image bytes returns the template; `POST /api/compare` two templates returns the score and
  whether they match; `POST /api/identify?modality=…` returns the best person of the sample gallery.
  `web/ci/run.sh` starts it and runs the check with curl.
- W2: `web/static/index.html` (plain HTML and JS, no build step): the sample photos as a grid,
  click one or upload your own, see who it is and the score. The server serves it.
- W3: `web/ci/run.sh` drives the page with Playwright (installed by the script) instead of curl,
  clicks every photo and checks who the page says it is; a screenshot goes to `results/`.

### android (app on an emulator)
- A1 (done, #6): `android/` app (Kotlin, one activity), SDK from `../sdk` with `includeBuild`. An instrumented
  test runs the check for iris on images that a Gradle task copies from `../samples/data` into the
  test assets at build time. `android/ci/run.sh` runs `connectedAndroidTest` on the emulator the
  workflow starts and writes `results/android.md` from the test output.
- A2: face in the test.
- A3: the activity shows the sample photos and the result of identifying a tapped one.

### ios (SwiftUI app on a simulator)
- I1: build the SDK framework from `sdk/` (its Kotlin CocoaPods setup; TensorFlow Lite comes from
  CocoaPods) and an Xcode project generated from a readable spec (XcodeGen `project.yml`, installed
  by the script) so the PR diff stays small. An XCTest runs the check for iris on images copied
  into the test bundle at build time. `ios/ci/run.sh` writes `results/ios.md`.
- I2: face in the test.
- I3: the app shows the sample photos and the result of identifying a tapped one.

### c (console application on a native SDK library)
- C1: `c/DESIGN.md` only: Kotlin/Native targets to add to the SDK (linuxX64 first, macosArm64
  later), what platform code needs `actual`s there (image decoding, resources, logging), iris only
  at first (face needs TensorFlow Lite for that target), the C API (a handful of functions over
  byte buffers: create, encode, compare, free; how errors and memory ownership work).
- C2: biometric-sdk PR: linuxX64 target that compiles the iris part, with the needed `actual`s.
- C3: biometric-sdk PR: `sharedLib` binary and the exported C API from C1, with a test.
- C4: `c/` console application (plain C, Makefile) and `c/ci/run.sh`: builds the library from
  `sdk/`, builds the app, runs the check for iris.

### finger (every platform, later)
- When biometric-sdk `iris-v2` has a public fingerprint API (biometric-lab `finger/DESIGN.md`,
  steps F1 to F9), add a step "fingerprints in the check" to every platform. Until then the
  samples ignore `"modality": "finger"` in the manifest.

## Results so far

Nothing yet.
