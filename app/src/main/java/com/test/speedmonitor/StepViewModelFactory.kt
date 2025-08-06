package com.test.speedmonitor


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StepCounterViewModelFactory(private val app: Application) : ViewModelProvider.AndroidViewModelFactory(app) {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StepCounterViewModel::class.java)) {
            return StepCounterViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

