plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val modName: String by extra
val mcVersion: String by extra
rootProject.name = "${modName.replace(" ", "-")}-$mcVersion-NeoForge"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
