package com.memelords.data.models.requests

data class ResetPasswordRequest(
    val email: String,
    val resetCode: String,
    val newPassword: String
)