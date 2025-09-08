package com.test.speedmonitor.db

import android.content.Context
import android.content.SharedPreferences

class StepPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    fun getLastSavedDate(): String? = prefs.getString("last_saved_date", null)
    fun saveLastSavedDate(date: String) {
        prefs.edit().putString("last_saved_date", date).apply()
    }

    fun getPreviousTotalSteps(): Float = prefs.getFloat("previous_total_steps", 0f)
    fun savePreviousTotalSteps(value: Float) {
        prefs.edit().putFloat("previous_total_steps", value).apply()
    }
    fun getStepsForDate(date: String): Int {
        val savedDate = prefs.getString("yesterday_date", null)
        return if (savedDate == date) {
            prefs.getInt("yesterday_steps", 0)
        } else {
            0
        }
    }
    fun getCurrentSteps(): Int = prefs.getInt("current_steps", 0)
    fun saveCurrentSteps(value: Int) {
        prefs.edit().putInt("current_steps", value).apply()
    }

    fun saveLatestSensorTotal(value: Float) {
        prefs.edit().putFloat("latest_sensor_total", value).apply()
    }
    fun getLatestSensorTotal(): Float = prefs.getFloat("latest_sensor_total", 0f)

    fun resetStepsForNewDay(sensorTotal: Float, todayDate: String) {
        prefs.edit()
            .putString("last_saved_date", todayDate)
            .putFloat("previous_total_steps", sensorTotal)
            .putInt("current_steps", 0)
            .apply()
    }
}
