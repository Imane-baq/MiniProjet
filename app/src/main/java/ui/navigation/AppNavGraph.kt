package com.example.miniprojet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miniprojet.ui.account.AccountScreen
import com.example.miniprojet.ui.auth.AuthViewModel
import com.example.miniprojet.ui.auth.LoginScreen
import com.example.miniprojet.ui.auth.RegisterScreen
import ui.account.DiceOptionsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        if (uiState.isLoggedIn) {
            authViewModel.loadCurrentUser()
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (uiState.isLoggedIn) Routes.Account else Routes.Login
    ) {
        composable(Routes.Login) {
            LoginScreen(
                uiState = uiState,
                onLogin = authViewModel::login,
                onGoogleLogin = authViewModel::loginWithGoogle,
                onGoToRegister = {
                    navController.navigate(Routes.Register)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.Account) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Register) {
            RegisterScreen(
                uiState = uiState,
                onRegister = authViewModel::register,
                onGoToLogin = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Register) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Routes.Account) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Account) {
            AccountScreen(
                uiState = uiState,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Account) { inclusive = true }
                    }
                },
                onGoToDiceOptions = {
                    navController.navigate(Routes.DiceOptions) {
                        popUpTo(Routes.Account) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DiceOptions) {
            DiceOptionsScreen(
                uiState = uiState,
                navController,
            )
        }

    }
}