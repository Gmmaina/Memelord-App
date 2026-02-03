package com.memelords.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memelords.data.models.Post
import com.memelords.data.models.User
import com.memelords.data.repository.PostRepository
import com.memelords.data.repository.UserRepository
import com.memelords.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User? = null,
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Get current user
            userRepository.getCurrentUser().collect { userResult ->
                when (userResult) {
                    is Resource.Success -> {
                        val user = userResult.data
                        _uiState.update { it.copy(user = user) }

                        // Load user's posts
                        user?.id?.let { userId ->
                            loadUserPosts(userId)
                        } ?: run {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = userResult.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Already set loading above
                    }
                }
            }
        }
    }

    private fun loadUserPosts(userId: String) {
        viewModelScope.launch {
            userRepository.getUserPosts(userId).collect { postsResult ->
                when (postsResult) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                posts = postsResult.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = postsResult.message)
                        }
                    }
                    is Resource.Loading -> {
                        // Keep loading state
                    }
                }
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            postRepository.deletePost(postId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(posts = it.posts.filter { post -> post.id != postId })
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun updateProfile(username: String?, password: String?) {
        viewModelScope.launch {
            userRepository.updateProfile(username, password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(user = result.data) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    // NEW: Upload profile picture
    fun updateProfilePicture(imageUrl: String) {
        viewModelScope.launch {
            // You'll need to add this endpoint to your backend
            // For now, just update locally
            _uiState.update {
                it.copy(user = it.user?.copy(profilePicture = imageUrl))
            }
        }
    }
}