package com.example.smartparking.presentation.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.model.User
import com.example.smartparking.data.repository.AdminDashboardStats
import com.example.smartparking.data.repository.AdminRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(

    val owners: List<User> = emptyList(),

    val drivers: List<User> = emptyList(),

    val facilities: List<Parking> = emptyList(),

    val bookings: List<Booking> = emptyList(),

    val stats: AdminDashboardStats = AdminDashboardStats(),

    val isLoading: Boolean = true,

    val isActionLoading: Boolean = false,

    val errorMessage: String? = null,

    val successMessage: String? = null
)

class AdminViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var ownersListener: ListenerRegistration? = null
    private var driversListener: ListenerRegistration? = null
    private var facilitiesListener: ListenerRegistration? = null
    private var bookingsListener: ListenerRegistration? = null

    private var hasStarted = false

    // ------------------------------------------------
    // START LISTENING
    // ------------------------------------------------

    fun startListening() {

        if (hasStarted) return

        hasStarted = true

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        observeOwners()
        observeDrivers()
        observeFacilities()
        observeBookings()
    }

    // ------------------------------------------------
    // OWNERS
    // ------------------------------------------------

    private fun observeOwners() {

        ownersListener = repository.observeOwners { result ->

            result.fold(

                onSuccess = { owners ->

                    _uiState.update {
                        it.copy(owners = owners)
                    }

                    updateDashboardStats()
                },

                onFailure = { error ->

                    showError(error.message ?: "Unable to load owners")
                }
            )
        }
    }

    // ------------------------------------------------
    // DRIVERS
    // ------------------------------------------------

    private fun observeDrivers() {

        driversListener = repository.observeDrivers { result ->

            result.fold(

                onSuccess = { drivers ->

                    _uiState.update {
                        it.copy(drivers = drivers)
                    }

                    updateDashboardStats()
                },

                onFailure = { error ->

                    showError(error.message ?: "Unable to load drivers")
                }
            )
        }
    }

    // ------------------------------------------------
    // PARKING FACILITIES
    // ------------------------------------------------

    private fun observeFacilities() {

        facilitiesListener = repository.observeFacilities { result ->

            result.fold(

                onSuccess = { facilities ->

                    _uiState.update {
                        it.copy(facilities = facilities)
                    }

                    updateDashboardStats()
                },

                onFailure = { error ->

                    showError(
                        error.message ?: "Unable to load parking facilities"
                    )
                }
            )
        }
    }

    // ------------------------------------------------
    // BOOKINGS
    // ------------------------------------------------

    private fun observeBookings() {

        bookingsListener = repository.observeBookings { result ->

            result.fold(

                onSuccess = { bookings ->

                    _uiState.update {
                        it.copy(bookings = bookings)
                    }

                    updateDashboardStats()
                },

                onFailure = { error ->

                    showError(error.message ?: "Unable to load bookings")
                }
            )
        }
    }

    // ------------------------------------------------
    // DASHBOARD STATISTICS
    // ------------------------------------------------

    private fun updateDashboardStats() {

        val currentState = _uiState.value

        val completedBookings = currentState.bookings.filter {
            repository.isCompletedAndPaid(it)
        }

        val totalBookingValue = completedBookings.sumOf {
            it.totalAmount
        }

        val totalPlatformFee = completedBookings.sumOf {
            repository.calculatePlatformFee(it.totalAmount)
        }

        val totalOwnerEarnings = completedBookings.sumOf {
            repository.calculateOwnerEarnings(it.totalAmount)
        }

        val stats = AdminDashboardStats(

            ownerCount = currentState.owners.size,

            driverCount = currentState.drivers.size,

            facilityCount = currentState.facilities.size,

            pendingFacilities = currentState.facilities.count {
                it.approvalStatus == AdminRepository.PENDING
            },

            approvedFacilities = currentState.facilities.count {
                it.approvalStatus == AdminRepository.APPROVED
            },

            rejectedFacilities = currentState.facilities.count {
                it.approvalStatus == AdminRepository.REJECTED
            },

            bookingCount = currentState.bookings.size,

            completedBookingCount = completedBookings.size,

            totalBookingValue = totalBookingValue,

            totalPlatformFee = totalPlatformFee,

            totalOwnerEarnings = totalOwnerEarnings
        )

        _uiState.update {
            it.copy(
                stats = stats,
                isLoading = false
            )
        }
    }

    // ------------------------------------------------
    // APPROVE FACILITY
    // ------------------------------------------------

    fun approveFacility(parkingId: String) {

        performAction(
            successText = "Parking facility approved successfully"
        ) {
            repository.approveFacility(parkingId)
        }
    }

    // ------------------------------------------------
    // REJECT FACILITY
    // ------------------------------------------------

    fun rejectFacility(
        parkingId: String,
        reason: String
    ) {

        performAction(
            successText = "Parking facility rejected"
        ) {
            repository.rejectFacility(parkingId, reason)
        }
    }

    // ------------------------------------------------
    // VERIFY PAYMENT
    // ------------------------------------------------

    fun verifyBookingPayment(
        bookingId: String,
        transactionId: String
    ) {

        performAction(
            successText = "Payment marked as verified"
        ) {
            repository.verifyBookingPayment(
                bookingId = bookingId,
                transactionId = transactionId
            )
        }
    }

    // ------------------------------------------------
    // COMMON ACTION HANDLER
    // ------------------------------------------------

    private fun performAction(
        successText: String,
        action: suspend () -> Result<Unit>
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isActionLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val result = try {
                action()
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            successMessage = successText
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionLoading = false,
                            errorMessage = error.message
                                ?: "Something went wrong"
                        )
                    }
                }
            )
        }
    }

    // ------------------------------------------------
    // ERROR HANDLING
    // ------------------------------------------------

    private fun showError(message: String) {

        _uiState.update {
            it.copy(errorMessage = message)
        }
    }

    fun clearMessages() {

        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // ------------------------------------------------
    // REFRESH
    // ------------------------------------------------

    fun refresh() {
        if (!hasStarted) {
            startListening()
        } else {
            updateDashboardStats()
        }
    }

    // ------------------------------------------------
    // LOGOUT
    // ------------------------------------------------

    fun logout() {
        repository.logout()
    }

    // ------------------------------------------------
    // CLEANUP
    // ------------------------------------------------

    override fun onCleared() {

        ownersListener?.remove()
        driversListener?.remove()
        facilitiesListener?.remove()
        bookingsListener?.remove()

        super.onCleared()
    }
}
