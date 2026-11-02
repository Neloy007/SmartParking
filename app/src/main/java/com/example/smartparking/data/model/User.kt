package com.example.smartparking.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val profileImageUrl: String = "",

    // Driver information
    val drivingLicenseNumber: String = "",
    val drivingLicenseImageUrl: String = "",
    val carNumber: String = "",
    val carImageUrl: String = "",

    // Account status
    val isVerified: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)