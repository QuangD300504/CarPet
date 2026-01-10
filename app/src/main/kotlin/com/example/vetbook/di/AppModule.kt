package com.example.vetbook.di

import com.example.vetbook.domain.interfaces.EmailValidator
import com.example.vetbook.utils.AndroidEmailValidator
import dagger.Module
import dagger.hilt.InstallIn
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEmailValidator(): EmailValidator {
        return AndroidEmailValidator()
    }
}
