#!/usr/bin/env bash
# CI entry point of the Android sample (README, "Platform contract"). Needs the SDK source in sdk/,
# the images in samples/data/ (scripts/fetch-samples.sh) and one emulator or device on adb (CI
# starts an emulator). Builds the app and its instrumented test against sdk/ and runs the test on
# the device; the test logs the results table, which goes to results/android.md. Exits non-zero
# if anything was not as expected.
set -euo pipefail
cd "$(dirname "$0")/../.."
if [ ! -f sdk/settings.gradle.kts ]; then
  echo "No biometric-sdk checkout in sdk/: the check builds against the SDK source, not a release" >&2
  exit 1
fi
mkdir -p results
rm -f results/android.md

# Empty the device log first, so that afterwards it holds only this run's table
adb logcat -b main -c

# The SDK's Gradle wrapper, so the app builds with the Gradle version the SDK is built with.
# connectedAndroidTest installs the app and the test on the device and runs the test there.
status=0
sdk/gradlew -p android connectedAndroidTest || status=$?

# The whole device log for the CI artifacts, and the table the test logged under its tag
# (logcat starts the output of each log buffer with a "--------- beginning of" line)
adb logcat -d > results/android-logcat.txt || true
adb logcat -b main -d -v raw -s SamplesCheck:I | grep -v '^--------- ' > results/android.md || true
if [ -s results/android.md ]; then
  cat results/android.md
else
  rm -f results/android.md
  echo "The test logged no results table: see the Gradle output above and results/android-logcat.txt" >&2
  [ "$status" != 0 ] || status=1
fi
exit "$status"
