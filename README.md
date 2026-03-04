# UniGear Tracker System

> A comprehensive uniform and gear management system for educational institutions, organizations, and teams to efficiently track and manage uniform distribution, equipment inventory, and gear allocation.

## 🎯 Overview

UniGear Tracker is a full-stack web application designed to streamline the management of uniforms, equipment, and gear. The system provides secure user authentication, inventory tracking, and distribution management capabilities.

### Current Features

- ✅ **User Authentication** - Secure registration and login system
- ✅ **Password Security** - BCrypt password hashing
- ✅ **Email Validation** - Duplicate prevention and format checking
- ✅ **User Dashboard** - Personalized user interface
- ✅ **Cloud Database** - Supabase PostgreSQL integration
- ✅ **RESTful API** - Clean and documented endpoints
- ✅ **Responsive Design** - Mobile and desktop compatible

---

## 🛠️ Technology Stack

### Backend
- **Java 19** - Modern Java features and performance
- **Spring Boot 3.2.0** - Enterprise application framework
- **Spring Data JPA** - Object-relational mapping
- **Supabase PostgreSQL** - Cloud-hosted database
- **BCrypt** - Industry-standard password hashing
- **Maven** - Build automation and dependency management
- **Hibernate** - JPA implementation

### Frontend
- **React 18** - Modern UI library
- **React Router DOM** - Client-side routing
- **Axios** - Promise-based HTTP client
- **CSS3** - Custom styling with modern design

### Infrastructure
- **Supabase** - Backend-as-a-Service platform
- **RESTful API** - Standard HTTP communication
- **CORS** - Cross-origin resource sharing enabled

---

## 📁 Project Structure

```
IT342_UniGear-Tracker_G4_Ybanez/
├── backend/                          # Spring Boot Application
│   ├── src/main/java/com/unigear/tracker/
│   │   ├── UniGearTrackerApplication.java
│   │   ├── entity/User.java          # User entity model
│   │   ├── repository/UserRepository.java
│   │   ├── service/AuthService.java  # Business logic
│   │   ├── controller/AuthController.java  # REST endpoints
│   │   ├── dto/                      # Request/Response DTOs
│   │   └── config/CorsConfig.java    # CORS configuration
│   ├── src/main/resources/
│   │   └── application.properties    # Database config
│   └── pom.xml                       # Maven dependencies
│
├── web/                              # React Application
│   ├── src/
│   │   ├── components/
│   │   │   ├── Register.js           # Registration page
│   │   │   ├── Login.js              # Login page
│   │   │   ├── Dashboard.js          # User dashboard
│   │   │   ├── Auth.css              # Auth pages styling
│   │   │   └── Dashboard.css         # Dashboard styling
│   │   ├── services/authService.js   # API communication
│   │   ├── App.js                    # Router configuration
│   │   └── index.js                  # Entry point
│   └── package.json                  # npm dependencies
│
└── docs/                             # Documentation
    └── UniGear Tracker System Design Document.pdf
```

---

## 🚀 Setup and Installation

### Prerequisites
- **Java 19** or higher
- **Node.js 16** or higher
- **Maven** (or use IntelliJ IDEA)
- **Supabase account** (free tier)
- **Git**

### 1. Clone Repository
```bash
git clone <repository-url>
cd IT342_UniGear-Tracker_G4_Ybanez
```

### 2. Database Setup (Supabase)

1. Create account at [supabase.com](https://supabase.com)
2. Create new project named "UniGear Tracker"
3. Get connection details from Settings → Database → Connection String
4. Update `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://[YOUR-POOLER-HOST]:6543/postgres
spring.datasource.username=postgres.[YOUR-PROJECT-ID]
spring.datasource.password=[YOUR-PASSWORD]
```

### 3. Backend Setup

Using Command Line:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Or using IntelliJ IDEA:
1. Open `backend` folder as Maven project
2. Click Run button (▶️) or press `Shift + F10`

**Backend runs on:** `http://localhost:8080`

### 4. Frontend Setup

```bash
cd web
npm install
npm start
```

**Frontend runs on:** `http://localhost:3000`

---

## 🔌 API Endpoints

### Authentication Endpoints

#### 1. Register User
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

Request Body:
{
  "name": "Juan Dela Cruz",
  "email": "juan@example.com",
  "password": "password123"
}

Response (201 Created):
{
  "id": 1,
  "name": "Juan Dela Cruz",
  "email": "juan@example.com",
  "message": "Registration successful"
}
```

#### 2. Login User
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

Request Body:
{
  "email": "juan@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "id": 1,
  "name": "Juan Dela Cruz",
  "email": "juan@example.com",
  "message": "Login successful"
}
```

---

## 💾 Database Schema

### `users` Table

| Column     | Type         | Constraints                     | Description                |
|------------|--------------|--------------------------------|----------------------------|
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT    | Unique user identifier     |
| name       | VARCHAR(255) | NOT NULL                       | User's full name           |
| email      | VARCHAR(255) | NOT NULL, UNIQUE               | User's email (login ID)    |
| password   | VARCHAR(255) | NOT NULL                       | BCrypt hashed password     |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP      | Account creation date      |

**Note:** The table is automatically created by Hibernate when the application starts.

---

## 🔒 Security Features

UniGear Tracker implements industry-standard security practices:

1. **BCrypt Password Hashing**
   - All passwords hashed using BCrypt (strength 10)
   - Irreversible cryptographic hashing
   - Salt generation for each password
   - Protection against rainbow table attacks

2. **Email Validation & Uniqueness**
   - Valid email format enforcement (RFC 5322)
   - Duplicate email prevention at database level
   - Case-insensitive email checking
   - UNIQUE constraint on email column

3. **Comprehensive Input Validation**
   - **Name**: Minimum 2 characters, required
   - **Email**: Valid format, unique, required
   - **Password**: Minimum 6 characters, required
   - **Frontend validation**: Immediate user feedback
   - **Backend validation**: Security enforcement

4. **CORS Security**
   - Configured allowed origins
   - Controlled HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
   - Prevents unauthorized cross-origin requests
   - Production-ready configuration

5. **Session Management**
   - Client-side secure storage
   - Protected routes and navigation guards
   - Automatic logout functionality
   - Session expiration handling

---

## 📝 System Architecture

### Authentication Flow

**User Registration Process**
1. User submits registration form with name, email, and password
2. Frontend validates input fields (required, format, length)
3. API request sent to `POST /api/auth/register`
4. Backend validates data and checks for duplicate email
5. Password is hashed using BCrypt algorithm
6. User record created in database
7. Success response returned to client
8. User redirected to login page

**User Login Process**
1. User enters email and password credentials
2. Frontend validates input format
3. API request sent to `POST /api/auth/login`
4. Backend retrieves user record by email
5. BCrypt verifies password against stored hash
6. User data returned on successful verification
7. Session established in browser storage
8. User redirected to dashboard

**Dashboard Access**
- Displays personalized welcome message
- Shows user profile information (ID, name, email)
- Provides access to system features
- Secure logout functionality
- Protected route (redirects to login if unauthenticated)

---

## 🧪 Usage Guide

### Starting the Application

**Step 1: Start Backend Server**
```bash
cd backend
mvn spring-boot:run
```
✅ Backend runs on `http://localhost:8080`

**Step 2: Start Frontend Application**
```bash
cd web
npm start
```
✅ Frontend runs on `http://localhost:3000`

### Using the System

**Register a New Account**
1. Navigate to `http://localhost:3000/register`
2. Fill in your information:
   - Full Name
   - Email Address
   - Password (minimum 6 characters)
   - Confirm Password
3. Click **Register**
4. You'll be redirected to the login page

**Login to Your Account**
1. Navigate to `http://localhost:3000/login`
2. Enter your registered email and password
3. Click **Login**
4. Access your personalized dashboard

**Dashboard Features**
- View your profile information
- Access system features (coming in future updates)
- Secure logout functionality

### Verifying Database
To check registered users in Supabase:
1. Go to your Supabase project dashboard
2. Navigate to **Table Editor**
3. Select the `users` table
4. View all registered users (passwords are securely hashed)

---

## 🔐 Security Implementation

UniGear Tracker implements multiple security layers:

1. **Password Protection**
   - BCrypt hashing algorithm (strength 10)
   - Passwords never stored in plain text
   - Secure password verification

2. **Email Security**
   - Format validation
   - Duplicate prevention
   - Unique constraint enforcement

3. **Input Validation**
   - Frontend validation for immediate feedback
   - Backend validation for security
   - Sanitized database queries

4. **Session Security**
   - Client-side session management
   - Protected routes
   - Secure logout process

5. **API Security**
   - CORS configuration
   - Validated request bodies
   - Error handling without information leakage

---

## 🚀 Future Features

Planned enhancements for upcoming releases:

- 📦 **Inventory Management** - Track uniforms and equipment
- 👥 **User Roles** - Admin, Manager, and User permissions
- 📊 **Reports & Analytics** - Distribution and usage statistics
- 🔍 **Search & Filter** - Advanced item search capabilities
- 📱 **Mobile App** - Native mobile applications
- 🔔 **Notifications** - Email and in-app notifications
- 📄 **PDF Reports** - Generate distribution reports
- 🖼️ **Image Upload** - Item photos and documentation

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

---

## 📧 Contact

For questions, support, or feedback about the UniGear Tracker system, please open an issue in this repository.

---

## 📄 License

© 2026 UniGear Tracker System. All rights reserved.

## Notes

- This is Phase 1 of the project focusing on authentication
- Password is securely hashed and never stored in plain text
- All API endpoints use JSON for data exchange
- Frontend includes comprehensive form validation
- Backend includes error handling and validation
