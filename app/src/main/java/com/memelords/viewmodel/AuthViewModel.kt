package com.memelords.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memelords.data.repository.AuthRepository
import com.memelords.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow(AuthState())
    val loginState: StateFlow<AuthState> = _loginState

    private val _registerState = MutableStateFlow(AuthState())
    val registerState: StateFlow<AuthState> = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repository.login(email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _loginState.value = AuthState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _loginState.value = AuthState(isSuccess = true)
                    }

                    is Resource.Error -> {
                        _loginState.value = AuthState(error = result.message)
                    }
                }
            }
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            repository.register(username, email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _registerState.value = AuthState(isLoading = true)
                    }

                    is Resource.Success -> {
                        _registerState.value = AuthState(isSuccess = true)
                    }

                    is Resource.Error -> {
                        _registerState.value = AuthState(error = result.message)
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun resetState() {
        _loginState.value = AuthState()
        _registerState.value = AuthState()
    }
}