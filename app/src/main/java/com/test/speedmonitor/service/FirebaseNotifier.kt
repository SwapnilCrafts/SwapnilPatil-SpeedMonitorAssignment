package com.test.speedmonitor.service

import com.test.speedmonitor.interfaces.Notifier
import com.test.speedmonitor.model.RentalSession

class FirebaseNotifier : Notifier {
    override fun notifyRentalCompany(session: RentalSession, currentSpeed: Int) {
        // Simulate FCM notification to rental company
        println("Firebase: Notification sent to rental company - Speed exceeded (${currentSpeed}km/h)")
    }

    override fun alertUser(currentSpeed: Int) {
        // Simulate user alert
        println("Firebase: Alert sent to user - You are overspeeding! Current Speed: ${currentSpeed}km/h")
    }
}