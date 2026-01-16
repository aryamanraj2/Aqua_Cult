package com.parth.aquasense.util

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

@Composable
fun getHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) {
        HapticFeedback(view)
    }
}

class HapticFeedback(private val view: View) {
    fun performHapticFeedback(feedbackType: FeedbackType) {
        view.performHapticFeedback(feedbackType.id)
    }

    enum class FeedbackType(val id: Int) {
        LIGHT(android.view.HapticFeedbackConstants.KEYBOARD_TAP),
        MEDIUM(android.view.HapticFeedbackConstants.VIRTUAL_KEY),
        HEAVY(android.view.HapticFeedbackConstants.LONG_PRESS)
    }
}
