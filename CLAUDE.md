# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**VetBook** is a monorepo containing a Kotlin/Android mobile app, a React admin web dashboard, Firebase Cloud Functions for backend logic, and a Cloudflare Worker for payment processing.

- **Firebase Project:** `vetbookexe`
- **Region:** `asia-southeast1`

## Repository Structure

```
app/              # Android app (Kotlin / Jetpack Compose)
admin-web/        # Admin dashboard (React 19 / Vite / Tailwind CSS 4)
functions/       # Firebase Cloud Functions v2 (TypeScript)
worker/           # Cloudflare Worker — PayOS payment (Hono)
tools/
  └── firestore-seed/   # Firestore seeding utility
gradle/           # Gradle wrapper & version catalog
```

## Build & Run Commands

### Android App
```bash
./gradlew build
./gradlew assembleDebug          # Debug APK
./gradlew installDebug           # Install on connected device
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Instrumented tests
./gradlew clean
```
> **Important:** Always use the Gradle wrapper (`./gradlew`), not `gradle`.

### Admin Web
```bash
cd admin-web
npm run dev      # Dev server
npm run build    # Production build
npm run lint     # ESLint
npm run preview  # Preview production build
```

### Firebase Functions
```bash
cd functions
npm run build         # TypeScript compilation
npm run build:watch   # Watch mode
npm run lint
npm run serve         # Firebase emulators
npm run deploy
npm run logs
```

### Cloudflare Worker
```bash
cd worker
npm run dev     # Local Wrangler dev
npm run deploy
```

### Firestore Seed
```bash
cd tools/firestore-seed && npm run seed
```

## Architecture

### Android App (`app/`)

Clean Architecture with three layers:

```
domain/       # Business logic — independent of frameworks
              # Contains: models (entities), repository interfaces, use cases
data/         # Data layer — implements repository interfaces
              # Contains: Firebase data sources, REST API clients, DTOs, mappers
presentation/ # UI layer — Jetpack Compose screens, ViewModels, navigation
              # Contains: screens/, viewmodels/, components/, theme/
```

**Key libraries:** Jetpack Compose (Material 3), Hilt (DI), Firebase Auth/Firestore, Retrofit, Coil 3, Navigation Compose, StateFlow + ViewModel.

**Compile SDK:** 36 | **Min SDK:** 24 | **Target SDK:** 36

The `domain/repository/` interfaces are the contracts — all data access goes through them. The `data/repository/` implementations handle Firestore reads and REST API calls.

### Admin Web (`admin-web/`)

React 19 SPA with Vite 7, Tailwind CSS 4, and React Router DOM 7. Firebase JS SDK is used directly. Routes are defined in `App.tsx`. Auth is handled via `contexts/AuthContext.tsx`. Protected routes use `components/Layout/ProtectedLayout.tsx`.

### Firebase Functions (`functions/`)

TypeScript Cloud Functions v2 (Node 24) deployed to `asia-southeast1`. Key functions:
- `reserveSlotAndCreateAppointment` (onCall) — Firestore transaction for booking
- `createPayosPaymentLink` (onCall) — Generates PayOS link for appointments
- `createStorePaymentLink` (onCall) — PayOS link for store orders
- `payosWebhook` (onRequest) — Handles PayOS payment callbacks

### Cloudflare Worker (`worker/`)

Hono-based worker for alternative PayOS processing. Uses `FIREBASE_SERVICE_ACCOUNT_JSON` secret.

## Known UI/UX Incomplete Items

`UI_UX_TODO.md` tracks incomplete interactions across the app. Key items to be aware of:

| Area | Issue |
|------|-------|
| Store | Filter icon, location selector, and promotional CTA buttons have `onClick = {}` (no-op) |
| Store | Product cards navigate to See-all instead of a dedicated `ProductDetailScreen` |
| Calendar | Appointment detail sheet has no cancel/reschedule/open-maps actions |
| Calendar | `CalendarScreen` does not surface `isLoading` or `error` states from `CalendarViewModel` |
| Community | Comment, share, and event CTA buttons are wired but non-functional |
| Accommodation | "Đặt ngay" CTA and static amenities list are not connected to data |
| Caring Banner | "Khám phá ngay" CTA and hard-coded Unsplash image are non-functional |
| Profile | Security and help menu items need real screens |
| Theming | Many hard-coded `sp`/`dp` values instead of theme tokens |

## Sensitive Files (Do Not Commit)

- `app/google-services.json` — Firebase credentials
- `functions/.env` — Environment variables
- `tools/firestore-seed/serviceAccountKey.json` — Firebase Admin service account
- `debug.keystore` — Android debug signing key

## Gradle Version Catalog

All dependency versions (Kotlin, Compose, Firebase, Hilt, etc.) are centralized in `gradle/libs.versions.toml`. When adding or updating dependencies, edit this file rather than hardcoding versions in `app/build.gradle.kts`.
