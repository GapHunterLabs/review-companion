package dev.gaphunter.reviewcompanion.detect

object LongFunctionDetector {
    fun findings(facts: FunctionFacts, threshold: Int): List<Finding> =
        if (facts.lineCount > threshold) {
            listOf(Finding(facts.name, "Function '${facts.name}' is ${facts.lineCount} lines long (threshold: $threshold) -- consider splitting it up.", 0))
        } else {
            emptyList()
        }
}
