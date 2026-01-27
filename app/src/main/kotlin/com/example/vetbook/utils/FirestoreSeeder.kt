package com.example.vetbook.utils

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await

/**
 * Utility object to seed Firestore with initial mock data.
 * 
 * This is a ONE-TIME operation that populates the database with:
 * - Users (2 documents)
 * - Pets (4 documents)
 * - Vaccinations (8 documents)
 * - Posts (2 documents)
 * - PetEvents (1 document)
 * - Veterinarians (4 documents)
 * - Services (7 categories + 8 packages)
 * 
 * Usage:
 * ```
 * FirestoreSeeder.seedAllData(FirebaseFirestore.getInstance())
 * ```
 * 
 * CRITICAL: Service document IDs MUST match app's icon mapping.
 * All image URLs are set to null (Cloudinary integration pending).
 */
object FirestoreSeeder {
    
    private const val TAG = "FirestoreSeeder"
    
    // Collection names
    private const val USERS_COLLECTION = "users"
    private const val PETS_COLLECTION = "pets"
    private const val VACCINATIONS_COLLECTION = "vaccinations"
    private const val POSTS_COLLECTION = "posts"
    private const val EVENTS_COLLECTION = "petEvents"
    private const val VETERINARIANS_COLLECTION = "veterinarians"
    private const val SERVICES_COLLECTION = "services"
    private const val PACKAGES_SUBCOLLECTION = "packages"
    
    /**
     * Main entry point to seed all Firestore collections.
     * Executes in the following order to respect foreign key relationships:
     * 1. Users
     * 2. Pets (references ownerId)
     * 3. Vaccinations (references petId)
     * 4. Veterinarians
     * 5. Posts (references authorId)
     * 6. PetEvents (references organizerId)
     * 7. Services + Packages
     */
    suspend fun seedAllData(firestore: FirebaseFirestore) {
        Log.i(TAG, "========================================")
        Log.i(TAG, "Starting Firestore seeding process...")
        Log.i(TAG, "========================================")
        
        try {
            // Seed in order to respect foreign key constraints
            seedUsers(firestore)
            seedPets(firestore)
            seedVaccinations(firestore)
            seedVeterinarians(firestore)
            seedPosts(firestore)
            seedPetEvents(firestore)
            seedServices(firestore)
            seedServicePackages(firestore)
            
            Log.i(TAG, "========================================")
            Log.i(TAG, "✅ Firestore seeding completed successfully!")
            Log.i(TAG, "========================================")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during seeding process", e)
            throw e
        }
    }
    
    /**
     * Seed Users collection (2 documents).
     * IDs: user_001, user_002
     */
    private suspend fun seedUsers(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding users collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        val users = listOf(
            mapOf(
                "uid" to "user_001",
                "fullName" to "John Pet Parent",
                "email" to "john@email.com",
                "phone" to "",
                "profileImageUrl" to null, // Cloudinary integration pending
                "points" to 230,
                "createdAt" to now,
                "updatedAt" to now,
                "isEmailVerified" to true,
                "lastLogin" to now
            ),
            mapOf(
                "uid" to "user_002",
                "fullName" to "Sarah M.",
                "email" to "sarah@email.com",
                "phone" to "",
                "profileImageUrl" to null,
                "points" to 0,
                "createdAt" to now,
                "updatedAt" to now,
                "isEmailVerified" to true,
                "lastLogin" to now
            )
        )
        
        users.forEach { userData ->
            val docRef = firestore.collection(USERS_COLLECTION)
                .document(userData["uid"] as String)
            batch.set(docRef, userData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${users.size} users")
    }
    
    /**
     * Seed Pets collection (4 documents).
     * IDs: pet_001, pet_002, adopt_001, adopt_002
     * Foreign Keys: ownerId references user_001 or null for adoption pets
     */
    private suspend fun seedPets(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding pets collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        val pets = listOf(
            mapOf(
                "id" to "pet_001",
                "ownerId" to "user_001", // Foreign key
                "name" to "PiCi",
                "type" to "Dog",
                "breed" to "Golden Retriever",
                "imageUrl" to null, // Cloudinary integration pending
                "age" to "3 years 6 months",
                "gender" to "Male",
                "weight" to "28 kg",
                "parasiticStatus" to "Healthy",
                "note" to "Very energetic and loves to play. Needs regular exercise.",
                "isForAdoption" to false,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "pet_002",
                "ownerId" to "user_001", // Foreign key
                "name" to "Bella",
                "type" to "Cat",
                "breed" to "Persian",
                "imageUrl" to null,
                "age" to "2 years 3 months",
                "gender" to "Female",
                "weight" to "9.5 kg",
                "parasiticStatus" to "Healthy",
                "note" to "Calm and affectionate. Prefers indoor environment.",
                "isForAdoption" to false,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "adopt_001",
                "ownerId" to null, // Adoption pet (no owner)
                "name" to "Luna",
                "type" to "Dog",
                "breed" to "Labrador",
                "imageUrl" to null,
                "age" to "2 years",
                "gender" to "Female",
                "weight" to "22 kg",
                "parasiticStatus" to "Healthy",
                "note" to "Luna is very friendly and well-trained. She loves long walks and playing fetch.",
                "isForAdoption" to true,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "adopt_002",
                "ownerId" to null, // Adoption pet (no owner)
                "name" to "Oliver",
                "type" to "Cat",
                "breed" to "British Shorthair",
                "imageUrl" to null,
                "age" to "1 year",
                "gender" to "Male",
                "weight" to "4.5 kg",
                "parasiticStatus" to "Healthy",
                "note" to "Oliver is a quiet and independent cat. He enjoys being petted but also likes his space.",
                "isForAdoption" to true,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
        
        pets.forEach { petData ->
            val docRef = firestore.collection(PETS_COLLECTION)
                .document(petData["id"] as String)
            batch.set(docRef, petData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${pets.size} pets")
    }
    
    /**
     * Seed Vaccinations collection (8 documents).
     * Foreign Keys: petId references pets
     */
    private suspend fun seedVaccinations(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding vaccinations collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        // Date calculations (relative to now)
        val fifteenDaysAgo = now - (15L * 24 * 60 * 60 * 1000)
        val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
        val sixtyDaysAgo = now - (60L * 24 * 60 * 60 * 1000)
        val ninetyDaysAgo = now - (90L * 24 * 60 * 60 * 1000)
        
        val vaccinations = listOf(
            // Pet 001 vaccinations
            mapOf(
                "id" to "vac_001",
                "petId" to "pet_001", // Foreign key
                "veterinarianId" to null,
                "title" to "5-in-1",
                "isCompleted" to true,
                "date" to fifteenDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            mapOf(
                "id" to "vac_002",
                "petId" to "pet_001", // Foreign key
                "veterinarianId" to null,
                "title" to "Rabies",
                "isCompleted" to true,
                "date" to fifteenDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            mapOf(
                "id" to "vac_003",
                "petId" to "pet_001", // Foreign key
                "veterinarianId" to null,
                "title" to "DHPP Booster",
                "isCompleted" to true,
                "date" to sixtyDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            mapOf(
                "id" to "vac_004",
                "petId" to "pet_001", // Foreign key
                "veterinarianId" to null,
                "title" to "Parasite Prevention",
                "isCompleted" to false,
                "date" to null,
                "notes" to null,
                "createdAt" to now
            ),
            // Adoption pet 001 vaccinations
            mapOf(
                "id" to "v1",
                "petId" to "adopt_001", // Foreign key
                "veterinarianId" to null,
                "title" to "Rabies",
                "isCompleted" to true,
                "date" to ninetyDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            mapOf(
                "id" to "v2",
                "petId" to "adopt_001", // Foreign key
                "veterinarianId" to null,
                "title" to "DHPP",
                "isCompleted" to true,
                "date" to sixtyDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            // Adoption pet 002 vaccinations
            mapOf(
                "id" to "v3",
                "petId" to "adopt_002", // Foreign key
                "veterinarianId" to null,
                "title" to "FVRCP",
                "isCompleted" to true,
                "date" to thirtyDaysAgo,
                "notes" to null,
                "createdAt" to now
            ),
            mapOf(
                "id" to "v4",
                "petId" to "adopt_002", // Foreign key
                "veterinarianId" to null,
                "title" to "Rabies",
                "isCompleted" to false,
                "date" to null,
                "notes" to null,
                "createdAt" to now
            )
        )
        
        vaccinations.forEach { vacData ->
            val docRef = firestore.collection(VACCINATIONS_COLLECTION)
                .document(vacData["id"] as String)
            batch.set(docRef, vacData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${vaccinations.size} vaccination records")
    }
    
    /**
     * Seed Veterinarians collection (4 documents).
     * IDs: vet_001, vet_002, vet_003, vet_004
     */
    private suspend fun seedVeterinarians(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding veterinarians collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        val veterinarians = listOf(
            mapOf(
                "id" to "vet_001",
                "name" to "Dr. Sarah Johnson",
                "specialty" to "Small Animal Medicine",
                "experience" to "12 years experience",
                "rating" to 4.8,
                "reviewsCount" to 120,
                "initials" to "DSJ",
                "bio" to "Compassionate veterinarian specializing in the comprehensive care of small household pets.",
                "imageUrl" to null, // Cloudinary integration pending
                "email" to "sarah.johnson@vetbook.com",
                "phone" to "+1-555-0101",
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "vet_002",
                "name" to "Dr. Michael Chen",
                "specialty" to "Surgery & Emergency Care",
                "experience" to "15 years experience",
                "rating" to 4.9,
                "reviewsCount" to 85,
                "initials" to "DMC",
                "bio" to "Highly skilled surgeon dedicated to providing emergency medical services for critical pet cases.",
                "imageUrl" to null,
                "email" to "michael.chen@vetbook.com",
                "phone" to "+1-555-0102",
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "vet_003",
                "name" to "Trương Tuấn Tú",
                "specialty" to "Exotic Animals",
                "experience" to "36 years experience",
                "rating" to 4.7,
                "reviewsCount" to 36,
                "initials" to "TTT",
                "bio" to "Expert in treating birds, reptiles, and other exotic pets with gentle and specialized care.",
                "imageUrl" to null,
                "email" to "tuan.tu@vetbook.com",
                "phone" to "+84-555-0103",
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "vet_004",
                "name" to "Dr. David Thompson",
                "specialty" to "Dental Care",
                "experience" to "10 years experience",
                "rating" to 4.6,
                "reviewsCount" to 50,
                "initials" to "DDT",
                "bio" to "Specialist in veterinary dentistry, focusing on maintaining optimal oral health for your furry companions.",
                "imageUrl" to null,
                "email" to "david.thompson@vetbook.com",
                "phone" to "+1-555-0104",
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
        
        veterinarians.forEach { vetData ->
            val docRef = firestore.collection(VETERINARIANS_COLLECTION)
                .document(vetData["id"] as String)
            batch.set(docRef, vetData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${veterinarians.size} veterinarians")
    }
    
    /**
     * Seed Posts collection (2 documents).
     * Foreign Keys: authorId references users
     */
    private suspend fun seedPosts(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding posts collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - (2L * 60 * 60 * 1000)
        
        val posts = listOf(
            mapOf(
                "id" to "post_001",
                "authorId" to "user_002", // Foreign key
                "authorName" to "Sarah M.",
                "authorAvatarUrl" to null,
                "content" to "Just adopted the sweetest chi-huahua retriever puppy! Any tips for first-time dog owners? 🐕",
                "imageUrl" to null, // Cloudinary integration pending
                "imageUrls" to null,
                "likesCount" to 42,
                "commentsCount" to 15,
                "createdAt" to twoHoursAgo,
                "updatedAt" to null,
                "isEdited" to false,
                "tags" to null
            ),
            mapOf(
                "id" to "post_002",
                "authorId" to "user_001", // Foreign key
                "authorName" to "Mike T.",
                "authorAvatarUrl" to null,
                "content" to "Does anyone know a good vet near downtown? My cat needs a checkup 😺",
                "imageUrl" to null,
                "imageUrls" to null,
                "likesCount" to 28,
                "commentsCount" to 9,
                "createdAt" to twoHoursAgo,
                "updatedAt" to null,
                "isEdited" to false,
                "tags" to null
            )
        )
        
        posts.forEach { postData ->
            val docRef = firestore.collection(POSTS_COLLECTION)
                .document(postData["id"] as String)
            batch.set(docRef, postData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${posts.size} posts")
    }
    
    /**
     * Seed PetEvents collection (1 document).
     * Foreign Keys: organizerId references users
     */
    private suspend fun seedPetEvents(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding petEvents collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        // Event date: 30 days from now
        val eventDate = now + (30L * 24 * 60 * 60 * 1000)
        
        val events = listOf(
            mapOf(
                "id" to "event_001",
                "organizerId" to "user_001", // Foreign key
                "organizerName" to "John Pet Parent",
                "title" to "Pet Adoption Fair",
                "description" to "Join us for a community pet adoption event!",
                "date" to eventDate,
                "location" to "Central Park, 10:00 AM",
                "imageUrl" to null, // Cloudinary integration pending
                "eventType" to "adoption",
                "maxParticipants" to 100,
                "currentParticipants" to 23,
                "isActive" to true,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
        
        events.forEach { eventData ->
            val docRef = firestore.collection(EVENTS_COLLECTION)
                .document(eventData["id"] as String)
            batch.set(docRef, eventData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${events.size} pet events")
    }
    
    /**
     * Seed Services collection (7 category documents).
     * 
     * ⚠️ CRITICAL: Document IDs MUST match app's icon mapping!
     * IDs: cat_vet, cat_hotel, cat_spa, cat_ride, cat_training, cat_party, cat_funeral
     */
    private suspend fun seedServices(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding services collection...")
        val batch = firestore.batch()
        val now = System.currentTimeMillis()
        
        val services = listOf(
            mapOf(
                "id" to "cat_vet", // EXACT ID - DO NOT CHANGE
                "title" to "Vet care",
                "shortDescription" to "24/7 smart booking with verified clinics",
                "iconUrl" to null, // Uses R.drawable.checkup
                "bannerGradientColors" to listOf(4292658409L, 4290870779L),
                "about" to "Professional veterinary care services for your beloved pets.",
                "rating" to 4.8,
                "reviewCount" to 1600,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_hotel", // EXACT ID - DO NOT CHANGE
                "title" to "Stay & Care",
                "shortDescription" to "Safe and comfortable stay for your pets",
                "iconUrl" to null, // Uses R.drawable.hotel
                "bannerGradientColors" to listOf(4293584894L, 4293713149L),
                "about" to "Premium pet boarding and accommodation services.",
                "rating" to 4.5,
                "reviewCount" to 1100,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_spa", // EXACT ID - DO NOT CHANGE
                "title" to "Pamper",
                "shortDescription" to "Deep cleaning and styling by certified experts",
                "iconUrl" to null, // Uses R.drawable.groom
                "bannerGradientColors" to listOf(4294958258L, 4292798152L),
                "about" to "Professional pet grooming and spa services.",
                "rating" to 4.3,
                "reviewCount" to 1200,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_ride", // EXACT ID - DO NOT CHANGE
                "title" to "Pet Ride",
                "shortDescription" to "Safe transportation for your pets",
                "iconUrl" to null, // Uses R.drawable.homecare
                "bannerGradientColors" to emptyList<Long>(),
                "about" to "Reliable pet transportation services.",
                "rating" to 4.0,
                "reviewCount" to 0,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_training", // EXACT ID - DO NOT CHANGE
                "title" to "Training",
                "shortDescription" to "Professional pet training services",
                "iconUrl" to null, // Uses R.drawable.checkup
                "bannerGradientColors" to emptyList<Long>(),
                "about" to "Expert pet training and behavior modification.",
                "rating" to 0.0,
                "reviewCount" to 0,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_party", // EXACT ID - DO NOT CHANGE
                "title" to "Party",
                "shortDescription" to "Pet party and celebration services",
                "iconUrl" to null, // Uses R.drawable.hotel
                "bannerGradientColors" to emptyList<Long>(),
                "about" to "Pet party planning and celebration services.",
                "rating" to 0.0,
                "reviewCount" to 0,
                "createdAt" to now,
                "updatedAt" to now
            ),
            mapOf(
                "id" to "cat_funeral", // EXACT ID - DO NOT CHANGE
                "title" to "Funeral",
                "shortDescription" to "Pet funeral and memorial services",
                "iconUrl" to null, // Uses R.drawable.groom
                "bannerGradientColors" to emptyList<Long>(),
                "about" to "Dignified pet funeral and memorial services.",
                "rating" to 0.0,
                "reviewCount" to 0,
                "createdAt" to now,
                "updatedAt" to now
            )
        )
        
        services.forEach { serviceData ->
            val docRef = firestore.collection(SERVICES_COLLECTION)
                .document(serviceData["id"] as String) // USE EXACT ID
            batch.set(docRef, serviceData)
        }
        
        batch.commit().await()
        Log.i(TAG, "✅ Seeded ${services.size} service categories")
    }
    
    /**
     * Seed Service Packages (subcollections under services).
     * 
     * Subcollection paths:
     * - services/cat_vet/packages (4 packages)
     * - services/cat_spa/packages (4 packages)
     * - services/cat_hotel/packages (0 packages - reuses spa packages in inventory)
     * 
     * Note: Each service category can have its own packages.
     */
    private suspend fun seedServicePackages(firestore: FirebaseFirestore) {
        Log.i(TAG, "Seeding service packages (subcollections)...")
        val now = System.currentTimeMillis()
        var totalPackages = 0
        
        // Packages for cat_vet
        val vetPackages = listOf(
            mapOf(
                "id" to "v1",
                "name" to "General Check-up",
                "price" to 25.0,
                "description" to "Comprehensive health examination",
                "durationMinutes" to 30,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "v2",
                "name" to "Vaccination",
                "price" to 50.0,
                "description" to "Standard vaccination package",
                "durationMinutes" to 20,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "v3",
                "name" to "Deworming & Parasite Control",
                "price" to 15.0,
                "description" to "Parasite prevention treatment",
                "durationMinutes" to 15,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "v4",
                "name" to "Dental Care",
                "price" to 30.0,
                "description" to "Dental cleaning and examination",
                "durationMinutes" to 45,
                "isActive" to true,
                "createdAt" to now
            )
        )
        
        // Seed cat_vet packages
        val vetBatch = firestore.batch()
        vetPackages.forEach { packageData ->
            val docRef = firestore.collection(SERVICES_COLLECTION)
                .document("cat_vet")
                .collection(PACKAGES_SUBCOLLECTION)
                .document(packageData["id"] as String)
            vetBatch.set(docRef, packageData)
        }
        vetBatch.commit().await()
        totalPackages += vetPackages.size
        Log.i(TAG, "  ✅ Seeded ${vetPackages.size} packages for cat_vet")
        
        // Packages for cat_spa
        val spaPackages = listOf(
            mapOf(
                "id" to "s1",
                "name" to "Basic Bath",
                "price" to 25.0,
                "description" to "Basic bathing service",
                "durationMinutes" to 30,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "s2",
                "name" to "Full Grooming",
                "price" to 50.0,
                "description" to "Complete grooming package",
                "durationMinutes" to 60,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "s3",
                "name" to "Nail Trim",
                "price" to 15.0,
                "description" to "Nail trimming service",
                "durationMinutes" to 15,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "s4",
                "name" to "Teeth Cleaning",
                "price" to 30.0,
                "description" to "Dental cleaning for pets",
                "durationMinutes" to 30,
                "isActive" to true,
                "createdAt" to now
            )
        )
        
        // Seed cat_spa packages
        val spaBatch = firestore.batch()
        spaPackages.forEach { packageData ->
            val docRef = firestore.collection(SERVICES_COLLECTION)
                .document("cat_spa")
                .collection(PACKAGES_SUBCOLLECTION)
                .document(packageData["id"] as String)
            spaBatch.set(docRef, packageData)
        }
        spaBatch.commit().await()
        totalPackages += spaPackages.size
        Log.i(TAG, "  ✅ Seeded ${spaPackages.size} packages for cat_spa")
        
        // For cat_hotel, we could add packages here if needed
        // Currently using the same structure as spa packages per inventory
        val hotelPackages = listOf(
            mapOf(
                "id" to "h1",
                "name" to "Basic Stay",
                "price" to 35.0,
                "description" to "Basic overnight stay with feeding",
                "durationMinutes" to null,
                "isActive" to true,
                "createdAt" to now
            ),
            mapOf(
                "id" to "h2",
                "name" to "Premium Suite",
                "price" to 75.0,
                "description" to "Premium accommodation with play time",
                "durationMinutes" to null,
                "isActive" to true,
                "createdAt" to now
            )
        )
        
        val hotelBatch = firestore.batch()
        hotelPackages.forEach { packageData ->
            val docRef = firestore.collection(SERVICES_COLLECTION)
                .document("cat_hotel")
                .collection(PACKAGES_SUBCOLLECTION)
                .document(packageData["id"] as String)
            hotelBatch.set(docRef, packageData)
        }
        hotelBatch.commit().await()
        totalPackages += hotelPackages.size
        Log.i(TAG, "  ✅ Seeded ${hotelPackages.size} packages for cat_hotel")
        
        Log.i(TAG, "✅ Seeded total $totalPackages service packages")
    }
}
