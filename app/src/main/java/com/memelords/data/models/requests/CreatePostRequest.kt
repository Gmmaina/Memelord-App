package com.memelords.data.models.requests

data class CreatePostRequest(
    val imageUrl: String,
    val caption: String? = null
)
