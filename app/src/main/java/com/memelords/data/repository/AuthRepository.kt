package com.memelords.data.repository

import com.memelords.data.models.requests.ForgotPasswordRequest
import com.memelords.data.models.requests.LoginRequest
import com.memelords.data.models.requests.RegisterRequest
import com.memelords.data.models.responses.AuthResponse
import com.memelords.data.remote.ApiService
import com.memelords.utils.Resource
import com.memelords.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {

    suspend fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    tokenManager.saveToken(authResponse.token)
                    authResponse.user?.id?.let { tokenManager.saveUserId(it) }
                    emit(Resource.Success(authResponse))
                } else {
                    emit(Resource.Error(authResponse.message ?: "Login failed"))
                }
            } else {
                emit(Resource.Error("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String
    ): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.register(RegisterRequest(username, email, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    tokenManager.saveToken(authResponse.token)
                    authResponse.user?.id?.let { tokenManager.saveUserId(it) }
                    emit(Resource.Success(authResponse))
                } else {
                    emit(Resource.Error(authResponse.message ?: "Registration failed"))
                }
            } else {
                emit(Resource.Error("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun forgotPassword(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to send reset email"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun logout() {
        tokenManager.clearToken()
    }
}
