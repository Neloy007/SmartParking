package com.example.smartparking.data.model


data class ParkingSession(
    val id: String = "",
    val bookingId: String = "",
    val driverId: String = "",
    val ownerId: String = "",
    val parkingId: String = "",
    val parkingName: String = "",
    val driverName: String = "",
    val driverPhone: String = "",
    val hourlyRate: Double = 0.0,
    val status: String = "",
    val paymentStatus: String = "",
    val durationMinutes: Int = 0,
    val startedAt: Long = 0L,
    val createdAt: Long = 0L,
    val paidAt: Long = 0L,
    val expiresAt: Long = 0L,
    val totalPaid: Double = 0.0,
    val extensionCount: Int = 0
)
