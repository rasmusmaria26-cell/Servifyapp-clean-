package com.servify.app.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servify.app.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onFullNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, error = null) }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPhoneChange(phone: String) {
        _uiState.update { it.copy(phone = phone, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun onRoleChange(role: String) {
        _uiState.update { it.copy(role = role, error = null) }
    }

    fun onSignupClick() {
        val state = uiState.value
        if (state.fullName.isBlank() || state.email.isBlank() || state.password.isBlank() || state.phone.isBlank()) {
            _uiState.update { it.copy(error = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.signUp(
                email = state.email,
                password = state.password,
                fullName = state.fullName,
                phone = state.phone,
                role = state.role
            ).onSuccess { userProfile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedUp = true,
                        userRole = userProfile.role
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Signup failed"
                    )
                }
            }
        }
    }
}

data class SignupUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "customer", // Default role
    val isLoading: Boolean = false,
    val isSignedUp: Boolean = false,
    val userRole: String? = null,
    val error: String? = null
)
