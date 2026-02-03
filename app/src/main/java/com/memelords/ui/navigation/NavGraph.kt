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
import com.memelords.ui.screens.main.MainScreen
import com.memelords.ui.screens.post.CreatePostScreen
import com.memelords.utils.TokenManager
import com.memelords.viewmodel.AuthViewModel
import com.memelords.viewmodel.HomeViewModel
import com.memelords.viewmodel.PostViewModel
import com.memelords.viewmodel.ProfileViewModel

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Main : Screen("main")
    object CreatePost : Screen("create_post")
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
    val startDestination = if (token != null) Screen.Main.route else Screen.Login.route

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
                    navController.navigate(Screen.Main.route) {
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
                    navController.navigate(Screen.Main.route) {
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

        // Main screen with bottom navigation
        composable(Screen.Main.route) {
            MainScreen(
                homeViewModel = homeViewModel,
                profileViewModel = profileViewModel,
                postViewModel = postViewModel, // PASS PostViewModel
                authViewModel = authViewModel,
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Create Post screen
        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                viewModel = postViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.popBackStack()
                    homeViewModel.loadPosts() // Refresh feed
                    profileViewModel.loadProfile() // Refresh profile posts
                }
            )
        }
    }
}