package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.DoctorReview
import com.example.vetbook.domain.repository.VeterinarianRepository
import javax.inject.Inject

class GetDoctorReviewsUseCase @Inject constructor(
    private val repository: VeterinarianRepository
) {
    suspend operator fun invoke(doctorId: String): List<DoctorReview> {
        return repository.getDoctorReviews(doctorId)
    }
}
