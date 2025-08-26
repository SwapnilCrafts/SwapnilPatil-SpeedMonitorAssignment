package com.test.speedmonitor

import android.content.Context
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

class DailySaveWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val prefs = StepPreferences(appContext)
    private val stepDao = AppDatabase.getDatabase(appContext).stepDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val todayDate = dateFormat.format(Date())
            val lastSavedDate = prefs.getLastSavedDate()
            val todaySteps = prefs.getCurrentSteps()
            val previousTotal = prefs.getPreviousTotalSteps()

            Log.d("DailySaveWorker", "▶ Running worker for $todayDate, last saved=$lastSavedDate")

            // Case 1: First ever run
            if (lastSavedDate.isEmpty()) {
                stepDao.insert(StepEntity(todayDate, todaySteps))
                prefs.resetStepsForNewDay(previousTotal, todayDate)
                Log.d("DailySaveWorker", " First run → saved $todaySteps for $todayDate")
                return@withContext Result.success()
            }

            // Calculate gap days
            val lastDate = dateFormat.parse(lastSavedDate)!!
            val today = dateFormat.parse(todayDate)!!
            val diffDays = ((today.time - lastDate.time) / (1000 * 60 * 60 * 24)).toInt()

            if (diffDays == 0) {
                // Already saved today → just ignore
                Log.d("DailySaveWorker", "✔ Already saved for today ($todayDate). Skipping.")
                return@withContext Result.success()
            }

            // Case 2: Missed some days → reconstruct
            if (diffDays > 1) {
                Log.d("DailySaveWorker", "⚠ Missed $diffDays days, reconstructing history...")

                // Steps since last save
                val totalSinceLast = todaySteps
                val avgSteps = if (diffDays > 0) totalSinceLast / diffDays else 0

                val calendar = Calendar.getInstance().apply { time = lastDate }

                for (i in 1..diffDays) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    val missingDate = dateFormat.format(calendar.time)

                    val stepsForDay = if (i < diffDays) avgSteps else (totalSinceLast - avgSteps * (diffDays - 1))
                    stepDao.insert(StepEntity(missingDate, stepsForDay))
                    Log.d("DailySaveWorker", "📝 Recovered $stepsForDay steps for $missingDate")
                }
            } else {
                // Normal case: just save today
                stepDao.insert(StepEntity(todayDate, todaySteps))
                Log.d("DailySaveWorker", "✅ Saved $todaySteps steps for $todayDate")
            }

            // Reset prefs for new day
            prefs.resetStepsForNewDay(previousTotal, todayDate)

            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e("DailySaveWorker", "❌ Error: ${e.message}", e)
            return@withContext Result.failure()
        }
    }
}
