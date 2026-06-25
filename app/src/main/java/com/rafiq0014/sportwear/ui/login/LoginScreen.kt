package com.rafiq0014.sportwear.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rafiq0014.sportwear.R
import com.rafiq0014.sportwear.viewmodel.AuthState
import com.rafiq0014.sportwear.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    navigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val authState by viewModel.authState.collectAsState()

    if (authState is AuthState.Authenticated) {
        // Using LaunchedEffect to avoid calling navigation during composition
        androidx.compose.runtime.LaunchedEffect(Unit) {
            navigateToHome()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (isLoading || authState is AuthState.Loading || authState is AuthState.Authenticated) {
            CircularProgressIndicator()
        } else {
            Button(onClick = { viewModel.signInWithGoogle(context) }) {
                Text(stringResource(R.string.sign_in_with_google))
            }
        }
    }
}
