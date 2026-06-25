package com.rafiq0014.sportwear.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rafiq0014.sportwear.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: AuthViewModel,
    navigateToHome: () -> Unit,
    navigateToLogin: () -> Unit
) {
    val user by viewModel.userFlow.collectAsState(initial = null)
    
    LaunchedEffect(user) {
        delay(1000) // Small delay to show splash
        if (user != null) {
            navigateToHome()
        } else {
            navigateToLogin()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
