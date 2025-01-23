package org.ivcode.gradle.badges

import java.net.URI

open class BadgesExt {
    private val badges = mutableListOf<BadgeFactory>()

    fun add(factory: BadgeFactory) {
        badges.add(factory)
    }

    fun addJacoco(jacocoDsl: JacocoDsl.() -> Unit) {
        val jacoco = JacocoDsl()
        jacoco.jacocoDsl()

        badges.add(jacoco.createFactory())
    }

    fun addBadge(badgesDsl: BadgesDsl.() -> Unit) {
        val badge = BadgesDsl()
        badge.badgesDsl()

        badges.add(badge.createFactory())
    }
}

class JacocoDsl {
    var report: String? = null
    var coverageThreshold: Double = 0.8
    var label: String = "coverage"
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