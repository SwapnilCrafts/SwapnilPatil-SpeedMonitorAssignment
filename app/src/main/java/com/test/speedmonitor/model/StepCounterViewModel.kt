package com.test.speedmonitor.model

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.test.speedmonitor.ActivityReceiver
import com.test.speedmonitor.db.AppDatabase
import com.test.speedmonitor.db.StepPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val today = getTodayDate()
        val lastSavedDate = prefs.getLastSavedDate()

        if (lastSavedDate == today) {
            previousTotalSteps = prefs.getPreviousTotalSteps()
            _stepCount.value = prefs.getCurrentSteps()
        } else {
            // will set after first event
            prefs.resetStepsForNewDay(0f, today)
        }
        registerStepSensor()
    }

    private fun registerStepSensor() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

//    override fun onSensorChanged(event: SensorEvent?) {
//        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
//            val totalSteps = event.values[0]
//            val today = getTodayDate()
//
//            if (prefs.getLastSavedDate() != today || previousTotalSteps == 0f) {
//                prefs.resetStepsForNewDay(totalSteps, today)
//                previousTotalSteps = totalSteps
//                _stepCount.value = 0
//                return
//            }
//
//            val stepsToday = (totalSteps - previousTotalSteps).toInt()
//            if (stepsToday >= 0) {
//                _stepCount.value = stepsToday
//                prefs.saveCurrentSteps(stepsToday)
//                prefs.savePreviousTotalSteps(previousTotalSteps)
//                prefs.saveLatestSensorTotal(totalSteps)
//            }
//        }
//    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            if (ActivityReceiver.isInVehicle) {
                // Ignore steps while in vehicle 🚗🚆🚌
                return
            }

            val totalSteps = event.values[0]
            val today = getTodayDate()

            if (prefs.getLastSavedDate() != today || previousTotalSteps == 0f) {
                prefs.resetStepsForNewDay(totalSteps, today)
                previousTotalSteps = totalSteps
                _stepCount.value = 0
                return
            }

            val stepsToday = (totalSteps - previousTotalSteps).toInt()
            if (stepsToday >= 0) {
                _stepCount.value = stepsToday
                prefs.saveCurrentSteps(stepsToday)
                prefs.savePreviousTotalSteps(previousTotalSteps)
                prefs.saveLatestSensorTotal(totalSteps)
            }
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
