package com.example.dsy1105_006d_lc7.ui.login

import androidx.lifecycle.ViewModel
import com.example.dsy1105_006d_lc7.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun submit(onSuccess: (String) -> Unit) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val usuario = _uiState.value.username.trim()
        val ok = repo.login(usuario, _uiState.value.password)

        _uiState.update { it.copy(isLoading = false) }

        if (ok) onSuccess(usuario)
        else _uiState.update { it.copy(error = "Credenciales inválidas") }
    }
}  // fin viewmodel