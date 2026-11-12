package com.example.smartparking.data.repository

import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class AdminRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        const val ADMIN_ROLE = "ADMIN"
        const val OWNER_ROLE = "OWNER"
        const val DRIVER_ROLE = "DRIVER"

        const val PENDING = "PENDING"
        const val APPROVED = "APPROVED"
        const val REJECTED = "REJECTED"

        const val COMPLETED = "COMPLETED"
        const val PAID = "PAID"

        const val PLATFORM_FEE_PERCENT = 2.5
    }

    // ------------------------------------------------
    // ADMIN AUTHENTICATION
    // ------------------------------------------------

    private suspend fun requireAdminId(): String {

        val uid = auth.currentUser?.uid
            ?: throw SecurityException("Admin is not logged in.")

        val userSnapshot = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()

        if (!userSnapshot.exists()) {
            throw SecurityException("Admin account not found.")
        }

        val role = userSnapshot.getString("role")

        if (role != ADMIN_ROLE) {
            throw SecurityException("Administrator access required.")
        }

        return uid
    }

    // ------------------------------------------------
    // OWNERS
    // ------------------------------------------------

    fun observeOwners(
        onResult: (Result<List<User>>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("users")
            .whereEqualTo("role", OWNER_ROLE)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val owners = snapshot?.documents
                    ?.mapNotNull { document ->
                        document.toObject(User::class.java)
                            ?.copy(uid = document.id)
                    }
                    ?: emptyList()

                onResult(Result.success(owners))
            }
    }

    // ------------------------------------------------
    // DRIVERS
    // ------------------------------------------------

    fun observeDrivers(
        onResult: (Result<List<User>>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("users")
            .whereEqualTo("role", DRIVER_ROLE)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val drivers = snapshot?.documents
                    ?.mapNotNull { document ->
                        document.toObject(User::class.java)
                            ?.copy(uid = document.id)
                    }
                    ?: emptyList()

                onResult(Result.success(drivers))
            }
    }

    // ------------------------------------------------
    // PARKING FACILITIES
    // ------------------------------------------------

    fun observeFacilities(
        onResult: (Result<List<Parking>>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("parkings")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val facilities = snapshot?.documents
                    ?.mapNotNull { document ->
                        document.toObject(Parking::class.java)
                            ?.copy(id = document.id)
                    }
                    ?: emptyList()

                onResult(Result.success(facilities))
            }
    }

    // ------------------------------------------------
    // APPROVE PARKING FACILITY
    // ------------------------------------------------

    suspend fun approveFacility(
        parkingId: String
    ): Result<Unit> {

        return try {

            val adminId = requireAdminId()

            require(parkingId.isNotBlank()) {
                "Parking ID is required."
            }

            val parkingRef = firestore
                .collection("parkings")
                .document(parkingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(parkingRef)

                require(snapshot.exists()) {
                    "Parking facility not found."
                }

                require(
                    snapshot.getString("approvalStatus") == PENDING
                ) {
                    "This facility is no longer pending."
                }

                transaction.update(
                    parkingRef,
                    mapOf(
                        "approvalStatus" to APPROVED,
                        "active" to true,
                        "approvedAt" to System.currentTimeMillis(),
                        "approvedBy" to adminId,
                        "rejectionReason" to ""
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------
    // REJECT PARKING FACILITY
    // ------------------------------------------------

    suspend fun rejectFacility(
        parkingId: String,
        reason: String
    ): Result<Unit> {

        return try {

            val adminId = requireAdminId()

            require(parkingId.isNotBlank()) {
                "Parking ID is required."
            }

            val cleanReason = reason.trim()

            require(cleanReason.isNotBlank()) {
                "Please provide a rejection reason."
            }

            val parkingRef = firestore
                .collection("parkings")
                .document(parkingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(parkingRef)

                require(snapshot.exists()) {
                    "Parking facility not found."
                }

                require(
                    snapshot.getString("approvalStatus") == PENDING
                ) {
                    "This facility is no longer pending."
                }

                transaction.update(
                    parkingRef,
                    mapOf(
                        "approvalStatus" to REJECTED,
                        "active" to false,
                        "approvedAt" to null,
                        "approvedBy" to adminId,
                        "rejectionReason" to cleanReason
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------
    // BOOKINGS
    // ------------------------------------------------

    fun observeBookings(
        onResult: (Result<List<Booking>>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("bookings")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val bookings = snapshot?.documents
                    ?.mapNotNull { document ->
                        document.toObject(Booking::class.java)
                            ?.copy(id = document.id)
                    }
                    ?: emptyList()

                onResult(Result.success(bookings))
            }
    }

    // ------------------------------------------------
    // VERIFY BOOKING PAYMENT
    // ------------------------------------------------

    suspend fun verifyBookingPayment(
        bookingId: String,
        transactionId: String
    ): Result<Unit> {

        return try {

            val adminId = requireAdminId()

            require(bookingId.isNotBlank()) {
                "Booking ID is required."
            }

            val cleanTransactionId = transactionId.trim()

            require(cleanTransactionId.isNotBlank()) {
                "Transaction ID is required."
            }

            val bookingRef = firestore
                .collection("bookings")
                .document(bookingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(bookingRef)

                require(snapshot.exists()) {
                    "Booking not found."
                }

                require(
                    snapshot.getString("paymentStatus") == PENDING
                ) {
                    "This payment is not pending verification."
                }

                transaction.update(
                    bookingRef,
                    mapOf(
                        "paymentStatus" to PAID,
                        "transactionId" to cleanTransactionId,
                        "verifiedBy" to adminId,
                        "verifiedAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    fun observeParkingSessions(
        onResult: (Result<List<AdminParkingSession>>) -> Unit
    ): ListenerRegistration {

        return firestore.collection("parking_sessions")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onResult(Result.failure(error))
                    return@addSnapshotListener
                }

                val sessions = snapshot?.documents?.mapNotNull { document ->

                    val hourlyRate =
                        document.getDouble("hourlyRate")
                            ?: document.getLong("hourlyRate")?.toDouble()
                            ?: 0.0

                    val totalPaid =
                        document.getDouble("totalPaid")
                            ?: document.getLong("totalPaid")?.toDouble()
                            ?: 0.0

                    AdminParkingSession(
                        id = document.id,
                        driverId = document.getString("driverId") ?: "",
                        parkingId = document.getString("parkingId") ?: "",
                        parkingName = document.getString("parkingName") ?: "",
                        hourlyRate = hourlyRate,
                        durationMinutes =
                            document.getLong("durationMinutes")?.toInt() ?: 0,
                        totalPaid = totalPaid,
                        status = document.getString("status") ?: "",
                        paymentStatus = document.getString("paymentStatus") ?: "",
                        startedAt = document.getLong("startedAt") ?: 0L,
                        paidAt = document.getLong("paidAt") ?: 0L,
                        expiresAt = document.getLong("expiresAt") ?: 0L,
                        extensionCount =
                            document.getLong("extensionCount")?.toInt() ?: 0
                    )
                } ?: emptyList()

                onResult(Result.success(sessions))
            }
    }

    // ------------------------------------------------
    // PLATFORM FEE CALCULATIONS
    // ------------------------------------------------

    fun calculatePlatformFee(amount: Double): Double {

        return if (amount.isFinite() && amount > 0.0) {
            amount * PLATFORM_FEE_PERCENT / 100.0
        } else {
            0.0
        }
    }

    fun calculateOwnerEarnings(amount: Double): Double {

        if (!amount.isFinite() || amount <= 0.0) {
            return 0.0
        }

        return (amount - calculatePlatformFee(amount))
            .coerceAtLeast(0.0)
    }

    fun isCompletedAndPaid(booking: Booking): Boolean {

        return booking.bookingStatus == COMPLETED &&
                booking.paymentStatus == PAID
    }

    // ------------------------------------------------
    // ADMIN DASHBOARD STATISTICS
    // ------------------------------------------------

    suspend fun getDashboardStats(): Result<AdminDashboardStats> {

        return try {

            requireAdminId()

            val usersSnapshot = firestore
                .collection("users")
                .get()
                .await()

            val parkingSnapshot = firestore
                .collection("parkings")
                .get()
                .await()

            val bookingsSnapshot = firestore
                .collection("bookings")
                .get()
                .await()

            val users = usersSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(User::class.java)
                }

            val parkings = parkingSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(Parking::class.java)
                }

            val bookings = bookingsSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(Booking::class.java)
                }

            val completedBookings = bookings.filter {
                isCompletedAndPaid(it)
            }

            val totalBookingValue = completedBookings.sumOf {
                it.totalAmount
            }

            val totalPlatformFee = completedBookings.sumOf {
                calculatePlatformFee(it.totalAmount)
            }

            val totalOwnerEarnings = completedBookings.sumOf {
                calculateOwnerEarnings(it.totalAmount)
            }

            val stats = AdminDashboardStats(

                ownerCount = users.count {
                    it.role == OWNER_ROLE
                },

                driverCount = users.count {
                    it.role == DRIVER_ROLE
                },

                facilityCount = parkings.size,

                pendingFacilities = parkings.count {
                    it.approvalStatus == PENDING
                },

                approvedFacilities = parkings.count {
                    it.approvalStatus == APPROVED
                },

                rejectedFacilities = parkings.count {
                    it.approvalStatus == REJECTED
                },

                bookingCount = bookings.size,

                completedBookingCount = completedBookings.size,

                totalBookingValue = totalBookingValue,

                totalPlatformFee = totalPlatformFee,

                totalOwnerEarnings = totalOwnerEarnings
            )

            Result.success(stats)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------
    // LOGOUT
    // ------------------------------------------------

    fun logout() {
        auth.signOut()
    }
}

// ------------------------------------------------
// DASHBOARD STATISTICS MODEL
// ------------------------------------------------

data class AdminDashboardStats(

    val ownerCount: Int = 0,

    val driverCount: Int = 0,

    val facilityCount: Int = 0,

    val pendingFacilities: Int = 0,

    val approvedFacilities: Int = 0,

    val rejectedFacilities: Int = 0,

    val bookingCount: Int = 0,

    val completedBookingCount: Int = 0,

    val totalBookingValue: Double = 0.0,

    val totalPlatformFee: Double = 0.0,

    val totalOwnerEarnings: Double = 0.0
)


data class AdminParkingSession(
    val id: String = "",
    val driverId: String = "",
    val parkingId: String = "",
    val parkingName: String = "",
    val hourlyRate: Double = 0.0,
    val durationMinutes: Int = 0,
    val totalPaid: Double = 0.0,
    val status: String = "",
    val paymentStatus: String = "",
    val startedAt: Long = 0L,
    val paidAt: Long = 0L,
    val expiresAt: Long = 0L,
    val extensionCount: Int = 0
)
