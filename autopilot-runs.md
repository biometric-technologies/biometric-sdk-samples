# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-11 21:39 UTC | models | biometric-sdk auto/models-interpreter-lock@838a7f2 | On Android and iOS a TensorFlow Lite model call (face encoder, photo liveness) holds a lock, so calls from several threads run one at a time as on the JVM; the samples run face on the JVM, which this change does not touch, so iris and face should give the same results as iris-v2
