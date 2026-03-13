package com.example.vetbook.appointment

import com.example.vetbook.data.datasource.RemotePetDataSource
import com.example.vetbook.data.models.PetDto
import com.example.vetbook.domain.models.Appointment
import com.example.vetbook.domain.models.PaymentLink
import com.example.vetbook.domain.repository.AuthRepository
import com.example.vetbook.domain.repository.BookingRepository
import com.example.vetbook.presentation.viewmodels.BookAppointmentViewModel
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class BookAppointmentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeBookingRepo: FakeBookingRepository
    private lateinit var fakePetDS: FakeRemotePetDataSource
    private lateinit var fakeAuthRepo: FakeAuthRepository
    private lateinit var viewModel: BookAppointmentViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeBookingRepo = FakeBookingRepository()
        fakePetDS = FakeRemotePetDataSource()
        fakeAuthRepo = FakeAuthRepository("user-123")
        
        // Setup initial pets
        fakePetDS.pets = listOf(
            PetDto(id = "pet-1", name = "Buddy", type = "Dog", ownerId = "user-123")
        )
        
        viewModel = BookAppointmentViewModel(fakeBookingRepo, fakePetDS, fakeAuthRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads user pets`() = runTest {
        advanceUntilIdle()
        assertEquals(1, viewModel.pets.value.size)
    }

    // Fakes
    private class FakeBookingRepository : BookingRepository {
        override suspend fun createAppointmentWithSlotLock(
            veterinarianId: String,
            appointmentAt: Date,
            totalPrice: Double,
            durationMinutes: Int,
            notes: String?,
            petId: String?
        ): BookingRepository.CreateAppointmentResult = BookingRepository.CreateAppointmentResult("appt-1", "lock-1")

        override suspend fun createPaymentLinkForAppointment(appointmentId: String): PaymentLink =
            PaymentLink(checkoutUrl = "https://checkout.url", orderCode = 123L, paymentLinkId = "pay-1")

        override suspend fun cancelAppointment(appointmentId: String, lockId: String) {}
        override fun getUserAppointments(userId: String): Flow<List<Appointment>> = MutableStateFlow(emptyList())
        override fun getAllAppointments(): Flow<List<Appointment>> = MutableStateFlow(emptyList())
        override suspend fun getLockedSlots(
        veterinarianId: String,
        year: Int,
        month: Int,
        day: Int
    ): Set<String> {
        return emptySet() // Return empty set for tests unless otherwise mocked
    }

    override suspend fun markAppointmentAsPaid(appointmentId: String) {
        // Do nothing in mock
    }
    }

    private class FakeRemotePetDataSource : RemotePetDataSource {
        var pets = emptyList<PetDto>()
        override suspend fun getUserPets(ownerId: String): List<PetDto> = pets
        override suspend fun getAdoptionPets(): List<PetDto> = emptyList()
        override suspend fun createPet(pet: PetDto): Result<PetDto> = Result.success(pet)
        override suspend fun updatePet(pet: PetDto): Result<Unit> = Result.success(Unit)
        override suspend fun deletePet(petId: String): Result<Unit> = Result.success(Unit)
        override suspend fun getPetById(petId: String): Result<PetDto> = Result.success(PetDto(id = petId))
    }

    private class FakeAuthRepository(private val userId: String?) : AuthRepository {
        override suspend fun signUp(fullName: String, email: String, phone: String, password: String): Result<AuthResult> = Result.failure(Exception())
        override suspend fun login(email: String, password: String): Result<AuthResult> = Result.failure(Exception())
        override suspend fun signInWithGoogle(context: android.content.Context): Result<AuthResult> = Result.failure(Exception())
        override suspend fun signOut(): Result<Unit> = Result.success(Unit)
        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
        override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)
        override suspend fun isEmailVerified(): Boolean = true
        override suspend fun reloadUser(): Result<Unit> = Result.success(Unit)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override fun getCurrentUser(): FirebaseUser? = null
        override fun isUserLoggedIn(): Boolean = userId != null
        override fun getCurrentUserId(): String? = userId
        override fun getAuthState(): Flow<FirebaseUser?> = MutableStateFlow(null)
    }
}
