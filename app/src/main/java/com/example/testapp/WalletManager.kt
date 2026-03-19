package com.example.testapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import androidx.core.app.NotificationCompat

object WalletManager {
    var minutes: Int = 0
    var isTimerRunning: Boolean = false
    var remainingSeconds: Long = 0

    private var timer: CountDownTimer? = null
    private const val PREFS_NAME = "prodlock_prefs"
    private const val KEY_MINUTES = "wallet_minutes"

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        minutes = prefs.getInt(KEY_MINUTES, 0)
    }

    private fun save(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MINUTES, minutes).apply()
    }

    fun addMinutes(context: Context, amount: Int) {
        minutes += amount
        save(context)
    }

    fun startTimer(context: Context, onTick: (Long) -> Unit, onFinish: () -> Unit) {
        if (minutes <= 0 || isTimerRunning) return

        isTimerRunning = true
        remainingSeconds = minutes * 60L
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        timer = object : CountDownTimer(remainingSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
                minutes = (remainingSeconds / 60).toInt()
                
                updateNotification(context, notificationManager)
                onTick(remainingSeconds)
            }

            override fun onFinish() {
                isTimerRunning = false
                minutes = 0
                remainingSeconds = 0
                save(context)
                notificationManager.cancel(101)
                onFinish()
            }
        }.start()
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_channel",
                "Session Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows active session time"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(context: Context, notificationManager: NotificationManager) {
        val m = remainingSeconds / 60
        val s = remainingSeconds % 60
        val timeStr = String.format("%02d:%02d", m, s)

        val notification = NotificationCompat.Builder(context, "timer_channel")
            .setContentTitle("Session Active")
            .setContentText("Time remaining: $timeStr")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        
        notificationManager.notify(101, notification)
    }

    fun stopTimer(context: Context) {
        timer?.cancel()
        isTimerRunning = false
        save(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(101)
    }
}