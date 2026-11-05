# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-05 09:19 UTC | face | biometric-sdk auto/face-jvm-tensor-sizes@0ed17e3 | On the JVM the TensorFlow Lite interpreter checks every input and output array against the model's tensor before copying and throws IllegalArgumentException when they differ; the samples' face models are configured with their real sizes, so every face and iris result should stay the same as on iris-v2
