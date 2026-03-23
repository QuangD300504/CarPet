package com.example.vetbook.data.repository

import android.util.Log
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.models.UserProfileDto
import com.example.vetbook.data.network.CloudinaryConfig
import com.example.vetbook.data.network.CloudinaryService
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User
import com.example.vetbook.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * UserRepository backed by Firebase Auth + Firestore via Remote*DataSource
 * abstractions. This keeps the data layer free from direct Firestore usage.
 */
class FirebaseAuthUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val remotePetDataSource: RemotePetDataSource,
    private val cloudinaryService: CloudinaryService
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        val domainUser = try {
            val uid = firebaseUser.uid
            val profile = remoteUserDataSource.getUserProfile(uid)
                ?: createInitialProfile(uid)
            profile.toDomain(points = 0, profileImage = null)
        } catch (e: Exception) {
            // FIX: Firestore failure used to silently return null, leaving the entire
            // user object null. Instead, fall back to a minimal User built from
            // the Firebase Auth token, which is always available while logged in.
            // This guarantees EditProfileScreen always has at least the auth email.
            Log.w("UserRepository", "Firestore profile load failed, falling back to auth data", e)
            User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                phoneNumber = firebaseUser.phoneNumber ?: "",
                points = 0,
                profileImageUrl = firebaseUser.photoUrl?.toString()
            )
        }

        // FIX: even when Firestore succeeds, the stored email may be blank
        // (accounts created before the email field was written, or legacy docs).
        // Always guarantee email is populated from the Auth token.
        return if (domainUser.email.isBlank()) {
            domainUser.copy(email = firebaseUser.email ?: "")
        } else {
            domainUser
        }
    }

    override suspend fun getUserPets(userId: String): List<Pet> {
        return try {
            remotePetDataSource
                .getUserPets(userId)
                .map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateUserAvatar(imageBytes: ByteArray): Result<String> {
        val firebaseUser = auth.currentUser ?: return Result.failure(
            IllegalStateException("User not logged in")
        )

        return try {
            Log.d("UserRepository", "Starting avatar upload for uid=${firebaseUser.uid}")
            val mediaType = "image/*".toMediaType()
            val requestFile = imageBytes.toRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = "avatar.jpg",
                body = requestFile
            )

            val preset = CloudinaryConfig.UPLOAD_PRESET_VETBOOK
                .toRequestBody("text/plain".toMediaType())
            val apiKey = CloudinaryConfig.API_KEY
                .toRequestBody("text/plain".toMediaType())

            val response = cloudinaryService.uploadImage(filePart, preset, apiKey)
            if (response.isSuccessful && response.body() != null) {
                val url = response.body()!!.secure_url
                Log.d("UserRepository", "Cloudinary upload success, url=$url")

                remoteUserDataSource.updateUserProfileFields(
                    uid = firebaseUser.uid,
                    fields = mapOf(
                        "profileImageUrl" to url,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )

                Result.success(url)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(
                    "UserRepository",
                    "Cloudinary upload failed: code=${response.code()} body=$errorBody"
                )
                Result.failure(IllegalStateException("Cloudinary error: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "updateUserAvatar threw exception", e)
            Result.failure(e)
        }
    }

    private suspend fun createInitialProfile(uid: String): UserProfileDto {
        val firebaseUser = auth.currentUser ?: throw IllegalStateException("User not logged in")

        val profile = UserProfileDto(
            uid = uid,
            fullName = firebaseUser.displayName ?: "User",
            email = firebaseUser.email ?: "",
            phone = firebaseUser.phoneNumber ?: "",
            createdAt = System.currentTimeMillis(),
            isEmailVerified = firebaseUser.isEmailVerified
        )

        try {
            remoteUserDataSource.setUserProfile(profile)
        } catch (e: Exception) {
            Log.w("UserRepository", "createInitialProfile Firestore write failed", e)
        }
        return profile
    }
}