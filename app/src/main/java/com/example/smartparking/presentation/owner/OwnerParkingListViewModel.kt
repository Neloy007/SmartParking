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

data class OwnerParkingListUiState(
    val isLoading: Boolean = false,
    val parkings: List<Parking> = emptyList(),
    val error: String? = null
)

class OwnerParkingListViewModel : ViewModel() {

    private val repository = ParkingRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(OwnerParkingListUiState())
    val uiState: StateFlow<OwnerParkingListUiState> =
        _uiState.asStateFlow()

    fun loadParkings() {
        val ownerId = auth.currentUser?.uid

        if (ownerId == null) {
            _uiState.value = OwnerParkingListUiState(
                error = "Please log in again."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = OwnerParkingListUiState(isLoading = true)

            repository.getOwnerParkings(ownerId).fold(
                onSuccess = { parkings ->
                    _uiState.value = OwnerParkingListUiState(
                        parkings = parkings
                    )
                },
                onFailure = { exception ->
                    _uiState.value = OwnerParkingListUiState(
                        error = exception.message
                            ?: "Unable to load parking facilities."
                    )
                }
            )
        }
    }
}