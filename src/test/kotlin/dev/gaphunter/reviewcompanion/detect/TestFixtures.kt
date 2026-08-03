package dev.gaphunter.reviewcompanion.detect

fun facts(lineCount: Int = 5, nesting: Int = 0, derefs: List<UnguardedDereference> = emptyList(), todos: Int = 0) =
    FunctionFacts("target", lineCount, nesting, derefs, todos)
