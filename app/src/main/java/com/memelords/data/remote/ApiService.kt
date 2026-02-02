package com.memelords.data.remote

import com.memelords.data.models.Post
import com.memelords.data.models.User
import com.memelords.data.models.requests.CreatePostRequest
import com.memelords.data.models.requests.ForgotPasswordRequest
import com.memelords.data.models.requests.LoginRequest
import com.memelords.data.models.requests.RegisterRequest
import com.memelords.data.models.requests.ResetPasswordRequest
import com.memelords.data.models.requests.UpdateProfileRequest
import com.memelords.data.models.responses.ApiResponse
import com.memelords.data.models.responses.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // Auth
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<ApiResponse<Unit>>

    // Posts
    @GET("api/posts")
    suspend fun getPosts(): Response<ApiResponse<List<Post>>>

    @POST("api/posts")
    suspend fun createPost(@Body request: CreatePostRequest): Response<ApiResponse<Post>>

    @DELETE("api/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    @POST("api/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    @DELETE("api/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): Response<ApiResponse<Unit>>

    // User
    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<User>>

    @GET("api/users/{id}/posts")
    suspend fun getUserPosts(@Path("id") userId: String): Response<ApiResponse<List<Post>>>

    @PUT("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<User>>
}
