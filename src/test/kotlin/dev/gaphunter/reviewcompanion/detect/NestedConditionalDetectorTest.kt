package dev.gaphunter.reviewcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NestedConditionalDetectorTest {

    @Test
    fun `no finding at the threshold`() {
        assertTrue(NestedConditionalDetector.findings(facts(nesting = 4), threshold = 4).isEmpty())
    }

    @Test
    fun `no finding under the threshold`() {
        assertTrue(NestedConditionalDetector.findings(facts(nesting = 1), threshold = 4).isEmpty())
    }

    @Test
    fun `finding when nesting exceeds the threshold`() {
        val findings = NestedConditionalDetector.findings(facts(nesting = 6), threshold = 4)
        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("6"))
    }
}
