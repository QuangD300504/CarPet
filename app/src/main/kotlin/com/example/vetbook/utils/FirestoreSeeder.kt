package com.example.vetbook.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Seeds Firestore with initial data for VetBook.
 *
 * Active services: cat_vet only.
 * cat_shop routes directly to the Store tab — no service document needed.
 * All other categories (cat_hotel, cat_ride, cat_spa, cat_training,
 * cat_party, cat_funeral) have been removed from the product.
 */
object FirestoreSeeder {

    private const val TAG = "FirestoreSeeder"

    private const val USERS_COLLECTION = "users"
    private const val PETS_COLLECTION = "pets"
    private const val VACCINATIONS_COLLECTION = "vaccinations"
    private const val POSTS_COLLECTION = "posts"
    private const val EVENTS_COLLECTION = "petEvents"
    private const val VETERINARIANS_COLLECTION = "veterinarians"
    private const val SERVICES_COLLECTION = "services"
    private const val PACKAGES_SUBCOLLECTION = "packages"

    suspend fun seedAllData(firestore: FirebaseFirestore) {
        Log.i(TAG, "Starting Firestore seeding...")
        try {
            seedUsers(firestore)
            seedPets(firestore)
            seedVaccinations(firestore)
            seedVeterinarians(firestore)
            seedPosts(firestore)
            seedPetEvents(firestore)
            seedServices(firestore)
            seedServicePackages(firestore)
            Log.i(TAG, "✅ Seeding completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Seeding failed", e)
            throw e
        }
    }

    private suspend fun seedUsers(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding users...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val users = listOf(
            mapOf(
                "uid" to "user_001", "fullName" to "John Pet Parent",
                "email" to "john@email.com", "phone" to "",
                "profileImageUrl" to null, "points" to 230,
                "createdAt" to now, "updatedAt" to now,
                "isEmailVerified" to true, "lastLogin" to now
            ),
            mapOf(
                "uid" to "user_002", "fullName" to "Sarah M.",
                "email" to "sarah@email.com", "phone" to "",
                "profileImageUrl" to null, "points" to 0,
                "createdAt" to now, "updatedAt" to now,
                "isEmailVerified" to true, "lastLogin" to now
            )
        )
        users.forEach { batch.set(firestore.collection(USERS_COLLECTION).document(it["uid"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${users.size} users")
    }

    private suspend fun seedPets(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding pets...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val pets = listOf(
            mapOf(
                "id" to "pet_001", "ownerId" to "user_001",
                "name" to "PiCi", "type" to "Dog", "breed" to "Golden Retriever",
                "imageUrl" to null, "age" to "3 years 6 months", "gender" to "Male",
                "weight" to "28 kg", "parasiticStatus" to "Healthy",
                "note" to "Very energetic and loves to play.",
                "isForAdoption" to false, "createdAt" to now, "updatedAt" to now
            ),
            mapOf(
                "id" to "pet_002", "ownerId" to "user_001",
                "name" to "Bella", "type" to "Cat", "breed" to "Persian",
                "imageUrl" to null, "age" to "2 years 3 months", "gender" to "Female",
                "weight" to "9.5 kg", "parasiticStatus" to "Healthy",
                "note" to "Calm and affectionate.",
                "isForAdoption" to false, "createdAt" to now, "updatedAt" to now
            ),
            mapOf(
                "id" to "adopt_001", "ownerId" to null,
                "name" to "Luna", "type" to "Dog", "breed" to "Labrador",
                "imageUrl" to null, "age" to "2 years", "gender" to "Female",
                "weight" to "22 kg", "parasiticStatus" to "Healthy",
                "note" to "Luna is very friendly and well-trained.",
                "isForAdoption" to true, "createdAt" to now, "updatedAt" to now
            ),
            mapOf(
                "id" to "adopt_002", "ownerId" to null,
                "name" to "Oliver", "type" to "Cat", "breed" to "British Shorthair",
                "imageUrl" to null, "age" to "1 year", "gender" to "Male",
                "weight" to "4.5 kg", "parasiticStatus" to "Healthy",
                "note" to "Oliver is quiet and independent.",
                "isForAdoption" to true, "createdAt" to now, "updatedAt" to now
            )
        )
        pets.forEach { batch.set(firestore.collection(PETS_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${pets.size} pets")
    }

    private suspend fun seedVaccinations(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding vaccinations...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val d15 = now - 15L * 86400000
        val d30 = now - 30L * 86400000
        val d60 = now - 60L * 86400000
        val d90 = now - 90L * 86400000
        val vaccinations = listOf(
            mapOf("id" to "vac_001", "petId" to "pet_001", "veterinarianId" to null, "title" to "5-in-1", "isCompleted" to true, "date" to d15, "notes" to null, "createdAt" to now),
            mapOf("id" to "vac_002", "petId" to "pet_001", "veterinarianId" to null, "title" to "Rabies", "isCompleted" to true, "date" to d15, "notes" to null, "createdAt" to now),
            mapOf("id" to "vac_003", "petId" to "pet_001", "veterinarianId" to null, "title" to "DHPP Booster", "isCompleted" to true, "date" to d60, "notes" to null, "createdAt" to now),
            mapOf("id" to "vac_004", "petId" to "pet_001", "veterinarianId" to null, "title" to "Parasite Prevention", "isCompleted" to false, "date" to null, "notes" to null, "createdAt" to now),
            mapOf("id" to "v1", "petId" to "adopt_001", "veterinarianId" to null, "title" to "Rabies", "isCompleted" to true, "date" to d90, "notes" to null, "createdAt" to now),
            mapOf("id" to "v2", "petId" to "adopt_001", "veterinarianId" to null, "title" to "DHPP", "isCompleted" to true, "date" to d60, "notes" to null, "createdAt" to now),
            mapOf("id" to "v3", "petId" to "adopt_002", "veterinarianId" to null, "title" to "FVRCP", "isCompleted" to true, "date" to d30, "notes" to null, "createdAt" to now),
            mapOf("id" to "v4", "petId" to "adopt_002", "veterinarianId" to null, "title" to "Rabies", "isCompleted" to false, "date" to null, "notes" to null, "createdAt" to now)
        )
        vaccinations.forEach { batch.set(firestore.collection(VACCINATIONS_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${vaccinations.size} vaccinations")
    }

    private suspend fun seedVeterinarians(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding veterinarians...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val vets = listOf(
            mapOf("id" to "vet_001", "name" to "Dr. Sarah Johnson", "specialty" to "Small Animal Medicine", "experience" to "12 years experience", "rating" to 4.8, "reviewsCount" to 120, "initials" to "DSJ", "bio" to "Compassionate veterinarian specializing in the comprehensive care of small household pets.", "imageUrl" to null, "email" to "sarah.johnson@vetbook.com", "phone" to "+1-555-0101", "servicePrice" to 150000.0, "isActive" to true, "createdAt" to now, "updatedAt" to now),
            mapOf("id" to "vet_002", "name" to "Dr. Michael Chen", "specialty" to "Surgery & Emergency Care", "experience" to "15 years experience", "rating" to 4.9, "reviewsCount" to 85, "initials" to "DMC", "bio" to "Highly skilled surgeon dedicated to providing emergency medical services for critical pet cases.", "imageUrl" to null, "email" to "michael.chen@vetbook.com", "phone" to "+1-555-0102", "servicePrice" to 200000.0, "isActive" to true, "createdAt" to now, "updatedAt" to now),
            mapOf("id" to "vet_003", "name" to "Trương Tuấn Tú", "specialty" to "Exotic Animals", "experience" to "36 years experience", "rating" to 4.7, "reviewsCount" to 36, "initials" to "TTT", "bio" to "Expert in treating birds, reptiles, and other exotic pets with gentle and specialized care.", "imageUrl" to null, "email" to "tuan.tu@vetbook.com", "phone" to "+84-555-0103", "servicePrice" to 120000.0, "isActive" to true, "createdAt" to now, "updatedAt" to now),
            mapOf("id" to "vet_004", "name" to "Dr. David Thompson", "specialty" to "Dental Care", "experience" to "10 years experience", "rating" to 4.6, "reviewsCount" to 50, "initials" to "DDT", "bio" to "Specialist in veterinary dentistry, focusing on maintaining optimal oral health for your furry companions.", "imageUrl" to null, "email" to "david.thompson@vetbook.com", "phone" to "+1-555-0104", "servicePrice" to 180000.0, "isActive" to true, "createdAt" to now, "updatedAt" to now)
        )
        vets.forEach { batch.set(firestore.collection(VETERINARIANS_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${vets.size} veterinarians")
    }

    private suspend fun seedPosts(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding posts...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - 2L * 3600000
        val posts = listOf(
            mapOf("id" to "post_001", "authorId" to "user_002", "authorName" to "Sarah M.", "authorAvatarUrl" to null, "content" to "Just adopted the sweetest chi-huahua retriever puppy! Any tips for first-time dog owners? 🐕", "imageUrl" to null, "imageUrls" to null, "likesCount" to 42, "commentsCount" to 15, "createdAt" to twoHoursAgo, "updatedAt" to null, "isEdited" to false, "tags" to null),
            mapOf("id" to "post_002", "authorId" to "user_001", "authorName" to "Mike T.", "authorAvatarUrl" to null, "content" to "Does anyone know a good vet near downtown? My cat needs a checkup 😺", "imageUrl" to null, "imageUrls" to null, "likesCount" to 28, "commentsCount" to 9, "createdAt" to twoHoursAgo, "updatedAt" to null, "isEdited" to false, "tags" to null)
        )
        posts.forEach { batch.set(firestore.collection(POSTS_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${posts.size} posts")
    }

    private suspend fun seedPetEvents(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding pet events...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val events = listOf(
            mapOf("id" to "event_001", "organizerId" to "user_001", "organizerName" to "John Pet Parent", "title" to "Pet Adoption Fair", "description" to "Join us for a community pet adoption event!", "date" to now + 30L * 86400000, "location" to "Central Park, 10:00 AM", "imageUrl" to null, "eventType" to "adoption", "maxParticipants" to 100, "currentParticipants" to 23, "isActive" to true, "createdAt" to now, "updatedAt" to now)
        )
        events.forEach { batch.set(firestore.collection(EVENTS_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${events.size} events")
    }

    /** Only cat_vet is seeded. All other categories have been removed from the product. */
    private suspend fun seedServices(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding services (cat_vet only)...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val services = listOf(
            mapOf(
                "id" to "cat_vet",
                "title" to "Vet care",
                "shortDescription" to "24/7 smart booking with verified clinics",
                "iconUrl" to null,
                "bannerGradientColors" to listOf(4292658409L, 4290870779L),
                "about" to "Professional veterinary care services for your beloved pets.",
                "rating" to 4.8,
                "reviewCount" to 1600,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
        services.forEach { batch.set(firestore.collection(SERVICES_COLLECTION).document(it["id"] as String), it) }
        batch.commit().await()
        Log.i(TAG, "✅ ${services.size} service categories")
    }

    /** Only packages for cat_vet are seeded. */
    private suspend fun seedServicePackages(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding service packages...")
        val now = System.currentTimeMillis()
        val vetPackages = listOf(
            mapOf("id" to "v1", "name" to "General Check-up", "price" to 25.0, "description" to "Comprehensive health examination", "durationMinutes" to 30, "isActive" to true, "createdAt" to now),
            mapOf("id" to "v2", "name" to "Vaccination", "price" to 50.0, "description" to "Standard vaccination package", "durationMinutes" to 20, "isActive" to true, "createdAt" to now),
            mapOf("id" to "v3", "name" to "Deworming & Parasite Control", "price" to 15.0, "description" to "Parasite prevention treatment", "durationMinutes" to 15, "isActive" to true, "createdAt" to now),
            mapOf("id" to "v4", "name" to "Dental Care", "price" to 30.0, "description" to "Dental cleaning and examination", "durationMinutes" to 45, "isActive" to true, "createdAt" to now)
        )
        val batch = firestore.batch()
        vetPackages.forEach { pkg ->
            val ref = firestore.collection(SERVICES_COLLECTION)
                .document("cat_vet")
                .collection(PACKAGES_SUBCOLLECTION)
                .document(pkg["id"] as String)
            batch.set(ref, pkg)
        }
        batch.commit().await()
        Log.i(TAG, "✅ ${vetPackages.size} packages for cat_vet")
    }
}