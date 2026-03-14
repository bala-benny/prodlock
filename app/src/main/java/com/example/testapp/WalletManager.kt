package com.example.testapp

import android.os.CountDownTimer

object WalletManager {
    var minutes: Int = 0
    var isTimerRunning: Boolean = false
    var remainingSeconds: Long = 0

    private var timer: CountDownTimer? = null

    fun startTimer(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        if (minutes <= 0 || isTimerRunning) return

        isTimerRunning = true
        remainingSeconds = minutes * 60L

        timer = object : CountDownTimer(remainingSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingSeconds = millisUntilFinished / 1000
                minutes = (remainingSeconds / 60).toInt()
                onTick(remainingSeconds)
            }

            override fun onFinish() {
                isTimerRunning = false
                minutes = 0
                remainingSeconds = 0
                onFinish()
            }
        }.start()
    }

    fun stopTimer() {
        timer?.cancel()
        isTimerRunning = false
    }
}