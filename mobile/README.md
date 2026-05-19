# UniGear Tracker - Android Mobile Application

Native Android mobile application for the UniGear Tracker System built with Kotlin and modern Android development practices.

## Technology Stack

- **Language**: Kotlin
- **Target SDK**: Android 15 (API 35)
- **Min SDK**: Android 7.1 (API 24)
- **Build System**: Gradle
- **Image Loading**: Glide
- **HTTP Client**: HttpURLConnection (Android Built-in)
- **JSON Parser**: JSONObject (Android Built-in)

## Getting Started

### Prerequisites
- Android Studio 2024.1 or higher
- JDK 17 or higher
- Android SDK 35+ installed
- Backend server running on your network

### Installation

1. **Clone or Open Project**
   ```bash
   # Open the mobile/ folder in Android Studio
   ```

2. **Configure Backend URL**
   - The app defaults to `http://10.0.2.2:8080` (emulator)
   - For physical devices, update in app settings:
     - Go to Login screen
     - Long press on "Backend Setup" text to modify URL
     - Enter your PC's LAN IP: `http://<your-ip>:8080`

3. **Build & Run**
   - Select emulator or physical device
   - Click Run (Shift+F10)
   - App will launch on device

## Features

### Authentication
- **Email/Password Login**
  - Credentials validation
  - Error handling for invalid login
  - Token-based authentication (JWT)

- **User Registration**
  - New account creation
  - Email validation
  - Password confirmation

- **Google OAuth2 Login**
  - Simplified authentication
  - Deep linking support with custom URI scheme `unigear://auth`
  - For emulator: Use backend URL setup for OAuth flow

### Equipment Catalog
- Browse all available equipment
- Search by equipment name
- Filter by category (All, Microscopes, Glassware, Electronics, Safety, Chemicals)
- View equipment availability status
- Real-time equipment list from backend

### Equipment Details
- **Equipment Information**
  - Name, category, location, description
  - Detailed specifications
  - Availability status with color indicators

- **Image Gallery** (NEW)
  - Multi-image carousel for equipment
  - Prev/Next navigation controls
  - Image counter showing current position
  - Uses Glide library for efficient image loading
  - Placeholder support if images unavailable

- **Availability Calendar** (NEW)
  - Visual monthly calendar display
  - Month navigation (Previous/Next)
  - Color-coded dates:
    - **Red**: Already borrowed (unavailable)
    - **Gray**: Available for borrowing
  - Real-time availability from backend requests

- **Quick Request Button**
  - Pre-fills equipment name and category
  - Navigates to request form

### Equipment Requests
- **Create Requests**
  - Select equipment from catalog
  - Enter quantity needed
  - Add description
  - Select category
  - Status: PENDING (awaiting admin approval)

- **View Requests**
  - Two tabs: Active & History
  - See request status: Pending, Approved, Completed, Rejected
  - Request statistics: Total, Pending, Approved, Completed
  - Delete pending requests

### User Profile
- **View Profile**
  - User name, email, role (Student/Admin)
  - Join date
  - Profile picture with initials fallback
  
- **Edit Profile**
  - Change display name
  - Upload profile picture from gallery or camera
  - Real-time UI update

- **Navigation**
  - Quick access to all main features
  - Three-tab bottom navigation:
    - Catalog (home)
    - Requests (my requests)
    - Profile (account)

## Project Structure

```
mobile/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/unigear/tracker/mobile/
│   │       │   ├── LoginActivity.kt              # Login screen
│   │       │   ├── RegisterActivity.kt           # Registration screen
│   │       │   ├── OAuth2CallbackActivity.kt     # Google OAuth handler
│   │       │   ├── HomeActivity.kt               # Equipment catalog
│   │       │   ├── EquipmentDetailActivity.kt    # Equipment details with gallery & calendar
│   │       │   ├── MyRequestsActivity.kt         # Request management
│   │       │   ├── ProfileActivity.kt            # User profile
│   │       │   ├── SplashActivity.kt             # App splash screen
│   │       │   ├── AuthApiClient.kt              # Backend API communication
│   │       │   └── data classes (RequestItem, EquipmentItem, UserProfile, etc.)
│   │       └── res/
│   │           ├── layout/                       # XML layouts for each screen
│   │           ├── drawable/                     # UI components and icons
│   │           ├── values/                       # Colors, strings, styles
│   │           └── menu/                         # Navigation menus
│   └── build.gradle.kts
├── gradle/
│   └── wrapper/
└── README.md
```

## API Integration

All API calls go through `AuthApiClient.kt` singleton:

### Authentication Endpoints
- `POST /api/auth/register` - Create new user
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get logged-in user profile
- `PUT /api/auth/profile` - Update user profile
- `GET /api/auth/mobile/google` - Google OAuth2 flow

### Equipment Endpoints
- `GET /api/equipment` - List all equipment
- `GET /api/equipment/{id}` - Get equipment details (via request flow)

### Request Endpoints
- `GET /api/requests` - Get user's requests
- `POST /api/requests` - Create new request
- `DELETE /api/requests/{id}` - Delete request
- `PUT /api/requests/{id}` - Update request status (admin only)

## Network Configuration

### Backend URL Management
- **Default**: `http://10.0.2.2:8080` (for Android emulator)
- **Custom**: Stored in SharedPreferences under `unigear_config`
- **Update Flow**: Login screen → Long-press backend label → Enter URL

### Connection Details
- **Timeout**: 15 seconds for API calls, 5 seconds for connectivity check
- **Error Handling**: Graceful fallbacks with user-friendly messages
- **SSL**: Development uses HTTP (consider HTTPS for production)

## Data Storage

### SharedPreferences
- **unigear_auth**: Stores JWT token and user session
- **unigear_config**: Stores backend URL configuration

### Lifecycle
- Token cleared on logout
- Data persists on app close (user stays logged in)
- Manual logout required to clear session

## Features Shared with Web App

| Feature | Mobile | Web |
|---------|--------|-----|
| User Authentication (Email/Password) | ✅ | ✅ |
| Google OAuth2 | ✅ | ✅ |
| Equipment Catalog & Search | ✅ | ✅ |
| Equipment Details | ✅ | ✅ |
| Image Gallery | ✅ | ✅ (Unsplash integration) |
| Availability Calendar | ✅ | ✅ |
| Equipment Requests | ✅ | ✅ |
| User Profile | ✅ | ✅ |
| Admin Dashboard | ❌ | ✅ |

## Testing

### Device Testing
- **Emulator**: Use `10.0.2.2:8080` as backend URL
- **Physical Device**: Update backend URL to your PC's LAN IP
- Test with various screen sizes and orientations

### Recommended Test Cases
1. Login with valid/invalid credentials
2. Register new account
3. Browse and search equipment
4. View equipment details and calendar
5. Create, view, delete requests
6. Update profile picture
7. Network disconnection handling

## Build & Release

### Debug Build
```bash
# From Android Studio: Run → Run 'app'
# Or command line:
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
# Find APK in: app/build/outputs/apk/release/
```

## Troubleshooting

### Backend Connection Issues
- **Emulator**: Ensure backend runs on 10.0.2.2
- **Physical Device**: Use LAN IP (check `ipconfig` on Windows)
- **Firewall**: Allow port 8080 through Windows Firewall

### Image Gallery Not Loading
- Check Glide library is installed (`build.gradle.kts`)
- Verify images are accessible via URLs
- Check internet connectivity

### Calendar Not Showing Dates
- Verify backend is returning requests data
- Check token is valid (user is authenticated)
- Ensure request dates are properly formatted

### Google OAuth Redirect Issues
- Verify backend OAuth endpoint is accessible
- Check custom URI scheme `unigear://auth` is registered
- For physical device: Use valid LAN IP, not localhost

## Dependencies

Key libraries used:
- **Glide 4.16.0**: Image loading and caching
- **AndroidX Libraries**: Core components and compatibility

See `build.gradle.kts` for complete dependency list.
