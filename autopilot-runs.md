# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-19 07:50 UTC | models | biometric-sdk auto/models-android-16kb-pages@4b5e591 | On Android the SDK depends on LiteRT 1.4.2, litert-support-api 1.4.2 and ML Kit face detection 16.1.7 instead of TensorFlow Lite 2.12.0, its Support Library 0.4.3 and ML Kit 16.1.5, for 16 KB memory pages; the Android sample has to build and resolve these without duplicate classes, and iris on every platform and JVM face should give the same results as iris-v2
