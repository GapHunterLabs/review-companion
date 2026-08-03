package dev.gaphunter.reviewcompanion.detect

object NestedConditionalDetector {
    fun findings(facts: FunctionFacts, threshold: Int): List<Finding> =
        if (facts.maxNestingDepth > threshold) {
            listOf(
                Finding(
                    facts.name,
                    "Function '${facts.name}' has conditional nesting ${facts.maxNestingDepth} levels deep (threshold: $threshold) -- consider extracting or early-returning.",
                    0,
                ),
            )
        } else {
            emptyList()
        }
}
