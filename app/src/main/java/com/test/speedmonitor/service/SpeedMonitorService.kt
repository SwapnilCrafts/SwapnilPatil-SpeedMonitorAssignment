package com.test.speedmonitor.service

import com.test.speedmonitor.interfaces.Notifier
import com.test.speedmonitor.model.RentalSession

class SpeedMonitorService(
    private val notifier: Notifier
) {
    fun processSpeedUpdate(session: RentalSession, currentSpeed: Int) {
        println("Processing speed: $currentSpeed km/h for session with limit: ${session.speedLimit} km/h")
        if (currentSpeed > session.speedLimit) {
            notifier.notifyRentalCompany(session, currentSpeed)
            notifier.alertUser(currentSpeed)
        } else {
            println("Speed is within allowed limit.")
        }
    }
}