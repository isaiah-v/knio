plugins {
    id("badges")
    jacoco
    `java-library`
    kotlin("jvm") version "2.0.21"
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

badges {
    jacoco {
        report = "build/reports/jacoco/test/jacocoTestReport.xml"
    }
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