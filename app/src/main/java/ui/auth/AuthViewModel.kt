package com.example.miniprojet.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniprojet.data.auth.AuthRepository
import com.example.miniprojet.domain.model.AppUser
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: AppUser? = null,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(
        AuthUiState(isLoggedIn = authRepository.isUserLoggedIn())
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(name: String, email: String, password: String) {
        if (name.isBlank()) {
            showError("Le nom est obligatoire.")
            return
        }

        if (email.isBlank()) {
            showError("L'email est obligatoire.")
            return
        }

        if (password.length < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.register(
                name = name.trim(),
                email = email.trim(),
                password = password.trim()
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur lors de l'inscription."
                    )
                }
            )
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank()) {
            showError("L'email est obligatoire.")
            return
        }

        if (password.isBlank()) {
            showError("Le mot de passe est obligatoire.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.login(
                email = email.trim(),
                password = password.trim()
            )

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur de connexion."
                    )
                }
            )
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.loginWithGoogle(account)

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                },
                onFailure = { exception ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        errorMessage = exception.message ?: "Erreur Google Sign-In."
                    )
                }
            )
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUserProfile()

            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        isLoggedIn = true,
                        user = user
                    )
                },
                onFailure = {
                    _uiState.value = AuthUiState(isLoggedIn = false)
                }
            )
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState()
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
}