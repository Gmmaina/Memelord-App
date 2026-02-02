package com.memelords.data.models

data class User(
    val id: String,
    val username: String,
    val email: String,
    val profilePicture: String? = null,
    val createdAt: String? = null
)