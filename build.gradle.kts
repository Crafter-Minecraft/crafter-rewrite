plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "com.crafter"
version = "1.0"

repositories { mavenCentral() }

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        // Discord
        implementation("net.dv8tion:JDA:${property("jda_version")}")

        // Database
        implementation("org.postgresql:postgresql:42.7.3")
        implementation("org.jetbrains.exposed:exposed-core:${property("exposed_version")}")
        implementation("org.jetbrains.exposed:exposed-dao:${property("exposed_version")}")
        implementation("org.jetbrains.exposed:exposed-jdbc:${property("exposed_version")}")

        // Logging
        implementation("ch.qos.logback:logback-classic:1.5.6")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

        implementation("com.github.boticord:botikotlin:2.1.3.9")
    }

    kotlin.jvmToolchain(21)
}