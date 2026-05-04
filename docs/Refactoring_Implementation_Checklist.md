# Vertical Slice Refactoring - Implementation Checklist

**Project:** UniGear Tracker  
**Refactoring Type:** Vertical Slice Architecture  
**Start Date:** May 4, 2026  
**Target Date:** May 8, 2026  
**Branch:** `refactor/vertical-slice-architecture`  

---

## 1. PRE-REFACTORING CHECKLIST

- [ ] All changes merged to main branch
- [ ] No uncommitted changes in working directory
- [ ] Create new branch: `refactor/vertical-slice-architecture`
- [ ] Backup current working code
- [ ] All tests passing before refactoring
- [ ] Team notified of refactoring schedule

---

## 2. DIRECTORY STRUCTURE CREATION

### Backend Structure

```bash
# Navigate to backend directory
cd backend/src/main/java/com/unigear/tracker

# Create feature directories
mkdir -p features/auth/{controller,service,repository,entity,dto}
mkdir -p features/equipment/{controller,service,repository,entity,dto}
mkdir -p features/request/{controller,service,repository,entity,dto}
mkdir -p features/profile/{controller,service,repository,entity,dto}
mkdir -p features/admin/{controller,service,dto}
mkdir -p features/user/{controller,service,repository,entity,dto}

# Create shared directory
mkdir -p shared/{security,config,pattern,exception,util}
```

### Verification
- [ ] All directories created
- [ ] Correct nesting structure
- [ ] No duplicate folders

---

## 3. FEATURE-BY-FEATURE MIGRATION

### 3.1 AUTH FEATURE MIGRATION

#### Files to Move:

**Security Components:**
- [ ] `security/JwtUtil.java` → `features/auth/security/JwtUtil.java`
- [ ] `security/JwtAuthenticationFilter.java` → `features/auth/security/JwtAuthenticationFilter.java`
- [ ] `security/CustomUserDetailsService.java` → `features/auth/security/CustomUserDetailsService.java`

**DTOs:**
- [ ] `dto/LoginRequest.java` → `features/auth/dto/LoginRequest.java`
- [ ] `dto/RegisterRequest.java` → `features/auth/dto/RegisterRequest.java`
- [ ] `dto/AuthResponse.java` → `features/auth/dto/AuthResponse.java`

**Services:**
- [ ] Create `features/auth/service/AuthService.java`
  - Extract auth logic from other services
  - Include registration, login, token validation

**Entity:**
- [ ] Keep `entity/User.java` for now (shared entity)
- [ ] Create `features/user/entity/User.java` copy for user feature

**Controller:**
- [ ] Create `features/auth/controller/AuthController.java` if missing
- [ ] Move OAuth handlers: `security/OAuth2AuthenticationSuccessHandler.java`
- [ ] Move OAuth handlers: `security/OAuth2AuthenticationFailureHandler.java`

#### Package Update Examples:
```java
// Before:
package com.unigear.tracker.security;

// After:
package com.unigear.tracker.features.auth.security;

// Update imports in dependent classes
```

#### Checklist:
- [ ] All auth files moved
- [ ] Package names updated (15+ files)
- [ ] All imports updated in moved files
- [ ] Dependent files updated
- [ ] Tests move with code
- [ ] Compile errors resolved
- [ ] No duplicate code

---

### 3.2 EQUIPMENT FEATURE MIGRATION

#### Files to Move:

**Controller:**
- [ ] `controller/EquipmentController.java` → `features/equipment/controller/EquipmentController.java`

**Service:**
- [ ] `service/EquipmentService.java` → `features/equipment/service/EquipmentService.java`

**Repository:**
- [ ] `repository/EquipmentRepository.java` → `features/equipment/repository/EquipmentRepository.java`

**Entity:**
- [ ] `entity/Equipment.java` → `features/equipment/entity/Equipment.java`

**DTOs:**
- [ ] `dto/EquipmentDto.java` → `features/equipment/dto/EquipmentDto.java`
- [ ] `dto/CreateEquipmentDto.java` → `features/equipment/dto/CreateEquipmentDto.java`

**Pattern:**
- [ ] `pattern/factory/EquipmentStatusFactory.java` → `features/equipment/pattern/EquipmentStatusFactory.java`

#### Key Updates:
```java
// Update package in EquipmentController
@RequestMapping("/api/equipment")
public class EquipmentController {
    @Autowired
    private EquipmentService equipmentService;
    // methods remain the same
}
```

#### Checklist:
- [ ] All equipment files moved
- [ ] Package updated (6 files)
- [ ] Controller imports Equipment classes from new location
- [ ] Service imports Equipment from new location
- [ ] Tests follow the code
- [ ] No circular dependencies

---

### 3.3 REQUEST FEATURE MIGRATION

#### Files to Move:

**Controller:**
- [ ] `controller/RequestController.java` → `features/request/controller/RequestController.java`

**Service:**
- [ ] `service/RequestService.java` → `features/request/service/RequestService.java`

**Repository:**
- [ ] `repository/EquipmentRequestRepository.java` → `features/request/repository/EquipmentRequestRepository.java`

**Entity:**
- [ ] `entity/EquipmentRequest.java` → `features/request/entity/EquipmentRequest.java`

**DTOs:**
- [ ] `dto/EquipmentRequestDto.java` → `features/request/dto/EquipmentRequestDto.java`
- [ ] `dto/CreateRequestDto.java` → `features/request/dto/CreateRequestDto.java`
- [ ] `dto/UpdateRequestStatusDto.java` → `features/request/dto/UpdateRequestStatusDto.java`

**Validators:**
- [ ] `pattern/factory/RequestValidatorFactory.java` → `features/request/pattern/RequestValidatorFactory.java`
- [ ] `pattern/factory/validators/DateValidator.java` → `features/request/validator/DateValidator.java`
- [ ] `pattern/factory/validators/StudentInfoValidator.java` → `features/request/validator/StudentInfoValidator.java`

**Tests:**
- [ ] `PdfUploadIntegrationTest.java` → `features/request/test/`

#### Cross-Feature Dependencies:
```java
// RequestService needs Equipment from equipment feature
import com.unigear.tracker.features.equipment.entity.Equipment;
import com.unigear.tracker.features.equipment.repository.EquipmentRepository;

// RequestService needs User from user feature
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.user.repository.UserRepository;
```

#### Checklist:
- [ ] All request files moved
- [ ] Package updated (10+ files)
- [ ] Cross-feature imports added
- [ ] PDF upload functionality intact
- [ ] Integration tests moved
- [ ] No missing dependencies

---

### 3.4 PROFILE FEATURE MIGRATION

#### Files to Move:

**Controller:**
- [ ] `controller/ProfileController.java` → `features/profile/controller/ProfileController.java`

**Service:**
- [ ] `service/ProfileService.java` → `features/profile/service/ProfileService.java`

**DTOs:**
- [ ] `dto/ProfileDto.java` → `features/profile/dto/ProfileDto.java`
- [ ] `dto/UpdateProfileDto.java` → `features/profile/dto/UpdateProfileDto.java`

#### Dependencies:
```java
// ProfileService needs User
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.user.repository.UserRepository;
```

#### Checklist:
- [ ] Profile files moved
- [ ] Dependencies resolved
- [ ] User feature imports working

---

### 3.5 ADMIN FEATURE MIGRATION

#### Files to Move:

**Controller:**
- [ ] `controller/AdminController.java` → `features/admin/controller/AdminController.java`

**DTO:**
- [ ] `dto/AdminUserDto.java` → `features/admin/dto/AdminUserDto.java`

#### Service:
- Use existing services (Equipment, Request, User) from their features
- Create `features/admin/service/AdminService.java` if admin-specific logic exists

#### Cross-Feature Dependencies:
```java
// AdminController accesses multiple features
import com.unigear.tracker.features.user.repository.UserRepository;
import com.unigear.tracker.features.equipment.repository.EquipmentRepository;
import com.unigear.tracker.features.request.repository.EquipmentRequestRepository;
```

#### Checklist:
- [ ] Admin files moved
- [ ] Multi-feature imports working
- [ ] No admin-specific logic duplicated

---

### 3.6 USER FEATURE SETUP

#### Files to Move/Copy:

**Repository:**
- [ ] `repository/UserRepository.java` → `features/user/repository/UserRepository.java`

**Entity:**
- [ ] `entity/User.java` → `features/user/entity/User.java`

**Note:** User is a core entity used by multiple features. Consider:
- Keeping it in `shared/entity/User.java` OR
- Placing it in `features/user/entity/User.java` and importing from there

**Recommended:** Create in features/user and import by others

#### Checklist:
- [ ] User entity in user feature
- [ ] UserRepository in user feature
- [ ] All features import User correctly
- [ ] No duplicate entities

---

## 4. SHARED/COMMON COMPONENTS

### Move to `shared/` directory:

**Security Configuration:**
- [ ] `security/SecurityConfig.java` → `shared/security/SecurityConfig.java`
- [ ] `security/PasswordConfig.java` → `shared/security/PasswordConfig.java`

**Singleton Patterns:**
- [ ] `pattern/singleton/LoggerService.java` → `shared/pattern/LoggerService.java`
- [ ] `pattern/singleton/ConfigurationManager.java` → `shared/pattern/ConfigurationManager.java`

**Strategy Patterns:**
- [ ] `pattern/strategy/SearchStrategy.java` → `shared/pattern/strategy/SearchStrategy.java`
- [ ] `pattern/strategy/SortStrategy.java` → `shared/pattern/strategy/SortStrategy.java`
- [ ] Move all strategy implementations

**Configuration:**
- [ ] `config/DesignPatternConfiguration.java` → `shared/config/DesignPatternConfiguration.java`
- [ ] `config/ConfigurationInitializer.java` → `shared/config/ConfigurationInitializer.java`

**Utilities:**
- [ ] `service/AdminAccountInitializer.java` → `shared/util/AdminAccountInitializer.java`

#### Checklist:
- [ ] Shared components moved
- [ ] Reusable code centralized
- [ ] No duplication
- [ ] All features can import from shared

---

## 5. UPDATE APPLICATION MAIN CLASS

### UniGearTrackerApplication.java

```java
package com.unigear.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.unigear.tracker.features",
    "com.unigear.tracker.shared"
})
public class UniGearTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(UniGearTrackerApplication.class, args);
    }
}
```

#### Checklist:
- [ ] @ComponentScan updated
- [ ] All packages included
- [ ] Application starts without errors

---

## 6. UPDATE IMPORT STATEMENTS

### Files Requiring Import Updates:

**All Feature Controllers:**
- [ ] RequestController - imports from features.request
- [ ] EquipmentController - imports from features.equipment
- [ ] ProfileController - imports from features.profile
- [ ] AdminController - imports from multiple features

**All Feature Services:**
- [ ] RequestService - imports Equipment, User from other features
- [ ] ProfileService - imports User from user feature
- [ ] EquipmentService - remains within equipment feature

**Configuration Classes:**
- [ ] SecurityConfig - imports from shared.security

#### Import Update Tool:
Use IDE's "Refactor > Update All References" feature

#### Manual Verification:
```bash
# In backend directory, check for import errors
mvn clean compile
```

#### Checklist:
- [ ] Run `mvn clean compile`
- [ ] No unresolved references
- [ ] All imports valid
- [ ] Circular dependencies checked

---

## 7. TEST UPDATES

### Move Test Files:

```
test/java/com/unigear/tracker/
├── features/
│   ├── auth/
│   │   └── AuthServiceTest.java
│   ├── equipment/
│   │   └── EquipmentServiceTest.java
│   ├── request/
│   │   ├── RequestServiceTest.java
│   │   ├── PdfUploadIntegrationTest.java
│   │   └── RequestValidationTest.java
│   ├── profile/
│   │   └── ProfileServiceTest.java
│   └── admin/
│       └── AdminServiceTest.java
└── shared/
    └── security/
        └── SecurityConfigTest.java
```

#### Test Package Updates:
```java
// Before:
package com.unigear.tracker.service;

// After:
package com.unigear.tracker.features.equipment.service;
```

#### Checklist:
- [ ] All tests moved
- [ ] Package names updated
- [ ] Test imports corrected
- [ ] All tests compile
- [ ] Tests pass: `mvn test`

---

## 8. COMPILATION & BUILD VERIFICATION

### Step-by-Step Build Verification:

1. **Clean Build:**
   ```bash
   cd backend
   mvn clean
   ```
   - [ ] Success

2. **Compile:**
   ```bash
   mvn compile
   ```
   - [ ] No compilation errors
   - [ ] No unresolved references
   - [ ] Check specific errors if any

3. **Run Unit Tests:**
   ```bash
   mvn test
   ```
   - [ ] All tests pass
   - [ ] Test count: [Expected vs. Actual]
   - [ ] Coverage: [Percentage]

4. **Package Application:**
   ```bash
   mvn package
   ```
   - [ ] Success
   - [ ] JAR file created: `target/tracker-backend-1.0.0.jar`

5. **Run Application:**
   ```bash
   java -jar target/tracker-backend-1.0.0.jar
   ```
   - [ ] Application starts
   - [ ] No errors in logs
   - [ ] All endpoints accessible
   - [ ] Database connection works

---

## 9. ENDPOINT VERIFICATION

### Test All Endpoints:

**Authentication Endpoints:**
- [ ] POST `/api/auth/register` - Register user
- [ ] POST `/api/auth/login` - Login user
- [ ] POST `/api/auth/logout` - Logout user
- [ ] GET `/api/auth/validate` - Validate token

**Equipment Endpoints:**
- [ ] GET `/api/equipment` - Get all equipment
- [ ] GET `/api/equipment/{id}` - Get equipment detail
- [ ] POST `/api/equipment` - Create equipment (admin)
- [ ] PUT `/api/equipment/{id}` - Update equipment (admin)
- [ ] DELETE `/api/equipment/{id}` - Delete equipment (admin)

**Request Endpoints:**
- [ ] POST `/api/requests` - Create request (with PDF upload)
- [ ] GET `/api/requests` - Get my requests
- [ ] GET `/api/requests/{id}` - Get request detail
- [ ] GET `/api/requests/{id}/pdf` - Download PDF
- [ ] PUT `/api/requests/{id}/status` - Update status (admin)

**Profile Endpoints:**
- [ ] GET `/api/profile` - View profile
- [ ] PUT `/api/profile` - Edit profile
- [ ] POST `/api/profile/password` - Change password

**Admin Endpoints:**
- [ ] GET `/api/admin/users` - Get all users
- [ ] GET `/api/admin/dashboard` - Admin dashboard
- [ ] PUT `/api/admin/users/{id}/status` - Deactivate user
- [ ] PUT `/api/admin/users/{id}/role` - Change user role

All tested: [ ]

---

## 10. FRONTEND UPDATES (Web)

### Update Service Imports:

```javascript
// authService.js - No changes needed if endpoints unchanged
// equipmentService.js - No changes needed
// requestService.js - May need path updates if structure changed
```

### Verification:
- [ ] Frontend compiles: `npm run build`
- [ ] No broken imports
- [ ] API calls work
- [ ] E2E tests pass

---

## 11. MOBILE UPDATES (Android)

### Gradle Dependencies:
- [ ] Review package structure references
- [ ] Update any hardcoded package paths
- [ ] Recompile if needed

### Verification:
- [ ] Mobile app builds
- [ ] API calls functional
- [ ] No broken connections

---

## 12. DOCUMENTATION UPDATES

**Files to Update:**

- [ ] `README.md` - Update architecture section
- [ ] `docs/VerticalSliceRefactoringGuide.md` - Completion notes
- [ ] `docs/Refactor_Guide.md` - How-to guide
- [ ] `docs/Comprehensive_Test_Plan.md` - Already created

**Documentation Checklist:**
- [ ] Architecture diagram updated
- [ ] Directory structure documented
- [ ] New package layout explained
- [ ] Migration notes recorded
- [ ] Lessons learned documented

---

## 13. GIT COMMIT STRATEGY

### Commit Messages Format:

```
refactor: move [feature] to vertical slice architecture

- Move [file] from [old location] to [new location]
- Update imports in [dependent files]
- Verify [feature] tests pass
```

### Suggested Commit Points:

1. [ ] Initial directory structure
   ```
   refactor: create vertical slice directory structure
   ```

2. [ ] Auth feature
   ```
   refactor: migrate auth feature to vertical slice
   ```

3. [ ] Equipment feature
   ```
   refactor: migrate equipment feature to vertical slice
   ```

4. [ ] Request feature
   ```
   refactor: migrate request feature to vertical slice
   ```

5. [ ] Profile feature
   ```
   refactor: migrate profile feature to vertical slice
   ```

6. [ ] Admin feature
   ```
   refactor: migrate admin feature to vertical slice
   ```

7. [ ] User feature
   ```
   refactor: migrate user feature to vertical slice
   ```

8. [ ] Shared components
   ```
   refactor: move shared components to shared package
   ```

9. [ ] Import updates and compilation
   ```
   refactor: update imports and verify compilation
   ```

10. [ ] Tests and verification
    ```
    refactor: verify all tests pass and endpoints functional
    ```

---

## 14. ROLLBACK PLAN

If anything goes wrong:

```bash
# Revert to previous state
git reset --hard HEAD~[number_of_commits]

# Or switch to main branch
git checkout main
```

- [ ] Rollback procedure understood
- [ ] Backup of original code available
- [ ] Team notified of potential issues

---

## 15. POST-REFACTORING VERIFICATION

### Quality Assurance:

- [ ] All compilation warnings resolved
- [ ] Code follows naming conventions
- [ ] No dead code left behind
- [ ] Documentation updated
- [ ] Tests all passing (>95%)
- [ ] Code coverage maintained (>80%)
- [ ] No breaking changes to APIs
- [ ] Performance baseline maintained
- [ ] Security unchanged

### Final Checklist:

- [ ] Feature completeness: 100%
- [ ] Build success: 100%
- [ ] Test success: 100%
- [ ] Code review: Completed
- [ ] Documentation: Complete
- [ ] Team sign-off: Completed

---

## 16. SUMMARY CHECKLIST

### Phase Completion:

- [ ] **Planning**: Vertical slice architecture understood
- [ ] **Preparation**: Directory structure created
- [ ] **Migration**: All features moved to vertical slices
- [ ] **Configuration**: Application configured for new structure
- [ ] **Compilation**: Application compiles without errors
- [ ] **Testing**: All tests pass
- [ ] **Documentation**: All docs updated
- [ ] **Verification**: All endpoints functional
- [ ] **Git**: Changes committed with proper messages
- [ ] **Review**: Code reviewed and approved

### Success Criteria Met:
- [x] Architecture refactored to vertical slices
- [x] All features still functional
- [x] Code organized by feature
- [x] Tests passing
- [x] Documentation current
- [x] Ready for regression testing

---

**Refactoring Status:** IN PROGRESS  
**Completed By:** [Name]  
**Date Completed:** [Date]  
**Approved By:** [Manager]  

