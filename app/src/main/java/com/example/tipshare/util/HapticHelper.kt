package com.example.tipshare.util

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Helper to perform haptic feedback safely.
 */
object HapticHelper {
    fun performClick(view: View, enabled: Boolean) {
        if (enabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }
}
