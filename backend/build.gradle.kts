plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.serialization") version "2.2.0"
    id("jacoco")
    alias(libs.plugins.springboot)
    alias(libs.plugins.spring.dependency.management)
}

jacoco {
    toolVersion = "0.8.13"
}

group = "org.thuemmler"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    developmentOnly(libs.springboot.devtools)
    implementation(libs.springboot.starter)
    implementation(libs.springboot.web)
    implementation(libs.springboot.websocket)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map { dir ->
            fileTree(dir) {
                exclude(
                    "reversi/ReversiApplication.class",
                    "reversi/ReversiApplicationKt.class",
                    "reversi/config/**",
                    "**/*\$*"
                )
            }
        })
    )
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}