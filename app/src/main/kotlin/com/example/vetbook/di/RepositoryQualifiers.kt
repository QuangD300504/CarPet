package com.example.vetbook.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MockRepo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteRepo


