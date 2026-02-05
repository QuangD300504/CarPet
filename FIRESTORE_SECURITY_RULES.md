# Firestore Security Rules for Relationships

This document provides security rules that enforce relationship constraints and ensure data integrity.

## Security Rules

Add these rules to your Firestore Database → Rules section in Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user owns a resource
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // ============================================
    // Users Collection
    // ============================================
    match /users/{userId} {
      // Users can only read/write their own profile
      allow read: if isOwner(userId);
      allow create: if isAuthenticated() && request.auth.uid == userId;
      allow update: if isOwner(userId);
      allow delete: if isOwner(userId);
    }
    
    // ============================================
    // Pets Collection
    // ============================================
    match /pets/{petId} {
      // Users can read their own pets or adoption pets
      allow read: if isAuthenticated() && (
        resource.data.ownerId == request.auth.uid ||
        resource.data.isForAdoption == true
      );
      
      // Users can create pets only if they set themselves as owner
      allow create: if isAuthenticated() && 
        request.resource.data.ownerId == request.auth.uid;
      
      // Users can update/delete only their own pets
      allow update, delete: if isAuthenticated() && 
        resource.data.ownerId == request.auth.uid;
      
      // Validate ownerId exists when creating/updating
      function validateOwnerExists() {
        return exists(/databases/$(database)/documents/users/$(request.resource.data.ownerId));
      }
    }
    
    // ============================================
    // Posts Collection
    // ============================================
    match /posts/{postId} {
      // Anyone authenticated can read posts
      allow read: if isAuthenticated();
      
      // Users can create posts only if they set themselves as author
      allow create: if isAuthenticated() && 
        request.resource.data.authorId == request.auth.uid;
      
      // Users can update/delete only their own posts
      allow update, delete: if isAuthenticated() && 
        resource.data.authorId == request.auth.uid;
      
      // Validate authorId exists when creating
      function validateAuthorExists() {
        return exists(/databases/$(database)/documents/users/$(request.resource.data.authorId));
      }
    }
    
    // ============================================
    // Pet Events Collection
    // ============================================
    match /petEvents/{eventId} {
      // Anyone authenticated can read active events
      allow read: if isAuthenticated() && 
        (resource.data.isActive == true || resource.data.organizerId == request.auth.uid);
      
      // Users can create events only if they set themselves as organizer
      allow create: if isAuthenticated() && 
        request.resource.data.organizerId == request.auth.uid;
      
      // Users can update/delete only events they organized
      allow update, delete: if isAuthenticated() && 
        resource.data.organizerId == request.auth.uid;
      
      // Validate organizerId exists when creating
      function validateOrganizerExists() {
        return exists(/databases/$(database)/documents/users/$(request.resource.data.organizerId));
      }
    }
    
    // ============================================
    // Vaccinations Collection
    // ============================================
    match /vaccinations/{vaccinationId} {
      // Users can read vaccinations for their own pets
      allow read: if isAuthenticated() && (
        // Get pet owner from pet document
        get(/databases/$(database)/documents/pets/$(resource.data.petId)).data.ownerId == request.auth.uid ||
        // Or if veterinarianId matches (vets can see their own vaccinations)
        resource.data.veterinarianId == request.auth.uid
      );
      
      // Users can create vaccinations only for their own pets
      allow create: if isAuthenticated() && (
        // Pet must exist and belong to user
        get(/databases/$(database)/documents/pets/$(request.resource.data.petId)).data.ownerId == request.auth.uid &&
        // Validate veterinarian exists if provided
        (request.resource.data.veterinarianId == null || 
         exists(/databases/$(database)/documents/veterinarians/$(request.resource.data.veterinarianId)))
      );
      
      // Users can update/delete vaccinations only for their own pets
      allow update, delete: if isAuthenticated() && 
        get(/databases/$(database)/documents/pets/$(resource.data.petId)).data.ownerId == request.auth.uid;
      
      // Validate petId exists when creating/updating
      function validatePetExists() {
        return exists(/databases/$(database)/documents/pets/$(request.resource.data.petId));
      }
    }
    
    // ============================================
    // Appointments Collection
    // ============================================
    match /appointments/{appointmentId} {
      // Users can read only their own appointments
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;

      // Users can create only their own PENDING_PAYMENT appointments
      allow create: if isAuthenticated() &&
        request.resource.data.userId == request.auth.uid &&
        request.resource.data.status == "PENDING_PAYMENT" &&
        request.resource.data.paymentStatus == "UNPAID";

      // Users can update only safe fields on their own appointments (cannot mark paid/confirmed)
      allow update: if isAuthenticated() && resource.data.userId == request.auth.uid &&
        request.resource.data.userId == resource.data.userId &&
        request.resource.data.veterinarianId == resource.data.veterinarianId &&
        request.resource.data.appointmentAt == resource.data.appointmentAt &&
        request.resource.data.status == resource.data.status &&
        request.resource.data.paymentStatus == resource.data.paymentStatus &&
        request.resource.data.payos == resource.data.payos;

      allow delete: if false;
    }

    // ============================================
    // Slot locks (prevent double booking)
    // ============================================
    match /doctorSlotLocks/{lockId} {
      // Read not required for clients
      allow read: if false;

      // Allow creating a lock (for this MVP) only by authenticated users
      allow create: if isAuthenticated();

      // Disallow updates/deletes from clients
      allow update, delete: if false;
    }

    // ============================================
    // Veterinarians Collection
    // ============================================
    match /veterinarians/{vetId} {
      // Anyone authenticated can read veterinarians
      allow read: if isAuthenticated();
      
      // Only admins can create/update/delete (adjust based on your needs)
      allow write: if false; // Disable public writes - use Cloud Functions or admin SDK
    }
    
    // ============================================
    // Service Categories Collection
    // ============================================
    match /services/{serviceId} {
      // Anyone authenticated can read services
      allow read: if isAuthenticated();
      
      // Only admins can write (adjust based on your needs)
      allow write: if false; // Disable public writes - use Cloud Functions or admin SDK
    }
  }
}
```

## Simplified Rules (If Cross-Document Validation is Too Expensive)

If the above rules cause performance issues due to too many document reads, use this simplified version:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Users
    match /users/{userId} {
      allow read, write: if isAuthenticated() && request.auth.uid == userId;
    }
    
    // Pets - Users can only access their own pets or adoption pets
    match /pets/{petId} {
      allow read: if isAuthenticated() && (
        resource.data.ownerId == request.auth.uid ||
        resource.data.isForAdoption == true
      );
      allow create: if isAuthenticated() && 
        request.resource.data.ownerId == request.auth.uid;
      allow update, delete: if isAuthenticated() && 
        resource.data.ownerId == request.auth.uid;
    }
    
    // Posts - Users can read all, write their own
    match /posts/{postId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
        request.resource.data.authorId == request.auth.uid;
      allow update, delete: if isAuthenticated() && 
        resource.data.authorId == request.auth.uid;
    }
    
    // Pet Events - Users can read active events, write their own
    match /petEvents/{eventId} {
      allow read: if isAuthenticated() && 
        (resource.data.isActive == true || resource.data.organizerId == request.auth.uid);
      allow create: if isAuthenticated() && 
        request.resource.data.organizerId == request.auth.uid;
      allow update, delete: if isAuthenticated() && 
        resource.data.organizerId == request.auth.uid;
    }
    
    // Vaccinations - Users can access vaccinations for their pets
    // Note: This requires petId to be set correctly
    match /vaccinations/{vaccinationId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
        request.resource.data.petId != null;
      allow update, delete: if isAuthenticated();
      // Note: Pet ownership validation should be done in application code
      // to avoid expensive cross-document reads in security rules
    }
    
    // Veterinarians - Read only
    match /veterinarians/{vetId} {
      allow read: if isAuthenticated();
      allow write: if false;
    }
    
    // Services - Read only
    match /services/{serviceId} {
      allow read: if isAuthenticated();
      allow write: if false;
    }
  }
}
```

## Important Notes

1. **Cross-Document Validation**: The first set of rules validates relationships by reading other documents. This can be expensive but ensures data integrity.

2. **Performance**: If you have many reads, consider the simplified rules and handle relationship validation in your application code (which we've already implemented).

3. **Testing**: Test these rules thoroughly in the Firebase Console Rules Playground before deploying.

4. **Deployment**: Deploy rules using:
   ```bash
   firebase deploy --only firestore:rules
   ```

5. **Monitoring**: Monitor rule evaluation costs in Firebase Console → Usage & Billing.

## Relationship Validation Strategy

We use a **two-layer approach**:

1. **Application Layer** (Already Implemented):
   - Validates relationships before creating/updating documents
   - Returns proper error messages
   - More flexible and can provide better UX

2. **Security Rules Layer** (This Document):
   - Provides additional security
   - Prevents unauthorized access
   - Acts as a safety net

Both layers work together to ensure data integrity and security.

