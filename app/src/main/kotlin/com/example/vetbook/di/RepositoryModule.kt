package com.example.vetbook.di

import com.example.vetbook.data.datasource.RemoteCommunityDataSource
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.datasource.RemoteServiceDataSource
import com.example.vetbook.data.datasource.RemoteStoreDataSource
import com.example.vetbook.data.datasource.RemoteUserDataSource
import com.example.vetbook.data.datasource.RemoteVeterinarianDataSource
import com.example.vetbook.data.network.PaymentWorkerApi
import com.example.vetbook.data.repository.*
import com.example.vetbook.domain.repository.*
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
        remoteUserDataSource: RemoteUserDataSource
    ): AuthRepository {
        return AuthRepositoryImpl(auth, remoteUserDataSource)
    }

    @Provides
    @Singleton
    fun provideCommunityRepository(
        remoteCommunityDataSource: RemoteCommunityDataSource
    ): CommunityRepository {
        return FirebaseCommunityRepository(remoteCommunityDataSource)
    }

    @Provides
    @Singleton
    fun provideVeterinarianRepository(
        remoteVeterinarianDataSource: RemoteVeterinarianDataSource
    ): VeterinarianRepository {
        return FirebaseVeterinarianRepository(remoteVeterinarianDataSource)
    }

    @Provides
    @Singleton
    fun provideServiceRepository(
        remoteServiceDataSource: RemoteServiceDataSource
    ): ServiceRepository {
        return FirebaseServiceRepository(remoteServiceDataSource)
    }

    @Provides
    @Singleton
    fun provideStoreRepository(
        remoteStoreDataSource: RemoteStoreDataSource
    ): StoreRepository {
        return FirebaseStoreRepository(remoteStoreDataSource)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        remoteUserDataSource: RemoteUserDataSource,
        remotePetDataSource: RemotePetDataSource,
        cloudinaryService: com.example.vetbook.data.network.CloudinaryService
    ): UserRepository {
        return FirebaseAuthUserRepository(
            auth = auth,
            remoteUserDataSource = remoteUserDataSource,
            remotePetDataSource = remotePetDataSource,
            cloudinaryService = cloudinaryService
        )
    }

    @Provides
    @Singleton
    fun provideBookingRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        paymentWorkerApi: PaymentWorkerApi
    ): BookingRepository {
        return BookingRepositoryImpl(
            firestore = firestore,
            auth = auth,
            paymentWorkerApi = paymentWorkerApi
        )
    }

}
