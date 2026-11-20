
package com.example.smartparking.data.model

data class Booking(
    val id: String = "",

    // Facility and user references
    val parkingId: String = "",
    val parkingName: String = "",
    val ownerId: String = "",
    val driverId: String = "",

    // Driver information snapshot
    val driverName: String = "",
    val driverPhone: String = "",

    // Booking schedule (Unix timestamps in milliseconds)
    val startTime: Long = 0L,
    val endTime: Long = 0L,

    // Price information
    val pricePerHour: Double = 0.0,
    val durationHours: Double = 0.0,
    val totalAmount: Double = 0.0,

    // Owner's bKash payment information
    // ownerPhone is retained for compatibility with existing code.
    val ownerPhone: String = "",
    val ownerBkashNumber: String = "",

    // Payment information
    val paymentMethod: String = "BKASH",
    val transactionId: String = "",

    // Booking and payment states
    // WAITING_FOR_APPROVAL, CONFIRMED, REJECTED, CANCELLED
    val bookingStatus: String = "PENDING",

    // TRXID_SUBMITTED, PAID, REJECTED, CANCELLED
    val paymentStatus: String = "PENDING",

    // One parking slot reserved by this booking
    val reservedSlots: Int = 1,

    // Booking timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Owner approval information
    val verifiedBy: String = "",
    val verifiedAt: Long = 0L,
    val rejectionReason: String = "",

    // Parking session created after approval
    val sessionId: String = "",
    val sessionStartedAt: Long = 0L,

    val bookingType: String = "ADVANCE",
)
