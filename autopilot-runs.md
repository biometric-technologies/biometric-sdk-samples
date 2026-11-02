# Autopilot runs

One line per scheduled run that pushed here: a push to `auto/<area>` runs the samples against the
biometric-sdk branch of the same name.

- 2026-11-02 08:20 UTC | sdk | biometric-sdk auto/sdk-record-io@ba6f077 (PR #18) | io().readRecord and writeRecord read and write ISO/IEC 19794-6 and 19794-2 records; the JVM sample calls neither, so this run shows that nothing else changed
