package com.example.vetbook.data.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface PayosWorkerApi {
    data class CreatePaymentLinkRequest(val appointmentId: String)
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
}
