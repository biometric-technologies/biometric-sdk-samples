rootProject.name = "biometric-sdk-samples-visual"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

// With a biometric-sdk checkout in ../sdk (as CI makes it), the sample is built against that
// source, so any branch can be checked; otherwise it uses the release from Maven Central.
val localSdk = file("../sdk")
if (localSdk.resolve("settings.gradle.kts").exists()) {
    includeBuild(localSdk)
}
