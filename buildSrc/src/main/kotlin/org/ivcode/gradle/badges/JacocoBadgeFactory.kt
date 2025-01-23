package org.ivcode.gradle.badges

import org.gradle.api.Project
import java.net.URI
import java.text.DecimalFormat
import javax.xml.parsers.DocumentBuilderFactory

class JacocoBadgeFactory (
    private val report: String,
    private val coverageThreshold: Double,
    private val label: String,
    private val passingColor: String,
    private val failingColor: String,
    private val link: URI?,
): BadgeFactory {

    override fun invoke(project: Project): Badge {
        val coverage = project.getTestCoverage()
        val isPassed = coverage >= coverageThreshold

        val format = DecimalFormat("#,###.#")


        return Badge(
            label = label,
            message = "${format.format(coverage*100)}%",
            color = if (isPassed) passingColor else failingColor,
            link = link,
        )
    }

    private fun Project.getTestCoverage(): Double {
        val jacocoReportPath = file(report)
        if (jacocoReportPath.exists()) {
            val document = DocumentBuilderFactory.newInstance()
                .apply {
                    isValidating = false
                    isNamespaceAware = true
                    setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
                }
                .newDocumentBuilder()
                .parse(jacocoReportPath)

            var coveredInstructionNodes: Double = 0.0
            var missedInstructionNodes: Double = 0.0

            val nodeList = document.getElementsByTagName("counter")
            for (i in 0 until nodeList.length) {
                val node = nodeList.item(i)
                if (node.attributes.getNamedItem("type").nodeValue == "INSTRUCTION") {
                    coveredInstructionNodes += node.attributes.getNamedItem("covered").nodeValue.toDouble()
                    missedInstructionNodes += node.attributes.getNamedItem("missed").nodeValue.toDouble()
                }
            }

            return (coveredInstructionNodes / (coveredInstructionNodes + missedInstructionNodes))
        } else {
            throw IllegalStateException("report not found")
        }
    }
}