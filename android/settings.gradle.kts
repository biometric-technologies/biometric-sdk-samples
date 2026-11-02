pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "biometric-sdk-samples-android"

// With a biometric-sdk checkout in sdk/ at the repository root (as CI makes it), the app is built
// against that source, so any SDK branch can be checked; otherwise Gradle takes the release from
// Maven Central.
val localSdk = file("../sdk")
if (localSdk.resolve("settings.gradle.kts").exists()) {
    includeBuild(localSdk)
}
