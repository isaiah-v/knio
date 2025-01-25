plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    kotlin("jvm") version "2.0.21"
}

gradlePlugin {
    plugins {
        create("badges") {
            id = "badges"
            implementationClass = "org.ivcode.gradle.badges.BadgesPlugin"
        }

        create("mvnGitHub") {
            id = "mvnGitHub"
            implementationClass = "org.ivcode.gradle.mvngithub.MvnGitHubPlugin"
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}