# Refactor Guide — Applying Vertical Slice Architecture

## Goal
- Reorganize code by feature (vertical slices) instead of technical layers to improve modularity and maintainability.

## Suggested Feature Layout

- Backend (Java)
  - `backend/src/main/java/com/unigear/features/<feature>/controller`  
  - `backend/src/main/java/com/unigear/features/<feature>/service`  
  - `backend/src/main/java/com/unigear/features/<feature>/repository`  
  - `backend/src/main/resources/<feature>/` (if needed)

- Web (React)
  - `web/src/features/<feature>/components/`  
  - `web/src/features/<feature>/hooks/`  
  - `web/src/features/<feature>/services/`  
  - `web/src/features/<feature>/tests/`

- Mobile (Android)
  - `mobile/app/src/main/java/com/unigear/features/<feature>/` (activities/fragments/services)

## Step-by-step refactor workflow

1. Create branch (done): `git checkout -b refactor/vertical-slice`
2. Pick one small feature (e.g., Authentication) as a vertical-slice pilot.
3. Move related files into a new feature folder, update package/import paths.
4. Run the build and tests frequently; keep commits small and focused.
5. Repeat feature-by-feature until complete.

## Git workflow recommendations

- Work feature-by-feature in small commits.
- Run `mvn test`, `npm test`, and any mobile tests after each feature move.
- Use feature branches and open PRs for review.

## Common pitfalls & fixes

- Broken imports: update package names and IDE caches.
- Circular dependencies: avoid moving shared utilities into feature folders; create `common` or `shared` module.
- Large commits: keep history clear by many small commits.

## Verification

- After each feature migration, run unit tests, integration tests, and a quick E2E smoke test (Cypress) for critical flows.

## Example commands

```
git checkout -b refactor/vertical-slice
# move files, edit imports, run tests
cd backend
./mvnw -DskipTests=false test

cd ../web
npm install
npm test -- --watchAll=false
```
