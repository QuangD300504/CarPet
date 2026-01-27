package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray) =
        userRepository.updateUserAvatar(imageBytes)
}

