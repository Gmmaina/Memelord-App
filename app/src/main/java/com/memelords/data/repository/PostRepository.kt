package com.memelords.data.repository

import com.memelords.data.models.Post
import com.memelords.data.models.requests.CreatePostRequest
import com.memelords.data.remote.ApiService
import com.memelords.data.remote.CloudinaryService
import com.memelords.utils.Constants
import com.memelords.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostRepository(
    private val apiService: ApiService,
    private val cloudinaryService: CloudinaryService
) {

    suspend fun getPosts(): Flow<Resource<List<Post>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getPosts()
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data ?: emptyList()))
            } else {
                emit(Resource.Error("Failed to load posts"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun uploadImage(imageFile: File): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val uploadPreset =
                Constants.CLOUDINARY_UPLOAD_PRESET.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = cloudinaryService.uploadImage(
                Constants.CLOUDINARY_UPLOAD_URL,
                body,
                uploadPreset
            )

            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!.secure_url))
            } else {
                emit(Resource.Error("Image upload failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun createPost(imageUrl: String, caption: String?): Flow<Resource<Post>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.createPost(CreatePostRequest(imageUrl, caption))
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error("Failed to create post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun deletePost(postId: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deletePost(postId)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to delete post"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }

    suspend fun toggleLike(postId: String, isLiked: Boolean): Flow<Resource<Unit>> = flow {
        try {
            val response = if (isLiked) {
                apiService.unlikePost(postId)
            } else {
                apiService.likePost(postId)
            }

            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("Failed to update like"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An error occurred"))
        }
    }
}
