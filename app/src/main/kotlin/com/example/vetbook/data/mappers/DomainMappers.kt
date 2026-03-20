package com.example.vetbook.data.mappers

import com.example.vetbook.data.models.*
import com.example.vetbook.domain.models.*

// region User

fun UserProfileDto.toDomain(
    points: Int = 0,
    profileImage: Int? = null
): User =
    User(
        id = uid,
        name = fullName,
        email = email,
        points = points,
        profileImageUrl = profileImageUrl,
        profileImage = profileImage,
        isAdmin = isAdmin
    )

// endregion

// region Store

fun StoreProductDto.toDomain(): StoreProduct =
    StoreProduct(
        id = id,
        name = name,
        price = price,
        imageUrl = imageUrl,
        description = description,
        category = category,
        stock = stock,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun CartLineDto.toDomain(): CartLine =
    CartLine(
        productId = productId,
        quantity = quantity,
        addedAt = addedAt
    )

// endregion

// region Store Orders

fun StoreOrderDto.toDomain(): StoreOrder = StoreOrder(
    id = id,
    uid = uid,
    orderCode = orderCode,
    items = items.map { it.toDomain() },
    itemCount = itemCount,
    subtotal = subtotal,
    discount = discount,
    deliveryCharges = deliveryCharges,
    total = total,
    status = com.example.vetbook.domain.models.OrderStatus.fromString(status),
    createdAt = createdAt
)

fun OrderItemDto.toDomain(): com.example.vetbook.domain.models.OrderItem =
    com.example.vetbook.domain.models.OrderItem(
        productId = productId,
        productName = productName,
        quantity = quantity,
        lineTotal = lineTotal
    )

// endregion

// region Pets

fun PetDto.toDomain(vaccinations: List<Vaccination> = emptyList()): Pet =
    Pet(
        id = id,
        ownerId = ownerId,
        name = name,
        type = type,
        breed = breed,
        imageRes = null, // resolved in UI layer if needed
        age = age,
        gender = gender,
        weight = weight,
        parasiticStatus = parasiticStatus,
        note = note,
        realImgUrl = imageUrl,
        vaccinations = vaccinations
    )

// endregion

// region Vaccinations

fun VaccinationRecordDto.toDomain(): Vaccination {
    return Vaccination(
        id = id,
        petId = petId,
        veterinarianId = veterinarianId,
        veterinarianName = veterinarianName,
        clinicName = clinicName,

        title = title,
        type = when (type) {
            "CORE" -> VaccinationType.CORE
            "NON_CORE" -> VaccinationType.NON_CORE
            "OPTIONAL" -> VaccinationType.OPTIONAL
            else -> VaccinationType.CORE
        },
        manufacturer = manufacturer,
        batchNumber = batchNumber,

        status = when (status) {
            "SCHEDULED" -> VaccinationStatus.SCHEDULED
            "COMPLETED" -> VaccinationStatus.COMPLETED
            "OVERDUE" -> VaccinationStatus.OVERDUE
            "SKIPPED" -> VaccinationStatus.SKIPPED
            else -> VaccinationStatus.SCHEDULED
        },
        scheduledDate = scheduledDate?.let { java.time.Instant.ofEpochMilli(it) },
        completedDate = completedDate?.let { java.time.Instant.ofEpochMilli(it) },
        nextDueDate = nextDueDate?.let { java.time.Instant.ofEpochMilli(it) },

        certificateUrl = certificateUrl,
        notes = notes,
        sideEffects = sideEffects,

        createdAt = java.time.Instant.ofEpochMilli(createdAt),
        updatedAt = java.time.Instant.ofEpochMilli(updatedAt),

        reminderEnabled = reminderEnabled,
        reminderDaysBefore = reminderDaysBefore
    )
}

fun Vaccination.toDto(): VaccinationRecordDto {
    return VaccinationRecordDto(
        id = id,
        petId = petId,
        veterinarianId = veterinarianId,
        veterinarianName = veterinarianName,
        clinicName = clinicName,

        title = title,
        type = type.name,
        manufacturer = manufacturer,
        batchNumber = batchNumber,

        status = status.name,
        scheduledDate = scheduledDate?.toEpochMilli(),
        completedDate = completedDate?.toEpochMilli(),
        nextDueDate = nextDueDate?.toEpochMilli(),

        certificateUrl = certificateUrl,
        notes = notes,
        sideEffects = sideEffects,

        createdAt = createdAt.toEpochMilli(),
        updatedAt = updatedAt.toEpochMilli(),

        reminderEnabled = reminderEnabled,
        reminderDaysBefore = reminderDaysBefore
    )
}

// endregion

// region Community

fun PostDto.toDomain(): Post =
    Post(
        id = id,
        authorId = authorId, // Foreign key relationship
        authorName = authorName,
        authorAvatarUrl = authorAvatarUrl,
        timestamp = createdAt.toString(),
        content = content,
        imageUrl = imageUrl,
        likesCount = likesCount,
        commentsCount = commentsCount
    )

fun PetEventDto.toDomain(): PetEvent =
    PetEvent(
        id = id,
        organizerId = organizerId, // Foreign key relationship
        title = title,
        date = date.toString(),
        location = location,
        imageUrl = imageUrl,
        iconRes = null
    )

// endregion

// region Veterinarians

fun VeterinarianDto.toDomain(): Veterinarian =
    Veterinarian(
        id = id,
        name = name,
        specialty = specialty,
        experience = experience,
        rating = rating,
        reviewsCount = reviewsCount,
        initials = initials,
        bio = bio,
        imageUrl = imageUrl,
        clinicId = clinicId ?: "",
        servicePrice = servicePrice
    )

// endregion

// region Doctor Reviews

fun DoctorReviewDto.toDomain(): DoctorReview =
    DoctorReview(
        id = id,
        appointmentId = appointmentId,
        doctorId = doctorId,
        userId = userId,
        userName = userName,
        rating = rating,
        comment = comment,
        createdAt = createdAt
    )

// endregion

// region Services

fun ServiceCategoryDto.toDomain(iconRes: Int): ServiceCategory =
    ServiceCategory(
        id = id,
        title = title,
        shortDescription = shortDescription,
        iconRes = iconRes,
        iconUrl = iconUrl,
        imageUrl = iconUrl
    )

fun ServicePackageDto.toDomain(): ServicePackage =
    ServicePackage(
        id = id,
        name = name,
        price = price
    )

fun PetServiceDetailDto.toDomain(): PetServiceDetail =
    PetServiceDetail(
        categoryId = categoryId,
        rating = rating.toFloat(),
        reviewCount = reviewCount,
        about = about,
        packages = packages.map { it.toDomain() },
        availableTimes = availableTimes,
        bannerGradientColors = bannerGradientColors
    )

// endregion