package org.ivcode.gradle.badges

import org.gradle.api.Project

class StaticBadgeFactory(
    private val badge: Badge,
): BadgeFactory {

    override fun invoke(projects: Project): Badge {
        return badge
    }
}