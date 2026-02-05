package com.example.vetbook.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPetUiState(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,

    val name: String = "",
    val type: String = "Dog",
    val breed: String = "",
    val gender: String = "Male",

    // Real-life sensible defaults: keep optional fields empty unless user fills
    val ageYears: String = "",
    val ageMonths: String = "",
    val weightKg: String = "",

    val parasiticStatus: String = "Healthy",
    val note: String = ""
)

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val remotePetDataSource: RemotePetDataSource,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPetUiState())
    val uiState: StateFlow<AddPetUiState> = _uiState.asStateFlow()

    fun setName(value: String) = _uiState.update { it.copy(name = value) }
    fun setType(value: String) = _uiState.update { it.copy(type = value) }
    fun setBreed(value: String) = _uiState.update { it.copy(breed = value) }
    fun setGender(value: String) = _uiState.update { it.copy(gender = value) }
    fun setAgeYears(value: String) = _uiState.update { it.copy(ageYears = value) }
    fun setAgeMonths(value: String) = _uiState.update { it.copy(ageMonths = value) }
    fun setWeightKg(value: String) = _uiState.update { it.copy(weightKg = value) }
    fun setParasiticStatus(value: String) = _uiState.update { it.copy(parasiticStatus = value) }
    fun setNote(value: String) = _uiState.update { it.copy(note = value) }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun save(onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Not authenticated") }
            return
        }

        val state = _uiState.value

        val name = state.name.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Pet name is required") }
            return
        }

        val breed = state.breed.trim()
        if (breed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Breed is required") }
            return
        }

        val years = state.ageYears.trim().toIntOrNull()
        val months = state.ageMonths.trim().toIntOrNull()
        if (years != null && (years < 0 || years > 40)) {
            _uiState.update { it.copy(errorMessage = "Age (years) must be between 0 and 40") }
            return
        }
        if (months != null && (months < 0 || months > 11)) {
            _uiState.update { it.copy(errorMessage = "Age (months) must be between 0 and 11") }
            return
        }

        val weight = state.weightKg.trim().toDoubleOrNull()
        if (weight != null && (weight <= 0.0 || weight > 120.0)) {
            _uiState.update { it.copy(errorMessage = "Weight (kg) must be between 0 and 120") }
            return
        }

        val ageString = buildAgeString(years, months)
        val weightString = weight?.let { formatWeightKg(it) } ?: ""

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val petDto = PetDto(
                id = "", // auto-id
                ownerId = uid,
                name = name,
                type = state.type,
                breed = breed,
                imageUrl = null,
                age = ageString,
                gender = state.gender,
                weight = weightString,
                parasiticStatus = state.parasiticStatus,
                note = state.note.trim(),
                createdAt = null,
                updatedAt = null,
                isForAdoption = false,
                adoptionDetails = null
            )

            val result = remotePetDataSource.createPet(petDto)
            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = e.message ?: "Failed to save pet"
                        )
                    }
                }
        }
    }

    private fun buildAgeString(years: Int?, months: Int?): String {
        val y = years ?: 0
        val m = months ?: 0
        if (y == 0 && m == 0) return ""

        val parts = mutableListOf<String>()
        if (y > 0) parts.add("$y year" + if (y > 1) "s" else "")
        if (m > 0) parts.add("$m month" + if (m > 1) "s" else "")
        return parts.joinToString(" ")
    }

    private fun formatWeightKg(weightKg: Double): String {
        val trimmed = if (weightKg % 1.0 == 0.0) weightKg.toInt().toString() else weightKg.toString()
        return "$trimmed kg"
    }
}
