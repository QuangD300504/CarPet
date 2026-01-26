# Firestore Composite Indexes Required

This document lists all the composite indexes that need to be created in Firestore Console for optimal query performance.

## ⭐ RECOMMENDED: Auto-Generate Indexes by Running Queries

**🔥 Best Practice: Instead of manually defining indexes, run your queries in your app code first!**

Firestore will automatically provide a link to generate the required index when you run a query that needs one.

### How It Works:

1. **Run your app** and execute queries that use relationships (e.g., `getUserPets()`, `getPostsByAuthor()`)
2. **Check Logcat/console** - Firestore will log an error with a direct link
3. **Click the link** - It takes you to Firebase Console with the index pre-configured
4. **Click "Create Index"** - The index is created automatically with correct field ordering
5. **Wait 2-5 minutes** for the index to build
6. **Re-run your query** - It should work now!

### Why This Approach is Better:

- ✅ **Pre-configured correctly** - No manual field selection needed
- ✅ **Only creates what you need** - Indexes match your exact queries
- ✅ **Saves time** - No need to figure out field order manually
- ✅ **Less error-prone** - Firestore knows exactly what's needed

### Example Error Message:
```
FAILED_PRECONDITION: The query requires an index. 
You can create it here: https://console.firebase.google.com/...
```

**Just click the link!** 🎯

---

## Alternative: Manual Index Creation

**Note:** Only use this if you need to create indexes before running queries, or for CI/CD automation.

If you prefer to create indexes manually:

1. Go to Firebase Console → Firestore Database → Indexes
2. Click "Create Index"
3. Select the collection and fields as specified below
4. Click "Create"

## Required Indexes

### 1. Pets Collection

#### Index: `ownerId` + `createdAt`
- **Collection**: `pets`
- **Fields**:
  - `ownerId` (Ascending)
  - `createdAt` (Descending)
- **Query Scope**: Collection
- **Used By**: `getUserPets()` - Get pets ordered by creation date

**Query Pattern:**
```kotlin
firestore.collection("pets")
    .whereEqualTo("ownerId", userId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
```

---

### 2. Posts Collection

#### Index: `authorId` + `createdAt`
- **Collection**: `posts`
- **Fields**:
  - `authorId` (Ascending)
  - `createdAt` (Descending)
- **Query Scope**: Collection
- **Used By**: `getPostsByAuthor()` - Get posts by author ordered by date

**Query Pattern:**
```kotlin
firestore.collection("posts")
    .whereEqualTo("authorId", authorId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
```

---

### 3. Pet Events Collection

#### Index: `organizerId` + `date`
- **Collection**: `petEvents`
- **Fields**:
  - `organizerId` (Ascending)
  - `date` (Ascending)
- **Query Scope**: Collection
- **Used By**: `getEventsByOrganizer()` - Get events by organizer ordered by date

**Query Pattern:**
```kotlin
firestore.collection("petEvents")
    .whereEqualTo("organizerId", organizerId)
    .orderBy("date", Query.Direction.ASCENDING)
```

---

### 4. Vaccinations Collection

#### Index 1: `petId` + `date`
- **Collection**: `vaccinations`
- **Fields**:
  - `petId` (Ascending)
  - `date` (Descending)
- **Query Scope**: Collection
- **Used By**: `getVaccinationsByPet()` - Get vaccinations for a pet ordered by date

**Query Pattern:**
```kotlin
firestore.collection("vaccinations")
    .whereEqualTo("petId", petId)
    .orderBy("date", Query.Direction.DESCENDING)
```

#### Index 2: `veterinarianId` + `date`
- **Collection**: `vaccinations`
- **Fields**:
  - `veterinarianId` (Ascending)
  - `date` (Descending)
- **Query Scope**: Collection
- **Used By**: `getVaccinationsByVeterinarian()` - Get vaccinations by vet ordered by date

**Query Pattern:**
```kotlin
firestore.collection("vaccinations")
    .whereEqualTo("veterinarianId", veterinarianId)
    .orderBy("date", Query.Direction.DESCENDING)
```

---

## Index Creation Commands (Firebase CLI)

If you prefer using Firebase CLI, you can create these indexes by adding them to `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "pets",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "ownerId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "posts",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "authorId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "petEvents",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "organizerId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "date",
          "order": "ASCENDING"
        }
      ]
    },
    {
      "collectionGroup": "vaccinations",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "petId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "date",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "vaccinations",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "veterinarianId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "date",
          "order": "DESCENDING"
        }
      ]
    }
  ],
  "fieldOverrides": []
}
```

Then deploy with:
```bash
firebase deploy --only firestore:indexes
```

## Notes

- ⭐ **Recommended**: Let Firestore auto-generate indexes by running queries first - it's faster and less error-prone
- Index creation can take a few minutes (usually 2-5 minutes)
- The error link in Logcat/console takes you directly to create the index with all fields pre-filled
- Single-field indexes are created automatically, only composite indexes need creation
- You can also use Firebase CLI (see below) if you prefer programmatic index management

## Quick Test Queries to Trigger Index Creation

Run these queries in your app to trigger automatic index creation:

```kotlin
// 1. Get user's pets (triggers pets index)
petDataSource.getUserPets("test_user_id")

// 2. Get posts by author (triggers posts index)
communityDataSource.getPostsByAuthor("test_user_id")

// 3. Get events by organizer (triggers petEvents index)
communityDataSource.getEventsByOrganizer("test_user_id")

// 4. Get vaccinations by pet (triggers vaccinations petId index)
vaccinationDataSource.getVaccinationsByPet("test_pet_id")

// 5. Get vaccinations by veterinarian (triggers vaccinations veterinarianId index)
vaccinationDataSource.getVaccinationsByVeterinarian("test_vet_id")
```

## Finding Index Creation Links

After running these queries, check for index errors:

### In Android Studio Logcat:
1. Open Logcat (View → Tool Windows → Logcat)
2. Filter by your app package name or search for "index"
3. Look for error messages like:
   ```
   FAILED_PRECONDITION: The query requires an index. 
   You can create it here: https://console.firebase.google.com/...
   ```
4. Click the link or copy-paste it into your browser
5. Click "Create Index" on the Firebase Console page

### What the Error Looks Like:
```
E/Firestore: Query requires an index. 
The query requires an index. You can create it here: 
https://console.firebase.google.com/project/YOUR_PROJECT/firestore/indexes?create_composite=...
```

### After Creating Index:
- Wait 2-5 minutes for the index to build
- The status will change from "Building" to "Enabled" in Firebase Console
- Re-run your query - it should work now!

