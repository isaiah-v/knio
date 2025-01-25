plugins {
    jacoco
    `java-library`
    kotlin("jvm") version "2.0.21"
    id("badges")
    id("mvnGitHub")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "org.ivcode"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
}

java {
    withSourcesJar()
}

badges {
    jacoco {
        report = "build/reports/jacoco/test/jacocoTestReport.xml"
    }
}

mvnGitHub {
    owner = "isaiah-v"
    repository = "Knio"
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}