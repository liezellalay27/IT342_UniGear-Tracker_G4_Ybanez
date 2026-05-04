# UniGear Tracker - Comprehensive Software Test Plan

**Project Name:** UniGear Tracker  
**Version:** 1.0.0  
**Date Created:** May 4, 2026  
**Last Updated:** May 4, 2026  

---

## 1. EXECUTIVE SUMMARY

This Test Plan documents the comprehensive testing strategy for the UniGear Tracker system across all implemented features. It covers functional requirements, test cases, automated tests, and regression testing procedures to ensure system stability after vertical slice refactoring.

---

## 2. SCOPE & OBJECTIVES

### Scope
- **In Scope:**
  - Backend API functionality (Auth, Equipment, Requests, Profile, Admin, User)
  - Web Frontend (React)
  - Mobile Application (Android)
  - Database operations
  - Security validations
  - PDF upload/download functionality
  
- **Out of Scope:**
  - Performance testing
  - Load testing
  - Security penetration testing

### Testing Objectives
1. Validate all functional requirements work correctly
2. Ensure no regression after vertical slice refactoring
3. Verify data integrity and security
4. Test edge cases and error scenarios
5. Validate cross-platform functionality

---

## 3. FUNCTIONAL REQUIREMENTS COVERAGE

### 3.1 Authentication & Authorization (Auth Feature)

#### FR-1: User Registration
- **Description:** New users can register with email and password
- **Test Cases:**
  - TC-1.1: Register with valid credentials → Success
  - TC-1.2: Register with duplicate email → Error
  - TC-1.3: Register with invalid email format → Error
  - TC-1.4: Register with weak password → Error
  - TC-1.5: Register with missing fields → Error

#### FR-2: User Login
- **Description:** Registered users can login with credentials
- **Test Cases:**
  - TC-2.1: Login with correct credentials → JWT token returned
  - TC-2.2: Login with incorrect password → Error
  - TC-2.3: Login with non-existent user → Error
  - TC-2.4: Login with empty fields → Error

#### FR-3: OAuth2 Login (Google/GitHub)
- **Description:** Users can login using OAuth2 providers
- **Test Cases:**
  - TC-3.1: Login with Google OAuth → Account created/linked
  - TC-3.2: Login with GitHub OAuth → Account created/linked
  - TC-3.3: OAuth callback handling → Token generation
  - TC-3.4: Existing user OAuth login → Session established

#### FR-4: JWT Token Management
- **Description:** JWT tokens are properly generated, validated, and expired
- **Test Cases:**
  - TC-4.1: Valid JWT token grants access → Success
  - TC-4.2: Expired token rejected → 401 Unauthorized
  - TC-4.3: Invalid token rejected → 401 Unauthorized
  - TC-4.4: Missing token blocked → 401 Unauthorized
  - TC-4.5: Token refresh → New valid token

#### FR-5: Role-Based Access Control
- **Description:** Access controlled based on user roles (STUDENT, STAFF, ADMIN)
- **Test Cases:**
  - TC-5.1: Student accessing student endpoints → Success
  - TC-5.2: Student accessing admin endpoints → 403 Forbidden
  - TC-5.3: Admin accessing admin endpoints → Success
  - TC-5.4: Role-based endpoint protection → Verified

---

### 3.2 Equipment Management (Equipment Feature)

#### FR-6: View Equipment Catalog
- **Description:** Users can browse available equipment
- **Test Cases:**
  - TC-6.1: Fetch all equipment → List returned
  - TC-6.2: Filter by category → Filtered list
  - TC-6.3: Search by name → Matching results
  - TC-6.4: Pagination → 10 items per page
  - TC-6.5: Sort by availability → Correct order

#### FR-7: Equipment Details
- **Description:** View detailed information about equipment
- **Test Cases:**
  - TC-7.1: Get equipment detail → Full info returned
  - TC-7.2: Get non-existent equipment → 404 Not Found
  - TC-7.3: Equipment availability status → Correct display
  - TC-7.4: Equipment description/specs → Accurate data

#### FR-8: Equipment Availability
- **Description:** Track and display equipment availability
- **Test Cases:**
  - TC-8.1: Available equipment shows "Available" → Verified
  - TC-8.2: Borrowed equipment shows "Borrowed" → Verified
  - TC-8.3: Equipment count updates → Real-time update
  - TC-8.4: Multiple borrowers handled → Correct quantities

#### FR-9: Create Equipment (Admin Only)
- **Description:** Admins can add new equipment to catalog
- **Test Cases:**
  - TC-9.1: Admin creates equipment → Success
  - TC-9.2: Admin with all required fields → Equipment stored
  - TC-9.3: Missing required fields → Error
  - TC-9.4: Duplicate equipment name → Error/Warning
  - TC-9.5: Student creating equipment → 403 Forbidden

---

### 3.3 Equipment Requests (Request Feature)

#### FR-10: Create Equipment Request
- **Description:** Users can submit requests to borrow equipment
- **Test Cases:**
  - TC-10.1: Create with all required fields → Request created
  - TC-10.2: Create without PDF → Request created (PDF optional)
  - TC-10.3: Create with PDF attachment → PDF stored
  - TC-10.4: Request with invalid dates → Error
  - TC-10.5: Request duration validation → Must be future date
  - TC-10.6: Quantity exceeds availability → Error
  - TC-10.7: Missing required fields → Validation error

#### FR-11: PDF Upload for Event Approval
- **Description:** Users can upload PDF approval documents for events
- **Test Cases:**
  - TC-11.1: Upload valid PDF → File stored
  - TC-11.2: Upload non-PDF file → Rejected
  - TC-11.3: Upload file > 10MB → Rejected
  - TC-11.4: Upload multiple files → Only one stored
  - TC-11.5: PDF with valid headers → Accepted

#### FR-12: PDF Download
- **Description:** Users and admins can download stored PDFs
- **Test Cases:**
  - TC-12.1: User downloads own PDF → File downloaded
  - TC-12.2: User downloads other's PDF → 403 Forbidden
  - TC-12.3: Admin downloads any PDF → Success
  - TC-12.4: Download non-existent PDF → 404 Not Found
  - TC-12.5: PDF filename preserved → Original name in download

#### FR-13: View My Requests
- **Description:** Users can view their submitted requests
- **Test Cases:**
  - TC-13.1: View own requests → All requests shown
  - TC-13.2: View requests with status → Status displayed
  - TC-13.3: Empty request list → No error, empty display
  - TC-13.4: Pagination of requests → Works correctly
  - TC-13.5: Request details → Complete information shown

#### FR-14: Request Status Management
- **Description:** Admins can approve/reject requests
- **Test Cases:**
  - TC-14.1: Admin approves request → Status = "APPROVED"
  - TC-14.2: Admin rejects request → Status = "REJECTED"
  - TC-14.3: Admin leaves comment → Stored and displayed
  - TC-14.4: Status change notification → User notified
  - TC-14.5: Cannot change completed request → Error

#### FR-15: Track Borrowed Equipment
- **Description:** Admins can track all borrowed equipment
- **Test Cases:**
  - TC-15.1: View all borrowed items → Complete list
  - TC-15.2: Filter by status → Filtered correctly
  - TC-15.3: View return dates → Correct display
  - TC-15.4: Track overdue items → Highlighted
  - TC-15.5: View associated PDFs → Download links available

---

### 3.4 User Profile Management (Profile Feature)

#### FR-16: View Profile
- **Description:** Users can view their profile information
- **Test Cases:**
  - TC-16.1: View own profile → All data displayed
  - TC-16.2: View another user's profile → Limited data (if allowed)
  - TC-16.3: Profile fields populated → Correct data
  - TC-16.4: Profile picture display → Shows correctly

#### FR-17: Edit Profile
- **Description:** Users can update their profile information
- **Test Cases:**
  - TC-17.1: Update name → Saved and displayed
  - TC-17.2: Update email → Validation performed
  - TC-17.3: Update phone → Validated
  - TC-17.4: Update profile picture → Image uploaded and displayed
  - TC-17.5: Cancel edit → Changes discarded

#### FR-18: Password Management
- **Description:** Users can change their password
- **Test Cases:**
  - TC-18.1: Change password with correct old password → Success
  - TC-18.2: Change password with incorrect old password → Error
  - TC-18.3: New password same as old → Error
  - TC-18.4: Password strength validation → Enforced
  - TC-18.5: Password change notification → Email sent

---

### 3.5 Admin Functions (Admin Feature)

#### FR-19: Admin Dashboard
- **Description:** Admins view system overview and manage content
- **Test Cases:**
  - TC-19.1: Access admin dashboard → Only for admins
  - TC-19.2: Dashboard statistics display → Correct data
  - TC-19.3: Quick action buttons → Functional
  - TC-19.4: Admin menu visible → Correctly displayed

#### FR-20: User Management (Admin)
- **Description:** Admins can manage user accounts
- **Test Cases:**
  - TC-20.1: View all users → User list displayed
  - TC-20.2: Deactivate user → Status changed
  - TC-20.3: Change user role → Role updated
  - TC-20.4: Delete user → User removed
  - TC-20.5: Search users → Find specific user

#### FR-21: Equipment Management (Admin)
- **Description:** Admins manage equipment inventory
- **Test Cases:**
  - TC-21.1: Create new equipment → Added to catalog
  - TC-21.2: Edit equipment details → Changes saved
  - TC-21.3: Delete equipment → Removed from catalog
  - TC-21.4: Update quantities → Inventory adjusted
  - TC-21.5: Archive equipment → Removed from active list

#### FR-22: Request Approval Workflow
- **Description:** Admins process equipment requests
- **Test Cases:**
  - TC-22.1: Approve pending request → Status updated
  - TC-22.2: Reject with comment → User notified
  - TC-22.3: Request deadline management → Enforced
  - TC-22.4: Multiple simultaneous requests → Handled correctly
  - TC-22.5: Request history → All changes tracked

---

## 4. TEST CASES DETAILED SPECIFICATION

### Test Case Format

```
Test Case ID: TC-[Feature]-[Number]
Test Case Name: [Descriptive name]
Preconditions: [What must be true before test]
Test Steps:
  1. [Step 1]
  2. [Step 2]
  ...
Expected Result: [What should happen]
Actual Result: [To be filled during testing]
Status: PASS / FAIL / BLOCKED
Severity: CRITICAL / HIGH / MEDIUM / LOW
```

### Example Test Case

```
Test Case ID: TC-AUTH-1.1
Test Case Name: User Registration with Valid Credentials
Preconditions: 
  - User is on registration page
  - Email not previously registered
  - No database errors
Test Steps:
  1. Enter valid email (format: user@example.com)
  2. Enter valid password (min 8 chars, 1 uppercase, 1 number)
  3. Confirm password
  4. Click "Register" button
Expected Result:
  - Registration successful
  - User redirected to login page or auto-logged in
  - Success message displayed
  - User data stored in database
Actual Result: [To be filled during testing]
Status: [To be filled]
Severity: CRITICAL
```

---

## 5. AUTOMATED TEST CASES

### Backend Integration Tests (Java/JUnit5/MockMvc)

#### 1. Auth Feature Tests
```
AuthServiceTest
  ✓ testRegisterNewUser() - Register valid user
  ✓ testRegisterDuplicateEmail() - Duplicate email rejected
  ✓ testLoginSuccess() - Login with correct credentials
  ✓ testLoginFailure() - Login with wrong password
  ✓ testJwtTokenGeneration() - JWT properly generated
  ✓ testJwtTokenValidation() - Token validated correctly
  ✓ testTokenExpiration() - Expired token rejected
  ✓ testRoleBasedAccess() - RBAC enforced
```

#### 2. Equipment Feature Tests
```
EquipmentServiceTest
  ✓ testGetAllEquipment() - List all equipment
  ✓ testGetEquipmentById() - Get specific equipment
  ✓ testCreateEquipment() - Create new equipment (admin only)
  ✓ testUpdateEquipment() - Update equipment details
  ✓ testDeleteEquipment() - Delete equipment
  ✓ testSearchEquipment() - Search by name/category
  ✓ testFilterByCategory() - Filter equipment
  ✓ testAvailabilityUpdate() - Availability status updated
```

#### 3. Request Feature Tests
```
RequestServiceTest
  ✓ testCreateRequest() - Create equipment request
  ✓ testUploadPdf() - Upload and store PDF
  ✓ testDownloadPdf() - Download stored PDF
  ✓ testPdfValidation() - PDF format validated
  ✓ testPdfSizeLimit() - File size limit enforced
  ✓ testGetRequestById() - Retrieve request details
  ✓ testUpdateRequestStatus() - Update status (admin)
  ✓ testGetRequestsByUser() - Get user's requests
  
RequestValidationTest
  ✓ testDateValidation() - Dates validated
  ✓ testQuantityValidation() - Quantity checked
  ✓ testMissingFieldsValidation() - Required fields enforced
  
PdfUploadIntegrationTest (Existing)
  ✓ testUploadValidPdf() - Valid PDF accepted
  ✓ testUploadWithoutPdf() - Optional PDF works
  ✓ testRejectNonPdfFile() - Non-PDF rejected
  ✓ testUserDownloadOwnPdf() - User downloads own PDF
  ✓ testAdminDownloadUserPdf() - Admin downloads PDF
  ✓ testUnauthorizedPdfDownload() - Access control
  ✓ testPdfDownloadWithoutAuth() - Auth required
  ✓ testRejectLargeFile() - File size limit
```

#### 4. Profile Feature Tests
```
ProfileServiceTest
  ✓ testGetUserProfile() - Retrieve profile
  ✓ testUpdateProfile() - Update user info
  ✓ testUploadProfilePicture() - Upload image
  ✓ testChangePassword() - Password change
  ✓ testPasswordValidation() - Password strength checked
```

#### 5. Admin Feature Tests
```
AdminServiceTest
  ✓ testGetAllUsers() - Admin views users
  ✓ testDeactivateUser() - Deactivate user account
  ✓ testChangeUserRole() - Change user role
  ✓ testGetAdminDashboard() - Dashboard data
  ✓ testApproveRequest() - Approve equipment request
  ✓ testRejectRequest() - Reject with comment
```

### Frontend E2E Tests (Cypress)

```javascript
describe('UniGear Tracker - Full E2E Tests', () => {
  
  // Auth Tests
  describe('Authentication Flow', () => {
    it('should register new user', () => {...})
    it('should login with credentials', () => {...})
    it('should logout user', () => {...})
    it('should handle OAuth login', () => {...})
  })
  
  // Equipment Tests
  describe('Equipment Catalog', () => {
    it('should display all equipment', () => {...})
    it('should filter by category', () => {...})
    it('should search equipment', () => {...})
    it('should show equipment details', () => {...})
  })
  
  // Request Tests
  describe('Equipment Requests', () => {
    it('should create new request', () => {...})
    it('should upload PDF', () => {...})
    it('should download PDF', () => {...})
    it('should view my requests', () => {...})
    it('should update request status', () => {...})
  })
  
  // Profile Tests
  describe('User Profile', () => {
    it('should view profile', () => {...})
    it('should edit profile', () => {...})
    it('should change password', () => {...})
  })
  
  // Admin Tests
  describe('Admin Functions', () => {
    it('should access admin dashboard', () => {...})
    it('should manage users', () => {...})
    it('should manage equipment', () => {...})
    it('should approve requests', () => {...})
  })
})
```

---

## 6. TEST EXECUTION PROCEDURE

### Pre-Test Checklist
- [ ] All code merged to main branch
- [ ] Database initialized with test data
- [ ] Test environment configured
- [ ] Credentials prepared
- [ ] Test data seeding completed
- [ ] API endpoints verified
- [ ] Frontend build successful

### Test Execution Steps

1. **Unit Tests**
   ```bash
   cd backend
   mvn test
   ```

2. **Integration Tests**
   ```bash
   mvn verify -Dtest=*IntegrationTest
   ```

3. **E2E Tests**
   ```bash
   cd web
   npm run cypress:run
   ```

4. **Full Regression Test**
   - Execute all manual test cases
   - Document results
   - Identify failures

### Test Data

#### Test Users
- **Student Account**: student@test.com / Password123
- **Admin Account**: admin@test.com / Admin@123
- **Staff Account**: staff@test.com / Staff@123

#### Test Equipment
- Laptop (10 available)
- Projector (5 available)
- Microphone (3 available)
- Camera (2 available)

---

## 7. REGRESSION TEST MATRIX

| Feature | Test Case | Status | Severity | Notes |
|---------|-----------|--------|----------|-------|
| Auth | User Registration | | CRITICAL | |
| Auth | User Login | | CRITICAL | |
| Auth | JWT Validation | | HIGH | |
| Equipment | View Catalog | | HIGH | |
| Equipment | Search & Filter | | MEDIUM | |
| Request | Create Request | | CRITICAL | |
| Request | Upload PDF | | CRITICAL | |
| Request | Download PDF | | HIGH | |
| Request | Approve Request | | CRITICAL | |
| Profile | View Profile | | MEDIUM | |
| Profile | Edit Profile | | MEDIUM | |
| Admin | User Management | | HIGH | |
| Admin | Equipment Management | | HIGH | |

---

## 8. DEFECT TRACKING

### Defect Report Format

```
Defect ID: DEF-[Date]-[Number]
Title: [Brief description]
Severity: CRITICAL / HIGH / MEDIUM / LOW
Description: [Detailed explanation]
Steps to Reproduce: [Exact steps]
Expected Result: [What should happen]
Actual Result: [What actually happened]
Environment: [Where it occurred]
Attached Evidence: [Screenshots/logs]
Status: OPEN / IN PROGRESS / RESOLVED / CLOSED
```

---

## 9. PASS/FAIL CRITERIA

### Test Suite Passes If:
✓ All CRITICAL severity tests pass  
✓ 95% of HIGH severity tests pass  
✓ 90% of MEDIUM severity tests pass  
✓ No regression bugs found  
✓ Code coverage ≥ 80%  
✓ All automated tests pass  

### Test Suite Fails If:
✗ Any CRITICAL test fails  
✗ More than 5% of HIGH severity tests fail  
✗ Blocking issues prevent feature use  
✗ Security vulnerabilities found  

---

## 10. SIGN-OFF

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Test Lead | | | |
| QA Manager | | | |
| Project Manager | | | |
| Technical Lead | | | |

---

## 11. APPROVAL & DOCUMENTATION

**Test Plan Prepared By:** [Name]  
**Date Prepared:** May 4, 2026  
**Test Plan Approved By:** [Manager Name]  
**Date Approved:** [Date]  
**Version:** 1.0  

**Document Location:** `docs/Test_Plan.md`  
**Related Documents:**
- `VerticalSliceRefactoringGuide.md`
- `Refactor_Guide.md`
- `Automated_Test_Evidence.md`
