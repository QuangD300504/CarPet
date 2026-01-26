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
import kotlinx.coroutines.tasks.await

private const val POSTS_COLLECTION = "posts"
private const val PETS_COLLECTION = "pets"
private const val EVENTS_COLLECTION = "petEvents"
private const val USERS_COLLECTION = "users"

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
                        organizerId = doc.getString("organizerId") ?: "", // Foreign key relationship
                        organizerName = doc.getString("organizerName") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        date = doc.getLong("date") ?: 0L,
                        location = doc.getString("location") ?: "",
                        imageUrl = doc.getString("imageUrl"),
                        eventType = doc.getString("eventType") ?: "",
                        maxParticipants = doc.getLong("maxParticipants")?.toInt(),
                        currentParticipants = (doc.getLong("currentParticipants") ?: 0L).toInt(),
                        isActive = doc.getBoolean("isActive") ?: true,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        updatedAt = doc.getLong("updatedAt")
                    )
                }
                trySend(events).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getPostsByAuthor(authorId: String): List<PostDto> {
        return try {
            val snapshot = firestore
                .collection(POSTS_COLLECTION)
                .whereEqualTo("authorId", authorId) // Query by foreign key
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createPost(post: PostDto): Result<PostDto> {
        return try {
            // Validate author exists (foreign key validation)
            if (post.authorId.isBlank()) {
                return Result.failure(Exception("Author ID is required"))
            }

            val authorDoc = firestore.collection(USERS_COLLECTION)
                .document(post.authorId)
                .get()
                .await()

            if (!authorDoc.exists()) {
                return Result.failure(Exception("Author user not found: ${post.authorId}"))
            }

            // Create post document
            val docRef = if (post.id.isBlank()) {
                firestore.collection(POSTS_COLLECTION).document()
            } else {
                firestore.collection(POSTS_COLLECTION).document(post.id)
            }

            val now = System.currentTimeMillis()
            val postData = post.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            )

            docRef.set(postData.toMap()).await()
            Result.success(postData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<PetEventDto> {
        return try {
            val snapshot = firestore
                .collection(EVENTS_COLLECTION)
                .whereEqualTo("organizerId", organizerId) // Query by foreign key
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.map { doc ->
                PetEventDto(
                    id = doc.id,
                    organizerId = doc.getString("organizerId") ?: "",
                    organizerName = doc.getString("organizerName") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    date = doc.getLong("date") ?: 0L,
                    location = doc.getString("location") ?: "",
                    imageUrl = doc.getString("imageUrl"),
                    eventType = doc.getString("eventType") ?: "",
                    maxParticipants = doc.getLong("maxParticipants")?.toInt(),
                    currentParticipants = (doc.getLong("currentParticipants") ?: 0L).toInt(),
                    isActive = doc.getBoolean("isActive") ?: true,
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    updatedAt = doc.getLong("updatedAt")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createEvent(event: PetEventDto): Result<PetEventDto> {
        return try {
            // Validate organizer exists (foreign key validation)
            if (event.organizerId.isBlank()) {
                return Result.failure(Exception("Organizer ID is required"))
            }

            val organizerDoc = firestore.collection(USERS_COLLECTION)
                .document(event.organizerId)
                .get()
                .await()

            if (!organizerDoc.exists()) {
                return Result.failure(Exception("Organizer user not found: ${event.organizerId}"))
            }

            // Create event document
            val docRef = if (event.id.isBlank()) {
                firestore.collection(EVENTS_COLLECTION).document()
            } else {
                firestore.collection(EVENTS_COLLECTION).document(event.id)
            }

            val now = System.currentTimeMillis()
            val eventData = event.copy(
                id = docRef.id,
                createdAt = now,
                updatedAt = now
            )

            docRef.set(eventData.toMap()).await()
            Result.success(eventData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions for converting DTOs to Firestore maps
private fun PostDto.toMap(): Map<String, Any?> {
    return mapOf(
        "authorId" to authorId,
        "authorName" to authorName,
        "authorAvatarUrl" to authorAvatarUrl,
        "content" to content,
        "imageUrl" to imageUrl,
        "imageUrls" to imageUrls,
        "likesCount" to likesCount,
        "commentsCount" to commentsCount,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "isEdited" to isEdited,
        "tags" to tags
    )
}

private fun PetEventDto.toMap(): Map<String, Any?> {
    return mapOf(
        "organizerId" to organizerId,
        "organizerName" to organizerName,
        "title" to title,
        "description" to description,
        "date" to date,
        "location" to location,
        "imageUrl" to imageUrl,
        "eventType" to eventType,
        "maxParticipants" to maxParticipants,
        "currentParticipants" to currentParticipants,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}


