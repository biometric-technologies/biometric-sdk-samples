plugins {
    kotlin("jvm") version "2.2.20"
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // resolved from ../sdk when that checkout exists (see settings.gradle.kts)
    implementation("net.iriscan:biometric-sdk:" + (findProperty("sdkVersion") ?: "0.7.0"))
    implementation("org.json:json:20240303")
}

application {
    mainClass.set("net.iriscan.samples.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootDir.parentFile
    maxHeapSize = "2g"
}
