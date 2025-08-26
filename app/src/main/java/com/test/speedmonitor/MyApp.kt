package com.test.speedmonitor

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.text.set

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleDailySaveWorker(this)
        // Schedule exact alarm only if permission granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (alarmManager?.canScheduleExactAlarms() == true) {
                scheduleMidnightAlarm(this)
            } else {
                // Permission missing — prompt user to grant it
                requestExactAlarmPermission(this)
                // Optional: fallback to inexact alarm or skip scheduling exact alarm now
            }
        } else {
            // On older Android versions, permission not required
            scheduleMidnightAlarm(this)
        }
    }
}
fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager?.canScheduleExactAlarms() == false) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }
}

fun scheduleDailySaveWorker(context: Context) {
    val now = Calendar.getInstance()
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1) // next midnight
    }

    val initialDelay = midnight.timeInMillis - now.timeInMillis

    val workRequest = PeriodicWorkRequestBuilder<DailySaveWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
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

fun scheduleMidnightAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager

    val midnight = Calendar.getInstance().apply {
        timeInMillis = System.currentTimeMillis()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_MONTH, 1) // next midnight
    }

    val intent = Intent(context, MidnightAlarmReceiver::class.java)
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        0,
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        android.app.AlarmManager.RTC_WAKEUP,
        midnight.timeInMillis,
        pendingIntent
    )
}