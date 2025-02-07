package org.knio.gradle.badges

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class BadgesPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("badges", BadgesPluginExtension::class.java)

        project.tasks.register("generateBadges") {
            doLast {
                val extension = project.extensions.findByType(BadgesPluginExtension::class.java) ?: return@doLast

                val path = extension.path ?: project.layout.buildDirectory.dir("generated/badges").get()
                path.asFile.mkdirs()

                HttpClients.createDefault().use { client ->
                    extension.badges.forEach { b ->
                        val badge = b.invoke(project)
                        val uri = badge.toUri()

                        val request = HttpGet(uri.toString())

                        client.execute(request) { response ->
                            val entity = response.entity!!


                            val contentType = ContentType.parse(entity.contentType)
                            val mineExtension = BadgeMimeExtension.getByMimeType(contentType.mimeType)
                                ?: throw IllegalStateException("invalid badge context-type: $contentType")

                            if(mineExtension.extension != EXTENSION_SVG) {
                                throw IllegalStateException("Only svg context is supported: $contentType")
                            }

                            val charset = contentType.charset ?: Charsets.UTF_8

                            // read data
                            val data = ByteArrayOutputStream().use { outputStream ->
                                entity.writeTo(outputStream)
                                outputStream.flush()
                                outputStream.toByteArray()
                            }

                            // read chars (svg+xml) using the content-type defined charset
                            ByteArrayInputStream(data).reader(charset).use { reader ->
                                val file = path.file("${badge.label}${mineExtension.extension}")

                                // write the same chars to the file using UTF-8
                                file.asFile.writer(Charsets.UTF_8).use { writer ->
                                    reader.transferTo(writer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}