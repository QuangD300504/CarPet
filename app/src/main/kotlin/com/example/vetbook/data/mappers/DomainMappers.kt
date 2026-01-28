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
        profileImage = profileImage
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

// region Pets

fun PetDto.toDomain(): Pet =
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
        vaccinations = emptyList()
    )

// endregion

// region Vaccinations

fun VaccinationRecordDto.toDomain(): Vaccination =
    Vaccination(
        id = id,
        petId = petId, // Foreign key relationship
        veterinarianId = veterinarianId, // Optional foreign key relationship
        title = title,
        isCompleted = isCompleted,
        date = date?.toString(),
        notes = notes
    )

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
        rating = rating.toString(),
        reviewsCount = reviewsCount,
        initials = initials,
        bio = bio,
        imageUrl = imageUrl
    )

// endregion

// region Services

fun ServiceCategoryDto.toDomain(iconRes: Int): ServiceCategory =
    ServiceCategory(
        id = id,
        title = title,
        shortDescription = shortDescription,
        iconRes = iconRes
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


