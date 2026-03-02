package com.example.vetbook.di

import com.example.vetbook.data.network.PaymentWorkerApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentWorkerModule {

    private const val BASE_URL = "https://vetbook-payment-worker.duyq099.workers.dev/"

    @Provides
    @Singleton
    @PaymentWorkerClient
    fun providePaymentWorkerOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun providePaymentWorkerApi(@PaymentWorkerClient okHttpClient: OkHttpClient): PaymentWorkerApi {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PaymentWorkerApi::class.java)
    }
}
