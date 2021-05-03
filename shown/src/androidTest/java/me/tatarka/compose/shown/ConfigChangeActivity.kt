package me.tatarka.compose.shown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier

var isShownCount = 0

class ConfigChangeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicText(
                text = "Button",
                modifier = Modifier.onShown { isShownCount++ }
            )
        }
    }
}
