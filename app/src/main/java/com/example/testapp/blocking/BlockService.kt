package com.example.testapp.blocking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.testapp.WalletManager

class BlockService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == "com.google.android.youtube") {

            if (WalletManager.minutes <= 0) {

                Toast.makeText(
                    this,
                    "YouTube blocked. Complete tasks to unlock.",
                    Toast.LENGTH_LONG
                ).show()

                performGlobalAction(GLOBAL_ACTION_HOME)

            } else {

                WalletManager.minutes -= 1

                Toast.makeText(
                    this,
                    "YouTube allowed. Remaining minutes: ${WalletManager.minutes}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onInterrupt() {}
}