plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    alias(libs.plugins.springboot)
    alias(libs.plugins.spring.dependency.management)
}

group = "org.thuemmler"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.reflect)
    developmentOnly(libs.springboot.devtools)
    implementation(libs.springboot.starter)
    implementation(libs.springboot.web)
    implementation(libs.springboot.websocket)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}