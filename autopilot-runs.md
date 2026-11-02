# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-02 02:20 UTC | face | biometric-sdk auto/face@f0944cd | face matcher: matchesAny and matchScoreMin on an empty list return false and +infinity instead of throwing; one-to-one face results should not change
