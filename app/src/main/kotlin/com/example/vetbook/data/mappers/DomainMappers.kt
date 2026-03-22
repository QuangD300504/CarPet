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
        imageRes = null,
        age = age,
        gender = gender,
        weight = weight,
        parasiticStatus = parasiticStatus,
        note = note,
        realImgUrl = imageUrl,
        birthDate = birthDate?.let { java.time.Instant.ofEpochMilli(it) },
        vaccinations = vaccinations
    )

fun Pet.toDto(): PetDto = PetDto(
    id = id,
    ownerId = ownerId,
    name = name,
    type = type,
    breed = breed,
    imageUrl = realImgUrl,
    age = age,
    gender = gender,
    weight = weight,
    parasiticStatus = parasiticStatus,
    note = note,
    birthDate = birthDate?.toEpochMilli(),
    createdAt = null,
    updatedAt = null
)

// endregion

// region Vaccinations

fun VaccinationRecordDto.toDomain(): Vaccination {
    return Vaccination(
        id = id,
        petId = petId,
        ownerId = ownerId,
        petName = petName,
        veterinarianId = veterinarianId,
        veterinarianName = veterinarianName,
        clinicName = clinicName,

        title = title,
        type = when (type) {
            "CORE"            -> VaccinationType.CORE
            "REGIONAL"        -> VaccinationType.REGIONAL
            "LIFESTYLE"       -> VaccinationType.LIFESTYLE
            "NOT_RECOMMENDED" -> VaccinationType.NOT_RECOMMENDED
            "CUSTOM"          -> VaccinationType.CUSTOM
            "NON_CORE"        -> VaccinationType.REGIONAL
            "OPTIONAL"        -> VaccinationType.LIFESTYLE
            else              -> VaccinationType.CORE
        },
        alsoKnownAs = alsoKnownAs,
        manufacturer = manufacturer,
        batchNumber = batchNumber,
        offsetDays = offsetDays,
        isRecurring = isRecurring,
        intervalDays = intervalDays,
        lifestyleTrigger = lifestyleTrigger,

        // FIX: "PENDING" was hitting `else -> SCHEDULED`, so generated vaccines
        // (status="PENDING", scheduledDate=null) displayed as "Đã hẹn" with no date
        // and no "Đặt lịch tiêm" button visible to the user.
        status = when (status) {
            "PENDING"   -> VaccinationStatus.PENDING
            "SCHEDULED" -> VaccinationStatus.SCHEDULED
            "COMPLETED" -> VaccinationStatus.COMPLETED
            "OVERDUE"   -> VaccinationStatus.OVERDUE
            "SKIPPED"   -> VaccinationStatus.SKIPPED
            else        -> VaccinationStatus.PENDING
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
        ownerId = ownerId,
        petName = petName,
        veterinarianId = veterinarianId,
        veterinarianName = veterinarianName,
        clinicName = clinicName,

        title = title,
        type = type.name,
        alsoKnownAs = alsoKnownAs,
        manufacturer = manufacturer,
        batchNumber = batchNumber,
        offsetDays = offsetDays,
        isRecurring = isRecurring,
        intervalDays = intervalDays,
        lifestyleTrigger = lifestyleTrigger,

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
        authorId = authorId,
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
        organizerId = organizerId,
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