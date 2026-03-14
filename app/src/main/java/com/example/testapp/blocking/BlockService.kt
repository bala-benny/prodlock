package com.example.testapp.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import com.example.testapp.R
import com.example.testapp.WalletManager
import java.util.Locale

class BlockService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var timerOverlay: View? = null
    private var timerTextView: TextView? = null
    private var currentPackage: String? = null

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val checkRunnable = object : Runnable {
        override fun run() {
            checkAndBlock()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onServiceConnected() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        handler.post(checkRunnable)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            currentPackage = event.packageName?.toString()
        }
    }

    private fun checkAndBlock() {
        val packageName = currentPackage ?: return
        val blockedApps = listOf(
            "com.google.android.youtube",
            "com.instagram.android",
            "com.facebook.katana",
            "com.netflix.mediaclient",
            "com.supercell.clashofclans"
        )

        if (blockedApps.contains(packageName)) {
            if (!WalletManager.isTimerRunning) {
                // If we are in a blocked app and timer is NOT running, block it
                performGlobalAction(GLOBAL_ACTION_HOME)
                removeTimerOverlay()
            } else {
                // Timer is running, show the overlay
                showTimerOverlay()
            }
        } else {
            // Not in a blocked app, hide overlay
            removeTimerOverlay()
        }
    }

    private fun showTimerOverlay() {
        if (timerOverlay != null) {
            updateOverlayText()
            return
        }

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100 

            val inflater = LayoutInflater.from(this)
            timerOverlay = inflater.inflate(R.layout.timer_overlay, null)
            timerTextView = timerOverlay?.findViewById(R.id.timerText)

            windowManager?.addView(timerOverlay, params)
            updateOverlayText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateOverlayText() {
        val seconds = WalletManager.remainingSeconds
        val m = seconds / 60
        val s = seconds % 60
        timerTextView?.text = String.format(Locale.getDefault(), "%02d:%02d", m, s)
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