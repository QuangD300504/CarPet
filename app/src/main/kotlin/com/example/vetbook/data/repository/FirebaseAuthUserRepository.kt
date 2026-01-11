package com.example.vetbook.data.repository

import com.example.vetbook.R
import com.example.vetbook.domain.models.Pet
import com.example.vetbook.domain.models.User
import com.example.vetbook.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        
        return try {
            val document = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            if (document.exists()) {
                User(
                    id = firebaseUser.uid,
                    name = document.getString("fullName") ?: "User",
                    email = document.getString("email") ?: firebaseUser.email ?: "",
                    points = (document.getLong("points") ?: 0).toInt(),
                    profileImage = R.drawable.profile
                )
            } else {
                User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "User",
                    email = firebaseUser.email ?: "",
                    points = 0,
                    profileImage = R.drawable.profile
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getUserPets(userId: String): List<Pet> {
        return listOf(
            Pet(
                id = "pet_001",
                ownerId = userId,
                name = "PiCi",
                type = "Dog",
                breed = "Golden Retriever",
                imageRes = R.drawable.dog_icon,
                age = "3 years 6 months",
                gender = "Male",
                weight = "28 kg",
                parasiticStatus = "Healthy",
                note = "Very energetic and loves to play.",
                realImgUrl = "https://example.com/pici.jpg",
                vaccinations = emptyList()
            ),
            Pet(
                id = "pet_002",
                ownerId = userId,
                name = "Bella",
                type = "Cat",
                breed = "Persian",
                imageRes = R.drawable.cat_icon,
                age = "2 years 3 months",
                gender = "Female",
                weight = "9.5 kg",
                parasiticStatus = "Healthy",
                note = "Calm and affectionate.",
                realImgUrl = "https://example.com/bella.jpg",
                vaccinations = emptyList()
            )
        )
    }
}
