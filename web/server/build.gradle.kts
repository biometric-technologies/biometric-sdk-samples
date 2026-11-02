plugins {
    application
}

dependencies {
    // built from the SDK checkout in sdk/ when there is one (see settings.gradle.kts)
    implementation("net.iriscan:biometric-sdk:0.7.0")
    // JSON in and out of the API
    implementation("org.json:json:20240303")
}

tasks.compileJava {
    // Java 17 language and API, so any JDK from 17 on builds and runs the server
    options.release = 17
}

application {
    mainClass = "net.iriscan.samples.web.Server"
}
