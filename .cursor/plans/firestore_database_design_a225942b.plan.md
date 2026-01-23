---
name: Firestore Database Design
overview: Design a comprehensive Firestore database structure for VetBook app covering users, pets, veterinarians, services, appointments, community posts, events, reviews, and related data with proper collections, subcollections, and field structures.
todos:
  - id: design-users
    content: Enhance users collection structure with additional fields (profileImageUrl, address, preferences, etc.)
    status: completed
  - id: design-pets
    content: Design pets collection with subcollections for vaccinations and medical records
    status: completed
  - id: design-veterinarians
    content: Design veterinarians collection with clinic info, availability, and rating fields
    status: completed
  - id: design-services
    content: Design services collection with packages subcollection
    status: completed
  - id: design-appointments
    content: Design appointments collection with status tracking and payment info
    status: completed
  - id: design-reviews
    content: Design reviews collection for veterinarian ratings and feedback
    status: completed
  - id: design-community
    content: Design posts, comments, and likes collections for community features
    status: completed
  - id: design-events
    content: Design petEvents collection for community events
    status: completed
  - id: design-notifications
    content: Design notifications collection for user notifications
    status: completed
  - id: create-documentation
    content: Create comprehensive documentation file with all collection structures, field types, and indexes
    status: completed
---

# Firestore Database Design for VetBook

## Overview

This plan designs a comprehensive Firestore database structure for the VetBook application, covering all features identified in the codebase including user management, pet profiles, veterinarian services, appointments, community features, and reviews.

## Current State

The database currently only has a `users` collection with basic fields:

- `uid`, `fullName`, `email`, `phone`, `createdAt`, `isEmailVerified`

## Database Structure

### 1. Users Collection (`users`)

**Document ID**: User UID (from Firebase Auth)

**Fields**:

```typescript
{
  uid: string,
  fullName: string,
  email: string,
  phone: string,
  profileImageUrl?: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  isEmailVerified: boolean,
  lastLogin?: timestamp,
  points: number (default: 0),
  address?: {
    street: string,
    city: string,
    state: string,
    zipCode: string,
    country: string
  },
  preferences?: {
    notificationsEnabled: boolean,
    language: string
  }
}
```

**Subcollections**:

- `pets` - User's owned pets
- `appointments` - User's appointments
- `reviews` - Reviews written by user
- `notifications` - User notifications

### 2. Pets Collection (`pets`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  ownerId: string, // Reference to users/{userId}
  name: string,
  type: string, // "Dog", "Cat", etc.
  breed: string,
  age: string, // "2 years 3 months"
  gender: string, // "Male", "Female"
  weight: string, // "9.5 kg"
  parasiticStatus: string, // "Healthy", etc.
  note: string,
  imageUrl?: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  isForAdoption: boolean (default: false),
  adoptionDetails?: {
    description: string,
    adoptionFee?: number,
    contactInfo: string
  }
}
```

**Subcollections**:

- `vaccinations` - Vaccination records
- `medicalRecords` - Medical history (future)

**Indexes Required**:

- `ownerId` (ascending)
- `isForAdoption` (ascending) + `createdAt` (descending)
- `type` (ascending) + `isForAdoption` (ascending)

### 3. Vaccinations Subcollection (`pets/{petId}/vaccinations`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  title: string, // "5-in-1", "Rabies", etc.
  isCompleted: boolean,
  date?: timestamp,
  veterinarianId?: string, // Reference to veterinarians/{vetId}
  notes?: string,
  createdAt: timestamp
}
```

### 4. Veterinarians Collection (`veterinarians`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  name: string,
  specialty: string, // "General", "Surgery", "Dermatology", etc.
  experience: string, // "5 years"
  bio: string,
  imageUrl?: string,
  email: string,
  phone: string,
  clinic?: {
    name: string,
    address: {
      street: string,
      city: string,
      state: string,
      zipCode: string
    },
    coordinates?: {
      latitude: number,
      longitude: number
    }
  },
  availability?: {
    days: string[], // ["Monday", "Tuesday", ...]
    hours: {
      start: string, // "09:00"
      end: string // "17:00"
    }
  },
  rating: number (default: 0), // Calculated from reviews
  reviewsCount: number (default: 0),
  isActive: boolean (default: true),
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Subcollections**:

- `reviews` - Reviews for this veterinarian
- `appointments` - Appointments with this veterinarian

**Indexes Required**:

- `isActive` (ascending) + `rating` (descending)
- `specialty` (ascending) + `rating` (descending)

### 5. Services Collection (`services`)

**Document ID**: Category ID (e.g., "cat_vet", "dog_grooming")

**Fields**:

```typescript
{
  id: string,
  title: string,
  shortDescription: string,
  iconUrl?: string,
  bannerGradientColors: number[], // [color1, color2, ...]
  about: string,
  rating: number (default: 0),
  reviewCount: number (default: 0),
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Subcollections**:

- `packages` - Service packages
- `availableTimes` - Available time slots (if needed)

### 6. Service Packages Subcollection (`services/{serviceId}/packages`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  name: string,
  price: number,
  description?: string,
  duration?: number, // in minutes
  isActive: boolean (default: true),
  createdAt: timestamp
}
```

### 7. Appointments Collection (`appointments`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  userId: string, // Reference to users/{userId}
  veterinarianId?: string, // Reference to veterinarians/{vetId}
  serviceId?: string, // Reference to services/{serviceId}
  petId: string, // Reference to pets/{petId}
  packageId?: string, // Reference to services/{serviceId}/packages/{packageId}
  status: string, // "pending", "confirmed", "completed", "cancelled"
  appointmentDate: timestamp,
  appointmentTime: string, // "09:00"
  duration: number, // in minutes
  notes?: string,
  totalPrice: number,
  paymentStatus: string, // "pending", "paid", "refunded"
  createdAt: timestamp,
  updatedAt: timestamp,
  cancelledAt?: timestamp,
  cancellationReason?: string
}
```

**Subcollections**:

- `reminders` - Appointment reminders (future)

**Indexes Required**:

- `userId` (ascending) + `appointmentDate` (descending)
- `veterinarianId` (ascending) + `appointmentDate` (ascending)
- `status` (ascending) + `appointmentDate` (ascending)
- `petId` (ascending) + `appointmentDate` (descending)

### 8. Reviews Collection (`reviews`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  userId: string, // Reference to users/{userId}
  veterinarianId: string, // Reference to veterinarians/{vetId}
  appointmentId?: string, // Reference to appointments/{appointmentId}
  rating: number, // 1-5
  title?: string,
  comment: string,
  createdAt: timestamp,
  updatedAt: timestamp,
  isVerified: boolean (default: false), // Verified if from actual appointment
  helpfulCount: number (default: 0)
}
```

**Indexes Required**:

- `veterinarianId` (ascending) + `createdAt` (descending)
- `userId` (ascending) + `createdAt` (descending)
- `rating` (descending) + `createdAt` (descending)

### 9. Posts Collection (`posts`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  authorId: string, // Reference to users/{userId}
  authorName: string, // Denormalized for performance
  authorAvatarUrl?: string, // Denormalized for performance
  content: string,
  imageUrl?: string,
  imageUrls?: string[], // For multiple images
  likesCount: number (default: 0),
  commentsCount: number (default: 0),
  createdAt: timestamp,
  updatedAt: timestamp,
  isEdited: boolean (default: false),
  tags?: string[] // For categorization
}
```

**Subcollections**:

- `comments` - Post comments
- `likes` - User likes (for tracking who liked)

**Indexes Required**:

- `authorId` (ascending) + `createdAt` (descending)
- `createdAt` (descending)
- `likesCount` (descending) + `createdAt` (descending)

### 10. Comments Subcollection (`posts/{postId}/comments`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  authorId: string, // Reference to users/{userId}
  authorName: string, // Denormalized
  authorAvatarUrl?: string, // Denormalized
  content: string,
  likesCount: number (default: 0),
  createdAt: timestamp,
  updatedAt: timestamp,
  isEdited: boolean (default: false),
  parentCommentId?: string // For nested replies
}
```

**Indexes Required**:

- `createdAt` (ascending)

### 11. Likes Subcollection (`posts/{postId}/likes`)

**Document ID**: User ID

**Fields**:

```typescript
{
  userId: string,
  createdAt: timestamp
}
```

### 12. PetEvents Collection (`petEvents`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  title: string,
  description: string,
  date: timestamp,
  location: string,
  imageUrl?: string,
  organizerId: string, // Reference to users/{userId}
  organizerName: string, // Denormalized
  eventType: string, // "Adoption", "Workshop", "Meetup", etc.
  maxParticipants?: number,
  currentParticipants: number (default: 0),
  isActive: boolean (default: true),
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Subcollections**:

- `participants` - Event participants

**Indexes Required**:

- `date` (ascending) + `isActive` (ascending)
- `eventType` (ascending) + `date` (ascending)
- `organizerId` (ascending) + `date` (descending)

### 13. Notifications Collection (`notifications`)

**Document ID**: Auto-generated

**Fields**:

```typescript
{
  id: string,
  userId: string, // Reference to users/{userId}
  type: string, // "appointment", "review", "comment", "like", "system"
  title: string,
  message: string,
  relatedId?: string, // ID of related entity (appointment, post, etc.)
  isRead: boolean (default: false),
  createdAt: timestamp
}
```

**Indexes Required**:

- `userId` (ascending) + `isRead` (ascending) + `createdAt` (descending)
- `userId` (ascending) + `createdAt` (descending)

## Design Decisions

### Denormalization Strategy

- User names and avatars are denormalized in posts/comments for better read performance
- Rating and review counts are stored on veterinarians for quick access
- Like and comment counts are stored on posts for efficient queries

### Subcollections vs Top-level Collections

- **Subcollections used for**: Vaccinations (under pets), Comments/Likes (under posts), Packages (under services)
- **Top-level collections for**: Independent entities that need cross-referencing (appointments, reviews, posts, events)

### Indexing Strategy

- Composite indexes for common query patterns (user + date, status + date, etc.)
- Single-field indexes for filtering and sorting

### Data Consistency

- Use Cloud Functions (future) to maintain denormalized counts
- Timestamps for audit trails and sorting
- Status fields for state management

## Implementation Notes

1. **Migration Path**: The existing `users` collection structure will be enhanced with new optional fields
2. **Backward Compatibility**: All new fields should be optional to support existing users
3. **Security Rules**: Will need to be configured to allow appropriate read/write access
4. **Cloud Functions**: Recommended for maintaining calculated fields (ratings, counts)

## Files to Reference

- Current user structure: `app/src/main/kotlin/com/example/vetbook/data/repository/AuthRepositoryImpl.kt` (lines 42-49)
- Domain models: `app/src/main/kotlin/com/example/vetbook/domain/models/`
- Repository interfaces: `app/src/main/kotlin/com/example/vetbook/domain/repository/`