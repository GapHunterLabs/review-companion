package dev.gaphunter.reviewcompanion.detect

/**
 * Pure, PSI-free abstraction of a function/method body, extracted by
 * psi/JavaFunctionWalker.kt or psi/KotlinFunctionWalker.kt. Every
 * detector in this package operates on this shape, never on real PSI --
 * same separation already used in api-security-companion (detect/ pure,
 * annotator/ PSI-touching) and highlight-companion (complexity/ pure,
 * psi/ PSI-touching).
 */
data class FunctionFacts(
    val name: String,
    val lineCount: Int,
    val maxNestingDepth: Int,
    val unguardedDereferences: List<UnguardedDereference>,
    val todoFixmeCount: Int,
)

/** A dereference site the walker judged unguarded -- see NullDereferenceDetector's own doc comment for the exact (deliberately conservative) rule. */
data class UnguardedDereference(val expressionText: String, val lineOffset: Int)

data class Finding(val functionName: String, val message: String, val lineOffset: Int)
