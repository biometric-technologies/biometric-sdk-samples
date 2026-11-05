# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-05 04:49 UTC | face | biometric-sdk auto/face-model-sizes@52fc7a4 | Face model configurations reject an inputWidth, inputHeight or outputLength of 0 or less; the samples' configurations use real sizes, so they should be accepted and every face and iris result should stay the same
