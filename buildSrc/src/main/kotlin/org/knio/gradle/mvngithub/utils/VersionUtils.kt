package org.knio.gradle.mvngithub.utils

fun isSnapshot(version: String): Boolean {
    return version.endsWith("-SNAPSHOT")
}