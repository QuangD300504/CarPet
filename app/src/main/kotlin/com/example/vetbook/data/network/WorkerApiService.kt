package com.example.vetbook.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface WorkerApiService {

    @POST("push/subscribe")
    suspend fun subscribeToPush(
        @Header("Authorization") idToken: String,
        @Body body: SubscribePushBody
    )

    @POST("push/unsubscribe")
    suspend fun unsubscribeFromPush(
        @Header("Authorization") idToken: String
    )

    @POST("push/send-instant")
    suspend fun triggerInstantPush(
        @Header("Authorization") idToken: String,
        @Body body: InstantPushBody
    )
}

data class SubscribePushBody(
    @SerializedName("fcmToken") val fcmToken: String
)

data class InstantPushBody(
    @SerializedName("type") val type: String,
    @SerializedName("refId") val refId: String
)
