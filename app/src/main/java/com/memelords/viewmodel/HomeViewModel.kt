package com.memelords.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memelords.data.models.Post
import com.memelords.data.repository.PostRepository
import com.memelords.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val repository: PostRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            repository.getPosts().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                posts = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            val currentPosts = _uiState.value.posts
            val post = currentPosts.find { it.id == postId } ?: return@launch

            // Optimistic update
            val updatedPosts = currentPosts.map {
                if (it.id == postId) {
                    it.copy(
                        isLikedByUser = !it.isLikedByUser,
                        likes = if (it.isLikedByUser) it.likes - 1 else it.likes + 1
                    )
                } else it
            }
            _uiState.update { it.copy(posts = updatedPosts) }

            // Actual API call
            repository.toggleLike(postId, post.isLikedByUser).collect { result ->
                if (result is Resource.Error) {
                    // Revert on error
                    _uiState.update { it.copy(posts = currentPosts) }
                }
            }
        }
    }
}
