package com.test.speedmonitor

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore(name = "step_prefs")



class StepCounterViewModel(app: Application) : AndroidViewModel(app), SensorEventListener {

    private val context = app.applicationContext
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val PREF_KEY_BASE_STEPS = intPreferencesKey("base_steps")
    private val PREF_KEY_LAST_STEP_COUNT = intPreferencesKey("last_step_count")

    private var baseSteps = 0

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    init {
        viewModelScope.launch {
            baseSteps = loadBaseStep()
            val lastCount = loadLastStepCount()
            _stepCount.value = lastCount
            Log.d("StepCounter", "Loaded baseSteps: $baseSteps, lastStepCount: $lastCount")

            stepSensor?.let {
                sensorManager.registerListener(this@StepCounterViewModel, it, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val totalSteps = event?.values?.get(0)?.toInt() ?: return
        Log.d("StepCounter", "Sensor changed - totalSteps: $totalSteps, baseSteps: $baseSteps")

        if (baseSteps == 0 || totalSteps < baseSteps) {
            // First launch or device reboot/reset of step sensor
            baseSteps = totalSteps
            saveBaseStep(baseSteps)
            Log.d("StepCounter", "Base step initialized/reset to $baseSteps")
        }

        val steps = totalSteps - baseSteps
        Log.d("StepCounter", "Calculated steps: $steps, Previous stepCount: ${_stepCount.value}")

        if (_stepCount.value != steps) {
            _stepCount.value = steps
            saveLastStepCount(steps)
            Log.d("StepCounter", "StepCount updated to $steps")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private suspend fun loadBaseStep(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[PREF_KEY_BASE_STEPS] ?: 0
    }

    private suspend fun loadLastStepCount(): Int {
        val prefs = context.dataStore.data.first()
        return prefs[PREF_KEY_LAST_STEP_COUNT] ?: 0
    }

    private fun saveBaseStep(steps: Int) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[PREF_KEY_BASE_STEPS] = steps
            }
        }
    }

    private fun saveLastStepCount(count: Int) {
        viewModelScope.launch {
            context.dataStore.edit {
                it[PREF_KEY_LAST_STEP_COUNT] = count
            }
        }
    }

    override fun onCleared() {
        sensorManager.unregisterListener(this)
        super.onCleared()
    }
}
