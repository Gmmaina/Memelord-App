package com.memelords.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.memelords.data.repository.PostRepository
import com.memelords.utils.ImageUtils
import com.memelords.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePostState(
    val isLoading: Boolean = false,
    val uploadProgress: Int = 0,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    private val _createPostState = MutableStateFlow(CreatePostState())
    val createPostState: StateFlow<CreatePostState> = _createPostState

    fun createPost(context: Context, imageUri: Uri, caption: String?) {
        viewModelScope.launch {
            _createPostState.update { it.copy(isLoading = true, uploadProgress = 0) }

            // Compress image
            val compressedFile = ImageUtils.compressImage(context, imageUri)
            if (compressedFile == null) {
                _createPostState.update {
                    it.copy(isLoading = false, error = "Failed to process image")
                }
                return@launch
            }

            // Upload to Cloudinary
            repository.uploadImage(compressedFile).collect { uploadResult ->
                when (uploadResult) {
                    is Resource.Loading -> {
                        _createPostState.update { it.copy(uploadProgress = 50) }
                    }

                    is Resource.Success -> {
                        val imageUrl = uploadResult.data ?: return@collect

                        // Create post
                        repository.createPost(imageUrl, caption).collect { createResult ->
                            when (createResult) {
                                is Resource.Loading -> {
                                    _createPostState.update { it.copy(uploadProgress = 75) }
                                }

                                is Resource.Success -> {
                                    _createPostState.update {
                                        it.copy(
                                            isLoading = false,
                                            uploadProgress = 100,
                                            isSuccess = true
                                        )
                                    }
                                }

                                is Resource.Error -> {
                                    _createPostState.update {
                                        it.copy(
                                            isLoading = false,
                                            error = createResult.message
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is Resource.Error -> {
                        _createPostState.update {
                            it.copy(isLoading = false, error = uploadResult.message)
                        }
                    }
                }
            }

            // Clean up
            compressedFile.delete()
        }
    }

    fun resetState() {
        _createPostState.value = CreatePostState()
    }
}
