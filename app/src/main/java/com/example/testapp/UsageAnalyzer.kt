package com.example.testapp

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context

class UsageAnalyzer {

    fun getDailyUsage(context: Context): List<UsageStats> {

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24)

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
    }

    fun predictMonthlyUsage(dailyMinutes: Int): Int {
        return dailyMinutes * 30
    }
}