# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-02 09:04 UTC | iris | biometric-sdk auto/iris-template-cols@24656d7 (PR #19) | IrisConfig rejects a templateCols that does not divide normalizedWidth; the samples use the default iris settings, so this run shows that they still initialize and nothing else changed
