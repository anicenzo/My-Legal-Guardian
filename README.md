# Legal AI: Offline Contract Scan

**Legal AI: Offline Contract Scan** is a privacy-first, on-device legal contract scanner and risk analyzer for Android. Built with Kotlin, Jetpack Compose, Room Database, Google ML Kit, and TensorFlow Lite, it audits residential leases, NDAs, employment contracts, and freelance agreements 100% offline without sending sensitive contract documents to cloud servers.

---

## Key Features

- **100% On-Device Document Scanning & OCR**: Utilizes Google Play Services Document Scanner and ML Kit Text Recognition for offline digitizing.
- **Automated Predatory Clause Auditing**: On-device regex & TensorFlow Lite analysis identifies high-risk legal clauses (uncapped indemnification, auto-renewal traps, unilateral modifications, hidden penalty fees, mandatory arbitration waivers).
- **Missing Safeguards Detection**: Analyzes contracts for statutory tenant and contractor protections (habitability, security deposit return windows, cure periods, mutual termination notice).
- **Interactive Negotiation Drafter**: Automatically drafts professional, category-tailored email counter-proposals ready to copy or share directly to landlords and counterparties.
- **Confidential Encrypted Vault**: Full offline local scan history powered by Room Database with cascade-delete protection.
- **Pro PDF Export**: Generates professional, multi-page confidential A4 legal audit reports exportable to PDF.
- **Subscription Management**: In-app billing via Qonversion SDK for Google Play Billing.

---

## Tech Stack & Architecture

- **Language & Runtime**: Kotlin 2.2.0, JVM 21, Android SDK 36 (minSdk 26).
- **UI Framework**: Modern Jetpack Compose with Material 3, dynamic light/dark theming, and custom micro-animations.
- **Architecture**: Clean MVVM (Model-View-ViewModel) pattern with StateFlow and Coroutines.
- **Persistence**: Room Database (SQLite) + Jetpack DataStore Preferences.
- **Machine Learning & Vision**: ML Kit Document Scanner, ML Kit OCR, TensorFlow Lite Java Runtime (`contract_risk_classifier.tflite`).
- **Billing & Analytics**: Qonversion Android SDK (`9.4.1`).

---

## Getting Started

### Prerequisites

- [Android Studio Ladybug | 2024.2.1+](https://developer.android.com/studio)
- JDK 17 or JDK 21

### Local Setup

1. Clone the repository and open the project in Android Studio.
2. Create a `.env` file in the root project directory (see `.env.example`):
   ```properties
   QONVERSION_PROJECT_KEY=your_qonversion_project_key_here
   ```
3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```
