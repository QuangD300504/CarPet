package com.example.vetbook.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PayosApiService {
    @POST("v2/payment-requests")
    suspend fun createPaymentLink(
        @Header("x-client-id") clientId: String,
        @Header("x-api-key") apiKey: String,
        @Body request: PayosPaymentRequest
    ): PayosPaymentResponse
}

data class PayosPaymentRequest(
    val orderCode: Long,
    val amount: Int,
    val description: String,
    val cancelUrl: String,
    val returnUrl: String,
    val signature: String
)

data class PayosPaymentResponse(
    val code: String?,
    val desc: String?,
    val data: PayosPaymentData?
)

data class PayosPaymentData(
    val orderCode: Long,
    val amount: Int,
    val description: String,
    val bin: String,
    val accountNumber: String,
    val accountName: String,
    val qrCode: String,
    val checkoutUrl: String,
    val status: String
)
