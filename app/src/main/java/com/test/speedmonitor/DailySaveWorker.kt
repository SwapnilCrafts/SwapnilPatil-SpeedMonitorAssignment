package com.test.speedmonitor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepEntity
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailySaveWorker(val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prefs = StepPreferences(applicationContext)
        val stepDao = AppDatabase.getDatabase(applicationContext).stepDao()

        val yesterday = LocalDate.now().minusDays(1).toString()
        val today = LocalDate.now().toString()

        val stepsYesterday = prefs.getCurrentSteps()

        Log.d("DailySaveWorker", "▶ Worker running for $yesterday, steps=$stepsYesterday")

        // Save yesterday’s steps if not already saved
        val already = stepDao.getStepsForDate(yesterday)
        if (already == null) {
            stepDao.insert(StepEntity(yesterday, stepsYesterday))
            Log.d("DailySaveWorker", "💾 Saved $stepsYesterday steps for $yesterday")
        } else {
            Log.d("DailySaveWorker", "✔ Already saved for $yesterday, skipping")
        }

        // Reset baseline for today (so new steps start from 0)
        val sensorTotal = prefs.getLatestSensorTotal()
        if (sensorTotal > 0f) {
            prefs.resetStepsForNewDay(sensorTotal, today)
            Log.d("DailySaveWorker", "🔄 Reset baseline=$sensorTotal for $today")
        } else {
            Log.d("DailySaveWorker", "⏸ Baseline not reset (no sensor data yet)")
        }

        scheduleNextRun(appContext)
        Result.success()
    }
    private fun scheduleNextRun(context: Context) {
        val now = Calendar.getInstance()
        val next = now.clone() as Calendar
        next.set(Calendar.HOUR_OF_DAY, 0)
        next.set(Calendar.MINUTE, 0)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)

        if (next.before(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delay = next.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<DailySaveWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "dailySaveWork",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
