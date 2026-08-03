package dev.gaphunter.reviewcompanion.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NullDereferenceDetectorTest {

    @Test
    fun `no finding when there are no unguarded dereferences`() {
        assertTrue(NullDereferenceDetector.findings(facts(derefs = emptyList())).isEmpty())
    }

    @Test
    fun `one finding per unguarded dereference`() {
        val findings = NullDereferenceDetector.findings(
            facts(derefs = listOf(UnguardedDereference("user.getName()", 3), UnguardedDereference("order.getTotal()", 7))),
        )
        assertEquals(2, findings.size)
        assertTrue(findings.any { it.message.contains("user.getName()") })
        assertTrue(findings.any { it.message.contains("order.getTotal()") })
    }
}
