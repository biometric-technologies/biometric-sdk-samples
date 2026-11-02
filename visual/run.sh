#!/usr/bin/env bash
# Writes results/report.html: every sample image through the SDK's steps, and every pair's score.
# Needs the images in samples/data/ (scripts/fetch-samples.sh) and the SDK source in sdk/.
# Open the report in a browser afterwards. Extra arguments go to the program (e.g. --no-images).
set -euo pipefail
cd "$(dirname "$0")/.."
if [ ! -f sdk/settings.gradle.kts ]; then
  echo "No biometric-sdk checkout in sdk/ (git clone -b iris-v2 https://github.com/biometric-technologies/biometric-sdk sdk)" >&2
  exit 1
fi
status=0
# Gradle rejects an empty --args, so pass it only when there are arguments
if [ $# -gt 0 ]; then sdk/gradlew -p visual run --args="$*" || status=$?
else sdk/gradlew -p visual run || status=$?
fi
echo "Open $(pwd)/results/report.html"
exit $status
