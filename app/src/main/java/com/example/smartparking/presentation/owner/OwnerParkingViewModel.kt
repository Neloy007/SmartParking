package com.example.smartparking.presentation.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.repository.ParkingRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OwnerParkingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class OwnerParkingViewModel : ViewModel() {

    private val repository = ParkingRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(OwnerParkingUiState())
    val uiState: StateFlow<OwnerParkingUiState> = _uiState.asStateFlow()

    fun registerParking(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        totalSlots: Int,
        availableSlots: Int,
        pricePerHour: Double
    ) {
        val ownerId = auth.currentUser?.uid

        if (ownerId == null) {
            _uiState.value = OwnerParkingUiState(
                error = "Please log in before registering parking."
            )
            return
        }

        if (name.isBlank() || address.isBlank()) {
            _uiState.value = OwnerParkingUiState(
                error = "Please enter the parking name and address."
            )
            return
        }

        if (totalSlots <= 0 ||
            availableSlots < 0 ||
            availableSlots > totalSlots ||
            pricePerHour < 0 ||
            !pricePerHour.isFinite()
        ) {
            _uiState.value = OwnerParkingUiState(
                error = "Please enter valid parking capacity and pricing."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = OwnerParkingUiState(isLoading = true)

            val parking = Parking(
                ownerId = ownerId,
                name = name.trim(),
                address = address.trim(),
                latitude = latitude,
                longitude = longitude,
                totalSlots = totalSlots,
                availableSlots = availableSlots,
                pricePerHour = pricePerHour,
                active = true
            )

            repository.addParking(parking).fold(
                onSuccess = {
                    _uiState.value = OwnerParkingUiState(
                        isSuccess = true
                    )
                },
                onFailure = {
                    _uiState.value = OwnerParkingUiState(
                        error = it.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = OwnerParkingUiState()
    }
}