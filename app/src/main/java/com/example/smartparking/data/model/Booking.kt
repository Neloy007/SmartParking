package com.example.smartparking.data.model


data class Booking(
    val id: String = "",

    // Facility and user references
    val parkingId: String = "",
    val parkingName: String = "",
    val ownerId: String = "",
    val driverId: String = "",

    // Booking schedule (Unix timestamps in milliseconds)
    val startTime: Long = 0L,
    val endTime: Long = 0L,

    // Price information
    val pricePerHour: Double = 0.0,
    val durationHours: Double = 0.0,
    val totalAmount: Double = 0.0,

    // Owner's bKash payment information
    val ownerPhone: String = "",
    val paymentMethod: String = "BKASH",
    val transactionId: String = "",

    // Booking and payment states
    val bookingStatus: String = "PENDING",
    val paymentStatus: String = "PENDING",

    // One parking slot reserved by this booking
    val reservedSlots: Int = 1,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    val verifiedBy: String = "",
    val verifiedAt: Long = 0L
)
