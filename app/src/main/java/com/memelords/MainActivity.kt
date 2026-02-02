package com.memelords

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.memelords.data.remote.ApiService
import com.memelords.data.remote.CloudinaryService
import com.memelords.data.repository.AuthRepository
import com.memelords.data.repository.PostRepository
import com.memelords.data.repository.UserRepository
import com.memelords.network.RetrofitClient
import com.memelords.ui.navigation.NavGraph
import com.memelords.ui.theme.AppTheme
import com.memelords.utils.TokenManager
import com.memelords.viewmodel.AuthViewModel
import com.memelords.viewmodel.HomeViewModel
import com.memelords.viewmodel.PostViewModel
import com.memelords.viewmodel.ProfileViewModel


class MainActivity : ComponentActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var authViewModel: AuthViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var postViewModel: PostViewModel
    private lateinit var profileViewModel: ProfileViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request storage permissions for downloads
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Initialize dependencies
        tokenManager = TokenManager(applicationContext)

        val retrofit = RetrofitClient.create(tokenManager)
        val apiService = retrofit.create(ApiService::class.java)

        val cloudinaryRetrofit = RetrofitClient.createCloudinary()
        val cloudinaryService = cloudinaryRetrofit.create(CloudinaryService::class.java)

        // Initialize repositories
        val authRepository = AuthRepository(apiService, tokenManager)
        val postRepository = PostRepository(apiService, cloudinaryService)
        val userRepository = UserRepository(apiService)

        // Initialize ViewModels
        authViewModel = AuthViewModel(authRepository)
        homeViewModel = HomeViewModel(postRepository)
        postViewModel = PostViewModel(postRepository)
        profileViewModel = ProfileViewModel(userRepository, postRepository)

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        tokenManager = tokenManager,
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        postViewModel = postViewModel,
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}