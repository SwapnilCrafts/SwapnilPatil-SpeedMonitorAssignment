package com.test.speedmonitor.ui

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepEntity
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StepCounterViewModel(application: Application) :
    AndroidViewModel(application), SensorEventListener {

    private val stepDao = AppDatabase.getDatabase(application).stepDao()
    private val prefs = StepPreferences(application)

    private val sensorManager =
        application.getSystemService(Application.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var previousTotalSteps = 0f

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    val history: StateFlow<Map<String, Int>> = stepDao.getAllStepsFlow()
        .map { list -> list.associate { it.date to it.steps } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        // Load initial steps from preferences
        _stepCount.value = prefs.getCurrentSteps()
        registerStepSensor()
    }

    private fun registerStepSensor() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0]

            if (previousTotalSteps == 0f) {
                previousTotalSteps = totalSteps
            }

            val stepsToday = (totalSteps - previousTotalSteps).toInt()

            if (stepsToday >= 0) {
                _stepCount.value = stepsToday
                prefs.saveCurrentSteps(stepsToday)
                saveStepsToDb(stepsToday)
            }
        }
    }

    private fun saveStepsToDb(stepsToday: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayDate = getTodayDate()
            stepDao.insert(StepEntity(todayDate, stepsToday))
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}
