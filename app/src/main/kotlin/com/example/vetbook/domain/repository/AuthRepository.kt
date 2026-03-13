package com.example.vetbook.domain.repository

import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<AuthResult>

    suspend fun login(email: String, password: String): Result<AuthResult>
    
    suspend fun signInWithGoogle(context: Context): Result<AuthResult>
    
    suspend fun signOut(): Result<Unit>
    
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    
    suspend fun sendEmailVerification(): Result<Unit>
    
    suspend fun isEmailVerified(): Boolean
    
    suspend fun reloadUser(): Result<Unit>
    
    suspend fun deleteAccount(): Result<Unit>
    
    fun getCurrentUser(): FirebaseUser?
    
    fun isUserLoggedIn(): Boolean
    
    fun getCurrentUserId(): String?

    /**
     * Observes the authentication state changes.
     * Emits the current [FirebaseUser] or null if signed out.
     */
    fun getAuthState(): Flow<FirebaseUser?>
}
