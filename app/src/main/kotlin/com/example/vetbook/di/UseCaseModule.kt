package com.example.vetbook.di

import com.example.vetbook.di.MockRepo
import com.example.vetbook.domain.interfaces.EmailValidator
import com.example.vetbook.domain.usecases.*
import com.example.vetbook.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideValidateLoginUseCase(emailValidator: EmailValidator): ValidateLoginUseCase {
        return ValidateLoginUseCase(emailValidator)
    }

    @Provides
    fun provideValidateSignUpUseCase(emailValidator: EmailValidator): ValidateSignUpUseCase {
        return ValidateSignUpUseCase(emailValidator)
    }

    @Provides
    fun provideGetCommunityDataUseCase(repository: CommunityRepository): GetCommunityDataUseCase {
        return GetCommunityDataUseCase(repository)
    }

    @Provides
    fun provideGetVeterinariansUseCase(repository: VeterinarianRepository): GetVeterinariansUseCase {
        return GetVeterinariansUseCase(repository)
    }

    @Provides
    fun provideGetServiceCategoriesUseCase(repository: ServiceRepository): GetServiceCategoriesUseCase {
        return GetServiceCategoriesUseCase(repository)
    }

    @Provides
    fun provideGetUserProfileUseCase(repository: UserRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(repository)
    }

    @Provides
    fun provideGetPetProfileUseCase(
        userRepository: UserRepository,
        communityRepository: CommunityRepository
    ): GetPetProfileUseCase {
        return GetPetProfileUseCase(userRepository, communityRepository)
    }
}
