plugins {
    id("net.neoforged.moddev") version "2.0.+"
    id("com.almostreliable.almostgradle")
}

almostgradle.setup {
    withSourcesJar = false
    recipeViewers.emi.mavenRepository
}

neoForge {
    runs {
        configureEach {
            systemProperties = mapOf(
                "guideDev.ae2guide.sources" to file("guidebook").absolutePath,
                "guideDev.ae2guide.sourcesNamespace" to almostgradle.modId
            )
        }

        create("guide") {
            client()
            systemProperty("guideDev.ae2guide.startupPage", "${almostgradle.modId}:${almostgradle.modId}.md")
        }
    }
}

repositories {
    maven("https://modmaven.dev")
    mavenLocal()
}

dependencies {
    implementation("appeng:appliedenergistics2:${almostgradle.getProperty("aeVersion")}")
}

tasks.withType<Jar> {
    from("guidebook") {
        into("assets/${almostgradle.modId}/ae2guide")
    }
}
