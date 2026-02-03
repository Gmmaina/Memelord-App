package com.memelords.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.memelords.viewmodel.AuthViewModel
import com.memelords.viewmodel.PostViewModel
import com.memelords.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    postViewModel: PostViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uploadState by postViewModel.createPostState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Dialog states
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Selected tab (0 = Posts, 1 = Liked)
    var selectedTab by remember { mutableStateOf(0) }

    // Profile picture picker
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            scope.launch {
                // Upload profile picture
                postViewModel.createPost(context, it, null)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                // Profile Overview
                ProfileOverview(
                    username = uiState.user?.username ?: "",
                    email = uiState.user?.email ?: "",
                    profilePicture = selectedImageUri?.toString() ?: uiState.user?.profilePicture,
                    onEditPhotoClick = { imagePickerLauncher.launch("image/*") },
                    isUploading = uploadState.isLoading,
                    uploadProgress = uploadState.uploadProgress
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Posts Section
                ProfilePosts(
                    posts = uiState.posts,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    }

    // Settings Bottom Sheet Dialog
    if (showSettingsDialog) {
        SettingsBottomSheet(
            onDismiss = { showSettingsDialog = false },
            onChangeUsername = {
                showSettingsDialog = false
                showUsernameDialog = true
            },
            onChangePassword = {
                showSettingsDialog = false
                showPasswordDialog = true
            },
            onLogout = {
                showSettingsDialog = false
                showLogoutDialog = true
            }
        )
    }

    // Change Username Dialog
    if (showUsernameDialog) {
        ChangeUsernameDialog(
            currentUsername = uiState.user?.username ?: "",
            onDismiss = { showUsernameDialog = false },
            onConfirm = { newUsername ->
                viewModel.updateProfile(username = newUsername, password = null)
                showUsernameDialog = false
            }
        )
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { newPassword ->
                viewModel.updateProfile(username = null, password = newPassword)
                showPasswordDialog = false
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                authViewModel.logout()
                showLogoutDialog = false
                onLogout()
            }
        )
    }
}