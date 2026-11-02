import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // The Android Gradle plugin and Kotlin versions of the SDK (sdk/settings.gradle.kts and
    // sdk/build.gradle.kts): its build is included in this one, and the Android plugin refuses two
    // of its versions in one build.
    id("com.android.application") version "8.7.3"
    kotlin("android") version "2.2.20"
}

android {
    namespace = "net.iriscan.samples.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.iriscan.samples.android"
        minSdk = 23 // as the SDK
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // built from the SDK checkout in sdk/ when there is one (see settings.gradle.kts)
    implementation("net.iriscan:biometric-sdk:0.7.0")

    // the instrumented test: AndroidJUnitRunner and JUnit 4
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("junit:junit:4.13.2")
}

// The sample images are not in this repository: scripts/fetch-samples.sh downloads them into
// samples/data/. Before every build this copies them with the manifest into the test APK's assets,
// under the paths they have in the repository (samples/manifest.json, samples/data/<id>.<ext>).
val sampleAssets = layout.buildDirectory.dir("sample-assets").get().asFile
val copySamples = tasks.register<Sync>("copySamples") {
    from(rootDir.parentFile.resolve("samples")) {
        include("manifest.json", "data/**")
        into("samples")
    }
    into(sampleAssets)
}
android.sourceSets["androidTest"].assets.srcDir(sampleAssets)
tasks.named("preBuild") { dependsOn(copySamples) }
