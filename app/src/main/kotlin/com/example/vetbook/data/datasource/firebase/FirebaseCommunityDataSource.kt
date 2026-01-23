package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteCommunityDataSource
import com.example.vetbook.data.models.PetDto
import com.example.vetbook.data.models.PetEventDto
import com.example.vetbook.data.models.PostDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val POSTS_COLLECTION = "posts"
private const val PETS_COLLECTION = "pets"
private const val EVENTS_COLLECTION = "petEvents"

class FirebaseCommunityDataSource(
    private val firestore: FirebaseFirestore
) : RemoteCommunityDataSource {

    override fun observePosts(): Flow<List<PostDto>> = callbackFlow {
        val registration = firestore
            .collection(POSTS_COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val posts = snapshot.documents.map { doc ->
                    PostDto(
                        id = doc.id,
                        authorId = doc.getString("authorId") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        authorAvatarUrl = doc.getString("authorAvatarUrl"),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        content = doc.getString("content") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                        commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt()
                    )
                }
                trySend(posts).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override fun observeAdoptionPets(): Flow<List<PetDto>> = callbackFlow {
        val registration = firestore
            .collection(PETS_COLLECTION)
            .whereEqualTo("isForAdoption", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val pets = snapshot.documents.map { doc ->
                    PetDto(
                        id = doc.id,
                        ownerId = doc.getString("ownerId"),
                        name = doc.getString("name") ?: "",
                        type = doc.getString("type") ?: "",
                        breed = doc.getString("breed") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        age = doc.getString("age") ?: "",
                        gender = doc.getString("gender") ?: "",
                        weight = doc.getString("weight") ?: "",
                        parasiticStatus = doc.getString("parasiticStatus") ?: "",
                        note = doc.getString("note") ?: "",
                        isForAdoption = doc.getBoolean("isForAdoption") ?: false
                    )
                }
                trySend(pets).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override fun observeEvents(): Flow<List<PetEventDto>> = callbackFlow {
        val registration = firestore
            .collection(EVENTS_COLLECTION)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                val events = snapshot.documents.map { doc ->
                    PetEventDto(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        location = doc.getString("location") ?: "",
                        imageUrl = doc.getString("imageUrl")
                    )
                }
                trySend(events).isSuccess
            }

        awaitClose { registration.remove() }
    }
}


