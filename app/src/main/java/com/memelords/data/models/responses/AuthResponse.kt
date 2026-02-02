package com.memelords.data.models.responses

import com.memelords.data.models.User

data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val user: User? = null,
    val message: String? = null
)
