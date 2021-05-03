@file:Suppress("NOTHING_TO_INLINE")

package me.tatarka.compose.shown

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.SparseBooleanArray
import android.view.View
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.OnGloballyPositionedModifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.util.*

/**
 * We need to remember which composable have been seen to not re-fire on a configuration change.
 */
private val seenMap = SparseBooleanArray()

/**
 * A callback when the composable is shown to the user. It will fire only when completely visible
 * and when the app is resumed, and it will not fire again on rotation.
 *
 * @param onShown The callback to call when the composable is shown.
 */
inline fun Modifier.onShown(noinline onShown: () -> Unit): Modifier = onShown(0, onShown)

/**
 * A callback when the composable is shown to the user. It will fire only when completely visible
 * and when the app is resumed, and it will not fire again on rotation.
 *
 * @param key1 A set of keys that if changes, will re-trigger the callback.
 * @param onShown The callback to call when the composable is shown.
 */
inline fun Modifier.onShown(key1: Any?, noinline onShown: () -> Unit): Modifier =
    onShown(key1.hashCode(), onShown)

/**
 * A callback when the composable is shown to the user. It will fire only when completely visible
 * and when the app is resumed, and it will not fire again on rotation.
 *
 * @param keys A set of keys that if changes, will re-trigger the callback.
 * @param onShown The callback to call when the composable is shown.
 */
inline fun Modifier.onShown(vararg keys: Any?, noinline onShown: () -> Unit): Modifier =
    onShown(Objects.hash(*keys), onShown)

@PublishedApi
internal fun Modifier.onShown(
    extraKeys: Int,
    onShown: () -> Unit,
): Modifier {
    return composed {
        val view = LocalView.current
        val lifecycleState = LocalLifecycleOwner.current.lifecycle.collectState()
        val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
        var isVisible: Boolean? by remember { mutableStateOf(null) }

        val hash = currentCompositeKeyHash + extraKeys

        var seen by remember(hash) {
            // Check to see if the seen value was saved for a config change.
            mutableStateOf(seenMap[hash])
        }

        // Fire the analytics event if we are resumed, visible, and it hasn't already been seen.
        DisposableEffect(isResumed, isVisible, seen, onShown) {
            if (isResumed && isVisible == true) {
                if (!seen) {
                    seen = true
                    // Remember the seen value to prevent re-sending on configuration changes.
                    seenMap.put(hash, true)
                    onShown()
                }
            } else if (!isResumed || isVisible == false) {
                seen = false
                seenMap.delete(hash)
            }
            onDispose { }
        }

        // Clean up the seen value unless we are going through a configuration change.
        DisposableEffect(hash) {
            onDispose {
                val activity = view.context.findActivity()
                if (!activity.isChangingConfigurations) {
                    seenMap.delete(hash)
                }
            }
        }

        onGloballyPositioned { coordinates ->
            isVisible = coordinates.isCompletelyVisible(view)
        }
    }
}

private fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> throw IllegalStateException("unable to find Activity")
    }
}

@Composable
private fun Lifecycle.collectState(): Lifecycle.State {
    var state by remember { mutableStateOf(currentState) }
    DisposableEffect(this) {
        val listener = LifecycleEventObserver { _, _ ->
            state = currentState
        }
        addObserver(listener)
        onDispose {
            removeObserver(listener)
        }
    }
    return state
}

private fun LayoutCoordinates.isCompletelyVisible(view: View): Boolean {
    if (!isAttached) return false
    // Window relative bounds of our compose root view that are visible on the screen
    val globalRootRect = android.graphics.Rect()
    if (!view.getGlobalVisibleRect(globalRootRect)) {
        // we aren't visible at all.
        return false
    }
    val bounds = boundsInWindow()
    // Make sure we are completely in bounds.
    return bounds.top >= globalRootRect.top &&
            bounds.left >= globalRootRect.left &&
            bounds.right <= globalRootRect.right &&
            bounds.bottom <= globalRootRect.bottom
}
