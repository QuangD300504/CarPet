package com.example.vetbook.domain.repository

/**
 * Repository for handling image uploads to a remote storage provider.
 */
interface ImageRepository {
    /**
     * Upload an image to remote storage.
     * @param imageBytes The raw bytes of the image to upload.
     * @return A [Result] containing the secure URL of the uploaded image.
     */
    suspend fun uploadImage(imageBytes: ByteArray): Result<String>
}
