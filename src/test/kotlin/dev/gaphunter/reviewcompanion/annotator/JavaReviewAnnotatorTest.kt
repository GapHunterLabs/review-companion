package dev.gaphunter.reviewcompanion.annotator

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Unattended replacement for a manual runIde smoke pass -- confirms the
 * real PSI -> FunctionFacts -> detector -> annotation wiring actually
 * connects end-to-end for Java, with a concrete assertion per finding.
 */
class JavaReviewAnnotatorTest : BasePlatformTestCase() {

    private fun warningsFor(java: String): List<String> {
        myFixture.configureByText("Acme.java", java)
        return myFixture.doHighlighting(HighlightSeverity.WEAK_WARNING)
            .filter { it.severity == HighlightSeverity.WEAK_WARNING }
            .mapNotNull { it.description }
    }

    fun testLongMethodProducesAFinding() {
        val body = (1..60).joinToString("\n") { "        int x$it = $it;" }
        val warnings = warningsFor(
            """
            class Acme {
                void target() {
            $body
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("target") && it.contains("lines long") })
    }

    fun testShortMethodProducesNoLongFunctionFinding() {
        val warnings = warningsFor(
            """
            class Acme {
                void target() {
                    int x = 1;
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.none { it.contains("lines long") })
    }

    fun testDeeplyNestedConditionalsProducesAFinding() {
        val warnings = warningsFor(
            """
            class Acme {
                void target(boolean a, boolean b, boolean c, boolean d, boolean e) {
                    if (a) {
                        if (b) {
                            if (c) {
                                if (d) {
                                    if (e) {
                                        System.out.println("deep");
                                    }
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

    fun testUnguardedParameterDereferenceProducesAFinding() {
        val warnings = warningsFor(
            """
            class Acme {
                void target(String customer) {
                    int len = customer.length();
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("customer") && it.contains("null check") })
    }

    fun testGuardedParameterDereferenceProducesNoFinding() {
        val warnings = warningsFor(
            """
            class Acme {
                void target(String customer) {
                    if (customer != null) {
                        int len = customer.length();
                    }
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.none { it.contains("null check") })
    }

    // Regression (2026-10-01): "null != customer" was not recognized as a
    // null check, so a guarded dereference was reported.
    fun testReversedNullCheckGuardsTheDereference() {
        val warnings = warningsFor(
            """
            class Acme {
                String target(String customer) {
                    if (null != customer) {
                        return customer.trim();
                    }
                    return "";
                }
            }
            """.trimIndent(),
        )
        assertTrue("Expected no null-check finding, got: $warnings", warnings.none { it.contains("null check") })
    }

    // Regression (2026-10-01): a parameter declared non-null by annotation
    // was reported as an unguarded dereference.
    fun testParameterAnnotatedNotNullIsNotReported() {
        val warnings = warningsFor(
            """
            import java.lang.annotation.*;
            class Acme {
                @Retention(RetentionPolicy.CLASS) @interface NotNull {}
                @Retention(RetentionPolicy.CLASS) @interface NonNull {}
                String a(@NotNull String customer) { return customer.trim(); }
                String b(@NonNull String customer) { return customer.trim(); }
            }
            """.trimIndent(),
        )
        assertTrue("Expected no null-check finding, got: $warnings", warnings.none { it.contains("null check") })
    }

    fun testParameterAnnotatedNullableIsStillReported() {
        val warnings = warningsFor(
            """
            import java.lang.annotation.*;
            class Acme {
                @Retention(RetentionPolicy.CLASS) @interface Nullable {}
                String a(@Nullable String customer) { return customer.trim(); }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("customer") && it.contains("null check") })
    }

    fun testTodoDensityAboveThresholdProducesAFinding() {
        val warnings = warningsFor(
            """
            class Acme {
                void target() {
                    // TODO: one
                    // TODO: two
                    // FIXME: three
                    int x = 1;
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("TODO/FIXME") })
    }

    fun testHackCommentsAloneCountTowardTheSameDensityThreshold() {
        val warnings = warningsFor(
            """
            class Acme {
                void target() {
                    // HACK: one
                    // HACK: two
                    // HACK: three
                    int x = 1;
                }
            }
            """.trimIndent(),
        )
        assertTrue(warnings.any { it.contains("TODO/FIXME") })
    }

    fun testCleanMethodProducesNoFindingsAtAll() {
        val warnings = warningsFor(
            """
            class Acme {
                int add(int a, int b) {
                    return a + b;
                }
            }
            """.trimIndent(),
        )
        assertTrue("Expected zero findings on clean code, got: $warnings", warnings.isEmpty())
    }
}
