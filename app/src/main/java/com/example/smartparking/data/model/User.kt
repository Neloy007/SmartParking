
package com.example.smartparking.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bkashNumber: String = "",
    val role: String = "",
    val profileImageUrl: String = "",

    // Driver verification information
    val carNumber: String = "",
    val nidNumber: String = "",
    val drivingLicenseNumber: String = "",

    // Verification workflow
    val verificationStatus: String = "NOT_SUBMITTED",
    val verificationSubmittedAt: Long = 0L,
    val verifiedAt: Long = 0L,
    val rejectionReason: String = "",

    // Backward compatibility with existing screens and records
    val isVerified: Boolean = false,
    val drivingLicenseImageUrl: String = "",
    val carImageUrl: String = "",

    val createdAt: Long = System.currentTimeMillis()
)
