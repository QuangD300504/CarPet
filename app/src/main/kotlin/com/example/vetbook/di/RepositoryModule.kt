package com.example.vetbook.di

import com.example.vetbook.data.repository.AuthRepositoryImpl
import com.example.vetbook.data.repository.MockCommunityRepository
import com.example.vetbook.data.repository.MockServiceRepository
import com.example.vetbook.data.repository.MockUserRepository
import com.example.vetbook.data.repository.MockVeterinarianRepository
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.CommunityRepository
import com.example.vetbook.domain.repository.ServiceRepository
import com.example.vetbook.domain.repository.UserRepository
import com.example.vetbook.domain.repository.VeterinarianRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(auth, firestore)
    }

    @Provides
    @Singleton
    fun provideCommunityRepository(): CommunityRepository {
        return MockCommunityRepository()
    }

    @Provides
    @Singleton
    fun provideVeterinarianRepository(): VeterinarianRepository {
        return MockVeterinarianRepository()
    }

    @Provides
    @Singleton
    fun provideServiceRepository(): ServiceRepository {
        return MockServiceRepository()
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return MockUserRepository()
    }
}
