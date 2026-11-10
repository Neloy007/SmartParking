package com.example.smartparking.presentation.owner


import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OwnerProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val error: String? = null
)

class OwnerProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = mutableStateOf(OwnerProfileUiState())
    val uiState: State<OwnerProfileUiState> = _uiState

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = OwnerProfileUiState(isLoading = true)

            try {
                val uid = auth.currentUser?.uid
                    ?: throw Exception("User is not logged in.")

                val document = firestore
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                val user = document.toObject(User::class.java)
                    ?: throw Exception("Owner profile not found.")

                _uiState.value = OwnerProfileUiState(
                    isLoading = false,
                    user = user.copy(uid = uid)
                )

            } catch (e: Exception) {
                _uiState.value = OwnerProfileUiState(
                    isLoading = false,
                    error = e.message ?: "Unable to load profile."
                )
            }
        }
    }
}
