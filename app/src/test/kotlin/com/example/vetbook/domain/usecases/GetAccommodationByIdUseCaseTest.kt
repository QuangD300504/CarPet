package com.example.vetbook.domain.usecases

import com.example.vetbook.domain.repository.AccommodationRepository
import com.example.vetbook.presentation.models.Accommodation
import com.example.vetbook.presentation.models.AccommodationCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetAccommodationByIdUseCaseTest {

    private lateinit var fakeRepository: FakeAccommodationRepository
    private lateinit var useCase: GetAccommodationByIdUseCase

    @Before
    fun setup() {
        fakeRepository = FakeAccommodationRepository()
        useCase = GetAccommodationByIdUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns accommodation when id exists`() = runTest {
        val accommodation = Accommodation(
            id = "1",
            name = "Test Home",
            category = AccommodationCategory.HOMESTAY,
            location = "Location",
            district = "District",
            rating = 4.5f,
            reviewCount = 10,
            price = 100.0,
            description = "Description"
        )
        fakeRepository.addAccommodation(accommodation)

        val result = useCase("1")
        assertEquals(accommodation, result)
    }

    @Test
    fun `invoke returns null when id does not exist`() = runTest {
        val result = useCase("non-existent")
        assertNull(result)
    }

    private class FakeAccommodationRepository : AccommodationRepository {
        private val accommodations = mutableMapOf<String, Accommodation>()

        fun addAccommodation(accommodation: Accommodation) {
            accommodations[accommodation.id] = accommodation
        }

        override suspend fun getAccommodations(): List<Accommodation> = accommodations.values.toList()

        override suspend fun getAccommodationById(id: String): Accommodation? = accommodations[id]
    }
}
