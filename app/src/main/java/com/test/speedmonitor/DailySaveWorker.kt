package com.test.speedmonitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepEntity
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class DailySaveWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val stepDao = AppDatabase.getDatabase(context).stepDao()
    private val prefs = StepPreferences(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val today = getTodayDate()
                val todaySteps = prefs.getCurrentSteps()

                // 1. Save today's steps to DB
                stepDao.insert(StepEntity(date = today, steps = todaySteps))

                // 2. Get the current device lifetime steps from the sensor
                val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

                var totalStepsFromSensor = 0f
                if (stepSensor != null) {
                    // Use direct listener to fetch current total steps quickly
                    val listener = object : android.hardware.SensorEventListener {
                        override fun onSensorChanged(event: android.hardware.SensorEvent) {
                            if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                                totalStepsFromSensor = event.values[0]
                                sensorManager.unregisterListener(this)
                            }
                        }
                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    }
                    sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    // Give sensor a moment to respond
                    Thread.sleep(200)
                }

                // 3. Reset preferences for the new day
                prefs.resetStepsForNewDay(totalStepsFromSensor, today)

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}
