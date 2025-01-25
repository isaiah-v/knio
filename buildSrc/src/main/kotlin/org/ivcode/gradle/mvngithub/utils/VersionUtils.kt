package org.ivcode.gradle.mvngithub.utils

fun isSnapshot(version: String): Boolean {
    return version.endsWith("-SNAPSHOT")
}