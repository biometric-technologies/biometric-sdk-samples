#!/usr/bin/env bash
# CI entry point of the Java sample (README, "Platform contract"). Needs the SDK source in sdk/
# and the images in samples/data/ (scripts/fetch-samples.sh). Builds the sample against sdk/, runs
# the check, leaves the table in results/java.md and exits non-zero if anything was not as expected.
set -euo pipefail
cd "$(dirname "$0")/../.."
if [ ! -f sdk/settings.gradle.kts ]; then
  echo "No biometric-sdk checkout in sdk/: the check builds against the SDK source, not a release" >&2
  exit 1
fi
# the SDK's Gradle wrapper, so the sample builds with the Gradle version the SDK is built with
sdk/gradlew -p java run
