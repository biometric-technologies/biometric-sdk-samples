# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-02 10:10 UTC | face | biometric-sdk auto/face-match-threshold@62beb54 (PR #20) | FaceMatchProperties rejects thresholds outside 0 to 2; the samples configure face with threshold 1.0, so this run shows that face still initializes and nothing else changed
