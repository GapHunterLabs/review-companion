package dev.gaphunter.reviewcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LongFunctionDetectorTest {

    @Test
    fun `no finding when line count is at the threshold`() {
        assertTrue(LongFunctionDetector.findings(facts(lineCount = 50), threshold = 50).isEmpty())
    }

    @Test
    fun `no finding when line count is under the threshold`() {
        assertTrue(LongFunctionDetector.findings(facts(lineCount = 10), threshold = 50).isEmpty())
    }

    @Test
    fun `finding when line count exceeds the threshold`() {
        val findings = LongFunctionDetector.findings(facts(lineCount = 60), threshold = 50)
        assertEquals(1, findings.size)
        assertTrue(findings.first().message.contains("60"))
    }
}
