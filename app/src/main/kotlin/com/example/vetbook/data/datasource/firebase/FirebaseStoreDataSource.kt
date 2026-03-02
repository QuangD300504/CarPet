package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.models.CartLineDto
import com.example.vetbook.data.models.StoreProductDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val PRODUCTS_COLLECTION = "products"
private const val USERS_COLLECTION = "users"
private const val CART_SUBCOLLECTION = "cart"

class FirebaseStoreDataSource(
    private val firestore: FirebaseFirestore
) : RemoteStoreDataSource {

    override fun observeProducts(): Flow<List<StoreProductDto>> = callbackFlow {
        val reg: ListenerRegistration = firestore
            .collection(PRODUCTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        StoreProductDto(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl"),
                            description = doc.getString("description"),
                            category = doc.getString("category"),
                            createdAt = doc.getLong("createdAt"),
                            updatedAt = doc.getLong("updatedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(items)
            }

        awaitClose { reg.remove() }
    }

    override fun observeProductsByCategory(category: String): Flow<List<StoreProductDto>> = callbackFlow {
        val reg: ListenerRegistration = firestore
            .collection(PRODUCTS_COLLECTION)
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        StoreProductDto(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            price = doc.getDouble("price") ?: 0.0,
                            imageUrl = doc.getString("imageUrl"),
                            description = doc.getString("description"),
                            category = doc.getString("category"),
                            createdAt = doc.getLong("createdAt"),
                            updatedAt = doc.getLong("updatedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(items)
            }

        awaitClose { reg.remove() }
    }

    override fun observeUserCart(uid: String): Flow<List<CartLineDto>> = callbackFlow {
        val reg: ListenerRegistration = firestore
            .collection(USERS_COLLECTION)
            .document(uid)
            .collection(CART_SUBCOLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val lines = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        CartLineDto(
                            productId = doc.id,
                            quantity = (doc.getLong("quantity") ?: 0L).toInt(),
                            addedAt = doc.getLong("addedAt")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(lines)
            }

        awaitClose { reg.remove() }
    }

    override suspend fun setCartQuantity(uid: String, productId: String, quantity: Int): Result<Unit> {
        return try {
            val docRef = firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .collection(CART_SUBCOLLECTION)
                .document(productId)

            if (quantity <= 0) {
                docRef.delete().await()
            } else {
                val now = System.currentTimeMillis()
                docRef.set(
                    hashMapOf(
                        "productId" to productId,
                        "quantity" to quantity,
                        "addedAt" to now
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCart(uid: String): Result<Unit> {
        return try {
            val cartRef = firestore
                .collection(USERS_COLLECTION)
                .document(uid)
                .collection(CART_SUBCOLLECTION)

            val docs = cartRef.get().await()
            if (docs.isEmpty) return Result.success(Unit)

            val batch = firestore.batch()
            docs.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


