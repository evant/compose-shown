package me.tatarka.compose.shown

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShownTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun calls_onShown() {
        var isShown = false

        composeTestRule.setContent {
            BasicText(
                text = "Button",
                modifier = Modifier.onShown { isShown = true }
            )
        }

        composeTestRule.waitForIdle()

        assertThat(isShown).isTrue()
    }

    @Test
    fun doesnt_call_on_partial_shown() {
        var isShown = false

        composeTestRule.setContent {
            val windowWidth =
                (LocalContext.current as Activity).windowManager.defaultDisplay.width
            val windowWidthDp = with(LocalDensity.current) { (windowWidth).toDp() }

            BasicText(
                text = "Button",
                modifier = Modifier
                    .width(windowWidthDp)
                    .offset(x = windowWidthDp / 2)
                    .onShown { isShown = true }
            )
        }

        assertThat(isShown).isFalse()
    }

    @Test
    fun call_onShown_again_on_resume() {
        var isShownCount = 0

        composeTestRule.setContent {
            BasicText(
                text = "Button",
                modifier = Modifier.onShown { isShownCount++ }
            )
        }

        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.STARTED)
        composeTestRule.waitForIdle()
        composeTestRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeTestRule.waitForIdle()

        assertThat(isShownCount).isEqualTo(2)
    }

    @Test
    fun call_onShown_again_on_remove_and_re_add() {
        var isShownCount = 0
        val show = MutableStateFlow(true)

        composeTestRule.setContent {
            if (show.collectAsState().value) {
                BasicText(
                    text = "Button",
                    modifier = Modifier.onShown { isShownCount++ }
                )
            }
        }
        composeTestRule.waitForIdle()
        show.value = false
        composeTestRule.waitForIdle()
        show.value = true
        composeTestRule.waitForIdle()

        assertThat(isShownCount).isEqualTo(2)
    }

    @Test
    fun call_onShown_again_on_recomposition_with_new_state() {
        var isShownCount = 0
        val text = MutableStateFlow("Button1")

        composeTestRule.setContent {
            val buttonText by text.collectAsState()
            BasicText(
                text = buttonText,
                modifier = Modifier.onShown(buttonText) { isShownCount++ }
            )
        }
        composeTestRule.waitForIdle()
        text.value = "Button2"
        composeTestRule.waitForIdle()

        assertThat(isShownCount).isEqualTo(2)
    }
}