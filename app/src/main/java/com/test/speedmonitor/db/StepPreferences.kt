package com.test.speedmonitor.db


import android.content.Context

class StepPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    fun saveCurrentSteps(steps: Int) {
        prefs.edit().putInt("current_steps", steps).apply()
    }

    fun getCurrentSteps(): Int {
        return prefs.getInt("current_steps", 0)
    }

    fun savePreviousTotalSteps(total: Float) {
        prefs.edit().putFloat("previous_total", total).apply()
    }

    fun getPreviousTotalSteps(): Float {
        return prefs.getFloat("previous_total", 0f)
    }

    fun getLastSavedDate(): String {
        return prefs.getString("last_date", "") ?: ""
    }

    fun resetStepsForNewDay(baseline: Float, todayDate: String) {
        prefs.edit()
            .putFloat("previous_total", baseline)
            .putInt("current_steps", 0)
            .putString("last_date", todayDate)
            .apply()
    }
}

