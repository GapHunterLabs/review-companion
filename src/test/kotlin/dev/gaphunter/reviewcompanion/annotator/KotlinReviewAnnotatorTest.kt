package dev.gaphunter.reviewcompanion.annotator

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinReviewAnnotatorTest : BasePlatformTestCase() {

    private fun warningsFor(kotlin: String): List<String> {
        myFixture.configureByText("Acme.kt", kotlin)
        return myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING)
            .filter { it.severity == HighlightSeverity.WEAK_WARNING }
            .mapNotNull { it.description }
    }

    fun testLongFunctionProducesAFinding() {
        val body = (1..60).joinToString("\n") { "    val x$it = $it" }
        val warnings = warningsFor(
            """
            fun target() {
            $body
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("target") && it.contains("lines long") })
    }

    fun testDeeplyNestedConditionalsProducesAFinding() {
        val warnings = warningsFor(
            """
            fun target(a: Boolean, b: Boolean, c: Boolean, d: Boolean, e: Boolean) {
                if (a) {
                    if (b) {
                        if (c) {
                            if (d) {
                                if (e) {
                                    println("deep")
                                }
                            }
                        }
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("nesting") })
    }

    fun testTodoDensityAboveThresholdProducesAFinding() {
        val warnings = warningsFor(
            """
            fun target() {
                // TODO: one
                // TODO: two
                // FIXME: three
                val x = 1
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("TODO/FIXME") })
    }

    fun testHackCommentsAloneCountTowardTheSameDensityThreshold() {
        val warnings = warningsFor(
            """
            fun target() {
                // HACK: one
                // HACK: two
                // HACK: three
                val x = 1
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("TODO/FIXME") })
    }

    fun testCleanFunctionProducesNoFindings() {
        val warnings = warningsFor(
            """
            fun add(a: Int, b: Int): Int = a + b
            """.trimIndent(),
        )
        assertTrue("Expected zero findings on clean code, got: $warnings", warnings.isEmpty())
    }
}
