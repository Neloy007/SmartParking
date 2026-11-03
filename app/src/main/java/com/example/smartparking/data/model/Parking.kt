package com.example.smartparking.data.model

data class Parking(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val totalSlots: Int = 0,
    val availableSlots: Int = 0,
    val pricePerHour: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)