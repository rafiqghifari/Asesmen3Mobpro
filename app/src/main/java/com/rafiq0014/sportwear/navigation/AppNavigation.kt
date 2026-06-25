package com.rafiq0014.sportwear.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rafiq0014.sportwear.ui.home.HomeScreen
import com.rafiq0014.sportwear.ui.login.LoginScreen
import com.rafiq0014.sportwear.ui.login.SplashScreen
import com.rafiq0014.sportwear.viewmodel.AuthViewModel
import com.rafiq0014.sportwear.viewmodel.ProductViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModel.Factory)

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                navigateToForm = {
                    navController.navigate("form")
                },
                navigateToDetail = { productId ->
                    navController.navigate("detail?productId=$productId")
                },
                navigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("profile") {
            com.rafiq0014.sportwear.ui.profile.ProfileScreen(
                viewModel = authViewModel,
                navigateBack = { navController.popBackStack() },
                navigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "detail?productId={productId}",
            arguments = listOf(androidx.navigation.navArgument("productId") { 
                type = androidx.navigation.NavType.StringType 
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            com.rafiq0014.sportwear.ui.detail.ProductDetailScreen(
                productId = productId,
                viewModel = productViewModel,
                navigateBack = { navController.popBackStack() },
                navigateToEdit = { id -> navController.navigate("form?productId=$id") }
            )
        }
        composable(
            route = "form?productId={productId}",
            arguments = listOf(androidx.navigation.navArgument("productId") { 
                nullable = true
                type = androidx.navigation.NavType.StringType 
            })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            com.rafiq0014.sportwear.ui.form.ProductFormScreen(
                productId = productId,
                viewModel = productViewModel,
                navigateBack = { navController.popBackStack() },
                onSaveComplete = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
    }
}
