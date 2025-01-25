package org.ivcode.gradle.mvngithub

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.ivcode.gradle.mvngithub.utils.isSnapshot

/**
 * Plugin to publish artifacts to GitHub Maven Repository
 */
class MvnGitHubPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("maven-publish")
        project.extensions.create("mvnGitHub", GitHubMvnPluginExtension::class.java)

        project.afterEvaluate {
            val extension = project.extensions.findByType(GitHubMvnPluginExtension::class.java)
                ?: return@afterEvaluate

            val url = extension.getMvnRepository(project) ?: return@afterEvaluate
            val username = extension.getUsernameOrDefault(project) ?: return@afterEvaluate
            val token = extension.getTokenOrDefault(project) ?: return@afterEvaluate

            if(extension.failOnRelease && !isSnapshot(project.version.toString())) {
                throw IllegalStateException("Release version detected: ${project.version}")
            }

            project.extensions.configure(PublishingExtension::class.java) {
                publications {
                    create("GitHub", MavenPublication::class.java) {
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = project.version.toString()

                        from(project.components.getByName("java"))
                    }
                }

                repositories {
                    maven {
                        this.name = "GitHub"
                        this.url = url
                        credentials {
                            this.username = username
                            this.password = token
                        }
                    }
                }
            }
        }
    }
}