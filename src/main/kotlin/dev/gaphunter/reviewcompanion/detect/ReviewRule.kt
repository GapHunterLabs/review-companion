package dev.gaphunter.reviewcompanion.detect

enum class ReviewRule(val id: String, val displayName: String, val defaultThreshold: Int) {
    LONG_FUNCTION("longFunction", "Function/method body longer than N lines", 50),
    NESTED_CONDITIONAL("nestedConditional", "Conditional nesting deeper than N levels", 4),
    NULL_DEREFERENCE("nullDereference", "Dereference without a preceding null check (Java only)", 0),
    TODO_FIXME_DENSITY("todoFixmeDensity", "TODO/FIXME comments per function above N", 2),
}
