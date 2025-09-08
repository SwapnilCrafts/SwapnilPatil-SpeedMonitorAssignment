package com.test.speedmonitor

import android.Manifest
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.work.*
import com.google.android.gms.location.ActivityRecognition
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailyStepSave()
        registerActivityRecognition()
    }

    private fun scheduleDailyStepSave() {
        val now = Calendar.getInstance()
        val midnight = now.clone() as Calendar
        midnight.set(Calendar.HOUR_OF_DAY, 0)
        midnight.set(Calendar.MINUTE, 0)   // run at 00:00
        midnight.set(Calendar.SECOND, 0)
        midnight.set(Calendar.MILLISECOND, 0)

        if (midnight.before(now)) {
            midnight.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = midnight.timeInMillis - now.timeInMillis

        val dailyWork = PeriodicWorkRequestBuilder<DailySaveWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyStepSave",
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // ensures fresh schedule
            dailyWork
        )
    }
    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    private fun registerActivityRecognition() {
        val client = ActivityRecognition.getClient(this)
        val intent = Intent(this, ActivityReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        client.requestActivityUpdates(5000, pendingIntent) // every 5 sec
    }

}
