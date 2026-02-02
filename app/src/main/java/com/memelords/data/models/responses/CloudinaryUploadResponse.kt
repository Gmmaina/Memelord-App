package com.memelords.data.models.responses

data class CloudinaryUploadResponse(
    val secure_url: String,
    val public_id: String,
    val width: Int,
    val height: Int
)