package dev.gaphunter.reviewcompanion.detect

object TodoFixmeDensityDetector {
    fun findings(facts: FunctionFacts, threshold: Int): List<Finding> =
        if (facts.todoFixmeCount > threshold) {
            listOf(
                Finding(
                    facts.name,
                    "Function '${facts.name}' has ${facts.todoFixmeCount} TODO/FIXME comments (threshold: $threshold) -- worth a look before merging.",
                    0,
                ),
            )
        } else {
            emptyList()
        }
}
