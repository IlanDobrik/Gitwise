package com.example.gitwise.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gitwise.gitmanager.GitHubAPI
import com.example.gitwise.gitmanager.validateTokenSuspend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val token: String = "",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun setToken(token: String) {
        _state.value = _state.value.copy(token = token.trim(), error = null)
    }

    fun login(onSuccess: (token: String, username: String) -> Unit) {
        val token = _state.value.token.trim()
        if (token.isBlank()) return

        _state.value = _state.value.copy(loading = true, error = null)

        viewModelScope.launch {
            val api = GitHubAPI(token)
            val (success, usernameOrError) = api.validateTokenSuspend()

            if (success && !usernameOrError.isNullOrBlank()) {
                _state.value = _state.value.copy(loading = false, error = null)
                onSuccess(token, usernameOrError)
            } else {
                _state.value = _state.value.copy(
                    loading = false,
                    error = usernameOrError ?: "Authentication failed"
                )
            }
        }
    }
}
