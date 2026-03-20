package com.example.vetbook.di

import com.example.vetbook.data.datasource.RemoteCommunityDataSource
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteServiceDataSource
import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.datasource.RemoteVaccinationDataSource
import com.example.vetbook.data.datasource.RemoteVeterinarianDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseCommunityDataSource
import com.example.vetbook.data.datasource.firebase.FirebasePetDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseServiceDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseStoreDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseUserDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseVaccinationDataSource
import com.example.vetbook.data.datasource.firebase.FirebaseVeterinarianDataSource
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

    @Provides
    @Singleton
    fun provideRemoteVeterinarianDataSource(
        firestore: FirebaseFirestore
    ): RemoteVeterinarianDataSource = FirebaseVeterinarianDataSource(firestore)

    @Provides
    @Singleton
    fun provideRemoteServiceDataSource(
        firestore: FirebaseFirestore
    ): RemoteServiceDataSource = FirebaseServiceDataSource(firestore)

    @Provides
    @Singleton
    fun provideRemoteStoreDataSource(
        firestore: FirebaseFirestore
    ): RemoteStoreDataSource = FirebaseStoreDataSource(firestore)

    @Provides
    @Singleton
    fun provideRemoteAccommodationDataSource(
        firestore: FirebaseFirestore
    ): com.example.vetbook.data.datasource.RemoteAccommodationDataSource =
        com.example.vetbook.data.datasource.firebase.FirebaseAccommodationDataSource(firestore)

    @Provides
    @Singleton
    fun provideNotificationDataSource(
        firestore: FirebaseFirestore
    ): com.example.vetbook.data.datasource.NotificationDataSource =
        com.example.vetbook.data.datasource.firebase.FirebaseNotificationDataSource(firestore)
}