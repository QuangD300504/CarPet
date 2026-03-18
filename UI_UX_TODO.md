## VetBook App – UI/UX TODO & Incomplete Features

### 1. Store & Products
- **See-all filters**
  - Store home → category chip → navigates to See-all with that category.
  - See-all screen now has chips + search, but filters are not persisted across tab switches and can feel “stale”.
  - **Todo**: Decide desired behavior and persist/clear filters consistently when entering Store vs See-all.
- **Product details**
  - `ProductCard` tap currently reuses `onProductsClick` (navigates to See-all) instead of a dedicated detail page.
  - **Todo**: Add `ProductDetailScreen` and route `ProductCard.onClick` to it with product ID.
- **Promotional/CTA buttons with no behavior**
  - `StoreScreen.PromotionalBanner` “Mua ngay” button has `onClick = { }` (no-op).
  - **Decision**: Either wire to a featured products/category, or make the banner purely informational and remove the button.

### 2. Store Header / Search & Filters
- **Filter icon**
  - `StoreHeader` filter `Surface` uses `onClick = { }` – visually suggests advanced filters but does nothing.
  - **Todo**: Implement a filter bottom sheet (price range, category, sort) or remove the icon to avoid false affordance.
- **Location selector**
  - Location label is clickable and there is a `LocationDropdown` component, but there is no end-to-end flow for selecting/persisting city.
  - **Todo**: Hook `onLocationClick` to an actual dropdown/sheet and store the chosen city (and, optionally, filter store content by city).

### 3. Community
- **Comment & Share actions**
  - `FeedList` / `PostCard`:
    - `onCommentClick` and `onShareClick` are passed as lambdas but default to `{ }` and currently do nothing in `CommunityScreen`.
  - **Todo**:
    - Minimum: show simple snackbars (“Coming soon”) when comment/share is pressed.
    - Ideal: implement a `PostDetailScreen` with comments list and a share sheet.
- **Event CTA**
  - `EventCard` “Tham gia sự kiện” button uses `onClick = { }`.
  - **Todo**: Decide whether this should:
    - Open an event detail page.
    - Open map / external registration link.
    - Or be removed if events are only informational.

### 4. Caring Banner (Home / Services)
- **“Khám phá ngay” button**
  - `CaringBanner` primary CTA uses `onClick = { }`.
  - **Todo**: Wire to the Services tab or a curated services list. If not, remove the button or change to static copy.
- **Hard-coded remote image**
  - Uses a fixed Unsplash URL; this is fragile and may break or load slowly.
  - **Todo**: Move to a local asset or a controlled CDN/image source.

### 5. Accommodation Detail
- **“Đặt ngay” button**
  - At the bottom of `AccommodationDetailScreen` the main CTA currently has `onClick = { }`.
  - **Todo**:
    - Hook into a booking flow (date picker + pet info) or external booking link.
    - Until implemented, consider a “Liên hệ để đặt chỗ” flow (call/message).
- **Static amenities list**
  - “Thú cưng”, “Sân vườn”, “Wifi”, “24/7” are hard-coded, independent of actual accommodation data.
  - **Todo**: Move amenities into the `Accommodation` model and render dynamically, or clearly label as “Tiện ích phổ biến” if kept generic.

### 6. Calendar & Appointments
- **Current state**
  - Month grid with markers, scrollable day list, detail bottom sheet with clinic location, time, pets, status, payment status, price, notes.
  - Past `UPCOMING` appointments are rendered as “COMPLETED” with neutral styling in the detail sheet.
- **Missing interactions**
  - No actions on the detail sheet (cancel / reschedule / open maps / call clinic).
  - **Todo**:
    - Add primary & secondary actions:
      - “Đổi lịch” → reuse booking flow.
      - “Huỷ lịch” → confirmation + `BookingRepository.cancelAppointment`.
      - “Mở trên bản đồ” when `clinicAddress` is present.
    - Expose whether cancellation is allowed based on status/time.
- **Loading & error states**
  - `CalendarViewModel` tracks `isLoading` and `error`, but `CalendarScreen` does not surface them.
  - **Todo**: Show initial loading indicator and an inline error banner if appointments fail to load.

### 7. Profile & Settings
- **Buttons that depend on navigation wiring**
  - `ProfileScreen` exposes callbacks: edit profile, notifications, language, security, help, contact, privacy, logout.
  - In `MainScreen`, most of these are wired; some (security/help) still need real flows.
  - **Todo**:
    - Add actual screens or sheets for:
      - “Bảo mật tài khoản” (password/change-email, 2FA stubs).
      - “Trung tâm trợ giúp” (FAQ / support links).
    - Ensure notifications and language menu items always provide visible feedback on change.

### 8. Store & Service Promotions
- **Promotional CTAs with no behavior**
  - Store `PromotionalBanner` → “Mua ngay” no-op.
  - Other promotional banners may follow the same pattern.
  - **Todo**: For each banner, either:
    - Link it to a specific route (category, service, or campaign), or
    - Remove/disable the button to avoid dead taps.

### 9. General UX / Theming
- **Typography & spacing**
  - Many hard-coded font sizes and paddings (`14.sp`, `16.sp`, `20.dp`, `24.dp`).
  - **Todo**:
    - Define a small set of typography styles and spacing tokens in theme and reuse them.
    - This will make future visual tweaks (e.g. line up Store, Calendar, Profile) much easier.
- **Empty / error states**
  - Some screens have nice empty states (Products, Calendar daily view), others only show raw text errors.
  - **Todo**:
    - Align empty/error states across Store, Community, Cart, Calendar with consistent visuals and copy.

