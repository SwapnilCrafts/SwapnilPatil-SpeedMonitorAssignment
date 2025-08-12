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

    fun savePreviousTotalSteps(totalSteps: Float) {
        prefs.edit().putFloat("previous_total_steps", totalSteps).apply()
    }

    fun getPreviousTotalSteps(): Float {
        return prefs.getFloat("previous_total_steps", 0f)
    }

    fun saveLastSavedDate(date: String) {
        prefs.edit().putString("last_saved_date", date).apply()
    }

    fun getLastSavedDate(): String? {
        return prefs.getString("last_saved_date", null)
    }

    fun resetStepsForNewDay(totalSteps: Float, date: String) {
        prefs.edit()
            .putFloat("previous_total_steps", totalSteps)
            .putInt("current_steps", 0)
            .putString("last_saved_date", date)
            .apply()
    }
}

