package com.example.testapp.blocking

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.testapp.WalletManager

class BlockService : AccessibilityService() {

    private val blockedApps = listOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.facebook.katana",
        "com.netflix.mediaclient",
        "com.supercell.clashofclans"
    )

    private var currentPackage: String? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAndBlock()
            handler.postDelayed(this, 500) // Check twice a second
        }
    }

    override fun onServiceConnected() {
        handler.post(checkRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Track the current app the user is in
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString()
            if (pkg != null && pkg != "com.android.systemui" && pkg != "android") {
                currentPackage = pkg
            }
        }
    }

    private fun checkAndBlock() {
        val pkg = currentPackage ?: return
        
        if (blockedApps.contains(pkg)) {
            // Block if timer is not running
            if (!WalletManager.isTimerRunning) {
                // Use a handler to show toast on the UI thread
                handler.post {
                    Toast.makeText(this@BlockService, "⚠️ TIME IS OVER! Access Blocked.", Toast.LENGTH_LONG).show()
                }
                
                // Force return to home screen
                performGlobalAction(GLOBAL_ACTION_HOME)
                
                // Reset tracking to avoid showing multiple toasts
                currentPackage = "home"
            }
        }
    }

    override fun onInterrupt() {
        handler.removeCallbacks(checkRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
    }
}