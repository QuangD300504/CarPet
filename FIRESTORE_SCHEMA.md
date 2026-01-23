## Firestore schema overview

This file summarizes the schema from `.cursor/plans/firestore_database_design_a225942b.plan.md`
and links it to the DTOs under `data/models`.

### users

- Collection: `users`
- DTO: `UserProfileDto`
- Key fields: `uid`, `fullName`, `email`, `phone`, `profileImageUrl`, `createdAt`, `updatedAt`,
  `isEmailVerified`, `lastLogin`, `points`, `address`, `preferences`.
- Typical subcollections (future): `pets`, `appointments`, `reviews`, `notifications`.

### pets

- Collection: `pets`
- DTO: `PetDto`
- Key fields: `id`, `ownerId`, `name`, `type`, `breed`, `imageUrl`, `age`, `gender`, `weight`,
  `parasiticStatus`, `note`, `createdAt`, `updatedAt`, `isForAdoption`, `adoptionDetails`.
- Subcollection: `pets/{petId}/vaccinations` → `VaccinationRecordDto`.

### veterinarians

- Collection: `veterinarians`
- DTO: `VeterinarianDto`
- Key fields: `id`, `name`, `specialty`, `experience`, `bio`, `imageUrl`, `email`, `phone`,
  `clinic`, `availability`, `rating`, `reviewsCount`, `isActive`, `createdAt`, `updatedAt`.
- Related collections: `reviews` (top-level, see below) and `appointments`.

### services

- Collection: `services`
- DTOs: `ServiceCategoryDto`, `ServicePackageDto`, `PetServiceDetailDto`
- Service document fields: `id`, `title`, `shortDescription`, `iconUrl`, `bannerGradientColors`,
  `about`, `rating`, `reviewCount`, `createdAt`, `updatedAt`.
- Subcollection `services/{serviceId}/packages` → `ServicePackageDto`.

### appointments

- Collection: `appointments`
- DTO: `AppointmentDto`
- Key fields: `id`, `userId`, `veterinarianId`, `serviceId`, `petId`, `packageId`,
  `status`, `appointmentAt`, `durationMinutes`, `notes`, `totalPrice`, `paymentStatus`,
  `createdAt`, `updatedAt`, `cancelledAt`, `cancellationReason`.

### reviews

- Collection: `reviews`
- DTO: `ReviewDto`
- Key fields: `id`, `userId`, `veterinarianId`, `appointmentId`, `rating`, `title`,
  `comment`, `createdAt`, `updatedAt`, `isVerified`, `helpfulCount`.

### posts, comments, likes

- Collection: `posts` → `PostDto`
  - Fields: `id`, `authorId`, `authorName`, `authorAvatarUrl`, `content`,
    `imageUrl`, `imageUrls`, `likesCount`, `commentsCount`,
    `createdAt`, `updatedAt`, `isEdited`, `tags`.
- Subcollection `posts/{postId}/comments` → `CommentDto`.
- Subcollection `posts/{postId}/likes` → `LikeDto` (doc id usually `userId`).

### petEvents

- Collection: `petEvents` → `PetEventDto`
  - Fields: `id`, `title`, `description`, `date`, `location`, `imageUrl`,
    `organizerId`, `organizerName`, `eventType`, `maxParticipants`,
    `currentParticipants`, `isActive`, `createdAt`, `updatedAt`.
- Subcollection `petEvents/{eventId}/participants` → `EventParticipantDto`.

### notifications

- Collection: `notifications` → `NotificationDto`
- Key fields: `id`, `userId`, `type`, `title`, `message`, `relatedId`, `isRead`, `createdAt`.

### Notes

- Firestore is schemaless; DTOs act as the implicit schema in code.
- All new fields are optional / have defaults so existing data continues to work.
- For detailed field descriptions and index recommendations, see the full plan file
  `.cursor/plans/firestore_database_design_a225942b.plan.md`.


