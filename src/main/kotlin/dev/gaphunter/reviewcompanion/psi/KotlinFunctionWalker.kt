package dev.gaphunter.reviewcompanion.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.reviewcompanion.detect.FunctionFacts
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDoWhileExpression
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.KtWhileExpression

/**
 * Kotlin counterpart of JavaFunctionWalker, same relationship as
 * highlight-companion's KotlinCognitiveWalker to JavaCognitiveWalker
 * (real, confirmed-working PSI classes/accessors read directly from
 * that file before writing this one). No unguarded-dereference detection
 * for Kotlin in v0.1 (Java only -- see NullDereferenceDetector's doc
 * comment) since Kotlin's own null-safety type system already prevents
 * the exact bug class this rule targets for non-platform types.
 */
object KotlinFunctionWalker {

    fun walk(function: KtNamedFunction): FunctionFacts {
        val name = function.name ?: "<anonymous>"
        val body: PsiElement? = function.bodyBlockExpression ?: function.bodyExpression
        val lineCount = body?.let { it.text.count { c -> c == '\n' } + 1 } ?: 0
        val maxNesting = body?.let { maxNestingDepth(it, 0) } ?: 0
        val todoCount = body?.let { countTodoFixme(it) } ?: 0
        return FunctionFacts(name, lineCount, maxNesting, emptyList(), todoCount)
    }

    private fun maxNestingDepth(element: PsiElement, currentDepth: Int): Int {
        var max = currentDepth
        for (child in element.children) {
            val childDepth = when (child) {
                is KtIfExpression, is KtWhenExpression, is KtWhileExpression,
                is KtDoWhileExpression, is KtForExpression,
                -> maxNestingDepth(child, currentDepth + 1)
                else -> maxNestingDepth(child, currentDepth)
            }
            if (childDepth > max) max = childDepth
        }
        return max
    }

    private fun countTodoFixme(element: PsiElement): Int =
        PsiTreeUtil.findChildrenOfType(element, PsiComment::class.java).count { comment ->
            val text = comment.text.uppercase()
            text.contains("TODO") || text.contains("FIXME")
        }
}
