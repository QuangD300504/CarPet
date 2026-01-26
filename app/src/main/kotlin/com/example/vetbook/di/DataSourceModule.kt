package com.example.vetbook.di

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.datasource.firebase.FirebasePetDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseUserDataSource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideRemoteUserDataSource(
        firestore: FirebaseFirestore
    ): RemoteUserDataSource = FirebaseUserDataSource(firestore)

    @Provides
    @Singleton
    fun provideRemotePetDataSource(
        firestore: FirebaseFirestore
    ): RemotePetDataSource = FirebasePetDataSource(firestore)
}


