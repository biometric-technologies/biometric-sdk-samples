# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-05 01:48 UTC | iris | biometric-sdk auto/iris-srgb-segmentation@c971f03 (PR #23) | Iris segmentation searches an sRGB-encoded copy of the image, the grey levels its thresholds were tuned on; the iris samples are grey CASIA-Thousand eyes, so segmentation, templates and scores can change
