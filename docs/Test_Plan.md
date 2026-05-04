# Software Test Plan — UniGear Tracker

## Overview
- Project: UniGear-Tracker
- Purpose: Validate functional requirements after applying Vertical Slice refactoring.

## Scope
- Backend services (API endpoints, business logic)
- Web frontend features (authentication, equipment catalog, requests, profile)
- Mobile app features (if applicable)

## Test Objectives
- Verify each functional requirement works post-refactor.
- Execute automated tests for major features.
- Record and report regressions.

## Functional Requirements Coverage (mapped to features)
- Authentication: Login, Register, OAuth callback
- Equipment: Browse catalog, Equipment detail view
- Requests: Create request, My Requests, Admin approvals
- User profile: View/update profile
- Admin: Manage users

## Test Cases (examples)

### TC-Auth-01: User can register
- Preconditions: System running, DB seeded.
- Steps:
  1. Open registration page or call API `/register`.
  2. Submit valid user data.
  3. Expect success response and user created in DB.
- Expected result: 201 Created / success message.

### TC-Login-01: User can login
- Steps:
 1. Open login page or POST credentials to `/login`.
 2. Expect auth token, redirect to dashboard.

(Repeat for each feature: equipment browse, request create, profile update, admin user management.)

## Test Scripts / Test Steps
- For UI: manual steps, exact navigation and input values.
- For API: sample curl or HTTP requests with expected payload and response.

Example API test (curl):

```
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"pass"}'
```

## Automated Test Cases
- Backend: JUnit (maven) tests for services and controllers.
- Web: Jest + React Testing Library for components and integration tests.
- E2E: Cypress for major user flows (login, create request, view equipment).

## Test Environment
- Backend: Java 17, Maven, local DB (H2 or Postgres)
- Web: Node 18+, npm/yarn
- Mobile: Android SDK, Gradle

## Test Data
- Provide a seed script or sample SQL/JSON data in `docs/test-data/`.

## Traceability Matrix
- Link each requirement to test case IDs (maintain in a table here or spreadsheet).

## Test Schedule and Responsibilities
- Define who executes manual tests and who runs automated suites.

## Reporting
- Record results in `docs/regression-results/` and attach logs/screenshots.
