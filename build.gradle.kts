plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.crafter"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    // Discord
    implementation("net.dv8tion:JDA:${property("jda_version")}")
    implementation("net.dv8tion:JDA:${property("jda_version")}")
    implementation("club.minnced:jda-ktx:0.12.0")

    // Database
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("org.jetbrains.exposed:exposed-core:${property("exposed_version")}")
    implementation("org.jetbrains.exposed:exposed-dao:${property("exposed_version")}")
    implementation("org.jetbrains.exposed:exposed-jdbc:${property("exposed_version")}")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Json
    implementation("org.json:json:20240303")
}

kotlin {
    jvmToolchain(21)
}