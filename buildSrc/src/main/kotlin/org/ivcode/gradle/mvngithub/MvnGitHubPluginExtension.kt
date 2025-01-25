package org.ivcode.gradle.mvngithub

import org.gradle.api.Project
import java.net.URI

open class GitHubMvnPluginExtension {
    var owner: String? = null
    var repository: String? = null
    var username: String? = null
    var token: String? = null

    /** if true, fail on release builds (SNAPSHOT only) */
    val failOnRelease = true
}

/**
 * Get the maven repository url
 */
internal fun GitHubMvnPluginExtension.getMvnRepository(project: Project) =
    URI.create("https://maven.pkg.github.com/${owner}/${getRepositoryOrDefault(project)}")

/**
 * Get the username or GITHUB_ACTOR
 */
internal fun GitHubMvnPluginExtension.getUsernameOrDefault(project: Project) =
    username ?: project.findProperty("GITHUB_ACTOR") as String?

/**
 * Get the token or GITHUB_TOKEN
 */
internal fun GitHubMvnPluginExtension.getTokenOrDefault(project: Project) =
    token ?: project.findProperty("GITHUB_TOKEN") as String?

internal fun GitHubMvnPluginExtension.getRepositoryOrDefault(project: Project) =
    repository ?: project.name
