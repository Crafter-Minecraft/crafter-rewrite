plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "crafter-rewrite"

include(
    "crafter-common",
    "crafter-discord",
    "crafter-protocol"
)
