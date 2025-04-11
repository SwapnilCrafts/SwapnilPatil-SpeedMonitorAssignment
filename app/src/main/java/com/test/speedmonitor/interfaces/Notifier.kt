package com.test.speedmonitor.interfaces

import com.test.speedmonitor.model.RentalSession

interface Notifier {
    fun notifyRentalCompany(session: RentalSession, currentSpeed: Int)
    fun alertUser(currentSpeed: Int)
}