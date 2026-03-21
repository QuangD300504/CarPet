# VetBook System Design - Vaccination Management Module

**Last Updated:** March 20, 2026
**Status:** Phase 1 FULLY COMPLETED ✅ | Phase 2 Product Search & Orders ✅ COMPLETED | Phase 3 Reminders, Reviews & Calendar Fixes ✅ COMPLETED

---

## 🏗️ Architecture Overview

### Layer Structure (Clean Architecture)

```
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ VaccinationVM│  │ PetProfileVM │  │   Screens    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓↑
┌─────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Vaccination  │  │ Repository   │  │   Enums      │  │
│  │   Model      │  │  Interface   │  │  (Status,    │  │
│  │              │  │              │  │   Type)      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                           ↓↑
┌─────────────────────────────────────────────────────────┐
│                     DATA LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │VaccinationDTO│  │ Repository   │  │  Mappers     │  │
│  │              │  │Impl + Auto   │  │  (DTO ↔      │  │
│  │              │  │Overdue Logic │  │  Domain)     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                                                          │
│  ┌──────────────┐  ┌──────────────┐                    │
│  │  Firebase    │  │  Cloudinary  │                    │
│  │  DataSource  │  │  (Certs)     │                    │
│  └──────────────┘  └──────────────┘                    │
└─────────────────────────────────────────────────────────┘
                           ↓↑
┌─────────────────────────────────────────────────────────┐
│               EXTERNAL SERVICES                          │
│         Firestore DB    │    Cloudinary CDN             │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Data Models

### Domain Model (Vaccination.kt)

```kotlin
data class Vaccination(
    val id: String,
    val petId: String,
    val veterinarianId: String?,
    val veterinarianName: String?,
    val clinicName: String?,
    
    // Vaccination details
    val title: String,
    val type: VaccinationType,
    val manufacturer: String?,
    val batchNumber: String?,
    
    // Status & Dates
    val status: VaccinationStatus,
    val scheduledDate: Instant?,
    val completedDate: Instant?,
    val nextDueDate: Instant?,
    
    // Documentation
    val certificateUrl: String?,
    val notes: String?,
    val sideEffects: String?,
    
    // Metadata
    val createdAt: Instant,
    val updatedAt: Instant,
    
    // Reminder
    val reminderEnabled: Boolean,
    val reminderDaysBefore: Int
)

enum class VaccinationType {
    CORE,      // Essential (Rabies, Distemper)
    NON_CORE,  // Recommended
    OPTIONAL   // Based on risk
}

enum class VaccinationStatus {
    SCHEDULED,  // Future vaccination
    COMPLETED,  // Done
    OVERDUE,    // Missed deadline
    SKIPPED     // User opted out
}
```

### DTO Model (VaccinationRecordDto.kt)

```kotlin
data class VaccinationRecordDto(
    val id: String = "",
    val petId: String = "",
    val veterinarianId: String? = null,
    val veterinarianName: String? = null,
    val clinicName: String? = null,
    
    val title: String = "",
    val type: String = "CORE",
    val manufacturer: String? = null,
    val batchNumber: String? = null,
    
    val status: String = "SCHEDULED",
    val scheduledDate: Long? = null,
    val completedDate: Long? = null,
    val nextDueDate: Long? = null,
    
    val certificateUrl: String? = null,
    val notes: String? = null,
    val sideEffects: String? = null,
    
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 7,
    
    // Backward compatibility
    @Deprecated("Use status instead")
    val isCompleted: Boolean = false,
    @Deprecated("Use scheduledDate or completedDate")
    val date: Long? = null
)
```

---

## 🔄 Data Flow

### Read Flow (Fetch Vaccinations)

```
User Opens Pet Profile
        ↓
PetProfileViewModel initializes
        ↓
VaccinationViewModel.loadVaccinations()
        ↓
VaccinationRepository.getVaccinationsForPet(petId)
        ↓
FirebaseVaccinationDataSource.getVaccinationsByPet(petId)
        ↓
Firestore Query with ordering
        ↓
DocumentSnapshot → VaccinationRecordDto (toVaccinationDto)
        ↓
VaccinationRecordDto → Vaccination (toDomain mapper)
        ↓
Auto-detect OVERDUE status (if scheduledDate < now)
        ↓
Update in Firestore if status changed
        ↓
Emit List<Vaccination> via Flow
        ↓
VaccinationViewModel updates UI state
        ↓
PetProfileScreen displays vaccination cards
```

### Write Flow (Add New Vaccination)

```
User fills AddVaccinationScreen form
        ↓
User clicks "Save"
        ↓
VaccinationViewModel.addVaccination(vaccination)
        ↓
Vaccination → VaccinationRecordDto (toDto mapper)
        ↓
VaccinationRepository.addVaccination(vaccination)
        ↓
FirebaseVaccinationDataSource.createVaccination(dto)
        ↓
Validate pet exists (foreign key check)
        ↓
Validate veterinarian exists (if provided)
        ↓
Generate document ID
        ↓
Set createdAt & updatedAt timestamps
        ↓
VaccinationRecordDto → Map (toMap)
        ↓
Firestore.set(vaccinationData)
        ↓
Return Result<VaccinationRecordDto>
        ↓
Map back to domain model
        ↓
ViewModel updates success message
        ↓
Reload vaccinations list
        ↓
UI shows new vaccination
```

### Certificate Upload Flow

```
User selects certificate image
        ↓
Convert to ByteArray
        ↓
VaccinationViewModel.uploadCertificate(vaccinationId, bytes)
        ↓
VaccinationRepository.uploadCertificate(id, bytes)
        ↓
CloudinaryImageRepository.uploadImage(bytes)
        ↓
Cloudinary API upload
        ↓
Receive secure_url
        ↓
Update vaccination record with certificateUrl
        ↓
Return Result<String> (URL)
        ↓
ViewModel updates UI
        ↓
Certificate displayed in detail view
```

---

## 🗄️ Firestore Schema

### Collection: `vaccinations`

```json
{
  "vaccinations/{vaccinationId}": {
    "id": "vacc_abc123",
    "petId": "pet_xyz789",              // Foreign key
    "veterinarianId": "vet_def456",     // Optional foreign key
    "veterinarianName": "Dr. Nguyễn",
    "clinicName": "VetCare Clinic",
    
    "title": "Rabies Vaccination",
    "type": "CORE",                     // CORE | NON_CORE | OPTIONAL
    "manufacturer": "Merck",
    "batchNumber": "LOT-2025-001",
    
    "status": "COMPLETED",              // SCHEDULED | COMPLETED | OVERDUE | SKIPPED
    "scheduledDate": 1710950400000,     // Timestamp (milliseconds)
    "completedDate": 1710960000000,
    "nextDueDate": 1742486400000,
    
    "certificateUrl": "https://res.cloudinary.com/...",
    "notes": "No adverse reactions",
    "sideEffects": null,
    
    "createdAt": 1710950000000,
    "updatedAt": 1710960000000,
    
    "reminderEnabled": true,
    "reminderDaysBefore": 7,
    
    // Legacy fields (for backward compatibility)
    "isCompleted": true,
    "date": 1710960000000
  }
}
```

### Indexes Required

```json
{
  "indexes": [
    {
      "collectionGroup": "vaccinations",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "petId", "order": "ASCENDING" },
        { "fieldPath": "scheduledDate", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "vaccinations",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "veterinarianId", "order": "ASCENDING" },
        { "fieldPath": "scheduledDate", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## 🎨 UI Components (Phase 1)

### Current Implementation

#### 1. PetProfileScreen - VaccinationCard Component
**Location:** `presentation/screens/pets/PetProfileScreen.kt`

**Features:**
- ✅ Status-based color coding (green=completed, red=overdue, orange=scheduled)
- ✅ Status icon (✓=completed, !=overdue, ○=scheduled)
- ✅ Date formatting (scheduledDate or completedDate)
- ✅ Title display
- ✅ "Đã tiêm" badge for completed

**Display Logic:**
```kotlin
val isCompleted = vaccination.status == VaccinationStatus.COMPLETED
val isOverdue = vaccination.status == VaccinationStatus.OVERDUE

Surface(color = when {
    isCompleted -> Green
    isOverdue -> Red
    else -> Orange
})
```

### Upcoming UI Screens (Phase 1 - In Progress)

#### 2. VaccinationListScreen (NEW)
**Route:** `pet/{petId}/vaccinations`

**Features:**
- Tab navigation (Upcoming | Overdue | Completed)
- Floating Action Button (+ Add Vaccination)
- Empty state illustrations
- Pull-to-refresh
- Search/filter by vaccine name
- Sort by date

**Layout:**
```
┌─────────────────────────────────────┐
│  ← Vaccination History              │
├─────────────────────────────────────┤
│ [Upcoming] [Overdue] [Completed]    │
├─────────────────────────────────────┤
│  ┌─────────────────────────────┐    │
│  │ ○  Rabies Booster           │    │
│  │    Due: 25/03/2026          │    │
│  └─────────────────────────────┘    │
│                                     │
│  ┌─────────────────────────────┐    │
│  │ !  Distemper                │    │
│  │    Overdue: 3 days          │    │
│  └─────────────────────────────┘    │
│                                     │
│                     [+] FAB         │
└─────────────────────────────────────┘
```

#### 3. AddVaccinationScreen (NEW)
**Route:** `pet/{petId}/vaccinations/add`

**Form Fields:**
- Vaccine Name* (required)
- Type (Core/Non-Core/Optional) - Dropdown
- Scheduled Date* - Date Picker
- Veterinarian - Optional Autocomplete
- Clinic Name - Text Field
- Manufacturer - Text Field
- Batch Number - Text Field
- Notes - Multi-line Text
- Reminder (Enable/Disable + Days Before)

**Validation:**
- Required fields marked
- Date must be in future for scheduled
- Batch number format validation

**Layout:**
```
┌─────────────────────────────────────┐
│  ← Add Vaccination                  │
├─────────────────────────────────────┤
│  Vaccine Name *                     │
│  [                              ]   │
│                                     │
│  Type *                             │
│  [Core Vaccine          ▼]          │
│                                     │
│  Scheduled Date *                   │
│  [25/03/2026           📅]          │
│                                     │
│  Veterinarian (Optional)            │
│  [Dr. Nguyễn           ▼]          │
│                                     │
│  ... more fields ...                │
│                                     │
│  [ Cancel ]  [ Save Vaccination ]   │
└─────────────────────────────────────┘
```

#### 4. VaccinationDetailScreen (NEW)
**Route:** `vaccinations/{vaccinationId}`

**Sections:**
- Header (Status badge + Title)
- Date Information
- Vaccination Details
- Certificate (if uploaded)
- Notes & Side Effects
- Action Buttons (Edit | Delete | Upload Certificate)

**Layout:**
```
┌─────────────────────────────────────┐
│  ← Vaccination Details              │
├─────────────────────────────────────┤
│  ✅ COMPLETED                        │
│  Rabies Vaccination                 │
│                                     │
│  📅 Dates                            │
│  Scheduled: 20/03/2026              │
│  Completed: 20/03/2026              │
│  Next Due: 20/03/2027               │
│                                     │
│  💉 Details                          │
│  Type: Core Vaccine                 │
│  Manufacturer: Merck                │
│  Batch: LOT-2025-001                │
│                                     │
│  📄 Certificate                      │
│  [View Certificate Image]           │
│                                     │
│  📝 Notes                            │
│  No adverse reactions observed      │
│                                     │
│  [Edit] [Delete] [Upload Cert]      │
└─────────────────────────────────────┘
```

---

## 🛒 Phase 2: Product Search & Order Tracking (March 20, 2026 — COMPLETED)

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────────┐  ┌──────────────────────────────────┐ │
│  │  StoreViewModel  │  │   ProductDetailViewModel        │ │
│  │  (sort/filter    │  │   (product detail + cart)      │ │
│  │   state)         │  │                                  │ │
│  └──────────────────┘  └──────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  OrderHistoryViewModel                                │   │
│  │  (order list + tab filter + order detail)            │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                           ↓↑
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │ OrderStatus  │  │ StoreOrder   │  │ ObserveOrders    │  │
│  │ enum         │  │ domain model │  │ UseCase          │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐    │
│  │ GetStoreProductByIdUseCase                          │    │
│  └──────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                           ↓↑
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │StoreOrderDto │  │ Repository   │  │  Firebase DS     │  │
│  │+ OrderItemDto│  │Impl          │  │  (callbackFlow)   │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Data Models

#### OrderStatus Enum
```kotlin
enum class OrderStatus(val value: String, val displayName: String) {
    PENDING("PENDING", "Chờ xử lý"),
    PAID("PAID", "Đã thanh toán"),
    SHIPPED("SHIPPED", "Đang giao"),
    DELIVERED("DELIVERED", "Đã giao"),
    CANCELLED("CANCELLED", "Đã hủy");
    companion object {
        fun fromString(value: String): OrderStatus =
            entries.find { it.value == value } ?: PENDING
    }
}
```

#### StoreOrder Domain Model
```kotlin
data class StoreOrder(
    val id: String,
    val uid: String,
    val orderCode: String,
    val items: List<OrderItem>,
    val itemCount: Int,
    val subtotal: Double,
    val discount: Double,
    val deliveryCharges: Double,
    val total: Double,
    val status: OrderStatus,
    val createdAt: Long
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val lineTotal: Double
)
```

### Firestore Schema

#### Collection: `storeOrders`
```json
{
  "storeOrders/{orderId}": {
    "id": "ord_abc123",
    "uid": "user_xyz",
    "orderCode": "VB20260320001",
    "items": [
      {
        "productId": "prod_001",
        "productName": "Thức ăn cho mèo",
        "quantity": 2,
        "lineTotal": 300000
      }
    ],
    "itemCount": 2,
    "subtotal": 300000,
    "discount": 0,
    "deliveryCharges": 25000,
    "total": 325000,
    "status": "PENDING",
    "createdAt": 1742486400000
  }
}
```

#### Index Required
```json
{
  "collectionGroup": "storeOrders",
  "fields": [
    { "fieldPath": "uid", "order": "ASCENDING" },
    { "fieldPath": "createdAt", "order": "DESCENDING" }
  ]
}
```

### UI Screens

#### ProductDetailScreen
- Route: `product_detail/{productId}`
- Full-width product image (VetBookImage with fallback)
- Info card: category chip, stock badge (Còn hàng / Hết hàng), name, price
- Sticky bottom bar: "Thêm vào giỏ hàng" (disabled when out of stock)
- Navigate to Cart on success

#### FilterBottomSheet
- Sort chips: Mới nhất, Giá thấp → cao, Giá cao → thấp, Bán chạy
- Price RangeSlider: 0 – 5,000,000 VND
- In-stock toggle switch
- Đặt lại (reset) + Áp dụng (apply) buttons

#### OrderHistoryScreen
- Route: `order_history`
- TabRow: Tất cả | Đang xử lý | Hoàn thành | Đã hủy
- LazyColumn of OrderCard (order code, date, item count, total, status badge)
- StatusBadge colors: PENDING=orange, PAID=blue, SHIPPED=purple, DELIVERED=green, CANCELLED=red

#### OrderDetailScreen
- Route: `order_detail/{orderId}`
- Status card with badge + formatted date
- Section "Sản phẩm (N)" — OrderItemCard list
- OrderSummaryCard (subtotal, discount, delivery, total)
- Cache-first load: checks `allOrders` cache first, falls back to Firestore read

### Key Implementation Patterns

**Real-time order observation (FirebaseStoreDataSource):**
```kotlin
fun observeOrders(uid: String): Flow<List<StoreOrderDto>> = callbackFlow {
    val registration = firestore.collection(STORE_ORDERS_COLLECTION)
        .whereEqualTo("uid", uid)
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error -> ... }
    awaitClose { registration.remove() }
}
```

**Filter/sort pipeline (StoreViewModel):**
- `flatMapLatest` on `selectedCategory` re-triggers Firestore fetch
- Client-side sort + filter applied in combine block after cart merge
- `isFilterSheetVisible` controls ModalBottomSheet display in `ProductsScreen`

**Shared ViewModel pattern (MainScreen):**
- `StoreViewModel` declared at `MainScreen` level (shared between Store tab, ProductsScreen, CartScreen)
- `OrderHistoryViewModel` is screen-scoped (new instance per navigation)

---

## 🔔 Notification System (Phase 4 - Future)

### Reminder Logic
```kotlin
// When reminderEnabled = true
if (scheduledDate != null) {
    val reminderTime = scheduledDate.minus(
        reminderDaysBefore.days
    )
    
    scheduleNotification(
        time = reminderTime,
        title = "Vaccination Reminder",
        body = "${pet.name} has ${title} due on ${scheduledDate}"
    )
}
```

### Implementation Plan
- WorkManager for scheduled notifications
- FCM for push notifications
- Notification channels
- User preferences

---

## 🔒 Security & Validation

### Foreign Key Validation
```kotlin
// Before creating vaccination
val petDoc = firestore.collection("pets")
    .document(vaccination.petId)
    .get()
    .await()

if (!petDoc.exists()) {
    return Result.failure(Exception("Pet not found"))
}
```

### Data Integrity
- ✅ Required fields enforced at DTO level
- ✅ Enum validation with fallbacks
- ✅ Date validation (scheduledDate < nextDueDate)
- ✅ Status consistency checks

### User Permissions
- Users can only modify vaccinations for their own pets
- Veterinarians can view but not delete
- Admin can view all records

---

## 📈 Performance Optimizations

### Current Optimizations
1. **Firestore Indexes** - Fast queries by petId + scheduledDate
2. **Flow-based Reactive Data** - No polling, instant updates
3. **Local Sorting** - Post-query filtering for overdue detection
4. **Lazy Loading** - Only load when PetProfile viewed

### Future Optimizations (Planned)
1. **Pagination** - For pets with 50+ vaccinations
2. **Caching** - Room database for offline support
3. **Image Optimization** - Cloudinary transformations for thumbnails
4. **Batch Updates** - Bulk overdue status updates

---

## 🧪 Testing Strategy

### Unit Tests
- ✅ Mapper tests (DTO ↔ Domain)
- ✅ Repository tests (mock Firebase)
- ✅ ViewModel tests (state management)
- ✅ Overdue detection logic

### Integration Tests
- Firebase DataSource integration
- Cloudinary upload integration
- End-to-end CRUD operations

### UI Tests
- Screen navigation
- Form validation
- Tab switching
- Empty states

---

## 🚀 Deployment Checklist

### Phase 1 Foundation ✅
- [x] Data models
- [x] Repository layer
- [x] ViewModel
- [x] Updated existing UI
- [x] Build success
- [x] Backward compatibility

### Phase 1 UI ✅ (March 20, 2026 — Afternoon)
- [x] VaccinationListScreen — tabs (Upcoming/Overdue/Completed), FAB, empty states, pull-to-refresh
- [x] AddVaccinationScreen — 3 required fields, collapsible extras, vet autocomplete, DatePicker, reminder slider
- [x] VaccinationDetailScreen — status header, date section, vet profile link, "Book Appointment" CTA, cert upload/preview, mark complete/skipped, delete
- [x] Navigation integration — Routes.kt, MainScreen.kt NavGraph entries, bottom bar hiding
- [x] Form validation — required fields (name, type, date), error messages in Vietnamese
- [x] Error handling — loading states, error banners, confirmation dialogs
- [x] PetProfileScreen integration — vaccination cards, "View all", tappable vet name

### Phase 4 Notifications (Future)

### Phase 2 Product Search & Orders ✅ (March 20, 2026)
- [x] ProductDetailScreen
- [x] FilterBottomSheet — sort, price range, in-stock toggle
- [x] OrderHistoryScreen — tabs, OrderCard, StatusBadge
- [x] OrderDetailScreen — item list, OrderSummaryCard reuse
- [x] Navigation wiring — MainScreen NavHost, shared StoreViewModel, hideBottomBarRoutes
- [x] Firestore composite index: `storeOrders` (uid ASC, createdAt DESC)
- [x] Build verified

### Phase 3 Reminders, Reviews & Calendar Fixes (March 20, 2026) ✅
- [x] Slot Grid UX — 30-min fixed slots (09:00–11:30, 14:00–15:30), `SlotGrid.kt`
- [x] Lock TTL — `expiresAt` field on `doctorSlotLocks`, Cloud Function cleanup
- [x] Past appointments → COMPLETED — `CalendarViewModel` auto-complete on load
- [x] Doctor review system — `DoctorReview` model, `RateDoctorDialog`, `ReviewCard`, aggregate rating
- [x] Vaccination reminders — WorkManager `VaccinationReminderWorker`, `ReminderNotificationHelper`
- [x] Appointment reminders — `AppointmentReminderWorker`, 24h-before scheduling
- [x] Build verified ✅

---

**System Design Version:** 1.4 — Phase 1 + Phase 2 + Phase 3 Completed
**Last Review:** March 20, 2026
**Next Review:** After Phase 4 Community Engagement
