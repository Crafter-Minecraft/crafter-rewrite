import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
}

group = project.group
version = project.version

repositories {
    mavenCentral()

    maven(url = "https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        minimize()
    }
}

kotlin {
    jvmToolchain(21)
}