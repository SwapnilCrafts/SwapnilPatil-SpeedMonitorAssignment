package com.test.speedmonitor
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
class MidnightAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Run DailySaveWorker immediately
        val workRequest = OneTimeWorkRequestBuilder<DailySaveWorker>()
            .setInitialDelay(0, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
        // Reschedule alarm for next midnight
        scheduleMidnightAlarm(context)
    }
}


