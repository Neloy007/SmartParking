
package com.example.smartparking.data.repository

import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil
import com.google.firebase.firestore.ListenerRegistration

class BookingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()

    // ==========================================
    // CURRENT USER
    // ==========================================

    private fun requireCurrentUserId(): String {
        return auth.currentUser?.uid
            ?: throw Exception("Please log in first.")
    }

    // ==========================================
    // OWNER BKASH NUMBER
    // ==========================================

    suspend fun getOwnerBkashNumber(
        ownerId: String
    ): Result<String> {
        return try {
            require(ownerId.isNotBlank()) {
                "Owner ID is required."
            }

            val snapshot = firestore
                .collection("owner_payment_profiles")
                .document(ownerId)
                .get()
                .await()

            require(snapshot.exists()) {
                "The parking owner has not configured a bKash number yet."
            }

            val number = snapshot.getString("bkashNumber")
                ?.trim()
                .orEmpty()

            require(number.matches(Regex("^01[3-9][0-9]{8}$"))) {
                "The owner's bKash number is invalid."
            }

            Result.success(number)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // VALIDATION
    // ==========================================

    private fun validateSchedule(
        startTime: Long,
        endTime: Long,
        allowImmediate: Boolean = false
    ) {
        require(endTime > startTime) {
            "End time must be after start time."
        }

        val now = System.currentTimeMillis()

        if (allowImmediate) {
            require(startTime >= now - 2 * 60 * 1000L) {
                "Immediate booking has expired. Please try again."
            }
        } else {
            require(startTime > now) {
                "Booking must start in the future."
            }
        }
    }

    private fun calculateBillableHours(
        startTime: Long,
        endTime: Long
    ): Double {
        return ceil(
            (endTime - startTime) / 3_600_000.0
        )
    }

    // ==========================================
    // CHECK PARKING AVAILABILITY
    // ==========================================



    suspend fun checkAvailability(
        parkingId: String,
        startTime: Long,
        endTime: Long,
        allowImmediate: Boolean = false
    ): Result<Int> {
        return try {
            validateSchedule(
                startTime = startTime,
                endTime = endTime,
                allowImmediate = allowImmediate
            )

            require(parkingId.isNotBlank()) {
                "Parking ID is required."
            }

            val parkingSnapshot = firestore
                .collection("parkings")
                .document(parkingId)
                .get()
                .await()

            require(parkingSnapshot.exists()) {
                "Parking facility not found."
            }

            require(parkingSnapshot.getBoolean("active") == true) {
                "This parking facility is not active."
            }

            require(
                parkingSnapshot.getString("approvalStatus") == "APPROVED"
            ) {
                "This parking facility is not approved."
            }

            val availableSlots = (
                    parkingSnapshot.getLong("availableSlots") ?: 0L
                    ).toInt()

            require(availableSlots >= 0) {
                "Invalid parking availability."
            }

            Result.success(availableSlots)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ==========================================
    // DRIVER: CREATE BOOKING WITH TRXID
    // ==========================================

    suspend fun createBooking(
        parking: Parking,
        driverId: String,
        startTime: Long,
        endTime: Long,
        allowImmediate: Boolean = false,
        transactionId: String
    ): Result<Booking> {

        return try {
            validateSchedule(
                startTime = startTime,
                endTime = endTime,
                allowImmediate = allowImmediate
            )
            val currentUserId = requireCurrentUserId()

            require(currentUserId == driverId) {
                "You are not authorized to create this booking."
            }

            require(parking.id.isNotBlank()) {
                "Parking ID is required."
            }

            require(parking.ownerId.isNotBlank()) {
                "Parking owner is missing."
            }

            require(parking.pricePerHour > 0.0) {
                "Invalid parking price."
            }

            val cleanTransactionId = transactionId.trim()

            require(cleanTransactionId.length in 6..30) {
                "Please enter a valid bKash Transaction ID."
            }

            require(cleanTransactionId.matches(Regex("^[A-Za-z0-9]+$"))) {
                "Transaction ID can contain only letters and numbers."
            }

            // Retrieve the driver's own profile.
            val driverSnapshot = firestore
                .collection("users")
                .document(driverId)
                .get()
                .await()

            require(driverSnapshot.exists()) {
                "Driver profile not found."
            }

            val driverName = driverSnapshot.getString("name").orEmpty()
            val driverPhone = driverSnapshot.getString("phone").orEmpty()

            // Check availability.
            val availableSlots = checkAvailability(
                parkingId = parking.id,
                startTime = startTime,
                endTime = endTime,
                allowImmediate = allowImmediate
            ).getOrThrow()

            require(availableSlots > 0) {
                "No parking slots are available for this time."
            }

            // Retrieve owner's public bKash number.
            val ownerBkashNumber = getOwnerBkashNumber(parking.ownerId).getOrThrow()

            val billableHours = calculateBillableHours(
                startTime,
                endTime
            )

            val totalAmount =
                billableHours * parking.pricePerHour

            val bookingRef = firestore
                .collection("bookings")
                .document()

            val now = System.currentTimeMillis()

            if (endTime <= startTime) {
                throw Exception("End time must be after start time.")
            }

            if (allowImmediate) {
                if (startTime < now - 2 * 60 * 1000L) {
                    throw Exception("Immediate booking has expired. Please try again.")
                }
            } else {
                if (startTime <= now) {
                    throw Exception("Booking must start in the future.")
                }
            }

            val booking = Booking(
                id = bookingRef.id,

                parkingId = parking.id,
                parkingName = parking.name,
                ownerId = parking.ownerId,
                driverId = driverId,

                driverName = driverName,
                driverPhone = driverPhone,

                startTime = startTime,
                endTime = endTime,

                pricePerHour = parking.pricePerHour,
                durationHours = billableHours,
                totalAmount = totalAmount,

                ownerPhone = "",
                ownerBkashNumber = ownerBkashNumber,

                paymentMethod = "BKASH",
                transactionId = cleanTransactionId,

                bookingStatus = "WAITING_FOR_APPROVAL",
                paymentStatus = "TRXID_SUBMITTED",

                reservedSlots = 1,

                createdAt = now,
                updatedAt = now
            )

            bookingRef.set(booking).await()

            Result.success(booking)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    suspend fun createParkNowBooking(
        parking: Parking,
        driverId: String,
        transactionId: String
    ): Result<Booking> {

        var stage = "START"

        return try {
            // =====================================
            // 1. INITIALIZE PARK NOW BOOKING
            // =====================================

            stage = "INITIALIZING_BOOKING"

            val now = System.currentTimeMillis()

            // Park Now: reserve one hour initially.
            val startTime = now
            val endTime = now + (60 * 60 * 1000L)

            // =====================================
            // 2. VALIDATE CURRENT USER
            // =====================================

            stage = "VALIDATING_USER"

            val currentUserId = requireCurrentUserId()

            require(currentUserId == driverId) {
                "You can only create bookings for your own account."
            }

            // =====================================
            // 3. VALIDATE PARKING INFORMATION
            // =====================================

            stage = "VALIDATING_PARKING"

            require(parking.id.isNotBlank()) {
                "Invalid parking facility."
            }

            require(parking.ownerId.isNotBlank()) {
                "Parking owner information is missing."
            }

            require(parking.pricePerHour > 0) {
                "Invalid parking price."
            }

            // =====================================
            // 4. VALIDATE BKASH TRANSACTION ID
            // =====================================

            stage = "VALIDATING_TRANSACTION_ID"

            require(
                transactionId.length in 6..30 &&
                        transactionId.all { it.isLetterOrDigit() }
            ) {
                "Enter a valid bKash transaction ID."
            }

            // =====================================
            // 5. READ DRIVER PROFILE
            // =====================================

            stage = "READING_DRIVER_PROFILE"

            val driverSnapshot = firestore
                .collection("users")
                .document(driverId)
                .get()
                .await()

            require(driverSnapshot.exists()) {
                "Driver profile not found."
            }

            val driverName = driverSnapshot.getString("name").orEmpty()
            val driverPhone = driverSnapshot.getString("phone").orEmpty()

            // =====================================
            // 6. CHECK PARKING AVAILABILITY
            // =====================================

            stage = "CHECKING_PARKING_AVAILABILITY"

            val availabilityResult = checkAvailability(
                parkingId = parking.id,
                startTime = startTime,
                endTime = endTime
            )

            val availableSlots = availabilityResult.getOrThrow()

            require(availableSlots > 0) {
                "No parking slots are currently available."
            }

            // =====================================
            // 7. READ OWNER BKASH NUMBER
            // =====================================

            stage = "READING_OWNER_BKASH"

            val ownerBkashNumber = getOwnerBkashNumber(
                parking.ownerId
            ).getOrThrow()

            require(ownerBkashNumber.isNotBlank()) {
                "Owner bKash number is not configured."
            }

            // =====================================
            // 8. CALCULATE BOOKING AMOUNT
            // =====================================

            stage = "CALCULATING_BOOKING_AMOUNT"

            val billableHours = 1.0

            val totalAmount = parking.pricePerHour * billableHours

            require(totalAmount > 0) {
                "Invalid booking amount."
            }

            // =====================================
            // 9. PREPARE BOOKING DOCUMENT
            // =====================================

            stage = "PREPARING_BOOKING"

            val bookingRef = firestore
                .collection("bookings")
                .document()

            val booking = Booking(
                id = bookingRef.id,
                parkingId = parking.id,
                parkingName = parking.name,
                ownerId = parking.ownerId,
                driverId = driverId,
                driverName = driverName,
                driverPhone = driverPhone,
                startTime = startTime,
                endTime = endTime,
                pricePerHour = parking.pricePerHour,
                durationHours = billableHours,
                totalAmount = totalAmount,
                ownerBkashNumber = ownerBkashNumber,
                paymentMethod = "BKASH",
                transactionId = transactionId,
                bookingStatus = "WAITING_FOR_APPROVAL",
                paymentStatus = "TRXID_SUBMITTED",
                reservedSlots = 1,
                createdAt = now,
                updatedAt = now
            )

            // =====================================
            // 10. SAVE BOOKING TO FIRESTORE
            // =====================================

            stage = "CREATING_BOOKING"

            bookingRef.set(booking).await()

            android.util.Log.d(
                "PARK_NOW_SUCCESS",
                "Booking created successfully: ${booking.id}"
            )

            Result.success(booking)

        } catch (e: Exception) {

            // =====================================
            // 11. LOG EXACT FAILURE STAGE
            // =====================================

            android.util.Log.e(
                "PARK_NOW_ERROR",
                "Failed at stage: $stage | Error: ${e.message}",
                e
            )

            Result.failure(e)
        }
    }





    // ==========================================
    // OWNER: GET PENDING PAYMENT REQUESTS
    // ==========================================

    suspend fun getPendingOwnerBookings(): Result<List<Booking>> {

        return try {
            val ownerId = requireCurrentUserId()

            val ownerSnapshot = firestore
                .collection("users")
                .document(ownerId)
                .get()
                .await()

            require(ownerSnapshot.getString("role") == "OWNER") {
                "Only parking owners can view payment requests."
            }

            val snapshot = firestore
                .collection("bookings")
                .whereEqualTo("ownerId", ownerId)
                .whereEqualTo("bookingStatus", "WAITING_FOR_APPROVAL")
                .get()
                .await()

            val bookings = snapshot.documents.mapNotNull { document ->
                document.toObject(Booking::class.java)
                    ?.copy(id = document.id)
            }.sortedBy { it.createdAt }

            Result.success(bookings)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // OWNER: APPROVE PAYMENT
    // ==========================================

    suspend fun approveBooking(
        bookingId: String
    ): Result<Booking> {

        return try {
            val ownerId = requireCurrentUserId()

            require(bookingId.isNotBlank()) {
                "Booking ID is required."
            }

            val ownerSnapshot = firestore
                .collection("users")
                .document(ownerId)
                .get()
                .await()

            require(ownerSnapshot.getString("role") == "OWNER") {
                "Only parking owners can approve bookings."
            }

            val bookingRef = firestore
                .collection("bookings")
                .document(bookingId)

            val sessionRef = firestore
                .collection("parking_sessions")
                .document()

            val approvedAt = System.currentTimeMillis()

            var approvedBooking: Booking? = null

            firestore.runTransaction { transaction ->

                val bookingSnapshot = transaction.get(bookingRef)

                require(bookingSnapshot.exists()) {
                    "Booking not found."
                }

                val booking = bookingSnapshot
                    .toObject(Booking::class.java)
                    ?: throw Exception("Invalid booking information.")

                require(booking.ownerId == ownerId) {
                    "You are not authorized to approve this booking."
                }

                require(
                    booking.bookingStatus == "WAITING_FOR_APPROVAL"
                ) {
                    "This booking is no longer waiting for approval."
                }

                require(
                    booking.paymentStatus == "TRXID_SUBMITTED"
                ) {
                    "This booking does not have a submitted TrxID."
                }

                require(booking.transactionId.isNotBlank()) {
                    "Transaction ID is missing."
                }

                require(booking.durationHours > 0.0) {
                    "Invalid booking duration."
                }

                require(booking.totalAmount > 0.0) {
                    "Invalid booking amount."
                }

                val durationMinutes = ceil(
                    booking.durationHours * 60.0
                ).toInt()

                val expiresAt =
                    approvedAt + durationMinutes * 60_000L

                // Prepare the confirmed booking.
                val updatedBooking = booking.copy(
                    id = bookingId,
                    bookingStatus = "CONFIRMED",
                    paymentStatus = "PAID",
                    verifiedBy = ownerId,
                    verifiedAt = approvedAt,
                    updatedAt = approvedAt,
                    rejectionReason = "",
                    sessionId = sessionRef.id,
                    sessionStartedAt = approvedAt
                )

                // Create the real parking session.
                val sessionData = mapOf(
                    "id" to sessionRef.id,
                    "bookingId" to bookingId,
                    "driverId" to booking.driverId,
                    "ownerId" to ownerId,
                    "parkingId" to booking.parkingId,
                    "parkingName" to booking.parkingName,

                    "hourlyRate" to booking.pricePerHour,

                    "status" to "CONFIRMED",
                    "paymentStatus" to "PAID",

                    "durationMinutes" to durationMinutes,

                    "startedAt" to approvedAt,
                    "createdAt" to approvedAt,
                    "paidAt" to approvedAt,

                    "expiresAt" to expiresAt,

                    "totalPaid" to booking.totalAmount,
                    "extensionCount" to 0
                )

                // Both writes happen atomically.
                transaction.update(
                    bookingRef,
                    mapOf(
                        "bookingStatus" to "CONFIRMED",
                        "paymentStatus" to "PAID",
                        "verifiedBy" to ownerId,
                        "verifiedAt" to approvedAt,
                        "updatedAt" to approvedAt,
                        "rejectionReason" to "",
                        "sessionId" to sessionRef.id,
                        "sessionStartedAt" to approvedAt
                    )
                )

                transaction.set(sessionRef, sessionData)

                approvedBooking = updatedBooking

            }.await()

            Result.success(
                approvedBooking
                    ?: throw Exception("Booking approval failed.")
            )

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // OWNER: REJECT PAYMENT
    // ==========================================

    suspend fun rejectBooking(
        bookingId: String,
        reason: String
    ): Result<Unit> {

        return try {
            val ownerId = requireCurrentUserId()

            require(bookingId.isNotBlank()) {
                "Booking ID is required."
            }

            val cleanReason = reason.trim()

            require(cleanReason.isNotBlank()) {
                "Please provide a rejection reason."
            }

            require(cleanReason.length <= 300) {
                "Rejection reason cannot exceed 300 characters."
            }

            val ownerSnapshot = firestore
                .collection("users")
                .document(ownerId)
                .get()
                .await()

            require(ownerSnapshot.getString("role") == "OWNER") {
                "Only parking owners can reject bookings."
            }

            val bookingRef = firestore
                .collection("bookings")
                .document(bookingId)

            val rejectedAt = System.currentTimeMillis()

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(bookingRef)

                require(snapshot.exists()) {
                    "Booking not found."
                }

                val booking = snapshot.toObject(Booking::class.java)
                    ?: throw Exception("Invalid booking information.")

                require(booking.ownerId == ownerId) {
                    "You are not authorized to reject this booking."
                }

                require(
                    booking.bookingStatus == "WAITING_FOR_APPROVAL"
                ) {
                    "This booking is no longer waiting for approval."
                }

                require(
                    booking.paymentStatus == "TRXID_SUBMITTED"
                ) {
                    "This payment cannot be rejected."
                }

                transaction.update(
                    bookingRef,
                    mapOf(
                        "bookingStatus" to "REJECTED",
                        "paymentStatus" to "REJECTED",
                        "verifiedBy" to ownerId,
                        "verifiedAt" to rejectedAt,
                        "rejectionReason" to cleanReason,
                        "updatedAt" to rejectedAt
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // DRIVER: CANCEL BOOKING
    // ==========================================

    suspend fun cancelBooking(
        bookingId: String
    ): Result<Unit> {

        return try {
            require(bookingId.isNotBlank()) {
                "Booking ID is required."
            }

            val driverId = requireCurrentUserId()

            val bookingRef = firestore
                .collection("bookings")
                .document(bookingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(bookingRef)

                require(snapshot.exists()) {
                    "Booking not found."
                }

                val booking = snapshot.toObject(Booking::class.java)
                    ?: throw Exception("Invalid booking.")

                require(booking.driverId == driverId) {
                    "You are not authorized to cancel this booking."
                }

                require(
                    booking.bookingStatus == "PENDING" ||
                            booking.bookingStatus == "WAITING_FOR_APPROVAL"
                ) {
                    "This booking can no longer be cancelled."
                }

                transaction.update(
                    bookingRef,
                    mapOf(
                        "bookingStatus" to "CANCELLED",
                        "paymentStatus" to "CANCELLED",
                        "updatedAt" to System.currentTimeMillis()
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
// DRIVER: LISTEN TO MY BOOKINGS
// ==========================================

    fun listenToDriverBookings(
        driverId: String,
        onBookingsChanged: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        require(driverId.isNotBlank()) {
            "Driver ID is required."
        }

        require(auth.currentUser?.uid == driverId) {
            "You are not authorized to view these bookings."
        }

        return firestore
            .collection("bookings")
            .whereEqualTo("driverId", driverId)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onBookingsChanged(emptyList())
                    return@addSnapshotListener
                }

                val bookings = snapshot.documents.mapNotNull { document ->
                    document.toObject(Booking::class.java)
                        ?.copy(id = document.id)
                }.sortedByDescending { it.createdAt }

                onBookingsChanged(bookings)
            }
    }

    // ==========================================
// OWNER: LISTEN TO PENDING PAYMENT REQUESTS
// ==========================================

    fun listenToOwnerPendingBookings(
        onBookingsChanged: (List<Booking>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {

        val ownerId = requireCurrentUserId()

        return firestore
            .collection("bookings")
            .whereEqualTo("ownerId", ownerId)
            .whereEqualTo("bookingStatus", "WAITING_FOR_APPROVAL")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onBookingsChanged(emptyList())
                    return@addSnapshotListener
                }

                val bookings = snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(Booking::class.java)
                            ?.copy(id = document.id)
                    }
                    .sortedByDescending { it.createdAt }

                onBookingsChanged(bookings)
            }
    }



}
