package com.example.vetbook.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CloudinaryClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymentWorkerClient
