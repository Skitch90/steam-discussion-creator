plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo1.maven.org/maven2/")
    }
    mavenCentral()
}

dependencies {
    api(libs.org.seleniumhq.selenium.selenium.java)
    api(libs.ch.qos.logback.logback.classic)
    api(libs.com.sksamuel.hoplite.hoplite.core)
    api(libs.com.sksamuel.hoplite.hoplite.yaml)
    api(libs.org.jetbrains.exposed.exposed.core)
    api(libs.org.jetbrains.exposed.exposed.kotlin.datetime)
    api(libs.org.jetbrains.exposed.exposed.jdbc)
    api(libs.org.xerial.sqlite.jdbc)
    implementation(kotlin("stdlib-jdk8"))
}

group = "it.alesc"
version = "0.0.1-SNAPSHOT"
description = "selenium"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.named<Test>("test") {
    useTestNG()
}
kotlin {
    jvmToolchain(8)
}