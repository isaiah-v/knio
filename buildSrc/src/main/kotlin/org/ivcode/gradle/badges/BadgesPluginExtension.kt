package org.ivcode.gradle.badges

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import java.net.URI

open class BadgesPluginExtension {
    internal val badges = mutableListOf<BadgeFactory>()

    var path: Directory? = null

    fun add(factory: BadgeFactory) {
        badges.add(factory)
    }

    fun jacoco(action: Action<JacocoDsl>) {
        val jacocoDsl = JacocoDsl()
        action.execute(jacocoDsl)

        badges.add(jacocoDsl.createFactory())
    }

    fun badge(action: Action<BadgesDsl>) {
        val badgesDsl = BadgesDsl()
        action.execute(badgesDsl)

        badges.add(badgesDsl.createFactory())
    }

    internal fun getBadges(project: Project): List<Badge> {
        return badges.stream().map { it(project) }.toList()
    }
}

class JacocoDsl {
    var report: String? = null
    var coverageThreshold: Double = 0.8
    var label: String = "Test-Coverage"
    var passingColor: String = GREEN
    var failingColor: String = RED
    var link: URI? = null

    internal fun createFactory() = JacocoBadgeFactory (
        report = report ?: throw IllegalArgumentException("report path is required"),
        coverageThreshold = coverageThreshold,
        label = label,
        passingColor = passingColor,
        failingColor = failingColor,
        link = link,
    )
}

class BadgesDsl {
    var label: String? = null
    var message: String? = null
    var color: String? = GREEN
    var link: URI? = null

    internal fun createFactory() = StaticBadgeFactory (
        badge = Badge(
            label = label ?: throw IllegalArgumentException("label is required"),
            message = message ?: throw IllegalArgumentException("message is required"),
            color = color ?: throw IllegalArgumentException("color is required"),
            link = link
        )
    )
}