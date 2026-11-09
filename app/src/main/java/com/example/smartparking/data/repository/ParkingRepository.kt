package com.example.smartparking.data.repository

import com.example.smartparking.data.model.Parking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ParkingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Register a new parking facility
    suspend fun addParking(parking: Parking): Result<Parking> {
        return try {
            val document = firestore
                .collection("parkings")
                .document()

            val newParking = parking.copy(
                id = document.id,
                createdAt = System.currentTimeMillis()
            )

            android.util.Log.d("ParkingRepository", "Parking ID: ${newParking.id}")
            android.util.Log.d("ParkingRepository", "Owner ID: ${newParking.ownerId}")
            android.util.Log.d("ParkingRepository", "Active: ${newParking.active}")
            android.util.Log.d("ParkingRepository", "Total slots: ${newParking.totalSlots}")
            android.util.Log.d("ParkingRepository", "Available slots: ${newParking.availableSlots}")
            android.util.Log.d("ParkingRepository", "Price: ${newParking.pricePerHour}")

            document.set(newParking).await()

            Result.success(newParking)

        } catch (e: Exception) {
            android.util.Log.e(
                "ParkingRepository",
                "Failed to register parking facility",
                e
            )
            Result.failure(e)
        }
    }

    // Retrieve all active parking facilities

    suspend fun getActiveParkings(): Result<List<Parking>> {
        return try {
            val snapshot = firestore.collection("parkings")
                .whereEqualTo("active", true)
                .get()
                .await()

            val parkings = snapshot.documents.mapNotNull {
                it.toObject(Parking::class.java)
            }

            Result.success(parkings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Retrieve parking facilities belonging to an owner
    suspend fun getOwnerParkings(ownerId: String): Result<List<Parking>> {
        return try {
            val snapshot = firestore
                .collection("parkings")
                .whereEqualTo("ownerId", ownerId)
                .get()
                .await()

            val parkingList = snapshot.documents.mapNotNull {
                it.toObject(Parking::class.java)
            }

            Result.success(parkingList)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update parking availability
    suspend fun updateAvailability(
        parkingId: String,
        availableSlots: Int
    ): Result<Unit> {
        return try {
            require(availableSlots >= 0) {
                "Available slots cannot be negative"
            }

            firestore.collection("parkings")
                .document(parkingId)
                .update("availableSlots", availableSlots)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateParking(
        parking: Parking
    ): Result<Unit> {
        return try {
            require(parking.name.isNotBlank()) {
                "Facility name cannot be empty."
            }
            require(parking.address.isNotBlank()) {
                "Address cannot be empty."
            }
            require(parking.totalSlots > 0) {
                "Total slots must be greater than zero."
            }
            require(parking.pricePerHour >= 0 && parking.pricePerHour.isFinite()) {
                "Enter a valid hourly price."
            }

            val parkingRef = firestore.collection("parkings")
                .document(parking.id)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(parkingRef)

                if (!snapshot.exists()) {
                    throw IllegalStateException("Parking facility not found.")
                }

                val oldTotal = snapshot.getLong("totalSlots")?.toInt()
                    ?: throw IllegalStateException("Current capacity not found.")

                val oldAvailable = snapshot.getLong("availableSlots")?.toInt()
                    ?: throw IllegalStateException("Current availability not found.")

                val occupiedSlots = oldTotal - oldAvailable

                if (occupiedSlots < 0) {
                    throw IllegalStateException("Invalid parking availability data.")
                }

                if (parking.totalSlots < occupiedSlots) {
                    throw IllegalArgumentException(
                        "Capacity cannot be less than $occupiedSlots occupied slots."
                    )
                }

                val newAvailable = parking.totalSlots - occupiedSlots

                transaction.update(
                    parkingRef,
                    mapOf(
                        "name" to parking.name.trim(),
                        "address" to parking.address.trim(),
                        "totalSlots" to parking.totalSlots,
                        "availableSlots" to newAvailable,
                        "pricePerHour" to parking.pricePerHour,
                        "active" to parking.active
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteParking(
        parkingId: String
    ): Result<Unit> {
        return try {
            val parkingRef = firestore.collection("parkings")
                .document(parkingId)

            // Check for an active parking session before deletion.
            val activeSessions = firestore.collection("parking_sessions")
                .whereEqualTo("parkingId", parkingId)
                .whereEqualTo("status", "CONFIRMED")
                .get()
                .await()

            val now = System.currentTimeMillis()

            val hasActiveSession = activeSessions.documents.any { session ->
                (session.getLong("expiresAt") ?: 0L) > now
            }

            if (hasActiveSession) {
                throw IllegalStateException(
                    "This facility has active parking sessions and cannot be deleted yet."
                )
            }

            parkingRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}