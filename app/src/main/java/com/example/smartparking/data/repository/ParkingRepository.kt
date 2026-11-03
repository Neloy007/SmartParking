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
            val snapshot = firestore
                .collection("parkings")
                .whereEqualTo("isActive", true)
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
}