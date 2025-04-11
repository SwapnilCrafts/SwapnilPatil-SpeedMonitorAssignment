package com.test.speedmonitor.service

import com.test.speedmonitor.interfaces.Notifier
import com.test.speedmonitor.model.RentalSession

class AwsNotifier : Notifier {
    override fun notifyRentalCompany(session: RentalSession, currentSpeed: Int) {
        // Stub for AWS SNS or other notification service
        println("AWS: Notification sent to rental company - Speed exceeded (${currentSpeed}km/h)")
    }

    override fun alertUser(currentSpeed: Int) {
        println("AWS: Alert sent to user - You are overspeeding! Current Speed: ${currentSpeed}km/h")
    }
}