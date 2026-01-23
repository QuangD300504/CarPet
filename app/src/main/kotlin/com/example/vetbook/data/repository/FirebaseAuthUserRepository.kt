package com.example.vetbook.data.repository

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.mappers.toDomain
import com.example.vetbook.data.models.UserProfileDto
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User
import com.example.vetbook.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * UserRepository backed by Firebase Auth + Firestore via Remote*DataSource
 * abstractions. This keeps the data layer free from direct Firestore usage.
 */
class FirebaseAuthUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val remoteUserDataSource: RemoteUserDataSource,
    private val remotePetDataSource: RemotePetDataSource
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null

        return try {
            val uid = firebaseUser.uid

            val profile = remoteUserDataSource.getUserProfile(uid)
                ?: createInitialProfile(uid)

            // Points & profile image can later be loaded from separate sources.
            profile.toDomain(points = 0, profileImage = 0)
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
