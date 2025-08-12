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

    fun resetSteps() {
        prefs.edit().putInt("current_steps", 0).apply()
    }
}
