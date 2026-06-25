package com.rafiq0014.sportwear.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rafiq0014.sportwear.SportWearApp
import com.rafiq0014.sportwear.auth.GoogleAuthManager
import com.rafiq0014.sportwear.data.model.User
import com.rafiq0014.sportwear.datastore.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
}

class AuthViewModel(
    private val googleAuthManager: GoogleAuthManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    val userFlow = sessionManager.userFlow

    val authState: StateFlow<AuthState> = sessionManager.userFlow.map { user ->
        if (user != null) {
            AuthState.Authenticated(user)
        } else {
            AuthState.Unauthenticated
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AuthState.Loading
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            val user = googleAuthManager.signIn(context)
            if (user != null) {
                sessionManager.saveSession(user)
            }
            _isLoading.value = false
        }
    }

    fun signOut(context: Context) {
        viewModelScope.launch {
            googleAuthManager.signOut(context)
            sessionManager.clearSession()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SportWearApp)
                val container = application.container
                AuthViewModel(
                    googleAuthManager = container.googleAuthManager,
                    sessionManager = container.sessionManager
                )
            }
        }
    }
}
