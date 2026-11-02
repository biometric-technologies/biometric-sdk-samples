rootProject.name = "biometric-sdk-samples-java"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// With a biometric-sdk checkout in ../sdk (as CI makes it), the sample is built against that
// source, so any SDK branch can be checked; otherwise Gradle takes the release from Maven Central.
val localSdk = file("../sdk")
if (localSdk.resolve("settings.gradle.kts").exists()) {
    includeBuild(localSdk)
}
