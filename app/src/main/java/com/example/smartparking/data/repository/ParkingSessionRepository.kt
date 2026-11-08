
package com.example.smartparking.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ParkingSessionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun currentDriverId(): String {
        return FirebaseAuth.getInstance()
            .currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")
    }


    suspend fun createPendingSession(
        parkingId: String,
        parkingName: String,
        hourlyRate: Double
    ): String {

        val driverId = FirebaseAuth.getInstance()
            .currentUser?.uid
            ?: throw IllegalStateException("Please log in first.")

        val currentTime = System.currentTimeMillis()

        val session = hashMapOf(
            "driverId" to driverId,
            "parkingId" to parkingId,
            "parkingName" to parkingName,
            "hourlyRate" to hourlyRate,
            "status" to "PENDING_PAYMENT",
            "paymentStatus" to "UNPAID",
            "startedAt" to currentTime,
            "createdAt" to currentTime
        )

        return firestore
            .collection("parking_sessions")
            .add(session)
            .await()
            .id
    }


    // Confirm the initial demo payment and start a 60-minute session.
    suspend fun markDemoPaymentSuccessful(
        sessionId: String,
        durationMinutes: Int = 60
    ) {
        require(durationMinutes > 0) {
            "Duration must be greater than zero."
        }

        val driverId = currentDriverId()
        val now = System.currentTimeMillis()

        val sessionRef = firestore
            .collection("parking_sessions")
            .document(sessionId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(sessionRef)

            if (snapshot.getString("driverId") != driverId) {
                throw IllegalStateException("This session does not belong to you.")
            }

            if (snapshot.getString("status") != "PENDING_PAYMENT") {
                throw IllegalStateException("This session is not awaiting payment.")
            }

            val hourlyRate = snapshot.getDouble("hourlyRate")
                ?: snapshot.getLong("hourlyRate")?.toDouble()
                ?: throw IllegalStateException("Parking rate not found.")

            val initialCost = hourlyRate * durationMinutes / 60.0

            transaction.update(
                sessionRef,
                mapOf(
                    "status" to "CONFIRMED",
                    "paymentStatus" to "DEMO_PAID",
                    "durationMinutes" to durationMinutes,
                    "expiresAt" to now + durationMinutes * 60_000L,
                    "totalPaid" to initialCost,
                    "extensionCount" to 0,
                    "paidAt" to now
                )
            )
        }.await()
    }

    // Extend an active session after demo payment confirmation.
    suspend fun extendDemoSession(
        sessionId: String,
        additionalMinutes: Int
    ) {
        require(additionalMinutes in listOf(30, 60, 120)) {
            "Select a valid extension duration."
        }

        val driverId = currentDriverId()
        val now = System.currentTimeMillis()

        val sessionRef = firestore
            .collection("parking_sessions")
            .document(sessionId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(sessionRef)

            if (snapshot.getString("driverId") != driverId) {
                throw IllegalStateException("This session does not belong to you.")
            }

            if (snapshot.getString("status") != "CONFIRMED" ||
                snapshot.getString("paymentStatus") != "DEMO_PAID"
            ) {
                throw IllegalStateException("This parking session is not active.")
            }

            val currentExpiry = snapshot.getLong("expiresAt")
                ?: throw IllegalStateException("Session expiry not found.")

            if (currentExpiry <= now) {
                throw IllegalStateException("Your parking session has expired.")
            }

            val currentDuration = snapshot.getLong("durationMinutes") ?: 60L

            val currentTotalPaid = snapshot.getDouble("totalPaid")
                ?: snapshot.getLong("totalPaid")?.toDouble()
                ?: 0.0

            val hourlyRate = snapshot.getDouble("hourlyRate")
                ?: snapshot.getLong("hourlyRate")?.toDouble()
                ?: throw IllegalStateException("Parking rate not found.")

            val extensionCost =
                hourlyRate * additionalMinutes / 60.0

            transaction.update(
                sessionRef,
                mapOf(
                    "durationMinutes" to currentDuration + additionalMinutes,
                    "expiresAt" to currentExpiry + additionalMinutes * 60_000L,
                    "totalPaid" to currentTotalPaid + extensionCost,
                    "extensionCount" to
                            ((snapshot.getLong("extensionCount") ?: 0L) + 1L),
                    "lastExtensionMinutes" to additionalMinutes,
                    "paidAt" to now
                )
            )
        }.await()
    }
}
