package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.models.DoctorReview
import com.example.vetbook.domain.repository.VeterinarianRepository
import javax.inject.Inject

class SubmitDoctorReviewUseCase @Inject constructor(
    private val repository: VeterinarianRepository
) {
    suspend operator fun invoke(review: DoctorReview): Result<Unit> {
        val result = repository.submitReview(review)
        if (result.isSuccess) {
            repository.updateDoctorRating(review.doctorId)
        }
        return result
    }
}
