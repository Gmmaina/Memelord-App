package com.memelords.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.memelords.ui.screens.auth.ForgotPasswordScreen
import com.memelords.ui.screens.auth.LoginScreen
import com.memelords.ui.screens.auth.RegisterScreen
import com.memelords.ui.screens.home.HomeScreen
import com.memelords.ui.screens.post.CreatePostScreen
import com.memelords.ui.screens.profile.EditProfileScreen
import com.memelords.ui.screens.profile.ProfileScreen
import com.memelords.utils.TokenManager
import com.memelords.viewmodel.AuthViewModel
import com.memelords.viewmodel.HomeViewModel
import com.memelords.viewmodel.PostViewModel
import com.memelords.viewmodel.ProfileViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object CreatePost : Screen("create_post")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
}

@Composable
fun NavGraph(
    tokenManager: TokenManager,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    postViewModel: PostViewModel,
    profileViewModel: ProfileViewModel,
    navController: NavHostController = rememberNavController()
) {
    val token by tokenManager.getToken().collectAsState(initial = null)
    val startDestination = if (token != null) Screen.Home.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Main screens
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                viewModel = postViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.popBackStack()
                    homeViewModel.loadPosts() // Refresh feed
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel,
                authViewModel = authViewModel,
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
