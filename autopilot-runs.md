# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-05 03:18 UTC | face | biometric-sdk auto/face-damaged-template@3a98a9d (PR #24) | Face matcher: a template without a direction (empty, all zero, NaN or infinite values) is at an infinite distance and matches no face, instead of 1.0 (a match at threshold 1.0) or NaN; real face distances should stay the same
