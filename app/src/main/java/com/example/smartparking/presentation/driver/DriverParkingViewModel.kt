package com.example.smartparking.presentation.driver


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.Parking
import com.example.smartparking.data.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DriverParkingUiState(
    val isLoading: Boolean = false,
    val parkings: List<Parking> = emptyList(),
    val error: String? = null
)

class DriverParkingViewModel : ViewModel() {

    private val repository = ParkingRepository()

    private val _uiState = MutableStateFlow(DriverParkingUiState())
    val uiState = _uiState.asStateFlow()

    fun loadParkings() {
        viewModelScope.launch {
            _uiState.value = DriverParkingUiState(isLoading = true)

            repository.getActiveParkings().fold(
                onSuccess = { parkingList ->
                    _uiState.value = DriverParkingUiState(
                        parkings = parkingList
                    )
                },
                onFailure = { exception ->
                    _uiState.value = DriverParkingUiState(
                        error = exception.message
                            ?: "Unable to load parking facilities."
                    )
                }
            )
        }
    }
}
