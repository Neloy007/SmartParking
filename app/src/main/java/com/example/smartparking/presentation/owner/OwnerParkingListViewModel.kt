
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
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val parkings: List<Parking> = emptyList(),
    val error: String? = null,
    val actionMessage: String? = null
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
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Please log in again."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                actionMessage = null
            )

            repository.getOwnerParkings(ownerId).fold(
                onSuccess = { parkings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        parkings = parkings,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                            ?: "Unable to load parking facilities."
                    )
                }
            )
        }
    }

    fun updateParking(parking: Parking) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                actionMessage = null
            )

            repository.updateParking(parking).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        actionMessage = "Parking facility updated successfully."
                    )
                    loadParkings()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = exception.message
                            ?: "Unable to update parking facility."
                    )
                }
            )
        }
    }

    fun deleteParking(parkingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                error = null,
                actionMessage = null
            )

            repository.deleteParking(parkingId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        actionMessage = "Parking facility deleted successfully."
                    )
                    loadParkings()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = exception.message
                            ?: "Unable to delete parking facility."
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            actionMessage = null
        )
    }
    fun showValidationError(message: String) {
        _uiState.value = _uiState.value.copy(
            error = message,
            actionMessage = null
        )
    }
}
