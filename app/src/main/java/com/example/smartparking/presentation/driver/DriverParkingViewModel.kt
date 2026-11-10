package com.example.smartparking.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.Parking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DriverParkingUiState(
    val isLoading: Boolean = false,
    val parkings: List<Parking> = emptyList(),
    val error: String? = null
)

class DriverParkingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(DriverParkingUiState())
    val uiState = _uiState.asStateFlow()

    private var parkingListener: ListenerRegistration? = null

    fun loadParkings() {

        if (parkingListener != null) return

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )

        parkingListener = firestore.collection("parkings")
            .whereEqualTo("approvalStatus", "APPROVED")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                            ?: "Unable to load parking facilities."
                    )
                    return@addSnapshotListener
                }

                val parkingList = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Parking::class.java)
                        ?.copy(id = document.id)
                } ?: emptyList()

                _uiState.value = DriverParkingUiState(
                    isLoading = false,
                    parkings = parkingList
                )
            }
    }

    fun refreshParkings() {
        parkingListener?.remove()
        parkingListener = null
        loadParkings()
    }

    override fun onCleared() {
        parkingListener?.remove()
        parkingListener = null
        super.onCleared()
    }
}
