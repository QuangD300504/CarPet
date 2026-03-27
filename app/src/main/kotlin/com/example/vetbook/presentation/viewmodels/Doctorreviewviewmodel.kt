package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.domain.models.DoctorReview
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.domain.repository.VeterinarianRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ReviewUiState(
    val reviews: List<DoctorReview> = emptyList(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val canReview: Boolean = false,
    val hasAlreadyReviewed: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class DoctorReviewViewModel @Inject constructor(
    private val vetRepository: VeterinarianRepository,
    private val bookingRepository: BookingRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun loadReviews(doctorId: String) {
        val uid = auth.currentUser?.uid
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val reviews = vetRepository.getDoctorReviews(doctorId)

                val hasCompletedAppt = uid != null &&
                    bookingRepository.getUserAppointments(uid).first()
                        .any { it.veterinarianId == doctorId && it.status == "COMPLETED" }

                val alreadyReviewed = uid != null && reviews.any { it.userId == uid }

                _uiState.update {
                    it.copy(
                        reviews = reviews.sortedByDescending { r -> r.createdAt },
                        isLoading = false,
                        canReview = hasCompletedAppt && !alreadyReviewed,
                        hasAlreadyReviewed = alreadyReviewed
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun submitReview(doctorId: String, rating: Int, comment: String) {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(errorMessage = "Vui lòng đăng nhập để đánh giá") }
            return
        }
        if (!_uiState.value.canReview) {
            _uiState.update {
                it.copy(
                    errorMessage = if (_uiState.value.hasAlreadyReviewed)
                        "Bạn đã đánh giá bác sĩ này rồi"
                    else
                        "Chỉ có thể đánh giá sau khi khám xong"
                )
            }
            return
        }
        if (rating == 0) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn số sao") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                val review = DoctorReview(
                    id = "",
                    appointmentId = "",
                    doctorId = doctorId,
                    userId = uid,
                    userName = auth.currentUser?.displayName ?: "Người dùng",
                    rating = rating,
                    comment = comment.trim(),
                    createdAt = Instant.now().toEpochMilli()
                )
                vetRepository.submitReview(review)

                // Recalculate and persist the vet's average rating
                vetRepository.updateDoctorRating(doctorId)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        canReview = false,
                        hasAlreadyReviewed = true,
                        successMessage = "Cảm ơn đánh giá của bạn! ⭐"
                    )
                }
                // Reload so the new review appears in the list
                loadReviews(doctorId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = "Không thể gửi đánh giá: ${e.message}")
                }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null, errorMessage = null) }
}