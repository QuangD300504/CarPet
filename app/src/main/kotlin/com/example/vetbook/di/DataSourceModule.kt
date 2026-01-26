package com.example.vetbook.di

import com.example.vetbook.data.datasource.RemoteCommunityDataSource
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseCommunityDataSource
import com.example.vetbook.data.datasource.firebase.FirebasePetDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseUserDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseVaccinationDataSource
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

    @Provides
    @Singleton
    fun provideRemoteCommunityDataSource(
        firestore: FirebaseFirestore
    ): RemoteCommunityDataSource = FirebaseCommunityDataSource(firestore)

    @Provides
    @Singleton
    fun provideRemoteVaccinationDataSource(
        firestore: FirebaseFirestore
    ): RemoteVaccinationDataSource = FirebaseVaccinationDataSource(firestore)
}


