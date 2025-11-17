plugins {
    kotlin("jvm") version "2.2.20"
    id("me.champeau.jmh") version "0.7.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(21)
}

jmh {
    jvmArgs.set(listOf("-Xmx2g"))
    verbosity.set("EXTRA")
    profilers.set(listOf("gc"))
    resultFormat.set("JSON")
    resultsFile.set(project.file("${layout.buildDirectory.get().asFile}/reports/jmh/results.json"))
}

// ktlint configuration: enable verbose output and print to console
ktlint {
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    // If you need to pin ktlint core version, uncomment and set a value:
    // version.set("0.50.0")
}

// Ensure ktlintCheck runs during the `check` lifecycle
tasks.named("check") {
    dependsOn(tasks.named("ktlintCheck"))
}
