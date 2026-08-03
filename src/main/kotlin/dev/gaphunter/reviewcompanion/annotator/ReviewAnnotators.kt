package dev.gaphunter.reviewcompanion.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import dev.gaphunter.reviewcompanion.detect.Finding
import dev.gaphunter.reviewcompanion.detect.FunctionFacts
import dev.gaphunter.reviewcompanion.detect.LongFunctionDetector
import dev.gaphunter.reviewcompanion.detect.NestedConditionalDetector
import dev.gaphunter.reviewcompanion.detect.NullDereferenceDetector
import dev.gaphunter.reviewcompanion.detect.ReviewRule
import dev.gaphunter.reviewcompanion.detect.TodoFixmeDensityDetector
import dev.gaphunter.reviewcompanion.psi.JavaFunctionWalker
import dev.gaphunter.reviewcompanion.psi.KotlinFunctionWalker
import dev.gaphunter.reviewcompanion.settings.ReviewCompanionSettings
import org.jetbrains.kotlin.psi.KtNamedFunction

/**
 * Shared entry point for all 4 rules: fires once per PsiMethod/KtNamedFunction
 * (not per-token), builds FunctionFacts once via the appropriate walker, and
 * runs every enabled detector against that same facts object -- avoids
 * re-walking the same PSI once per rule. Individual annotator classes below
 * exist only so plugin.xml can register per-language, per-rule entries
 * cleanly (Java rules include NULL_DEREFERENCE, Kotlin rules don't).
 */
private object ReviewFindingsEngine {

    fun findingsFor(method: PsiMethod, settings: ReviewCompanionSettings): List<Finding> {
        val facts = JavaFunctionWalker.walk(method)
        return collect(facts, settings, includeNullDereference = true)
    }

    fun findingsFor(function: KtNamedFunction, settings: ReviewCompanionSettings): List<Finding> {
        val facts = KotlinFunctionWalker.walk(function)
        return collect(facts, settings, includeNullDereference = false)
    }

    private fun collect(facts: FunctionFacts, settings: ReviewCompanionSettings, includeNullDereference: Boolean): List<Finding> {
        val findings = mutableListOf<Finding>()
        if (settings.isEnabled(ReviewRule.LONG_FUNCTION)) {
            findings += LongFunctionDetector.findings(facts, settings.threshold(ReviewRule.LONG_FUNCTION))
        }
        if (settings.isEnabled(ReviewRule.NESTED_CONDITIONAL)) {
            findings += NestedConditionalDetector.findings(facts, settings.threshold(ReviewRule.NESTED_CONDITIONAL))
        }
        if (includeNullDereference && settings.isEnabled(ReviewRule.NULL_DEREFERENCE)) {
            findings += NullDereferenceDetector.findings(facts)
        }
        if (settings.isEnabled(ReviewRule.TODO_FIXME_DENSITY)) {
            findings += TodoFixmeDensityDetector.findings(facts, settings.threshold(ReviewRule.TODO_FIXME_DENSITY))
        }
        return findings
    }
}

class JavaReviewAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is PsiMethod) return
        if (element.body == null) return
        val findings = ReviewFindingsEngine.findingsFor(element, ReviewCompanionSettings.getInstance())
        // Report on the method's own name identifier, not the whole body --
        // a warning underlining an entire 60-line method body is far more
        // visually intrusive than one on the method signature, and every
        // rule here is about the FUNCTION as a whole, not one specific line.
        val target = element.nameIdentifier ?: element
        report(findings, target, holder)
    }
}

class KotlinReviewAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is KtNamedFunction) return
        val findings = ReviewFindingsEngine.findingsFor(element, ReviewCompanionSettings.getInstance())
        val target = element.nameIdentifier ?: element
        report(findings, target, holder)
    }
}

private fun report(findings: List<Finding>, reportTarget: PsiElement, holder: AnnotationHolder) {
    for (finding in findings) {
        val range: TextRange = reportTarget.textRange
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, finding.message).range(range).create()
    }
}
