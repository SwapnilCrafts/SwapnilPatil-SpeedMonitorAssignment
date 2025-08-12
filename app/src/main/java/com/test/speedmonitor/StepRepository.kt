package com.test.speedmonitor

import com.test.speedmonitor.db.StepDao
import com.test.speedmonitor.db.StepEntity

class StepRepository(private val dao: StepDao) {
    fun getAllSteps() = dao.getAllStepsFlow() // now it's Flow
    suspend fun saveSteps(date: String, steps: Int) {
        dao.insert(StepEntity(date, steps))
    }
}


