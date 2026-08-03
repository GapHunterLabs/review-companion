package dev.gaphunter.reviewcompanion.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import dev.gaphunter.reviewcompanion.detect.ReviewRule

/** Every rule on by default, every rule independently disableable, none paywalled -- no evidence justifies a paid tier for this plugin, unlike ansible-companion. */
@State(name = "ReviewCompanionSettings", storages = [Storage("reviewCompanion.xml")])
class ReviewCompanionSettings : PersistentStateComponent<ReviewCompanionSettings.State> {

    class State {
        var disabledRuleIds: MutableSet<String> = mutableSetOf()
        var thresholds: MutableMap<String, Int> = mutableMapOf()
    }

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun isEnabled(rule: ReviewRule): Boolean = rule.id !in state.disabledRuleIds

    fun setEnabled(rule: ReviewRule, enabled: Boolean) {
        if (enabled) state.disabledRuleIds.remove(rule.id) else state.disabledRuleIds.add(rule.id)
    }

    fun threshold(rule: ReviewRule): Int = state.thresholds[rule.id] ?: rule.defaultThreshold

    fun setThreshold(rule: ReviewRule, value: Int) {
        state.thresholds[rule.id] = value
    }

    companion object {
        fun getInstance(): ReviewCompanionSettings = service()
    }
}
