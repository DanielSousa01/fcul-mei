plugins {
    kotlin("jvm") version "2.0.20"
    id("me.champeau.jmh") version "0.7.2"
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
kotlin {
    jvmToolchain(17)
}

jmh {
    jvmArgs.set(listOf("-Xmx2g"))
    verbosity.set("EXTRA")
    profilers.set(listOf("gc"))
    resultFormat.set("JSON")
    resultsFile.set(project.file("${layout.buildDirectory.get().asFile}/reports/jmh/results.json"))
}
