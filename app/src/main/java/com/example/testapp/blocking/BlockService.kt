package com.example.testapp.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import android.widget.Toast
import com.example.testapp.R
import com.example.testapp.WalletManager
import java.util.Locale

class BlockService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var timerOverlay: View? = null
    private var timerTextView: TextView? = null
    private var lastPackage: String? = null
    
    private val blockedApps = listOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.facebook.katana",
        "com.netflix.mediaclient",
        "com.supercell.clashofclans"
    )

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            monitorSession()
            handler.postDelayed(this, 200) // Fast check every 200ms
        }
    }

    override fun onServiceConnected() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        handler.post(checkRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return
            
            // Ignore system UI transitions so overlay doesn't flicker when notifications are touched
            if (pkg != "com.android.systemui" && pkg != "android") {
                lastPackage = pkg
            }
        }
        monitorSession()
    }

    private fun monitorSession() {
        val packageName = lastPackage ?: return
        val isBlockedApp = blockedApps.contains(packageName)

        if (isBlockedApp) {
            if (!WalletManager.isTimerRunning) {
                // SESSION ENDED: Immediate close and alert
                Toast.makeText(this, "⚠️ TIME IS OVER! Closing app...", Toast.LENGTH_LONG).show()
                performGlobalAction(GLOBAL_ACTION_HOME)
                lastPackage = "home" // Reset to prevent loop
                removeTimerOverlay()
            } else {
                // SESSION ACTIVE: Show/Update overlay
                showTimerOverlay()
                updateOverlayText()
            }
        } else {
            // NOT IN BLOCKED APP: Clean up overlay
            removeTimerOverlay()
        }
    }

    private fun showTimerOverlay() {
        if (timerOverlay != null) return

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE 
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 0 
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            val inflater = LayoutInflater.from(this)
            timerOverlay = inflater.inflate(R.layout.timer_overlay, null)
            timerTextView = timerOverlay?.findViewById(R.id.timerText)

            windowManager?.addView(timerOverlay, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayText() {
        val seconds = WalletManager.remainingSeconds
        val m = seconds / 60
        val s = seconds % 60
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", m, s)

        if (timerTextView?.text != timeStr) {
            timerTextView?.text = timeStr
        }
    }

    private fun removeTimerOverlay() {
        if (timerOverlay != null) {
            try {
                windowManager?.removeView(timerOverlay)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            timerOverlay = null
            timerTextView = null
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