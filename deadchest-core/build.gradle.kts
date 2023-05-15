import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.8.21"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {

    compileOnly("org.spigotmc:spigot-api:1.19-R0.1-SNAPSHOT")
    compileOnly("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:2.2.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
}

tasks.withType<ShadowJar> {
    exclude("**/kotlin/**")
    relocate("org.bstats", "me.crylonz.deadchest")
    archiveFileName.set("dead-chest-SNAPSHOT.jar")
}