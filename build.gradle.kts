plugins {
    java
    kotlin("jvm") version "1.9.22"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.7-SNAPSHOT"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.example"
version = "1.0.1"

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    silentMojangMappingsLicense()

    mixin {
        defaultRefmapName.set("mixins.${project.name}.refmap.json")
    }
}

val shadowCommon: Configuration by configurations.creating

repositories {
    mavenCentral()
    maven(url = "https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://oss.sonatype.org/content/repositories/snapshots") {
        name = "Sonatype Snapshots"
    }
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") {
        name = "Sonatype 01 Snapshots"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.1")
    mappings("net.fabricmc:yarn:1.21.1+build.3:v2")
    modImplementation("net.fabricmc:fabric-loader:0.16.2")

    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.5+kotlin.1.8.22")
    modImplementation(fabricApi.module("fabric-command-api-v2", "0.105.0+1.21.1"))
    modImplementation("dev.architectury", "architectury-fabric", "9.0.8")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.1+1.21.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    implementation("net.kyori:adventure-api:${property("minimessage_version")}")
    implementation("net.kyori:adventure-text-minimessage:${property("minimessage_version")}")
    implementation("net.kyori:adventure-text-serializer-gson:${property("minimessage_version")}")

    shadowCommon("net.kyori:adventure-api:${property("minimessage_version")}")
    shadowCommon("net.kyori:adventure-text-minimessage:${property("minimessage_version")}")
    shadowCommon("net.kyori:adventure-text-serializer-gson:${property("minimessage_version")}")

    compileOnly("net.luckperms:api:5.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

kotlin.target.compilations.all {
    kotlinOptions.jvmTarget = "21"
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("fabric")
}