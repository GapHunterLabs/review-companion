package dev.gaphunter.reviewcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoFixmeDensityDetectorTest {

    @Test
    fun `no finding at the threshold`() {
        assertTrue(TodoFixmeDensityDetector.findings(facts(todos = 2), threshold = 2).isEmpty())
    }

    @Test
    fun `finding when density exceeds the threshold`() {
        val findings = TodoFixmeDensityDetector.findings(facts(todos = 5), threshold = 2)
        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("5"))
    }
}
