plugins {
    application
}

dependencies {
    // built from ../sdk when that checkout exists (see settings.gradle.kts)
    implementation("net.iriscan:biometric-sdk:0.7.0")
    // reads samples/manifest.json
    implementation("org.json:json:20240303")
}

tasks.compileJava {
    // Java 17 language and API, so any JDK from 17 on builds and runs the sample
    options.release = 17
}

application {
    mainClass = "net.iriscan.samples.Main"
}

tasks.named<JavaExec>("run") {
    // the manifest, the images and results/ are relative to the repository root
    workingDir = rootDir.parentFile
}
