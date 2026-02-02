package com.memelords.data.models

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val userProfilePicture: String?,
    val imageUrl: String,
    val caption: String?,
    val likes: Int = 0,
    val isLikedByUser: Boolean = false,
    val createdAt: String
)