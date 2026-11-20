package com.example.smartparking.presentation.owner


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.Booking
import com.example.smartparking.data.repository.BookingRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OwnerBookingUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val processingBookingId: String? = null
)

class OwnerBookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    private val _uiState = MutableStateFlow(OwnerBookingUiState())
    val uiState: StateFlow<OwnerBookingUiState> =
        _uiState.asStateFlow()

    private var bookingListener: ListenerRegistration? = null

    init {
        startListening()
    }

    /**
     * Listen for real-time pending booking requests
     * belonging to the currently authenticated owner.
     */
    fun startListening() {

        if (bookingListener != null) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        try {
            bookingListener = repository.listenToOwnerPendingBookings(
                onBookingsChanged = { bookings ->

                    _uiState.value = _uiState.value.copy(
                        bookings = bookings,
                        isLoading = false,
                        errorMessage = null
                    )
                },

                onError = { exception ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage
                            ?: "Unable to load booking requests."
                    )
                }
            )

        } catch (exception: Exception) {

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = exception.localizedMessage
                    ?: "Unable to connect to booking requests."
            )
        }
    }

    /**
     * Manually refresh pending bookings.
     */
    fun refreshBookings() {

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {

            repository.getPendingOwnerBookings()
                .onSuccess { bookings ->

                    _uiState.value = _uiState.value.copy(
                        bookings = bookings,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                .onFailure { exception ->

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.localizedMessage
                            ?: "Unable to refresh booking requests."
                    )
                }
        }
    }

    /**
     * Approve a payment and create its parking session.
     */
    fun approveBooking(bookingId: String) {

        if (_uiState.value.processingBookingId != null) return

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                processingBookingId = bookingId,
                errorMessage = null
            )

            repository.approveBooking(bookingId)
                .onSuccess {

                    _uiState.value = _uiState.value.copy(
                        processingBookingId = null,
                        errorMessage = null
                    )

                    // The Firestore listener will automatically
                    // remove the approved booking from pending requests.
                }
                .onFailure { exception ->

                    _uiState.value = _uiState.value.copy(
                        processingBookingId = null,
                        errorMessage = exception.localizedMessage
                            ?: "Unable to approve this booking."
                    )
                }
        }
    }

    /**
     * Reject a payment request.
     */
    fun rejectBooking(
        bookingId: String,
        reason: String
    ) {

        if (_uiState.value.processingBookingId != null) return

        viewModelScope.launch {

            _uiState.value = _uiState.value.copy(
                processingBookingId = bookingId,
                errorMessage = null
            )

            repository.rejectBooking(
                bookingId = bookingId,
                reason = reason
            )
                .onSuccess {

                    _uiState.value = _uiState.value.copy(
                        processingBookingId = null,
                        errorMessage = null
                    )

                    // Firestore listener updates the pending list.
                }
                .onFailure { exception ->

                    _uiState.value = _uiState.value.copy(
                        processingBookingId = null,
                        errorMessage = exception.localizedMessage
                            ?: "Unable to reject this booking."
                    )
                }
        }
    }

    /**
     * Remove the Firestore listener when the ViewModel is cleared.
     */
    override fun onCleared() {
        bookingListener?.remove()
        bookingListener = null
        super.onCleared()
    }
}
