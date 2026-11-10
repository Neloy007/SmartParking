package com.example.smartparking.data.repository

import com.example.smartparking.data.model.Parking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class ParkingRepository {

    private val firestore = FirebaseFirestore.getInstance()


    // Register a new parking facility
    suspend fun addParking(parking: Parking): Result<Parking> {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("Please log in before registering a facility.")

            val document = firestore.collection("parkings").document()

            val newParking = parking.copy(
                id = document.id,
                ownerId = currentUser.uid,
                approvalStatus = "PENDING",
                active = false,
                createdAt = System.currentTimeMillis()
            )

            android.util.Log.d("ParkingRepository", "Parking ID: ${newParking.id}")
            android.util.Log.d("ParkingRepository", "Owner ID: ${newParking.ownerId}")
            android.util.Log.d("ParkingRepository", "Current UID: ${currentUser.uid}")
            android.util.Log.d("ParkingRepository", "Approval: ${newParking.approvalStatus}")
            android.util.Log.d("ParkingRepository", "Active: ${newParking.active}")
            android.util.Log.d("ParkingRepository", "Total slots: ${newParking.totalSlots}")
            android.util.Log.d("ParkingRepository", "Available slots: ${newParking.availableSlots}")
            android.util.Log.d("ParkingRepository", "Price: ${newParking.pricePerHour}")

            require(newParking.totalSlots > 0) {
                "Total slots must be greater than zero."
            }

            require(newParking.availableSlots in 0..newParking.totalSlots) {
                "Available slots must be between 0 and total slots."
            }

            require(newParking.pricePerHour >= 0 &&
                    newParking.pricePerHour.isFinite()) {
                "Enter a valid hourly price."
            }

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




    // Retrieve only admin-approved and active parking facilities.
    suspend fun getActiveParkings(): Result<List<Parking>> {
        return try {
            val snapshot = firestore.collection("parkings")
                .whereEqualTo("approvalStatus", "APPROVED")
                .whereEqualTo("active", true)
                .get()
                .await()

            val parkings = snapshot.documents.mapNotNull { document ->
                document.toObject(Parking::class.java)
                    ?.copy(id = document.id)
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

            val parkingList = snapshot.documents.mapNotNull { document ->
                document.toObject(Parking::class.java)
                    ?.copy(id = document.id)
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



    suspend fun updateParkingStatus(
        parkingId: String,
        isActive: Boolean
    ): Result<Unit> {
        return try {
            require(parkingId.isNotBlank()) {
                "Invalid parking facility ID."
            }

            firestore.collection("parkings")
                .document(parkingId)
                .update("active", isActive)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e(
                "ParkingRepository",
                "Failed to update parking status",
                e
            )
            Result.failure(e)
        }
    }



    suspend fun updateParking(
        parking: Parking
    ): Result<Unit> {
        return try {
            require(parking.id.isNotBlank()) {
                "Invalid parking facility ID."
            }

            require(parking.name.isNotBlank()) {
                "Facility name cannot be empty."
            }

            require(parking.address.isNotBlank()) {
                "Address cannot be empty."
            }

            require(parking.totalSlots > 0) {
                "Total slots must be greater than zero."
            }

            require(parking.availableSlots in 0..parking.totalSlots) {
                "Available slots must be between 0 and total slots."
            }

            require(
                parking.pricePerHour >= 0 &&
                        parking.pricePerHour.isFinite()
            ) {
                "Enter a valid hourly price."
            }

            val parkingRef = firestore.collection("parkings")
                .document(parking.id)

            // Save the owner's edited values.
            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(parkingRef)

                if (!snapshot.exists()) {
                    throw IllegalStateException("Parking facility not found.")
                }

                transaction.update(
                    parkingRef,
                    mapOf(
                        "name" to parking.name.trim(),
                        "address" to parking.address.trim(),
                        "totalSlots" to parking.totalSlots,
                        "availableSlots" to parking.availableSlots,
                        "pricePerHour" to parking.pricePerHour
                    )
                )
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e(
                "ParkingRepository",
                "Failed to update parking facility",
                e
            )
            Result.failure(e)
        }
    }



    suspend fun deleteParking(
        parkingId: String
    ): Result<Unit> {
        return try {
            require(parkingId.isNotBlank()) {
                "Invalid parking facility ID."
            }

            val currentUser = FirebaseAuth.getInstance().currentUser
                ?: throw IllegalStateException("Please log in first.")

            val parkingRef = firestore.collection("parkings")
                .document(parkingId)

            val snapshot = parkingRef.get().await()

            if (!snapshot.exists()) {
                throw IllegalStateException("Parking facility not found.")
            }

            val ownerId = snapshot.getString("ownerId")

            val userSnapshot = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val isAdmin = userSnapshot.getString("role") == "ADMIN"

            if (!isAdmin && ownerId != currentUser.uid) {
                throw SecurityException(
                    "You cannot delete this facility."
                )
            }

            parkingRef.delete().await()

            Result.success(Unit)

        } catch (e: Exception) {
            android.util.Log.e(
                "ParkingRepository",
                "Failed to delete parking facility: $parkingId",
                e
            )
            Result.failure(e)
        }
    }



}