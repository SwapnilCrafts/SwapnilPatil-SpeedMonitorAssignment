package com.test.speedmonitor.model

data class RentalSession(
    val renter: Renter,
    val car: Car,
    val startTime: Long,
    val endTime: Long?,
    val speedLimit: Int
)
