package com.memelords.data.models.requests

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)