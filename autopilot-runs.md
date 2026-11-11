# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-11 15:46 UTC | models | biometric-sdk auto/models-tflite-macos-arm64@e1612eb | On the JVM, TensorFlow Lite is 2.21.0-1.5.14 with natives for five desktop platforms (macOS arm64 included) instead of 2.10.0-1.5.8 through tensorflow-lite-platform; the samples run on Linux x86_64, where face detection, face encoding and iris should give the same results as on feat/jvm-blazeface (#31), face distances up to float rounding
