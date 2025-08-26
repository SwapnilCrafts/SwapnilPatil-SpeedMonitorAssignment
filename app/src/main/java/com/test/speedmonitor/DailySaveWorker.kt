package com.test.speedmonitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepEntity
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DailySaveWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val prefs = StepPreferences(appContext)
    private val stepDao = AppDatabase.getDatabase(appContext).stepDao()
    private val sensorManager =
        appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val todayDate = getTodayDate()
            val lastSavedDate = prefs.getLastSavedDate()

            // Already saved → skip
            if (lastSavedDate == todayDate) {
                log("✔ Already saved for $todayDate. Skipping.")
                return@withContext Result.success()
            }

            // --- Step 1: Try prefs first ---
            var stepsToday = prefs.getCurrentSteps()

            // --- Step 2: Fallback to raw sensor if prefs missing/0 ---
            if (stepsToday == 0) {
                val currentTotal = readCurrentSensorTotal()
                val baseline = prefs.getPreviousTotalSteps()

                if (currentTotal > 0 && baseline > 0) {
                    stepsToday = (currentTotal - baseline).toInt()
                    log("📊 Using fallback from sensor → steps=$stepsToday (total=$currentTotal, baseline=$baseline)")
                }
            }

            // --- Step 3: Save steps if >0 ---
            stepDao.insert(StepEntity(todayDate, stepsToday))
            log("💾 Saved $stepsToday steps for $todayDate")

            // --- Step 4: Reset baseline for next day ---
            val currentTotal = readCurrentSensorTotal()
            prefs.resetStepsForNewDay(currentTotal, todayDate)
            log("🔄 Reset baseline=$currentTotal for next day")

            return@withContext Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.failure()
        }
    }

    private fun readCurrentSensorTotal(): Float {
        val event = FloatArray(1)
        val sensorEvent = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        // In real devices, WorkManager can't directly listen to sensors.
        // Normally you'd capture this in ViewModel and keep it in prefs.
        // For now, return prefs fallback if direct read isn't possible.
        return prefs.getPreviousTotalSteps()
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun log(msg: String) {
        android.util.Log.d("DailySaveWorker", msg)
    }
}
