package dev.gaphunter.reviewcompanion.settings

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.openapi.options.Configurable
import dev.gaphunter.reviewcompanion.detect.ReviewRule
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class ReviewCompanionConfigurable : Configurable {

    private val checkboxes = ReviewRule.entries.associateWith { JBCheckBox(it.displayName) }
    private val thresholdFields = ReviewRule.entries
        .filter { it != ReviewRule.NULL_DEREFERENCE } // no numeric threshold -- it's a binary presence check
        .associateWith { JBTextField(6) }
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "Review Companion"

    override fun createComponent(): JComponent {
        val settings = ReviewCompanionSettings.getInstance()
        val newPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
        for (rule in ReviewRule.entries) {
            val row = JPanel()
            val checkbox = checkboxes.getValue(rule)
            checkbox.isSelected = settings.isEnabled(rule)
            row.add(checkbox)
            thresholdFields[rule]?.let { field ->
                field.text = settings.threshold(rule).toString()
                row.add(field)
            }
            newPanel.add(row)
        }
        panel = newPanel
        return newPanel
    }

    override fun isModified(): Boolean {
        val settings = ReviewCompanionSettings.getInstance()
        val checkboxChanged = ReviewRule.entries.any { checkboxes.getValue(it).isSelected != settings.isEnabled(it) }
        val thresholdChanged = thresholdFields.any { (rule, field) -> field.text.toIntOrNull() != settings.threshold(rule) }
        return checkboxChanged || thresholdChanged
    }

    override fun apply() {
        val settings = ReviewCompanionSettings.getInstance()
        for (rule in ReviewRule.entries) {
            settings.setEnabled(rule, checkboxes.getValue(rule).isSelected)
        }
        for ((rule, field) in thresholdFields) {
            field.text.toIntOrNull()?.let { settings.setThreshold(rule, it) }
        }
    }

    override fun reset() {
        val settings = ReviewCompanionSettings.getInstance()
        for (rule in ReviewRule.entries) {
            checkboxes.getValue(rule).isSelected = settings.isEnabled(rule)
        }
        for ((rule, field) in thresholdFields) {
            field.text = settings.threshold(rule).toString()
        }
    }
}
