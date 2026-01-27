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

        return try {
            val uid = firebaseUser.uid

            val profile = remoteUserDataSource.getUserProfile(uid)
                ?: createInitialProfile(uid)

            // Points & profile image can later be loaded from separate sources.
            profile.toDomain(points = 0, profileImage = null)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserPets(userId: String): List<Pet> {
        return try {
            remotePetDataSource
                .getUserPets(userId)
                .map { it.toDomain() }
        } catch (e: Exception) {
            // Return empty list on error instead of crashing
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
                Result.failure(
                    IllegalStateException(
                        "Cloudinary error: $errorBody"
                    )
                )
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

        // Try to save profile, but don't fail if it doesn't work
        try {
            remoteUserDataSource.setUserProfile(profile)
        } catch (e: Exception) {
            // Log error but continue - profile will be created in memory
            // This allows the app to work even if Firestore is temporarily unavailable
        }
        return profile
    }
}
