## Firestore Schema Overview

This document describes the Firestore database schema and links it to the DTOs under `data/models`.

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
- Related collection: `vaccinations` → `VaccinationRecordDto` (linked via `petId` foreign key).

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

### vaccinations

- Collection: `vaccinations` → `VaccinationRecordDto`
- Key fields: `id`, `petId` (foreign key to Pet), `veterinarianId` (optional foreign key to Veterinarian),
  `title`, `isCompleted`, `date`, `notes`, `createdAt`.
- Relationships: Linked to `pets` via `petId`, optionally linked to `veterinarians` via `veterinarianId`.

### Notes

- Firestore is schemaless; DTOs act as the implicit schema in code.
- All new fields are optional / have defaults so existing data continues to work.
- For relationship details, see `RELATIONSHIPS_SETUP_SUMMARY.md`.
- For index requirements, see `FIRESTORE_INDEXES.md`.
- For security rules, see `FIRESTORE_SECURITY_RULES.md`.


