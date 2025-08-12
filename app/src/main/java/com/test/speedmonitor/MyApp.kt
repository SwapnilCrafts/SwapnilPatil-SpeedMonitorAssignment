package com.test.speedmonitor

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailySaveWorker(this)
    }
}

fun scheduleDailySaveWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<DailySaveWorker>(1, TimeUnit.DAYS)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "DailySaveWorker",
        ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}
