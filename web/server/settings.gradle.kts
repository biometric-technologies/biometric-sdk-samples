rootProject.name = "web-server"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// With a biometric-sdk checkout in sdk/ at the repository root (as CI makes it), the server is
// built against that source, so any SDK branch can be checked; otherwise Gradle takes the release
// from Maven Central.
val localSdk = file("../../sdk")
if (localSdk.resolve("settings.gradle.kts").exists()) {
    includeBuild(localSdk)
}
