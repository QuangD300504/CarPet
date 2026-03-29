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

// data class PayosPaymentResponse(
//     val code: String?,
//     val desc: String?,
//     val data: PayosPaymentData?
// )

// data class PayosPaymentData(
//     val orderCode: Long,
//     val amount: Int,
//     val description: String,
//     val bin: String,
//     val accountNumber: String,
//     val accountName: String,
//     val qrCode: String,
//     val checkoutUrl: String,
//     val status: String
// )

data class PayosPaymentResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("desc") val desc: String?,
    @SerializedName("data") val data: PayosPaymentData?
)

data class PayosPaymentData(
    @SerializedName("orderCode") val orderCode: Long,
    @SerializedName("amount") val amount: Int,
    @SerializedName("description") val description: String,
    @SerializedName("bin") val bin: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("qrCode") val qrCode: String,
    @SerializedName("checkoutUrl") val checkoutUrl: String,
    @SerializedName("status") val status: String
)