package dev.gaphunter.reviewcompanion.psi

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDoWhileStatement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiForStatement
import com.intellij.psi.PsiForeachStatement
import com.intellij.psi.PsiIfStatement
import com.intellij.psi.PsiInstanceOfExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiSwitchStatement
import com.intellij.psi.PsiWhileStatement
import dev.gaphunter.reviewcompanion.detect.FunctionFacts
import dev.gaphunter.reviewcompanion.detect.UnguardedDereference

/**
 * Extracts pure FunctionFacts from a real PsiMethod -- mirrors the
 * PSI-walking/pure-model split already used by
 * highlight-companion/src/main/kotlin/.../psi/JavaCognitiveWalker.kt
 * (read for the recursion-visitor pattern this reuses), but computes
 * simple structural facts rather than a full control-flow tree.
 */
object JavaFunctionWalker {

    fun walk(method: PsiMethod): FunctionFacts {
        val body = method.body
        val name = method.name
        val lineCount = body?.let { lineCountOf(it.text) } ?: 0
        val maxNesting = body?.let { maxNestingDepth(it, 0) } ?: 0
        val todoCount = body?.let { countTodoFixme(it) } ?: 0
        val derefs = body?.let { findUnguardedDereferences(it, method.parameterList.parameters.toList()) } ?: emptyList()
        return FunctionFacts(name, lineCount, maxNesting, derefs, todoCount)
    }

    private fun lineCountOf(text: String): Int = text.count { it == '\n' } + 1

    private fun maxNestingDepth(element: PsiElement, currentDepth: Int): Int {
        var max = currentDepth
        for (child in element.children) {
            val childDepth = when (child) {
                is PsiIfStatement, is PsiWhileStatement, is PsiDoWhileStatement,
                is PsiForStatement, is PsiForeachStatement, is PsiSwitchStatement,
                -> maxNestingDepth(child, currentDepth + 1)
                else -> maxNestingDepth(child, currentDepth)
            }
            if (childDepth > max) max = childDepth
        }
        return max
    }

    private fun countTodoFixme(element: PsiElement): Int {
        var count = 0
        element.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitComment(comment: PsiComment) {
                super.visitComment(comment)
                val text = comment.text.uppercase()
                // HACK is the same class of "revisit this later" marker
                // as TODO/FIXME (Google's own style guides and most
                // real linters group all three together), so it's
                // counted toward the same density signal.
                if (text.contains("TODO") || text.contains("FIXME") || text.contains("HACK")) count++
            }
        })
        return count
    }

    /**
     * Deliberately narrow (see NullDereferenceDetector's own doc comment):
     * only flags a method PARAMETER dereferenced via `.` with no
     * preceding null-guard (`if (x != null)`, `Objects.requireNonNull(x)`,
     * or `x instanceof SomeType` pattern check) anywhere earlier in the
     * same method body. Once a parameter is seen in ANY guard shape, it's
     * treated as guarded for the rest of the method -- no attempt to model
     * which branch the guard actually applies to, favoring fewer false
     * positives over precision.
     */
    private fun findUnguardedDereferences(body: PsiElement, parameters: List<PsiParameter>): List<UnguardedDereference> {
        val paramNames = parameters.map { it.name }.toSet()
        if (paramNames.isEmpty()) return emptyList()
        val guardedSoFar = mutableSetOf<String>()
        val findings = mutableListOf<UnguardedDereference>()

        fun lineOf(element: PsiElement): Int {
            val relativeOffset = (element.textRange.startOffset - body.textRange.startOffset).coerceIn(0, body.text.length)
            return body.text.substring(0, relativeOffset).count { it == '\n' }
        }

        body.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitBinaryExpression(expression: PsiBinaryExpression) {
                super.visitBinaryExpression(expression)
                markGuardIfNullCheck(expression, paramNames, guardedSoFar)
            }

            override fun visitInstanceOfExpression(expression: PsiInstanceOfExpression) {
                super.visitInstanceOfExpression(expression)
                val ref = expression.operand as? PsiReferenceExpression
                ref?.referenceName?.let { if (it in paramNames) guardedSoFar.add(it) }
            }

            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                val methodName = expression.methodExpression.referenceName
                if (methodName == "requireNonNull" || methodName == "requireNonNullElse") {
                    val arg = expression.argumentList.expressions.firstOrNull() as? PsiReferenceExpression
                    arg?.referenceName?.let { if (it in paramNames) guardedSoFar.add(it) }
                }
            }

            override fun visitReferenceExpression(expression: PsiReferenceExpression) {
                super.visitReferenceExpression(expression)
                val qualifier = expression.qualifierExpression as? PsiReferenceExpression ?: return
                val name = qualifier.referenceName ?: return
                if (name !in paramNames || name in guardedSoFar) return
                findings.add(UnguardedDereference(expression.text, lineOf(expression)))
                // Report the first unguarded use only, per parameter, to
                // avoid flooding the same real issue as N findings.
                guardedSoFar.add(name)
            }
        })
        return findings
    }

    private fun markGuardIfNullCheck(expr: PsiBinaryExpression, paramNames: Set<String>, guardedSoFar: MutableSet<String>) {
        val op = expr.operationSign.text
        if (op != "!=" && op != "==") return
        val left = expr.lOperand as? PsiReferenceExpression
        val isNullLiteral = { e: PsiExpression? -> e?.text == "null" }
        if (isNullLiteral(expr.rOperand) && left != null) {
            left.referenceName?.let { if (it in paramNames) guardedSoFar.add(it) }
        }
    }
}
