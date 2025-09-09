# Sweet 🍯

A location-based Android application for discovering and reviewing local establishments. Built with modern Android development practices using Kotlin and Jetpack Compose.

## 📱 Project Overview

Sweet is a comprehensive mobile application that enables users to discover, review, and share local establishments in their area. The app leverages location services to provide a personalized experience, allowing users to find nearby restaurants, cafes, and other businesses, leave reviews, and compete with other users through a leaderboard system.

### Key Features

- **📍 Location-Based Discovery**: Automatically discover establishments near your location
- **⭐ Review System**: Rate and review establishments with both quality and price ratings
- **🏆 Leaderboard**: Compete with other users based on review contributions
- **🔔 Geofencing**: Receive notifications when near reviewed establishments
- **👤 User Profiles**: Manage personal profiles and view review history
- **🌙 Dark Mode**: Toggle between light and dark themes
- **🌍 Multi-Language**: Support for English and Portuguese
- **🗺️ Interactive Maps**: Visual representation of establishments using Google Maps

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (Local) + Firebase Firestore (Remote)
- **Authentication**: Firebase Auth
- **Cloud Storage**: Firebase Storage
- **Analytics**: Firebase Analytics

### Major Dependencies

#### Android & Compose
- `androidx.core:core-ktx` (1.17.0) - Core Android KTX extensions
- `androidx.lifecycle:lifecycle-runtime-ktx` (2.9.3) - Lifecycle aware components
- `androidx.activity:activity-compose` (1.10.1) - Activity Compose integration
- `androidx.compose:compose-bom` (2025.08.01) - Compose Bill of Materials
- `androidx.compose.material3:material3` (1.3.2) - Material Design 3 components
- `androidx.compose.material:material-icons-extended` (1.7.8) - Extended material icons

#### Navigation & Architecture
- `androidx.navigation:navigation-compose` (2.9.3) - Compose navigation
- `androidx.room:room-runtime` (2.7.2) - Local database
- `androidx.room:room-ktx` (2.7.2) - Room Kotlin extensions
- `androidx.datastore:datastore-preferences` (1.1.1) - Preferences storage

#### Firebase Services
- `com.google.firebase:firebase-bom` (34.2.0) - Firebase Bill of Materials
- `com.google.firebase:firebase-auth` - User authentication
- `com.google.firebase:firebase-firestore` - Cloud database
- `com.google.firebase:firebase-storage` - File storage
- `com.google.firebase:firebase-analytics` - Usage analytics

#### Location & Maps
- `com.google.android.gms:play-services-location` (21.3.0) - Location services
- `com.google.android.gms:play-services-maps` (19.2.0) - Google Maps
- `com.google.maps.android:maps-compose` (6.7.2) - Maps Compose integration
- `com.google.android.libraries.places:places` (4.4.1) - Places API

#### Utilities
- `com.jakewharton.timber:timber` (5.0.1) - Logging
- `io.coil-kt:coil-compose` (2.7.0) - Image loading

#### Testing
- `junit:junit` (4.13.2) - Unit testing
- `androidx.test.ext:junit` (1.3.0) - Android testing extensions
- `androidx.test.espresso:espresso-core` (3.7.0) - UI testing

## 🏗️ Project Architecture

### Package Structure
```
com.cmu.sweet/
├── data/                   # Data layer
│   ├── local/             # Local database (Room)
│   │   ├── dao/           # Data Access Objects
│   │   ├── entities/      # Database entities
│   │   └── relations/     # Entity relationships
│   ├── remote/            # Remote data sources
│   │   └── dto/           # Data Transfer Objects
│   ├── mappers/           # Data mappers
│   └── repository/        # Repository implementations
├── helpers/               # Utility classes and helpers
├── ui/                    # User Interface layer
│   ├── components/        # Reusable UI components
│   ├── navigation/        # Navigation configuration
│   ├── screen/           # Screen composables
│   ├── state/            # UI state classes
│   └── theme/            # App theming
├── utils/                # Utility functions
├── view_model/           # ViewModels (MVVM)
├── MainActivity.kt       # Main activity
└── SweetApplication.kt   # Application class
```

### Data Models

#### Core Entities

**Establishment**
- Primary entity representing local businesses
- Fields: id, name, address, type, description, latitude, longitude, addedBy, createdAt
- Supports foreign key relationships with User entity

**Review**
- User-generated reviews for establishments
- Fields: id, establishmentId, userId, rating, priceRating, comment, timestamp
- Links establishments with users through foreign keys

**User**
- User profile information
- Manages authentication and user-specific data

### Architecture Patterns

- **MVVM**: Clean separation between UI and business logic
- **Repository Pattern**: Centralized data access management
- **Dependency Injection**: Manual DI implementation
- **Single Source of Truth**: Room database with Firebase sync

## 🚀 Installation & Setup

### Prerequisites
- Android Studio (latest version recommended)
- Android SDK (API level 26 or higher)
- Java 17
- Firebase project setup

### Configuration Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/pedroteixeira2002/sweet.git
   cd sweet
   ```

2. **Firebase Setup**
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication, Firestore Database, and Storage
   - Download `google-services.json` and place it in the `app/` directory
   - Replace the API key in `AndroidManifest.xml` with your Google Maps API key

3. **Google Maps Setup**
   - Enable Maps SDK for Android in Google Cloud Console
   - Enable Places API
   - Update the API key in `AndroidManifest.xml`:
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_API_KEY_HERE" />
     ```

4. **Build Configuration**
   ```bash
   chmod +x gradlew
   ./gradlew clean build
   ```

### Required Permissions
The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - Precise location access
- `ACCESS_BACKGROUND_LOCATION` - Background location access
- `FOREGROUND_SERVICE` - Location service operation
- `POST_NOTIFICATIONS` - Push notifications
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_*` - Media access

## 🎯 Core Features

### 1. User Authentication
- Firebase Authentication integration
- Email/password login and registration
- Secure user session management

### 2. Location Services
- Real-time location tracking
- Geofencing for proximity notifications
- Background location service
- Location-based establishment discovery

### 3. Establishment Management
- Add new establishments with location data
- View detailed establishment information
- Search and filter establishments
- Category-based organization

### 4. Review System
- 5-star rating system for quality
- 5-star rating system for price
- Text-based reviews
- User review history

### 5. Interactive Maps
- Google Maps integration
- Custom map styling (light/dark modes)
- Establishment markers
- Current location indicator

### 6. Social Features
- User leaderboards
- Profile management
- Review statistics

### 7. Personalization
- Dark/light theme toggle
- Multi-language support (English/Portuguese)
- Persistent user preferences

## 📱 User Interface

### Screen Flow
1. **Splash Screen** → Initial app loading
2. **Welcome Screen** → First-time user introduction
3. **Login/Signup** → User authentication
4. **Home Screen** → Main dashboard with nearby establishments
5. **Profile Screen** → User information and settings
6. **Add Establishment** → Create new establishment entries
7. **Add Review** → Rate and review establishments
8. **Leaderboard** → User rankings and statistics
9. **Settings** → App preferences and configuration

### Design System
- **Material Design 3** components and theming
- **Responsive layouts** for different screen sizes
- **Accessibility support** with proper content descriptions
- **Consistent navigation** patterns throughout the app

## 🔧 Development Workflow

### Build System
- **Gradle** with Kotlin DSL
- **Version Catalogs** for dependency management
- **ProGuard** configuration for release builds

### Code Quality
- **Timber** for structured logging
- **Lint** checks for code quality
- **Type-safe navigation** with Compose Navigation

### Testing Strategy
- **Unit tests** with JUnit
- **Instrumented tests** with Espresso
- **Compose UI tests** for screen validation

## 🔐 Security & Privacy

### Data Protection
- User authentication through Firebase Auth
- Secure API key management
- Local data encryption with Room
- Privacy-compliant location data handling

### Permissions
- Runtime permission requests
- Granular location access control
- User consent for data collection

## 🌐 API Integration

### Google Services
- **Maps API**: Interactive map display and location services
- **Places API**: Establishment search and autocomplete
- **Location Services**: GPS and network-based positioning

### Firebase Services
- **Authentication**: User management and security
- **Firestore**: Real-time database synchronization
- **Storage**: Image and file management
- **Analytics**: Usage tracking and insights

## 📈 Performance Optimizations

### Location Services
- Intelligent geofence management (100 nearest establishments)
- Battery-efficient location updates
- Background service optimization

### Database
- Room database with foreign key constraints
- Efficient query optimization
- Proper indexing for performance

### UI Performance
- Compose performance best practices
- Lazy loading for large datasets
- Efficient state management

## 🤝 Contributing

### Development Guidelines
1. Follow Kotlin coding conventions
2. Use meaningful commit messages
3. Write unit tests for new features
4. Update documentation for API changes
5. Respect the established architecture patterns

### Pull Request Process
1. Fork the repository
2. Create a feature branch
3. Make your changes with proper testing
4. Submit a pull request with detailed description

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Author**: pedroteixeira2002
- **Institution**: Carnegie Mellon University (CMU)

## 📞 Support

For questions, issues, or contributions, please:
1. Check existing GitHub issues
2. Create a new issue with detailed description
3. Contact the development team

---

**Note**: This project is part of academic work at Carnegie Mellon University. Please ensure proper attribution when using or referencing this code.