package dev.gaphunter.reviewcompanion.detect

/**
 * Deliberately conservative -- the walker (JavaFunctionWalker) only marks
 * a dereference as "unguarded" for a narrow, high-confidence shape: a
 * method PARAMETER dereferenced with no preceding `if (x != null)`/
 * `Objects.requireNonNull(x)`/instanceof-pattern check anywhere earlier
 * in the same method body. No dataflow analysis, no field/local tracking,
 * no attempt at exhaustive nullability inference -- a review companion
 * that cries wolf on things that aren't real bugs is itself a trust
 * problem, so this favors missing real bugs over flagging non-bugs.
 */
object NullDereferenceDetector {
    fun findings(facts: FunctionFacts): List<Finding> =
        facts.unguardedDereferences.map { deref ->
            Finding(facts.name, "Parameter dereferenced in '${deref.expressionText}' with no preceding null check in '${facts.name}'.", deref.lineOffset)
        }
}
