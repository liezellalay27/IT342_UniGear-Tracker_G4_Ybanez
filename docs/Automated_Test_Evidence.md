# Automated Test Evidence — How to collect and include

## Backend (Maven)
- Run unit/integration tests:

```
cd backend
./mvnw test
```

- Collect surefire reports in `backend/target/surefire-reports` and copy into `docs/regression-results/backend/`.

## Web (React)
- Run unit tests and coverage:

```
cd web
npm install
npm test -- --coverage --watchAll=false
```

- Collect coverage report from `web/coverage` and copy to `docs/regression-results/web/`.

## E2E (Cypress)
- Install and run:

```
cd web
npx cypress run
```

- Save screenshots and videos from `cypress/screenshots` and `cypress/videos`.

## Mobile (Android)
- Run instrumentation tests (if configured):

```
cd mobile
./gradlew connectedAndroidTest
```

- Save test outputs from `mobile/app/build/reports/`.

## Capturing Screenshots and Logs
- For UI tests, take screenshots of failures and successful happy-paths.
- Save server logs from backend run and API responses.

## Packaging Evidence
- Create `docs/regression-results/` with subfolders for `backend`, `web`, `mobile`, and `e2e`.
- Include README in that folder describing file contents.

## Converting to PDF
- Use pandoc or VS Code print-to-PDF for `docs/FullRegressionReport_Template.md` after filling details.
