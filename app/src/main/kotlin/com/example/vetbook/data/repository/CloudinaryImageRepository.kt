package com.example.vetbook.data.repository

import android.util.Log
import com.example.vetbook.data.network.CloudinaryConfig
import com.example.vetbook.data.network.CloudinaryService
import com.example.vetbook.domain.repository.ImageRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * Implementation of [ImageRepository] that uploads images to Cloudinary.
 */
class CloudinaryImageRepository @Inject constructor(
    private val cloudinaryService: CloudinaryService
) : ImageRepository {

    override suspend fun uploadImage(imageBytes: ByteArray): Result<String> {
        return try {
            val mediaType = "image/*".toMediaType()
            val requestFile = imageBytes.toRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = "upload_${System.currentTimeMillis()}.jpg",
                body = requestFile
            )

            val preset = CloudinaryConfig.UPLOAD_PRESET_VETBOOK
                .toRequestBody("text/plain".toMediaType())
            val apiKey = CloudinaryConfig.API_KEY
                .toRequestBody("text/plain".toMediaType())

            val response = cloudinaryService.uploadImage(filePart, preset, apiKey)
            if (response.isSuccessful && response.body() != null) {
                val url = response.body()!!.secure_url
                Log.d("CloudinaryRepo", "Upload success: $url")
                Result.success(url)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("CloudinaryRepo", "Upload failed: $errorBody")
                Result.failure(IllegalStateException("Cloudinary error: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("CloudinaryRepo", "Exception during upload", e)
            Result.failure(e)
        }
    }
}
