
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
    val isSaving: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class OwnerProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = mutableStateOf(OwnerProfileUiState())
    val uiState: State<OwnerProfileUiState> = _uiState

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load profile."
                )
            }
        }
    }

    /**
     * Save the owner's bKash number in:
     *
     * 1. users/{uid}
     * 2. owner_payment_profiles/{uid}
     *
     * The second document contains only the payment
     * information needed by drivers.
     */
    fun updateBkashNumber(number: String) {
        val cleanNumber = number.trim()

        if (!cleanNumber.matches(Regex("^01[3-9][0-9]{8}$"))) {
            _uiState.value = _uiState.value.copy(
                error = "Enter a valid 11-digit Bangladeshi mobile number.",
                successMessage = null
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                error = null,
                successMessage = null
            )

            try {
                val uid = auth.currentUser?.uid
                    ?: throw Exception("User is not logged in.")

                val userRef = firestore
                    .collection("users")
                    .document(uid)

                val paymentProfileRef = firestore
                    .collection("owner_payment_profiles")
                    .document(uid)

                // Confirm this account belongs to an owner.
                val userSnapshot = userRef.get().await()

                require(userSnapshot.getString("role") == "OWNER") {
                    "Only parking owners can update a bKash payment number."
                }

                val now = System.currentTimeMillis()

                // Save both documents together.
                val batch = firestore.batch()

                batch.update(
                    userRef,
                    mapOf(
                        "bkashNumber" to cleanNumber
                    )
                )

                batch.set(
                    paymentProfileRef,
                    mapOf(
                        "ownerId" to uid,
                        "bkashNumber" to cleanNumber,
                        "updatedAt" to now
                    )
                )

                batch.commit().await()

                val updatedUser = _uiState.value.user?.copy(
                    bkashNumber = cleanNumber
                )

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    user = updatedUser,
                    successMessage = "bKash number updated successfully."
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message
                        ?: "Unable to update bKash number."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }
}
