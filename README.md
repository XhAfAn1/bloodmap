# RoktoDhara: Advanced Blood Donation & Healthcare Management Ecosystem

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com/)
[![Database](https://img.shields.io/badge/Database-Firebase-yellow.svg)](https://firebase.google.com/)
[![Map](https://img.shields.io/badge/Maps-OsmDroid-blue.svg)](https://github.com/osmdroid/osmdroid)

**RoktoDhara** (Blood Stream) is a high-performance, real-time Android ecosystem designed to revolutionize blood donation management and healthcare facility logistics. Built with a focus on speed, precision, and state-of-the-art UI/UX, RoktoDhara bridges the gap between those in urgent need and life-saving donors.

---

##  1. Core Features & User Tiers

RoktoDhara operates on a sophisticated dual-role architecture, providing tailored experiences for regular users and platform administrators.

###  Regulated User Features
*   **Precision Blood Requests**: Post urgent needs using specialized independent Date and Time selectors.
*   **Premium Visibility**: Requests from Premium users are pinned, highlighted, and trigger global broadcast notifications.
*   **Intelligent Response System**: Donors can view a live feed sorted by urgency and respond to matches instantly.
*   **Multi-History Tracking**: Dedicated context-aware adapters for "My Requests," "Received History," "My Responses," and "Donation History."
*   **Healthcare Directory**: Search and navigate to nearby Hospitals and Blood Banks sorted by real-time geographical distance.
*   **Interactive Heatmap**: A donor density visualization tool powered by **OsmDroid** and **OpenStreetMap**.
*   **Subscription Status**: Professional visual badges (FREE/PREMIUM) integrated into the user profile.
*   **Offline Mode**: A specialized dashboard that allows access to healthcare directories and available donors without an active internet connection.
*   **AI Support Chatbot**: Integrated assistant for donor guidance and platform FAQs.

###  Administrative Console
*   **System Integrity Controls**: Ban accounts or promote active donors to "Premium" status to maintain a high-quality community.
*   **Facility Management**: Dedicated activities for CRUD operations on the national directory of Hospitals and Blood Banks.
*   **Location Seeding**: Integrated utility classes (Hospital/BloodBank Seeders) for rapid data injection into the Firestore database.
*   **Emergency Mass Broadcast**: A unique client-side secure OAuth2 broadcast tool for sending mass push notifications during critical emergencies.

---

##  2. Technical Architecture

RoktoDhara is engineered for scalability and real-time performance using a modern Android stack.

###  Advanced Engineering Highlights
*   **Industrial-Grade Concurrency**: Uses a `FixedThreadPool` (`ExecutorService`) to offload heavy geographical math and sorting, ensuring the UI remains fluid.
*   **Client-Side FCM V1 OAuth2 Handshake**: Implements secure, short-lived access token generation directly within the app using a Google Service Account for serverless mass broadcasting.
*   **Live Synchronization**: Leverages **Firebase Firestore Snapshot Listeners** for zero-refresh dashboards and real-time status updates.
*   **Geographical Precision**: Uses the `FusedLocationProviderClient` for high-accuracy location tracking and proximity-based facility sorting.
*   **Startup Engine**: Automated checks on startup verify donor eligibility, update FCM tokens, and expire stale blood requests.

###  The Tech Stack
- **Languages**: Java (Android SDK 36)
- **Persistence**: Google Cloud Firestore (NoSQL)
- **Authentication**: Firebase Auth (Role-based access)
- **Messaging**: Firebase Cloud Messaging (FCM V1)
- **Mapping**: OsmDroid (OpenStreetMap) & Google Maps SDK (Hybrid)
- **Networking**: Google Auth Library for OAuth2, Picasso for image loading
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

##  3. Premium Design System

RoktoDhara follows a strict, state-of-the-art design system developed to feel like a modern flagship product.

*   **Glassmorphism & Card-Based UI**: Uses custom `16dp` corner radii with subtle shadows and flat elevation for a clean aesthetic.
*   **Curated Aesthetics**: Coordinated color palette including **Blood Red (#800000)**, **Success Green (#2E7D32)**, and **Premium Gold (#F57F17)**.
*   **High-Density Typography**: Sophisticated label system (`9sp`, bold, `0.05` letter spacing) for professional information layouts.
*   **Context-Aware Title Engine**: Specialized system synchronizes the app title with current fragment/activity context for seamless navigation.

---

##  4. Project Structure

```text
ROKTODHARA/
├── app/src/main/
│   ├── java/edu/ewubd/bloodmap/
│   │   ├── MainActivity.java            # Main Navigation & Title Sync Engine
│   │   ├── Authentication/               # User Lifecycle & Auth Logic
│   │   ├── ClassModels/                  # Firestore Pojo Entities
│   │   ├── DrawerPages/                  # History, Healthcare Directories, AI Chat
│   │   ├── HomePages/                    # Main Feed, Request Forms, Osm Heatmap
│   │   ├── Notifications/                # OAuth2 FCM V1 Notification Engine
│   │   ├── OfflineMode/                  # Local Data Persistence Logic
│   │   ├── ProfilePage/                  # Profile Management & Subscription badges
│   │   ├── admin/                        # Admin-exclusive Moderation & CRUD
│   │   └── database/                     # Firestore Helper Utilities
│   └── res/
│       ├── layout/                       # Premium XML UI Definitions
│       ├── raw/                          # service_account.json (FCM Private Key)
│       └── assets/                       # user_manual.html & documentation
```

---

##  5. Setup & Environment

### Prerequisites
- Android Studio (Koala+)
- Java JDK 17
- Firebase Account

### Installation Steps
1.  **Firebase Integration**:
    *   Download `google-services.json` from your Firebase project.
    *   Place it in the `app/` folder.
2.  **Notification Authorization**:
    *   Generate a Private Key from **Firebase Project Settings > Service Accounts**.
    *   Rename to `service_account.json` and place in `app/src/main/res/raw/`.
3.  **Database Configuration**:
    *   Ensure Firestore rules allow authenticated users to read/write transactions.
    *   Use the Admin Dashboard to seed initial hospital/blood bank data.
4.  **Local Build**:
    *   Sync Gradle (Kotlin DSL) and click **Run**.

---

##  6. Key Presentation Points
1.  **Concurrency Mastery**: Explain how `ExecutorService` prevents UI lag during heavy sorting.
2.  **Serverless FCM V1**: The high-efficiency client-side implementation of OAuth2 handshakes.
3.  **Dynamic UI**: Demonstrate the real-time title syncing and fragment navigation.
4.  **Advanced Mapping**: Show the OsmDroid heatmap integration for donor density tracking.

---

> [!NOTE]  
> This project was developed as a high-fidelity solution for digitized healthcare and emergency response management.

