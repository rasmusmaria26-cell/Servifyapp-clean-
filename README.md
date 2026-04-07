# Servify+

**AI-Powered Multi-Domain Service Aggregation Platform**

A native Android application that connects customers with verified local service providers across electronics, appliances, plumbing, and carpentry — driven by an AI fault diagnosis engine and a competitive vendor bidding marketplace.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Database Schema](#database-schema)
- [Booking Status Flow](#booking-status-flow)
- [Screens](#screens)
- [Known Limitations](#known-limitations)

---

## Overview

Servify+ is a capstone project built at **Kalasalingam Academy of Research and Education** by a team of 4 students. It addresses real gaps in India's informal repair sector:

- No unified platform to discover and book local technicians
- No price transparency before a technician visits
- No way for skilled vendors to build a digital presence
- No escrow or dispute mechanism for payments

The app runs as a **single APK with two modes** — Customer and Vendor — switching roles via `UserSession` after login. Each mode has its own UI theme: a light, animated customer experience and a dense dark vendor portal.

---

## Features

### Customer Side
- **AI Fault Diagnosis** — Describe your issue and optionally attach photos. Gemini 1.5 Flash returns an estimated cost in INR, urgency level, possible causes, repair timeline, and advice — before any vendor is contacted.
- **Direct Booking** — Pick a verified vendor, select date/time, pin your location on OSMDroid (OpenStreetMap), and submit a booking with attached photos.
- **Marketplace / Open Repair Requests** — Post a repair request publicly. All verified vendors in the matching category can see it and submit competitive quotes. Customer picks the best quote.
- **Real-Time Quote Updates** — Supabase Realtime pushes new vendor quotes to the customer instantly — no polling.
- **Booking Detail & Tracking** — View full booking info, vendor contact, AI diagnosis, cost breakdown, and live status updates via Realtime subscription.
- **Razorpay Payment** — Pay the confirmed final price directly in-app via Razorpay Checkout SDK.
- **Service Rating** — Rate completed jobs with a 1–5 star system.

### Vendor Side
- **Professional Portal** — Dark-mode dashboard showing total earnings, active job count, and a segmented tab list (Active / History / My Jobs).
- **Repair Feed** — Browse open repair requests from customers filtered by the vendor's service categories.
- **Quote Submission** — Submit competitive quotes with price, timeline, and remarks on open repair requests.
- **Direct Booking Management** — View incoming direct bookings with attached customer photos, propose a final repair price, accept or reject.
- **Job Completion** — Mark accepted jobs as completed after the work is done.
- **Customer Location Map** — View customer's pinned service location on OSMDroid.
- **Role Switch** — Vendors can switch to Customer mode from the dashboard without logging out.

---

## Tech Stack

| Category | Library / Tool |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Navigation Compose |
| **Architecture** | Clean Architecture — data / domain / presentation |
| **State Management** | ViewModel + StateFlow (MVVM) |
| **DI** | Hilt (Dagger) |
| **Backend** | Supabase (Auth, PostgREST, Realtime, Storage) |
| **AI** | Google Generative AI SDK — Gemini 1.5 Flash |
| **Maps** | OSMDroid 6.1.18 (OpenStreetMap) |
| **HTTP** | Ktor Client (Android engine) |
| **Payments** | Razorpay Checkout SDK |
| **Local DB** | Room KTX |
| **Image Loading** | Coil Compose |
| **Serialization** | KotlinX Serialization |
| **Async** | Kotlin Coroutines + Flow |
| **Preferences** | DataStore Preferences |
| **Testing** | JUnit 4, Espresso, Compose Test APIs |
| **Min SDK** | 26 (Android 8.0) |
| **Target SDK** | 35 (Android 15) |

---

## Architecture

Servify+ follows **Clean Architecture** with three layers per feature:

```
feature/
├── data/          → Repositories, DTOs, Supabase calls
├── domain/        → Use cases, domain models, repository interfaces
└── presentation/  → ViewModels, Screens, UI state
```

**External services** are injected via Hilt from `di/AppModule` and `di/RepositoryModule`. Credentials are never hardcoded — they are injected at build time from `local.properties` via `BuildConfig`.

```
Presentation Layer         External Services
─────────────────          ─────────────────
Jetpack Compose UI    →    Supabase
ViewModels (StateFlow)     Auth · PostgREST · Realtime · Storage
Hilt ViewModel Injection
Mode-Aware Nav Transitions → Gemini 1.5 Flash
        ↓                    Fault Diagnosis · Cost Estimation
Domain Layer
─────────────             → Razorpay
Use Cases                   Payment Checkout SDK
Domain Models
Repository Contracts      → OSMDroid
UserSession Singleton       OpenStreetMap · Tile Caching
        ↓
Data Layer
─────────────
Supabase Repositories
GeminiApiClient
Room DAO (offline cache)
OSMDroid Map Provider
```

---

## Project Structure

```
app/src/main/java/com/servify/app/
├── core/
│   ├── AppMode.kt                  # CUSTOMER / VENDOR enum
│   ├── UserSession.kt              # Singleton session state
│   ├── RenderCapabilities.kt
│   ├── model/
│   │   └── AIDiagnosis.kt          # Gemini response model
│   └── network/
│       ├── SupabaseClient.kt
│       └── GeminiApiClient.kt
├── di/
│   ├── AppModule.kt                # Supabase client provider
│   └── RepositoryModule.kt
├── feature/
│   ├── auth/
│   │   ├── data/AuthRepository.kt
│   │   ├── domain/User.kt
│   │   └── presentation/           # LoginScreen, SignupScreen + ViewModels
│   ├── customer/
│   │   ├── data/                   # Booking, BookingRepository, RepairRepository, etc.
│   │   ├── domain/usecase/         # CreateBookingUseCase
│   │   └── presentation/           # HomeScreen, CreateBookingScreen, BookingDetailScreen,
│   │                               # PostRepairRequestScreen, QuoteManagementScreen, etc.
│   ├── vendor/
│   │   ├── data/VendorRepository.kt
│   │   ├── domain/                 # Vendor model, GetMatchedVendorsUseCase
│   │   └── presentation/           # VendorDashboardScreen, RepairFeedScreen,
│   │                               # SubmitQuoteScreen, MyJobsContent
│   └── marketplace/
│       ├── data/MarketplaceRepositoryImpl.kt
│       ├── domain/                 # RepairRequest, Quote, BookingState
│       └── presentation/           # MarketplaceScreen, ActiveBiddingScreen, etc.
├── navigation/
│   └── ServifyRoutes.kt            # Single source of truth for all route strings
├── presentation/
│   └── splash/                     # SplashScreen + ViewModel
└── MainActivity.kt
```

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 17
- A Supabase project
- A Google AI Studio API key (Gemini)
- A Razorpay account (test mode is fine)

### Clone & Open

```bash
git clone https://github.com/your-username/servify-plus.git
cd servify-plus
```

Open the project in Android Studio.

### Configuration

Create a `local.properties` file in the **root** of the project (same level as `settings.gradle.kts`). This file is gitignored and must never be committed.

```properties
# local.properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-supabase-anon-key
GEMINI_API_KEY=your-google-ai-studio-key
```

These values are injected into the app at build time via `BuildConfig`.

### Build & Run

```bash
./gradlew assembleDebug
```

Or run directly from Android Studio on an emulator or physical device (API 26+).

---

## Database Schema

The following tables are required in your Supabase project:

| Table | Key Columns |
|---|---|
| `profiles` | `id`, `full_name`, `role` (customer / vendor) |
| `vendors` | `id`, `business_name`, `service_categories[]`, `hourly_rate`, `rating`, `latitude`, `longitude`, `service_radius_km`, `is_verified`, `is_available`, `phone` |
| `service_categories` | `id`, `name` |
| `services` | `id`, `name`, `category_id` |
| `bookings` | `id`, `customer_id`, `vendor_id`, `service_id`, `issue_description`, `ai_diagnosis` (jsonb), `scheduled_date`, `scheduled_time`, `address`, `latitude`, `longitude`, `estimated_cost`, `final_cost`, `image_urls` (text[]), `status`, `payment_status`, `created_at` |
| `repair_requests` | `id`, `customer_id`, `device_type`, `brand`, `issue_category`, `severity`, `description`, `image_urls[]`, `latitude`, `longitude`, `address`, `status`, `created_at` |
| `quotes` | `id`, `request_id`, `vendor_id`, `price`, `timeline`, `remarks`, `status`, `created_at` |

### Booking Status Values

The `status` column on the `bookings` table must accept these values:

```
PENDING → PRICE_PROPOSED → ACCEPTED → COMPLETED
                                    ↘ CANCELLED
```

### Supabase Storage

Create a storage bucket named **`booking-images`** with public read access for customer photo uploads on direct bookings.

### Row-Level Security

Enable RLS on all tables. Key policies:
- Customers can only read/write their own bookings and repair requests
- Vendors can read open repair requests scoped to their service categories
- Vendors can only update bookings where they are the assigned `vendor_id`

---

## Booking Status Flow

```
PENDING
  Customer created a direct booking. Vendor can view it with attached photos.
  └─► PRICE_PROPOSED
        Vendor has reviewed photos and proposed a final_cost.
        Customer is notified via Supabase Realtime.
        └─► ACCEPTED
              Customer approved the price. Razorpay payment triggered.
              └─► COMPLETED     ← Vendor marks job as done
              └─► CANCELLED     ← Either party cancels before payment
```

---

## Screens

### Customer Flow
| Screen | Description |
|---|---|
| `LoginScreen` / `SignupScreen` | Auth with Supabase GoTrue |
| `HomeScreen` | Service category picker, active bookings summary |
| `CreateBookingScreen` | 3-step wizard: issue + photos → AI diagnosis + vendor selection → schedule + location |
| `BookingDetailScreen` | Full booking info, vendor contact, real-time status, Pay Now (Razorpay) |
| `PostRepairRequestScreen` | Submit an open repair request to the marketplace |
| `QuoteManagementScreen` | View all vendor quotes on a repair request, accept one |
| `ActiveRepairScreen` | Track an accepted marketplace repair in progress |
| `LocationMapScreen` | OSMDroid map for pinning service location |

### Vendor Flow
| Screen | Description |
|---|---|
| `VendorDashboardScreen` | Earnings, active job count, booking list (Active / History / My Jobs tabs) |
| `VendorBookingDetailScreen` | View booking photos, propose final price, accept/reject |
| `RepairFeedScreen` | Browse open marketplace repair requests |
| `SubmitQuoteScreen` | Submit a competitive quote on a repair request |

---

## Known Limitations

- **Escrow payments** — Razorpay Checkout SDK is integrated for direct payment. The escrow hold-and-release flow (funds held until customer confirms job completion) is planned but not yet implemented.
- **Security audit** — Supabase RLS policies and BuildConfig credential injection are in place. A formal OWASP Mobile Top 10 penetration test, MobSF static analysis, and SSL pinning for Ktor are pending.
- **Rating persistence** — The star rating UI on completed bookings is rendered but the submission is not yet wired to the backend.
- **Push notifications** — Status changes (e.g. vendor proposes price) are surfaced via Realtime on the detail screen but there are no FCM push notifications for background updates.

---

## Team

Built as a B.Tech capstone project at **Kalasalingam Academy of Research and Education**, Department of Computer Science & Engineering, April 2025.

- R Maria Rasmus
- M Sowmya
- H K Pranathi
- B Manikanta

Supervised by **Dr. A. Parivazhagan**, Associate Professor — Dept. of Computer Science and Engineering
