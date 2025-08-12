// DailySaveWorker.kt
package com.test.speedmonitor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepEntity
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class DailySaveWorker(
    val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val stepDao = AppDatabase.getDatabase(context).stepDao()

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = StepPreferences(context)
                val todaySteps = prefs.getCurrentSteps() // live counter stored in prefs
             //   val today = LocalDate.now().toString()
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                stepDao.insert(StepEntity(date = today, steps = todaySteps))
                prefs.resetSteps() // reset live counter after saving

                Result.success()
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure()
            }
        }
    }
}
