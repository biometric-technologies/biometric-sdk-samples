# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-05 05:17 UTC | iris | biometric-sdk auto/iris-template-zero-size@c51b7a4 (PR #27) | Iris matcher: an IR2 template whose header has 0 rows or 0 columns is rejected as corrupted (IllegalArgumentException) instead of an ArithmeticException; templates the SDK writes and their scores should stay the same
