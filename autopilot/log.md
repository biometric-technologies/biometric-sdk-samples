# Platform autopilot log

One line per scheduled run: `- <UTC date time> | <platform> | <branch>@<short sha> (PR #N) | <what and why> | samples CI: <pending/green/red>`

- 2026-11-02 08:49 | java | auto/java-iris-check@63bdc54 (PR #2) | J1: java/ Gradle application built against sdk/ (includeBuild); Main.java runs the iris check and writes results/java.md; java/ci/run.sh. Question in the PR: the SDK's convert() brightens gray JPEGs on the JVM (getRGB treats TYPE_BYTE_GRAY as linear) | samples CI: green
- 2026-11-02 09:42 | web | auto/web-server@9374229 (PR #4) | W1: web/server, a plain Java JDK HttpServer built against sdk/ (includeBuild) with POST /api/encode and /api/compare for iris and face; web/ci/run.sh starts it, runs the check through the API with curl and writes results/web.md. /api/identify split off into a new step W1b to keep the PR small | samples CI: green
