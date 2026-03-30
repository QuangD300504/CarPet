package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.repository.ClinicRepository
import com.example.vetbook.domain.models.Clinic
import com.example.vetbook.domain.models.DoctorReview
import com.example.vetbook.domain.usecases.GetDoctorReviewsUseCase
import com.example.vetbook.domain.usecases.GetVeterinariansUseCase
import com.example.vetbook.domain.usecases.SubmitDoctorReviewUseCase
import com.example.vetbook.presentation.models.VeterinariansUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VeterinariansViewModel @Inject constructor(
    private val getVeterinariansUseCase: GetVeterinariansUseCase,
    private val clinicRepository: ClinicRepository,
    private val getDoctorReviewsUseCase: GetDoctorReviewsUseCase,
    private val submitDoctorReviewUseCase: SubmitDoctorReviewUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(VeterinariansUiState())
    val uiState: StateFlow<VeterinariansUiState> = _uiState.asStateFlow()

    private val _clinic = MutableStateFlow<Clinic?>(null)
    val clinic: StateFlow<Clinic?> = _clinic.asStateFlow()

    private val _reviews = MutableStateFlow<List<DoctorReview>>(emptyList())
    val reviews: StateFlow<List<DoctorReview>> = _reviews.asStateFlow()

    private val _reviewMessage = MutableStateFlow<String?>(null)
    val reviewMessage: StateFlow<String?> = _reviewMessage

    private val currentUserId: String? get() = auth.currentUser?.uid
    private val currentUserName: String get() = auth.currentUser?.displayName ?: "Người dùng"

    private var loadingJob: Job? = null

    init {
        loadVeterinarians()
    }

    override fun onCleared() {
        super.onCleared()
        loadingJob?.cancel()
    }

    private fun loadVeterinarians() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getVeterinariansUseCase().collect { vets ->
                    // Duyệt qua từng bác sĩ để lấy danh sách reviews "live"
                    val vetsWithReviews = vets.map { vet ->
                        val doctorReviews = getDoctorReviewsUseCase(vet.id)
                        // Gán danh sách reviews vào model Veterinarian
                        vet.copy(reviews = doctorReviews)
                    }

                    _uiState.update {
                        it.copy(
                            veterinarians = vetsWithReviews,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load veterinarians"
                    )
                }
            }
        }
    }

    /** Called from DoctorProfileScreen when a doctor is resolved. */
    fun fetchClinic(clinicId: String) {
        if (clinicId.isBlank()) {
            _clinic.value = null
            return
        }
        // Don't refetch if we already have the correct clinic
        if (_clinic.value?.id == clinicId) return
        viewModelScope.launch {
            _clinic.value = clinicRepository.getClinicById(clinicId)
        }
    }

    fun loadReviews(doctorId: String) {
        viewModelScope.launch {
            _reviews.value = getDoctorReviewsUseCase(doctorId)
        }
    }

    fun submitReview(review: DoctorReview) {
        viewModelScope.launch {
            val reviewWithUser = review.copy(
                userId = review.userId.ifBlank { currentUserId ?: "" },
                userName = if (review.userName == "Người dùng") currentUserName else review.userName
            )
            val result = submitDoctorReviewUseCase(reviewWithUser)
            if (result.isSuccess) {
                _reviewMessage.value = "Đã gửi đánh giá"
                // Reload reviews so the new one appears immediately
                loadReviews(review.doctorId)
            } else {
                _reviewMessage.value = "Không thể gửi đánh giá: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearReviewMessage() {
        _reviewMessage.value = null
    }
}
