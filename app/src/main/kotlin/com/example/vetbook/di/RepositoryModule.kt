package com.example.vetbook.di

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.repository.*
import com.example.vetbook.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
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
        remoteUserDataSource: RemoteUserDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(auth, remoteUserDataSource)
    }

    @Provides
    @Singleton
    @MockRepo
    fun provideMockCommunityRepository(): CommunityRepository =
        MockCommunityRepository()

    @Provides
    @Singleton
    @MockRepo
    fun provideVeterinarianRepository(): VeterinarianRepository =
        MockVeterinarianRepository()

    @Provides
    @Singleton
    @MockRepo
    fun provideMockServiceRepository(): ServiceRepository =
        MockServiceRepository()

    @Provides
    @Singleton
    fun provideServiceRepository(): ServiceRepository =
        MockServiceRepository()

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        remoteUserDataSource: RemoteUserDataSource,
        remotePetDataSource: RemotePetDataSource
    ): UserRepository {
        return FirebaseAuthUserRepository(auth, remoteUserDataSource, remotePetDataSource)
    }
}
