package com.example.testapp.blocking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast

class BlockService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        val packageName = event.packageName?.toString() ?: return

        if (packageName == "com.google.android.youtube") {

            Toast.makeText(
                this,
                "YouTube blocked. Complete tasks to unlock.",
                Toast.LENGTH_LONG
            ).show()

            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    override fun onInterrupt() {}
}