
package com.example.smartparking.data.repository

import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private fun requireAdminId(): String {
        return auth.currentUser?.uid
            ?: throw Exception("Admin is not logged in")
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
                "Parking ID is required"
            }

            val parkingRef = firestore
                .collection("parkings")
                .document(parkingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(parkingRef)

                require(snapshot.exists()) {
                    "Parking facility not found"
                }

                require(
                    snapshot.getString("approvalStatus") == PENDING
                ) {
                    "This facility is no longer pending"
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
                "Parking ID is required"
            }

            require(reason.isNotBlank()) {
                "Please provide a rejection reason"
            }

            val parkingRef = firestore
                .collection("parkings")
                .document(parkingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(parkingRef)

                require(snapshot.exists()) {
                    "Parking facility not found"
                }

                require(
                    snapshot.getString("approvalStatus") == PENDING
                ) {
                    "This facility is no longer pending"
                }

                transaction.update(
                    parkingRef,
                    mapOf(
                        "approvalStatus" to REJECTED,
                        "active" to false,
                        "approvedAt" to null,
                        "approvedBy" to adminId,
                        "rejectionReason" to reason
                    )
                )

            }.await()

            Result.success(Unit)

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

            requireAdminId()

            require(bookingId.isNotBlank()) {
                "Booking ID is required"
            }

            require(transactionId.isNotBlank()) {
                "Transaction ID is required"
            }

            val bookingRef = firestore
                .collection("bookings")
                .document(bookingId)

            firestore.runTransaction { transaction ->

                val snapshot = transaction.get(bookingRef)

                require(snapshot.exists()) {
                    "Booking not found"
                }

                require(
                    snapshot.getString("paymentStatus") == PENDING
                ) {
                    "This payment is not pending verification"
                }

                transaction.update(
                    bookingRef,
                    mapOf(
                        "paymentStatus" to PAID,
                        "transactionId" to transactionId,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )

            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ------------------------------------------------
    // PLATFORM FEE CALCULATIONS
    // ------------------------------------------------

    fun calculatePlatformFee(amount: Double): Double {

        return if (amount > 0.0) {
            amount * PLATFORM_FEE_PERCENT / 100.0
        } else {
            0.0
        }
    }

    fun calculateOwnerEarnings(amount: Double): Double {

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
