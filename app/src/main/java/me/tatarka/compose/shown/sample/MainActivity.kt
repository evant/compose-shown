package me.tatarka.compose.shown.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.tatarka.compose.shown.onShown
import me.tatarka.compose.shown.sample.theme.ComposeSampleTheme

@Immutable
data class AnalyticsEvent(val name: String, val properties: Map<String, Any> = emptyMap())

object Analytics {

    fun send(event: AnalyticsEvent) {
        Log.d(
            "Analytics",
            "${event.name}, props:${event.properties.render()}"
        )
    }

    private fun Map<String, Any?>.render(): String {
        return entries.joinToString(",") { (key, value) -> "$key=$value" }
    }
}

val LocalAnalytics = staticCompositionLocalOf { Analytics }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeSampleTheme {
                val analytics = LocalAnalytics.current
                Surface(color = MaterialTheme.colors.background) {
                    var checked by rememberSaveable { mutableStateOf(false) }
                    Greeting(
                        modifier = Modifier.onShown {
                            // send when greeting is shown to the user.
                            analytics.send(AnalyticsEvent("Greeting"))
                        },
                        name = "Android",
                        checked = checked,
                        onCheckedChange = { checked = it })
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    checked: Boolean,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    val analytics = LocalAnalytics.current
    val event = AnalyticsEvent("Hello", mapOf("Name" to name, "Enabled" to checked))
    Column(modifier = modifier) {
        // send when hello is shown to the user and when the checkbox is checked/unchecked.
        // we pass event in as a key to re-trigger when it changes.
        Text(
            text = "Hello $name!", modifier = Modifier.onShown(event) {
                analytics.send(event)
            }
        )
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeSampleTheme {
        Greeting("Android", true)
    }
}