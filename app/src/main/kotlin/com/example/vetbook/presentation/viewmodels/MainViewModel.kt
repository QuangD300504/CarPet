package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun signOut(onSignOutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onSignOutComplete()
        }
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    /** Returns true if the current user signed in via Google (no password needed). */
    fun isGoogleUser(): Boolean {
        return authRepository.getCurrentUser()
            ?.providerData
            ?.any { it.providerId == "google.com" }
            ?: false
    }

}