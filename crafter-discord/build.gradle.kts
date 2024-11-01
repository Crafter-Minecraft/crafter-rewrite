import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "8.3.0"
}

group = "com.crafter"
version = "1.0"

dependencies {
    implementation(project(":crafter-common"))
    implementation(project(":crafter-protocol"))

    implementation("com.github.crafter-minecraft:modrinth:1.0.1")
}

tasks {
    jar {
        dependsOn("shadowJar")
        from(shadowJar.get().archiveFile)
    }

    withType<ShadowJar> {
        manifest { attributes["Main-Class"] = "com.crafter.CrafterKt" }
        mergeServiceFiles()
    }
}
