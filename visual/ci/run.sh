#!/usr/bin/env bash
# CI entry point of the visual report (README, "Platform contract"). Runs it without pictures:
# the iris images may not be published, and CI results are public. The page still has the
# segmentation numbers and the score matrix; results/visual.md has the table.
set -euo pipefail
cd "$(dirname "$0")/../.."
exec bash visual/run.sh --no-images
