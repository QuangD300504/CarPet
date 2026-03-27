package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.models.CartLineDto
import com.example.vetbook.data.models.OrderItemDto
import com.example.vetbook.data.models.StoreOrderDto
import com.example.vetbook.data.models.StoreProductDto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val PRODUCTS_COLLECTION = "products"
private const val USERS_COLLECTION = "users"
private const val CART_SUBCOLLECTION = "cart"
private const val STORE_ORDERS_COLLECTION = "storeOrders"

class FirebaseStoreDataSource(
    private val firestore: FirebaseFirestore
) : RemoteStoreDataSource {

    override fun observeProducts(): Flow<List<StoreProductDto>> = callbackFlow {
        android.util.Log.d("FirebaseStoreDataSource", "observeProducts: starting listener on '$PRODUCTS_COLLECTION'")
        val reg: ListenerRegistration = firestore
            .collection(PRODUCTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseStoreDataSource", "observeProducts: Firestore error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.size ?: 0
                android.util.Log.d("FirebaseStoreDataSource", "observeProducts: received $count documents")
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        doc.toStoreProductDto()
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(items)
            }

        awaitClose { reg.remove() }
    }

    override fun observeProductsByCategory(category: String): Flow<List<StoreProductDto>> = callbackFlow {
        android.util.Log.d("FirebaseStoreDataSource", "observeProductsByCategory('$category'): starting listener")
        val reg: ListenerRegistration = firestore
            .collection(PRODUCTS_COLLECTION)
            .whereEqualTo("category", category)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseStoreDataSource", "observeProductsByCategory: Firestore error: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val count = snapshot?.documents?.size ?: 0
                android.util.Log.d("FirebaseStoreDataSource", "observeProductsByCategory: received $count documents")
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try {
                        doc.toStoreProductDto()
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

    override suspend fun addProduct(product: StoreProductDto): Result<String> {
        return try {
            val docRef = firestore.collection(PRODUCTS_COLLECTION).document()
            val productMap = hashMapOf(
                "name" to product.name,
                "price" to product.price,
                "imageUrl" to product.imageUrl,
                "description" to product.description,
                "category" to product.category,
                "stock" to product.stock,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(productMap).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(product: StoreProductDto): Result<Unit> {
        return try {
            val docRef = firestore.collection(PRODUCTS_COLLECTION).document(product.id)
            val productMap = mapOf(
                "name" to product.name,
                "price" to product.price,
                "imageUrl" to product.imageUrl,
                "description" to product.description,
                "category" to product.category,
                "stock" to product.stock,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.update(productMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            firestore.collection(PRODUCTS_COLLECTION).document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // === Order observation ===
    override fun observeOrders(uid: String): Flow<List<StoreOrderDto>> = callbackFlow {
        val reg: ListenerRegistration = firestore
            .collection(STORE_ORDERS_COLLECTION)
            .whereEqualTo("uid", uid)
            // Removed .orderBy("createdAt") — requires a composite Firestore index that
            // silently returns emptyList() when missing. Sort in-memory instead.
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseStoreDataSource", "observeOrders error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    try { doc.toStoreOrderDto() } catch (_: Exception) { null }
                }.sortedByDescending { it.createdAt } // sort in-memory
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getOrderById(orderId: String): StoreOrderDto? {
        return try {
            firestore.collection(STORE_ORDERS_COLLECTION)
                .document(orderId)
                .get()
                .await()
                ?.toStoreOrderDto()
        } catch (_: Exception) { null }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toStoreProductDto(): StoreProductDto {
        return StoreProductDto(
            id = id,
            name = getString("name") ?: "",
            price = getDouble("price") ?: 0.0,
            imageUrl = getString("imageUrl"),
            description = getString("description"),
            category = getString("category"),
            stock = (getLong("stock") ?: 0L).toInt(),
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toStoreOrderDto(): StoreOrderDto {
        @Suppress("UNCHECKED_CAST")
        val rawItems = get("items") as? List<*> ?: emptyList<Any>()
        val items = rawItems.mapNotNull { item ->
            (item as? Map<*, *>)?.let { map ->
                OrderItemDto(
                    productId = map["productId"] as? String ?: "",
                    productName = map["productName"] as? String ?: "",
                    quantity = (map["quantity"] as? Number)?.toInt() ?: 0,
                    lineTotal = (map["lineTotal"] as? Number)?.toDouble() ?: 0.0
                )
            }
        }
        return StoreOrderDto(
            id = id,
            uid = getString("uid") ?: "",
            orderCode = getString("orderCode") ?: "",
            items = items,
            itemCount = (getLong("itemCount") ?: 0L).toInt(),
            subtotal = getDouble("subtotal") ?: 0.0,
            discount = getDouble("discount") ?: 0.0,
            deliveryCharges = getDouble("deliveryCharges") ?: 0.0,
            total = getDouble("total") ?: 0.0,
            status = getString("status") ?: "PENDING",
            createdAt = getLong("createdAt") ?: 0L
        )
    }
}