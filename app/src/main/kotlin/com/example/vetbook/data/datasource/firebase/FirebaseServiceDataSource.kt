package com.example.vetbook.data.datasource.firebase

import com.example.vetbook.data.datasource.RemoteServiceDataSource
import com.example.vetbook.data.models.PetServiceDetailDto
import com.example.vetbook.data.models.ServiceCategoryDto
import com.example.vetbook.data.models.ServicePackageDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val SERVICES_COLLECTION = "services"
private const val PACKAGES_SUBCOLLECTION = "packages"

/**
 * Firebase Firestore implementation of RemoteServiceDataSource.
 * Manages service categories in the `services` collection and 
 * service packages in the `services/{categoryId}/packages` subcollection.
 */
class FirebaseServiceDataSource(
    private val firestore: FirebaseFirestore
) : RemoteServiceDataSource {

    override suspend fun getServiceCategories(): List<ServiceCategoryDto> {
        return try {
            val snapshot = firestore
                .collection(SERVICES_COLLECTION)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toServiceCategoryDto()
                } catch (e: Exception) {
                    null // Skip malformed documents
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getServiceCategoryById(categoryId: String): ServiceCategoryDto? {
        return try {
            val snapshot = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .get()
                .await()

            if (!snapshot.exists()) return null
            snapshot.toServiceCategoryDto()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getServicePackages(categoryId: String): List<ServicePackageDto> {
        return try {
            val snapshot = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .collection(PACKAGES_SUBCOLLECTION)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toServicePackageDto()
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getServiceDetail(categoryId: String): PetServiceDetailDto? {
        return try {
            // Fetch the service category
            val categoryDoc = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .get()
                .await()

            if (!categoryDoc.exists()) return null

            // Fetch packages from subcollection
            val packagesSnapshot = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .collection(PACKAGES_SUBCOLLECTION)
                .get()
                .await()

            val packages = packagesSnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toServicePackageDto()
                } catch (e: Exception) {
                    null
                }
            }

            // Construct PetServiceDetailDto
            PetServiceDetailDto(
                categoryId = categoryId,
                rating = categoryDoc.getDouble("rating") ?: 0.0,
                reviewCount = categoryDoc.getLong("reviewCount")?.toString() ?: "0",
                about = categoryDoc.getString("about") ?: "",
                packages = packages,
                availableTimes = (categoryDoc.get("availableTimes") as? List<*>)
                    ?.mapNotNull { it as? String } ?: emptyList(),
                bannerGradientColors = (categoryDoc.get("bannerGradientColors") as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createServiceCategory(service: ServiceCategoryDto): Result<ServiceCategoryDto> {
        return try {
            if (service.id.isBlank()) {
                return Result.failure(
                    IllegalArgumentException("Service category id must be provided (uses custom id, not auto-generated)")
                )
            }

            val docRef = firestore.collection(SERVICES_COLLECTION).document(service.id)

            val now = System.currentTimeMillis()
            val serviceWithTimestamps = service.copy(
                id = docRef.id,
                createdAt = service.createdAt ?: now,
                updatedAt = now
            )

            docRef.set(serviceWithTimestamps.toMap()).await()
            Result.success(serviceWithTimestamps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createServicePackage(
        categoryId: String,
        `package`: ServicePackageDto
    ): Result<ServicePackageDto> {
        return try {
            // Verify parent category exists
            val categoryExists = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .get()
                .await()
                .exists()

            if (!categoryExists) {
                return Result.failure(
                    IllegalArgumentException("Service category not found: $categoryId")
                )
            }

            // Packages/products should use auto-generated document IDs.
            // We ignore any client-provided id to keep the source of truth in Firestore.
            val docRef = firestore
                .collection(SERVICES_COLLECTION)
                .document(categoryId)
                .collection(PACKAGES_SUBCOLLECTION)
                .document()

            val now = System.currentTimeMillis()
            val packageWithTimestamps = `package`.copy(
                id = docRef.id,
                createdAt = `package`.createdAt ?: now
            )

            docRef.set(packageWithTimestamps.toMap()).await()
            Result.success(packageWithTimestamps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extension function to convert Firestore DocumentSnapshot to ServiceCategoryDto.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toServiceCategoryDto(): ServiceCategoryDto {
        return ServiceCategoryDto(
            id = id,
            title = getString("title") ?: "",
            shortDescription = getString("shortDescription") ?: "",
            iconUrl = getString("iconUrl"),
            bannerGradientColors = (get("bannerGradientColors") as? List<*>)
                ?.mapNotNull { (it as? Number)?.toLong() } ?: emptyList(),
            about = getString("about") ?: "",
            rating = getDouble("rating") ?: 0.0,
            reviewCount = getLong("reviewCount")?.toInt() ?: 0,
            createdAt = getLong("createdAt"),
            updatedAt = getLong("updatedAt")
        )
    }

    /**
     * Extension function to convert Firestore DocumentSnapshot to ServicePackageDto.
     */
    private fun com.google.firebase.firestore.DocumentSnapshot.toServicePackageDto(): ServicePackageDto {
        return ServicePackageDto(
            id = id,
            name = getString("name") ?: "",
            price = getDouble("price") ?: 0.0,
            description = getString("description") ?: "",
            durationMinutes = getLong("durationMinutes")?.toInt(),
            isActive = getBoolean("isActive") ?: true,
            createdAt = getLong("createdAt")
        )
    }

    /**
     * Convert ServiceCategoryDto to Firestore-compatible Map.
     */
    private fun ServiceCategoryDto.toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "title" to title,
            "shortDescription" to shortDescription,
            "iconUrl" to iconUrl,
            "bannerGradientColors" to bannerGradientColors,
            "about" to about,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    /**
     * Convert ServicePackageDto to Firestore-compatible Map.
     */
    private fun ServicePackageDto.toMap(): Map<String, Any?> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "price" to price,
            "description" to description,
            "durationMinutes" to durationMinutes,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }
}
