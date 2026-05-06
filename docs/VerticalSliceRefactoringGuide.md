# Vertical Slice Refactoring Guide - UniGear Tracker

## Current Architecture (Layered/N-Tier)
```
src/main/java/com/unigear/tracker/
├── controller/          (All controllers)
├── service/             (All services)
├── repository/          (All repositories)
├── entity/              (All entities)
├── dto/                 (All DTOs)
├── security/            (All security components)
├── config/              (All configurations)
└── pattern/             (Design patterns)
```

## Target Architecture (Vertical Slice)
```
src/main/java/com/unigear/tracker/
├── features/
│   ├── auth/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── equipment/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── request/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── profile/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── admin/
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   └── user/
│       ├── controller/
│       ├── service/
│       ├── repository/
│       ├── entity/
│       └── dto/
├── shared/
│   ├── security/
│   ├── config/
│   ├── pattern/
│   └── exception/
└── UniGearTrackerApplication.java
```

## Identified Features/Slices

### 1. **Auth Feature** (Authentication & Authorization)
- **Files to Move:**
  - Controller: `AuthController` (create if doesn't exist, or extract from others)
  - Service: `AuthService` (create new service for auth logic)
  - Entity: `User.java` (partial - keep Role enum)
  - DTO: `LoginRequest`, `RegisterRequest`, `AuthResponse`
  - Security: `JwtUtil`, `JwtAuthenticationFilter`, `CustomUserDetailsService`

### 2. **Equipment Feature** (Equipment Management)
- **Files to Move:**
  - Controller: `EquipmentController`
  - Service: `EquipmentService`
  - Repository: `EquipmentRepository`
  - Entity: `Equipment.java`
  - DTO: `EquipmentDto`, `CreateEquipmentDto`
  - Pattern: `EquipmentStatusFactory`

### 3. **Request Feature** (Equipment Requests)
- **Files to Move:**
  - Controller: `RequestController`
  - Service: `RequestService`
  - Repository: `EquipmentRequestRepository`
  - Entity: `EquipmentRequest.java`
  - DTO: `EquipmentRequestDto`, `CreateRequestDto`, `UpdateRequestStatusDto`
  - Pattern: `RequestValidatorFactory`

### 4. **Profile Feature** (User Profile Management)
- **Files to Move:**
  - Controller: `ProfileController`
  - Service: `ProfileService`
  - DTO: `ProfileDto`, `UpdateProfileDto`

### 5. **Admin Feature** (Admin Operations)
- **Files to Move:**
  - Controller: `AdminController`
  - Service: May use shared services
  - DTO: `AdminUserDto`

### 6. **User Feature** (User Management - Core)
- **Files to Move:**
  - Repository: `UserRepository`
  - Entity: `User.java` (core entity)

### 7. **Shared/Common** (Reusable Components)
- **Files to Keep:**
  - `SecurityConfig`, `PasswordConfig` (config)
  - `OAuth2AuthenticationSuccessHandler`, `OAuth2AuthenticationFailureHandler`
  - `LoggerService`, `ConfigurationManager` (singleton patterns)
  - Strategy patterns for search and sort
  - Validators
  - Custom exceptions

## Migration Steps

### Step 1: Create Directory Structure
```bash
mkdir -p src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/dto
mkdir -p src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/controller
mkdir -p src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/service
mkdir -p src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/repository
mkdir -p src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/entity
mkdir -p src/main/java/com/unigear/tracker/shared/{security,config,pattern,exception}
```

### Step 2: Move and Update Imports
- Move files to appropriate feature folders
- Update package declarations
- Update import statements in dependent files

### Step 3: Create Feature Configuration Classes
- Each feature may have its own `@Configuration` class
- Register feature-specific beans

### Step 4: Update Spring Component Scanning
- Update `@ComponentScan` in main application class to include new packages

## Benefits of Vertical Slice Architecture

✅ **Better Organization**: Features are self-contained and easier to locate  
✅ **Improved Maintainability**: Changes to a feature are localized  
✅ **Easier Scaling**: New developers can understand a complete feature quickly  
✅ **Reduced Coupling**: Less cross-cutting dependencies  
✅ **Independent Testing**: Test each feature in isolation  
✅ **Flexible Deployment**: Features can be deployed independently  

## Testing Strategy

### Layer 1: Unit Tests
- Test each service, controller, and validator independently
- Mock dependencies
- Quick to run

### Layer 2: Integration Tests
- Test feature-level interactions
- Use `@SpringBootTest` with feature-specific context
- Test database interactions

### Layer 3: Smoke Tests
- High-level feature tests
- Ensure APIs work end-to-end
- Test happy paths

### Layer 4: Full Regression Tests
- Complete end-to-end testing across all features
- Validate no existing functionality is broken
- Record all test results

## Common Pitfalls to Avoid

❌ **Don't**: Create circular dependencies between features  
❌ **Don't**: Mix shared and feature-specific code  
❌ **Don't**: Forget to update all import statements  
❌ **Don't**: Break the build while refactoring  
❌ **Don't**: Refactor without version control  

## Success Criteria

✓ All features compile without errors  
✓ All existing tests pass  
✓ New feature structure is clear and organized  
✓ No circular dependencies exist  
✓ All APIs continue to work as before  
✓ Code follows naming conventions  
✓ Documentation is updated  
