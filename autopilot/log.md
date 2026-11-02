# Platform autopilot log

One line per scheduled run: `- <UTC date time> | <platform> | <branch>@<short sha> (PR #N) | <what and why> | samples CI: <pending/green/red>`

- 2026-11-02 08:49 | java | auto/java-iris-check@63bdc54 (PR #2) | J1: java/ Gradle application built against sdk/ (includeBuild); Main.java runs the iris check and writes results/java.md; java/ci/run.sh. Question in the PR: the SDK's convert() brightens gray JPEGs on the JVM (getRGB treats TYPE_BYTE_GRAY as linear) | samples CI: green
