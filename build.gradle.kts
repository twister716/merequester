@file:Suppress("UnstableApiUsage")

val license: String by project
val loggingLevel: String by project
val mixinDebugExport: String by project
val mcVersion: String by project
val modVersion: String by project
val modPackage: String by project
val modId: String by project
val modName: String by project
val modAuthor: String by project
val modDescription: String by project
val neoVersion: String by project
val parchmentVersion: String by project
val aeVersion: String by project
val emiVersion: String by project
val githubUser: String by project
val githubRepo: String by project

plugins {
    id("net.neoforged.moddev") version "2.0.+"
    id("com.github.gmazzo.buildconfig") version "4.0.4"
    java
}

base {
    version = "$mcVersion-$modVersion"
    group = modPackage
    archivesName.set("$modId-neoforge")
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neoVersion

    mods {
        create("merequester") {
            modSourceSets.add(sourceSets.main)
        }
    }

    runs {
        configureEach {
            gameDirectory = project.file("run")
            systemProperties = mapOf(
                "forge.logging.console.level" to loggingLevel,
                "mixin.debug.export" to mixinDebugExport,
                "guideDev.ae2guide.sources" to file("guidebook").absolutePath,
                "guideDev.ae2guide.sourcesNamespace" to modId
            )
            jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
        }
        create("client") {
            client()
            programArguments.addAll("--quickPlaySingleplayer", "New World")
        }
        create("guide") {
            client()
            systemProperty("guideDev.ae2guide.startupPage", "$modId:$modId.md")
        }
        create("server") {
            server()
        }
    }

    parchment {
        minecraftVersion = "1.21"
        mappingsVersion = parchmentVersion
    }
}

repositories {
    maven("https://modmaven.dev") // Applied Energistics 2
    maven("https://maven.blamejared.com") // JEI
    maven("https://maven.shedaniel.me") // REI
    maven("https://maven.terraformersmc.com") // EMI
    mavenLocal()
}

dependencies {
    implementation("appeng:appliedenergistics2:$aeVersion")
    runtimeOnly("dev.emi:emi-neoforge:$emiVersion+1.21")
}

tasks {
    processResources {
        val resourceTargets = listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")

        val replaceProperties = mapOf(
            "license" to license,
            "mcVersion" to mcVersion,
            "version" to project.version as String,
            "modId" to modId,
            "modName" to modName,
            "modAuthor" to modAuthor,
            "modDescription" to modDescription,
            "neoVersion" to neoVersion,
            "aeVersion" to aeVersion,
            "githubUser" to githubUser,
            "githubRepo" to githubRepo
        )

        println("[Process Resources] Replacing properties in resources: ")
        replaceProperties.forEach { (key, value) -> println("\t -> $key = $value") }

        inputs.properties(replaceProperties)
        filesMatching(resourceTargets) {
            expand(replaceProperties)
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<Jar> {
        from("guidebook") {
            into("assets/$modId/ae2guide")
        }
    }
}

buildConfig {
    buildConfigField("String", "MOD_ID", "\"$modId\"")
    buildConfigField("String", "MOD_NAME", "\"$modName\"")
    buildConfigField("String", "MOD_VERSION", "\"$version\"")
    packageName(modPackage)
    useJavaOutput()
}
