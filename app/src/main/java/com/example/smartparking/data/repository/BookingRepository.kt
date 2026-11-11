package com.example.smartparking.data.repository


import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil
import com.google.firebase.auth.FirebaseAuth

class BookingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Retrieve the owner's bKash number from Firestore.
     */
    suspend fun getOwnerPhone(ownerId: String): Result<String> {
        return try {
            require(ownerId.isNotBlank()) {
                "Owner ID is required"
            }

            val snapshot = firestore
                .collection("users")
                .document(ownerId)
                .get()
                .await()

            val owner = snapshot.toObject(User::class.java)
                ?: throw Exception("Owner account not found")

            require(owner.role == "OWNER") {
                "Invalid parking owner"
            }

            require(owner.phone.isNotBlank()) {
                "Owner has not added a phone number"
            }

            Result.success(owner.phone)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate a requested booking schedule.
     */
    private fun validateSchedule(
        startTime: Long,
        endTime: Long
    ) {
        require(startTime > System.currentTimeMillis()) {
            "Booking must start in the future"
        }

        require(endTime > startTime) {
            "End time must be after start time"
        }
    }

    /**
     * Calculate billable hours.
     * Partial hours are rounded up.
     *
     * Example:
     * 10:00 AM - 11:30 AM = 2 billable hours.
     */
    private fun calculateBillableHours(
        startTime: Long,
        endTime: Long
    ): Double {
        val durationMillis = endTime - startTime
        val actualHours = durationMillis / 3_600_000.0

        return ceil(actualHours)
    }

    /**
     * Check availability for a custom time range.
     */
    suspend fun checkAvailability(
        parkingId: String,
        startTime: Long,
        endTime: Long
    ): Result<Int> {
        return try {
            validateSchedule(startTime, endTime)

            val parkingSnapshot = firestore
                .collection("parkings")
                .document(parkingId)
                .get()
                .await()

            val parking = parkingSnapshot.toObject(Parking::class.java)
                ?: throw Exception("Parking facility not found")

            require(parking.active) {
                "This parking facility is not active"
            }

            val bookingsSnapshot = firestore
                .collection("bookings")
                .whereEqualTo("parkingId", parkingId)
                .get()
                .await()

            // Count reservations that overlap the requested time.
            val occupiedSlots = bookingsSnapshot.documents.sumOf { document ->

                val booking = document.toObject(Booking::class.java)
                    ?: return@sumOf 0

                val isActiveBooking =
                    booking.bookingStatus == "PENDING" ||
                            booking.bookingStatus == "CONFIRMED"

                val overlaps =
                    startTime < booking.endTime &&
                            endTime > booking.startTime

                if (isActiveBooking && overlaps) {
                    booking.reservedSlots
                } else {
                    0
                }
            }

            val availableSlots =
                (parking.totalSlots - occupiedSlots).coerceAtLeast(0)

            Result.success(availableSlots)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a pending booking request.
     */
    suspend fun createBooking(
        parking: Parking,
        driverId: String,
        startTime: Long,
        endTime: Long
    ): Result<Booking> {
        return try {
            validateSchedule(startTime, endTime)

            require(parking.id.isNotBlank()) {
                "Parking ID is required"
            }

            require(driverId.isNotBlank()) {
                "Driver ID is required"
            }

            require(parking.ownerId.isNotBlank()) {
                "Parking owner is missing"
            }

            require(parking.pricePerHour > 0.0) {
                "Invalid parking price"
            }

            // Check available capacity.
            val availabilityResult = checkAvailability(
                parkingId = parking.id,
                startTime = startTime,
                endTime = endTime
            )

            val availableSlots = availabilityResult.getOrThrow()

            require(availableSlots > 0) {
                "No parking slots available for this time"
            }

            // Retrieve the owner's bKash number.
            val ownerPhone = getOwnerPhone(
                parking.ownerId
            ).getOrThrow()

            // Calculate the fee.
            val billableHours = calculateBillableHours(
                startTime,
                endTime
            )

            val totalAmount =
                billableHours * parking.pricePerHour

            // Create booking document.
            val document = firestore
                .collection("bookings")
                .document()

            val now = System.currentTimeMillis()

            val booking = Booking(
                id = document.id,
                parkingId = parking.id,
                parkingName = parking.name,
                ownerId = parking.ownerId,
                driverId = driverId,
                startTime = startTime,
                endTime = endTime,
                pricePerHour = parking.pricePerHour,
                durationHours = billableHours,
                totalAmount = totalAmount,
                ownerPhone = ownerPhone,
                paymentMethod = "BKASH",
                transactionId = "",
                bookingStatus = "PENDING",
                paymentStatus = "PENDING",
                reservedSlots = 1,
                createdAt = now,
                updatedAt = now
            )

            document.set(booking).await()

            Result.success(booking)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun cancelBooking(bookingId: String): Result<Unit> {
        return try {
            require(bookingId.isNotBlank()) {
                "Booking ID is required"
            }

            val currentUserId = FirebaseAuth.getInstance()
                .currentUser?.uid
                ?: throw Exception("Please log in to cancel a booking.")

            val bookingRef = firestore.collection("bookings")
                .document(bookingId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(bookingRef)

                if (!snapshot.exists()) {
                    throw Exception("Booking not found.")
                }

                val driverId = snapshot.getString("driverId")
                val status = snapshot.getString("bookingStatus")

                require(driverId == currentUserId) {
                    "You are not authorized to cancel this booking."
                }

                require(status == "PENDING" || status == "CONFIRMED") {
                    "This booking can no longer be cancelled."
                }

                transaction.update(
                    bookingRef,
                    mapOf(
                        "bookingStatus" to "CANCELLED",
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
