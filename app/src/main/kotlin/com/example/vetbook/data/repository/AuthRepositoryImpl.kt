package com.example.vetbook.data.repository

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.vetbook.R
import com.example.vetbook.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun signUp(
        fullName: String,
        email: String,
        phone: String,
        password: String
    ): Result<AuthResult> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("User creation failed")

            user.sendEmailVerification().await()

            val profile = hashMapOf(
                "uid" to user.uid,
                "fullName" to fullName,
                "email" to email,
                "phone" to phone,
                "createdAt" to System.currentTimeMillis(),
                "isEmailVerified" to false
            )

            firestore
                .collection("users")
                .document(user.uid)
                .set(profile)
                .await()

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<AuthResult> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            // Sync verification status on login
            result.user?.let { syncEmailVerificationStatus(it) }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthResult> {
        return try {
            val activity = context.findActivity() ?: throw Exception("Context is not an Activity")
            val credentialManager = CredentialManager.create(activity)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            
            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                
                val authResult = auth.signInWithCredential(authCredential).await()
                
                // For Google login, email is usually already verified
                authResult.user?.let { user ->
                    val profile = hashMapOf(
                        "uid" to user.uid,
                        "fullName" to (user.displayName ?: ""),
                        "email" to (user.email ?: ""),
                        "isEmailVerified" to true,
                        "lastLogin" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(user.uid).set(profile).await()
                }
                
                Result.success(authResult)
            } else {
                Result.failure(Exception("Invalid credential type"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Email cannot be empty"))
            }
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Log the actual error for debugging
            val errorMessage = when {
                e.message?.contains("user-not-found", ignoreCase = true) == true -> 
                    "Không tìm thấy tài khoản với email này"
                e.message?.contains("invalid-email", ignoreCase = true) == true -> 
                    "Địa chỉ email không hợp lệ"
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet"
                else -> e.localizedMessage ?: e.message ?: "Không thể gửi email đặt lại mật khẩu"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun sendEmailVerification(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Không có người dùng đăng nhập. Vui lòng đăng nhập lại."))
            }
            // Reload user to ensure we have the latest state
            currentUser.reload().await()
            currentUser.sendEmailVerification().await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Log the actual error for debugging
            val errorMessage = when {
                e.message?.contains("network", ignoreCase = true) == true -> 
                    "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet"
                e.message?.contains("too-many-requests", ignoreCase = true) == true -> 
                    "Đã gửi quá nhiều yêu cầu. Vui lòng đợi một chút trước khi thử lại"
                else -> e.localizedMessage ?: e.message ?: "Không thể gửi email xác minh. Vui lòng thử lại"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    override suspend fun isEmailVerified(): Boolean {
        auth.currentUser?.reload()?.await()
        val isVerified = auth.currentUser?.isEmailVerified ?: false
        if (isVerified) {
            auth.currentUser?.let { syncEmailVerificationStatus(it) }
        }
        return isVerified
    }

    override suspend fun reloadUser(): Result<Unit> {
        return try {
            auth.currentUser?.reload()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            auth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncEmailVerificationStatus(user: FirebaseUser) {
        if (user.isEmailVerified) {
            firestore.collection("users").document(user.uid)
                .update("isEmailVerified", true)
                .await()
        }
    }

    override fun getCurrentUser(): FirebaseUser? = auth.currentUser

    override fun isUserLoggedIn(): Boolean = auth.currentUser != null

    override fun getAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { authState ->
            trySend(authState.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
