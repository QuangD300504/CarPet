package com.example.vetbook.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

object CloudinaryConfig {
    const val CLOUD_NAME = "dcxlhzxvv"
    const val UPLOAD_PRESET_VETBOOK = "vetbook"
    const val API_KEY = "327185413894858"
}

interface CloudinaryService {

    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody,
        @Part("api_key") apiKey: RequestBody
    ): Response<CloudinaryUploadResponse>
}

data class CloudinaryUploadResponse(
    val public_id: String,
    val secure_url: String
)

