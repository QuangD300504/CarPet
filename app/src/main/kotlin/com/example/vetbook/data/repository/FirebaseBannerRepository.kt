package com.example.vetbook.data.repository

import com.example.vetbook.domain.models.Banner
import com.example.vetbook.domain.repository.BannerRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val BANNERS = "banners"

@Singleton
class FirebaseBannerRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
) : BannerRepository {

    override fun getBanners(): Flow<List<Banner>> = callbackFlow {
        val sub = firestore.collection(BANNERS)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val banners = snap?.documents?.mapNotNull { doc ->
                    val isActive = doc.getBoolean("isActive") ?: true
                    if (!isActive) return@mapNotNull null
                    Banner(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        subtitle = doc.getString("subtitle") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        targetUrl = doc.getString("targetUrl") ?: "",
                        isActive = isActive,
                        sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                }?.sortedBy { it.sortOrder } ?: emptyList()
                trySend(banners)
            }
        awaitClose { sub.remove() }
    }

    override suspend fun getBannerById(id: String): Banner? {
        return try {
            val doc = firestore.collection(BANNERS).document(id).get().await()
            if (!doc.exists()) return null
            Banner(
                id = doc.id,
                title = doc.getString("title") ?: "",
                subtitle = doc.getString("subtitle") ?: "",
                imageUrl = doc.getString("imageUrl") ?: "",
                targetUrl = doc.getString("targetUrl") ?: "",
                isActive = doc.getBoolean("isActive") ?: true,
                sortOrder = doc.getLong("sortOrder")?.toInt() ?: 0,
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) { null }
    }

    override suspend fun addBanner(banner: Banner): Result<Banner> {
        return try {
            val ref = if (banner.id.isBlank()) firestore.collection(BANNERS).document()
            else firestore.collection(BANNERS).document(banner.id)
            val now = System.currentTimeMillis()
            val toSave = banner.copy(id = ref.id, createdAt = now)
            ref.set(toSave).await()
            Result.success(toSave)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateBanner(id: String, fields: Map<String, Any?>) {
        try { firestore.collection(BANNERS).document(id).update(fields).await() } catch (_: Exception) {}
    }

    override suspend fun deleteBanner(id: String) {
        try { firestore.collection(BANNERS).document(id).delete().await() } catch (_: Exception) {}
    }
}
