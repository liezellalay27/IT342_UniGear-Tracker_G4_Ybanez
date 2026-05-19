# UniGear Tracker

UniGear Tracker is a comprehensive equipment borrowing and request management system designed for universities. It streamlines the process of sharing laboratory and research equipment across departments and students.

## System Overview

**UniGear Tracker** enables:

- **Students**: Browse available equipment, submit borrowing requests with date ranges, upload event approval documents, and track request status
- **Administrators**: Manage equipment inventory, approve/reject requests, track active loans, mark returns as on-time/late, and generate reports
- **Real-time Availability**: Equipment availability calendar shows which dates equipment is already booked
- **Multi-platform Access**: Use web app on desktop or mobile app on Android devices
- **Secure Authentication**: Email/password login and Google OAuth2 integration

## Key Features

✅ **Equipment Management**
- Browse and search equipment by name and category
- View detailed specifications, location, and real-time availability
- High-quality equipment images from Unsplash API

✅ **Request Management**
- Create borrowing requests with date range selection
- Upload event approval PDFs
- Track request status (Pending → Approved/Rejected → Completed)

✅ **Admin Dashboard**
- Overview: KPI cards for pending requests, active loans, low stock
- Equipment: Manage inventory and quantities
- Users: View all users and their roles
- Borrowed: Track active loans and process returns
- Requests: Review and approve/reject requests

✅ **Multi-Platform**
- Web app (React 18) with responsive design
- Android mobile app (Kotlin)
- Backend API (Spring Boot)

---

## Technology Stack

UniGear Tracker is a multi-platform project built with:

- **Backend**: Spring Boot 3.4.x Java API
- **Frontend**: React 18 web application
- **Mobile**: Android app (Kotlin)
- **Database**: PostgreSQL via Supabase
- **APIs**: Google OAuth2, Unsplash for images

This README reflects the current Phase 3 implementation status in this repository.

## Current Progress

### Backend (Spring Boot)

Implemented:

- User registration and login
- JWT token generation and JWT request filter
- Google OAuth2 login flow
- Mobile OAuth2 redirect support through /api/auth/mobile/google
- Profile APIs (view and update)
- Equipment request APIs (create, list, get by id, delete pending request)
- Spring Security route protection and stateless sessions
- Supabase/PostgreSQL persistence using Spring Data JPA

Main API groups:

- /api/auth
- /api/profile
- /api/requests

### Web (React) - **PHASE 3 COMPLETE** ✅

**Phase 3 Features (100% SDD Compliant):**

- Route-based app with protected routes and role-based access control
- User authentication (email/password + Google OAuth2)
- Equipment catalog with search and category filtering
- Equipment details with real Unsplash API image gallery
- Interactive availability calendar with real backend data
- Equipment request management (create, list, delete)
- PDF file upload for event approvals
- User profile management (view/edit profile picture)
- Admin dashboard with 5 management panels
  - Overview: KPI cards (pending requests, active loans, low stock, overdue)
  - Equipment: Add new inventory, track quantities
  - Users: View all users and roles
  - Borrowed: Track loans, mark returns, download documents
  - Requests: Approve/reject requests with optional notes

**Technical Improvements:**
- React 18 with functional components and hooks
- CSS Modules for scoped styling (no naming conflicts)
- .jsx file extension for React components
- Axios for API integration
- React Router DOM for client-side navigation

### Mobile (Android, Kotlin)

Implemented:

- Splash screen
- Email/password login
- Registration flow
- Google OAuth2 mobile flow via custom URI scheme
- OAuth2 callback activity and token persistence
- Home screen with searchable, filterable equipment catalog UI (currently sample data)

## Current Scope vs Next Scope

✅ **Phase 3 Complete:**

- ✅ Authentication (email/password + Google OAuth2)
- ✅ User profile management
- ✅ Equipment catalog with real data
- ✅ Equipment details with image gallery (Unsplash API)
- ✅ Item availability calendar (real backend data)
- ✅ Request management (create, list, delete)
- ✅ Admin dashboard (5 management panels)
- ✅ Request approval workflow
- ✅ PDF file upload & download
- ✅ Role-based access control
- ✅ CSS Modules for scoped styling

**Next Phase (Optional Enhancements):**

- Mobile Phase 4 features
- Advanced reporting and analytics
- Equipment maintenance scheduling
- User notifications and reminders
- QR code scanning for equipment checkout

## Tech Stack

**Backend:**
- Java 19, Spring Boot 3.4.x
- Spring Security, Spring Data JPA
- PostgreSQL via Supabase
- JWT authentication
- BCrypt password hashing

**Frontend (Web):**
- React 18 with functional components
- React Router DOM for navigation
- Axios for API calls
- CSS Modules for component-scoped styling
- JSX components (.jsx extension)
- External API: Unsplash for equipment images

**Mobile:**
- Android (Kotlin, SDK 35, min SDK 24)
- Google OAuth2 integration
- Native UI components

## Code Quality & Architecture

- **CSS Modules**: Scoped styling prevents naming conflicts
- **JSX Components**: Clear separation of React components (.jsx) from utilities (.js)
- **Modular Structure**: Components organized by feature/page
- **Environment Configuration**: .env for sensitive data
- **RESTful API**: Consistent endpoint design
- **Role-Based Access**: Token validation on frontend and backend

## Project Structure

```text
IT342_UniGear-Tracker_G4_Ybanez/
|- backend/   Spring Boot API
|- web/       React frontend
|- mobile/    Android app (Kotlin)
|- docs/      Project documentation
```

## Quick Start

### 1) Prerequisites

- Java 19+
- Maven 3.9+
- Node.js 18+ and npm
- Android Studio (for mobile)
- Supabase/PostgreSQL credentials
- Google OAuth client credentials (for OAuth login)

### 2) Backend Setup

Create backend/.env (or set equivalent environment variables) with:

```env
DB_URL=jdbc:postgresql://<host>:6543/postgres
DB_USERNAME=<db-username>
DB_PASSWORD=<db-password>

JWT_SECRET=<your-long-secret>
JWT_EXPIRATION=86400000

GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>
OAUTH2_REDIRECT_URI=http://localhost:3000
```

Run backend:

```bash
cd backend
mvn spring-boot:run
```

Backend default URL: http://localhost:8080

### 3) Web Setup

Optional web env file:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

Run web app:

```bash
cd web
npm install
npm start
```

Web default URL: http://localhost:3000

### 4) Mobile Setup

- Open mobile in Android Studio
- Use Android emulator when testing locally
- Backend base URL in mobile is set to http://10.0.2.2:8080 (emulator -> host machine)
- Build and run app module

## API Snapshot

Authentication:

- POST /api/auth/register
- POST /api/auth/login
- GET /api/auth/mobile/google?redirect_uri=unigear://auth

Profile:

- GET /api/profile
- PUT /api/profile

Requests:

- POST /api/requests
- GET /api/requests
- GET /api/requests/{id}
- DELETE /api/requests/{id}

All non-public APIs require Authorization: Bearer <jwt-token>.

## Notes

- The catalog UI in both web and mobile currently uses sample equipment data.
- The request feature is already connected to backend persistence.
- Security and OAuth flow are implemented, with platform-specific redirect handling.
