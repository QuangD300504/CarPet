package com.example.vetbook.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PaymentWorkerApi {
    data class CreatePaymentLinkRequest(
        val kind: String, // "APPOINTMENT" or "STORE_ORDER"
        val referenceId: String,
        val appointmentId: String? = null // For compatibility with existing worker
    )
    data class CreatePaymentLinkResponse(
        val checkoutUrl: String,
        val orderCode: Long,
        val paymentLinkId: String?
    )

    @POST("create-payment-link")
    suspend fun createPaymentLink(
        @Header("Authorization") authorization: String,
        @Body body: CreatePaymentLinkRequest
    ): CreatePaymentLinkResponse

    data class CreateVnpayLinkRequest(
        val amount: Long,
        val orderCode: Long,
        val description: String? = null,
        val locale: String? = null
    )
    data class CreateVnpayLinkResponse(
        val url: String
    )

    @POST("vnpay-create-link")
    suspend fun createVnpayLink(
        @Header("Authorization") authorization: String,
        @Body body: CreateVnpayLinkRequest
    ): CreateVnpayLinkResponse
}
