package com.example.testapp

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

fun getForegroundApp(context: Context): String? {

    val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    val endTime = System.currentTimeMillis()
    val beginTime = endTime - 10000

    val usageEvents: UsageEvents =
        usageStatsManager.queryEvents(beginTime, endTime)

    var lastApp: String? = null

    val event = UsageEvents.Event()

    while (usageEvents.hasNextEvent()) {
        usageEvents.getNextEvent(event)

        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            lastApp = event.packageName
        }
    }

    return lastApp
}