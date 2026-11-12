# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-12 00:26 UTC | models | biometric-sdk auto/models-blazeface-tflite-2.10@2eb4b3d | Measurement, no SDK change: the SDK branch is the head of biometric-sdk #31 (BlazeFace on TensorFlow Lite 2.10.0-1.5.8), so its JVM face scores can be compared with those of auto/models-tflite-macos-arm64 (#33: the same code on TensorFlow Lite 2.21.0-1.5.14); equal score ranges mean the upgrade alone does not move face templates at the precision the samples print
