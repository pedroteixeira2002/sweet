# API Configuration & Usage Guide

This document provides detailed information about API configurations and usage in the Sweet application.

## 🔑 API Keys & Configuration

### Google Maps API
The application uses Google Maps SDK for Android and Places API.

#### Setup Steps
1. **Enable APIs** in Google Cloud Console:
   - Maps SDK for Android
   - Places API
   - Geolocation API

2. **Create API Key**:
   - Go to Google Cloud Console → APIs & Services → Credentials
   - Create API Key and restrict it to Android apps
   - Add your app's package name and SHA-1 fingerprint

3. **Configure in AndroidManifest.xml**:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_GOOGLE_MAPS_API_KEY" />
   ```

#### Current Configuration
- **Package Name**: `com.cmu.sweet`
- **API Key Location**: `AndroidManifest.xml`
- **Usage**: Interactive maps, location services, place autocomplete

### Firebase Configuration
The app integrates with multiple Firebase services.

#### Required Services
1. **Firebase Authentication**
   - Email/password authentication
   - User session management

2. **Cloud Firestore**
   - Real-time database for establishments and reviews
   - User profile data storage

3. **Firebase Storage**
   - Image upload and storage
   - User profile pictures
   - Establishment photos

4. **Firebase Analytics**
   - User behavior tracking
   - App usage statistics

#### Setup Steps
1. **Create Firebase Project**:
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create new project or use existing one

2. **Add Android App**:
   - Package name: `com.cmu.sweet`
   - Download `google-services.json`
   - Place in `app/` directory

3. **Configure Services**:
   ```kotlin
   // Firebase initialization in SweetApplication.kt
   class SweetApplication : Application() {
       override fun onCreate() {
           super.onCreate()
           // Firebase is auto-initialized via google-services.json
       }
   }
   ```

## 📊 Database Schema

### Firestore Collections

#### `establishments`
```typescript
interface EstablishmentDto {
  id: string
  name: string
  address: string
  type: string
  description: string
  latitude: number
  longitude: number
  addedBy: string
  createdAt?: number
}
```

#### `reviews`
```typescript
interface ReviewDto {
  id: string
  establishmentId: string
  userId: string
  rating: number
  priceRating: number
  comment: string
  timestamp: number
}
```

#### `users`
```typescript
interface UserDto {
  id: string
  email: string
  displayName: string
  profileImageUrl?: string
  createdAt: number
  reviewCount?: number
}
```

### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read/write their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Anyone can read establishments, only authenticated users can write
    match /establishments/{establishmentId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Anyone can read reviews, only authenticated users can write their own
    match /reviews/{reviewId} {
      allow read: if true;
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
  }
}
```

## 🗺️ Location Services

### Geofencing Implementation
The app uses geofencing to provide location-based notifications.

```kotlin
// Geofence configuration
private fun addGeofence(context: Context, lat: Double, lng: Double, radius: Float) {
    val geofence = Geofence.Builder()
        .setRequestId("establishment_${lat}_${lng}")
        .setCircularRegion(lat, lng, radius)
        .setExpirationDuration(Geofence.NEVER_EXPIRE)
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .build()
    
    // Add geofence to system
}
```

### Location Updates
```kotlin
// Location service configuration
class LocationService : Service() {
    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val LOCATION_UPDATE_INTERVAL = 10000L // 10 seconds
        private const val FASTEST_LOCATION_INTERVAL = 5000L // 5 seconds
    }
}
```

### Required Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

## 🔐 Authentication Flow

### Firebase Auth Integration
```kotlin
// Authentication in repository pattern
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Session Management
- Automatic session restoration on app launch
- Token refresh handling
- Secure logout with session cleanup

## 🖼️ Image Handling

### Firebase Storage Integration
```kotlin
// Image upload implementation
class ImageRepository {
    private val storage = FirebaseStorage.getInstance()
    
    suspend fun uploadImage(uri: Uri, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            val uploadTask = ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Image Loading with Coil
```kotlin
// Compose image loading
@Composable
fun AsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    coil.compose.AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = painterResource(R.drawable.placeholder),
        error = painterResource(R.drawable.error_image)
    )
}
```

## 📊 Analytics Integration

### Firebase Analytics Events
```kotlin
// Custom event tracking
class AnalyticsManager {
    private val analytics = FirebaseAnalytics.getInstance(context)
    
    fun logEstablishmentAdded(establishmentType: String) {
        val bundle = bundleOf(
            "establishment_type" to establishmentType,
            "user_id" to getCurrentUserId()
        )
        analytics.logEvent("establishment_added", bundle)
    }
    
    fun logReviewSubmitted(rating: Int, hasComment: Boolean) {
        val bundle = bundleOf(
            "rating" to rating,
            "has_comment" to hasComment
        )
        analytics.logEvent("review_submitted", bundle)
    }
}
```

## 🔄 Data Synchronization

### Repository Pattern Implementation
```kotlin
class EstablishmentRepository {
    private val localDao = database.establishmentDao()
    private val remoteSource = FirebaseFirestore.getInstance()
    
    fun getEstablishments(): Flow<List<Establishment>> {
        return localDao.getAllEstablishments()
            .onStart { syncFromRemote() }
    }
    
    private suspend fun syncFromRemote() {
        try {
            val remoteData = remoteSource.collection("establishments")
                .get()
                .await()
                .toObjects(EstablishmentDto::class.java)
            
            val entities = remoteData.map { it.toEntity() }
            localDao.insertAll(entities)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync establishments")
        }
    }
}
```

## ⚡ Performance Considerations

### Geofence Optimization
- Limit to 100 nearest establishments
- Dynamic geofence management based on user location
- Battery-efficient location updates

### Database Optimization
- Proper indexing on foreign keys
- Efficient query patterns
- Local caching with Room

### Network Optimization
- Offline-first approach
- Smart sync strategies
- Image optimization and caching

## 🛠️ Development Tools

### Debugging APIs
```kotlin
// Debug logging for API calls
if (BuildConfig.DEBUG) {
    Timber.plant(Timber.DebugTree())
}

// Log API responses
Timber.d("Firebase query result: ${documents.size} establishments")
```

### Testing API Integration
```kotlin
@Test
fun `establishment repository returns cached data when offline`() = runTest {
    // Mock remote failure
    coEvery { remoteSource.getEstablishments() } throws NetworkException()
    
    // Verify local cache is used
    val result = repository.getEstablishments().first()
    assertThat(result).isEqualTo(cachedEstablishments)
}
```

## 🚨 Error Handling

### API Error Management
```kotlin
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val exception: Exception) : ApiResult<T>()
    data class Loading<T>(val data: T? = null) : ApiResult<T>()
}

// Usage in ViewModels
viewModelScope.launch {
    _uiState.value = UiState.Loading
    when (val result = repository.getEstablishments()) {
        is ApiResult.Success -> _uiState.value = UiState.Success(result.data)
        is ApiResult.Error -> _uiState.value = UiState.Error(result.exception.message)
    }
}
```

## 📋 API Limits & Quotas

### Google Maps API
- **Daily limit**: Check Google Cloud Console for your project
- **Rate limiting**: Implement proper request throttling
- **Cost optimization**: Use efficient map loading and caching

### Firebase
- **Firestore**: Free tier includes 50,000 reads/writes per day
- **Storage**: 5GB free storage
- **Authentication**: Unlimited for most auth methods

## 🔧 Troubleshooting

### Common Issues
1. **API Key Issues**:
   - Verify API key is correctly set in AndroidManifest.xml
   - Check API restrictions in Google Cloud Console
   - Ensure SHA-1 fingerprint is correctly configured

2. **Firebase Connection Issues**:
   - Verify google-services.json is in correct location
   - Check network connectivity
   - Review Firebase security rules

3. **Location Permission Issues**:
   - Handle runtime permissions properly
   - Request background location permission when needed
   - Test on physical device for accurate results

### Debug Commands
```bash
# Check API key configuration
adb shell dumpsys package com.cmu.sweet | grep -A 3 "meta-data"

# Monitor Firebase connections
adb logcat -s FirebaseFirestore

# Check location permissions
adb shell dumpsys package com.cmu.sweet | grep permission
```

---

For additional support, refer to the [README.md](README.md) or create an issue in the GitHub repository.