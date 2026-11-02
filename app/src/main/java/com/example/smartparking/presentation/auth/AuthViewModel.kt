package com.example.smartparking.presentation.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartparking.data.model.User
import com.example.smartparking.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(isLoading = true)

            val result = repository.registerUser(
                name = name,
                email = email,
                password = password,
                phone = phone,
                role = role
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(user = user)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState(
                        error = exception.message ?: "Something went wrong"
                    )
                }
            )
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            _uiState.value = AuthUiState(isLoading = true)

            val result = repository.loginUser(email, password)

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(user = user)
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState(
                        error = exception.message ?: "Login failed"
                    )
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }
}