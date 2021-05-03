package me.tatarka.compose.shown

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigChangeShownTest {

    init {
        isShownCount = 0
    }

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(ConfigChangeActivity::class.java)

    @Test
    fun doesnt_call_onShown_again_on_config_change() {

        activityScenarioRule.scenario.recreate()

        assertThat(isShownCount).isEqualTo(1)
    }
}