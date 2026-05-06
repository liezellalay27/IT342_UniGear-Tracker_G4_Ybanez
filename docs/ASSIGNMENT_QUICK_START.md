# Assignment Quick Start Guide

## Overview
You have been assigned **Vertical Slice Refactoring and Full Regression Testing** for the UniGear Tracker project. This guide will help you complete the assignment step-by-step.

**Due Date:** May 9, 2026 (11:59 PM)  
**Extension Closes:** May 10, 2026 (11:59 PM)  

---

## ✅ Completed for You

### 1. Error Fixed
- **File:** `backend/src/test/java/.../PdfUploadIntegrationTest.java`
- **Issue:** Incorrect role enum usage and method name
- **Fix:** Updated role assignment and JWT method call
- **Status:** ✓ FIXED

### 2. Documentation Created

#### A. Vertical Slice Refactoring Guide
- **File:** `docs/VerticalSliceRefactoringGuide.md`
- **Contents:**
  - Before/after architecture comparison
  - Identified features for refactoring
  - Benefits of vertical slice architecture
  - Common pitfalls to avoid
  - Success criteria

#### B. Comprehensive Test Plan
- **File:** `docs/Comprehensive_Test_Plan.md`
- **Contents:**
  - All 22 functional requirements mapped
  - 92+ test cases specified
  - Automated test specifications
  - Test execution procedures
  - Pass/fail criteria

#### C. Refactoring Implementation Checklist
- **File:** `docs/Refactoring_Implementation_Checklist.md`
- **Contents:**
  - Step-by-step refactoring instructions
  - Feature-by-feature migration details
  - Directory structure to create
  - Import update guidelines
  - Verification checklists
  - Commit strategy

#### D. Full Regression Test Report Template
- **File:** `docs/FullRegressionReport_Template.md`
- **Contents:**
  - Executive summary section
  - Project information template
  - Refactoring summary section
  - Test results table
  - Issues and fixes tracking
  - Metrics and conclusions
  - Sign-off section

---

## 📋 What You Need to Do

### Part 1: Create Refactoring Branch ✓

```bash
# Navigate to your repository
cd IT342_UniGear-Tracker_G4_Ybanez

# Ensure main branch is up to date
git checkout main
git pull origin main

# Create refactoring branch
git checkout -b refactor/vertical-slice-architecture

# Push to GitHub
git push origin refactor/vertical-slice-architecture
```

**Status:** Ready to execute

---

### Part 2: Perform Vertical Slice Refactoring

**Reference:** `docs/Refactoring_Implementation_Checklist.md` (Pages 3-20)

**High-Level Steps:**

1. **Create Directory Structure** (Pages 5-6)
   ```bash
   mkdir -p backend/src/main/java/com/unigear/tracker/features/{auth,equipment,request,profile,admin,user}/{controller,service,repository,entity,dto}
   mkdir -p backend/src/main/java/com/unigear/tracker/shared/{security,config,pattern,exception}
   ```

2. **Migrate Features** (Pages 7-18)
   - Move Auth files to `features/auth/`
   - Move Equipment files to `features/equipment/`
   - Move Request files to `features/request/` (includes PDF feature)
   - Move Profile files to `features/profile/`
   - Move Admin files to `features/admin/`
   - Move User files to `features/user/`

3. **Update Shared Components** (Page 19)
   - Move Security configuration to `shared/security/`
   - Move Patterns to `shared/pattern/`
   - Move Configuration to `shared/config/`

4. **Update Application Class** (Page 20)
   ```java
   @ComponentScan(basePackages = {
       "com.unigear.tracker.features",
       "com.unigear.tracker.shared"
   })
   ```

5. **Verify Build** (Pages 21-22)
   ```bash
   mvn clean compile
   mvn test
   mvn package
   ```

**Estimated Time:** 4-6 hours  
**Complexity:** HIGH (requires careful file management and import updates)

---

### Part 3: Create/Update Test Plan ✓

**Reference:** `docs/Comprehensive_Test_Plan.md`

**Already Completed:**
- ✓ 22 functional requirements documented
- ✓ 92+ test cases specified
- ✓ Test execution procedures defined
- ✓ Test data requirements listed
- ✓ Pass/fail criteria established

**You Need To:**
- Execute the tests according to the plan
- Document actual test results
- Record any issues found

---

### Part 4: Perform Full Regression Testing

**Reference:** `docs/Comprehensive_Test_Plan.md` (Sections 6-7)

**Steps:**

1. **Run Automated Tests**
   ```bash
   cd backend
   mvn clean test           # Unit tests
   mvn verify              # Integration tests
   ```

2. **Run Frontend Tests**
   ```bash
   cd web
   npm run cypress:run     # E2E tests
   ```

3. **Manual Testing**
   - Go through `docs/Comprehensive_Test_Plan.md` - Section 6
   - Test each functional area (26 test areas)
   - Document results in regression report

4. **Document Findings**
   - Record any bugs or issues found
   - Note any regressions
   - Verify fixes

**Estimated Time:** 4-8 hours  
**Tests to Execute:** 92+ automated + 26 manual areas

---

### Part 5: Create Full Regression Test Report ✓

**Reference:** `docs/FullRegressionReport_Template.md`

**Structure Already Prepared:**
- ✓ Executive summary template
- ✓ Project information section
- ✓ Refactoring summary format
- ✓ Test results tables
- ✓ Issues tracking format
- ✓ Metrics section
- ✓ Sign-off area

**You Need To:**
1. Fill in actual test results
2. Document issues found
3. Record fixes applied
4. Calculate metrics
5. Add screenshots/logs as evidence
6. Get team sign-offs
7. Export to PDF

**File Format:** `FullRegressionReport_G4_UniGearTracker.pdf`

**Estimated Time:** 2-3 hours

---

## 📊 Detailed Timeline

### Week of May 4-9, 2026

| Date | Task | Time | Status |
|------|------|------|--------|
| May 4 | ✓ Error fix & documentation prep | 2 hrs | DONE |
| May 5-6 | Refactor backend architecture | 6 hrs | TODO |
| May 7 | Verify refactoring & fix issues | 3 hrs | TODO |
| May 8 | Execute comprehensive test plan | 6 hrs | TODO |
| May 9 | Document results & create report | 3 hrs | TODO |
| May 9 | Final review & submission | 1 hr | TODO |

**Total Estimated Time:** 21 hours (across week)

---

## 🔧 Tools & Resources Needed

### Prerequisites
- Git & GitHub account (for branch management)
- Java 19+ & Maven 3.8+
- VS Code or preferred IDE
- PostgreSQL (or Supabase access)
- Node.js & npm (for frontend tests)
- Cypress (npm package)

### Documentation Files
- `docs/VerticalSliceRefactoringGuide.md` - Architecture reference
- `docs/Comprehensive_Test_Plan.md` - Test specifications
- `docs/Refactoring_Implementation_Checklist.md` - Step-by-step guide
- `docs/FullRegressionReport_Template.md` - Report template

### Test Evidence Location
- Backend test results: `backend/target/surefire-reports/`
- Coverage reports: `backend/target/site/jacoco/`
- Frontend test results: `web/coverage/`
- E2E test reports: `web/cypress/screenshots/` & `videos/`

---

## 💡 Key Success Tips

### 1. Version Control Best Practices
```bash
# Commit frequently during refactoring
git add .
git commit -m "refactor: migrate auth feature to vertical slice"
git push origin refactor/vertical-slice-architecture
```

### 2. Build Verification
- Test after each feature migration
- Don't refactor multiple features without verifying
- Keep code compiling at all times

### 3. Documentation
- Update package names in Javadoc
- Add feature READMEs if needed
- Document breaking changes (if any)

### 4. Testing Strategy
- Run unit tests first
- Then integration tests
- Finally manual regression tests
- Document issues as you find them

### 5. Issue Tracking
- Record all bugs found
- Note severity level
- Include reproduction steps
- Document fix and verification

---

## 📝 Submission Checklist

### Part 1: Git Repository ✓
- [ ] Main branch updated with all features
- [ ] `refactor/vertical-slice-architecture` branch created
- [ ] Branch pushed to GitHub with complete history
- [ ] All commits properly documented

### Part 2: Refactored Code ✓
- [ ] Vertical slice directory structure created
- [ ] All 61 Java classes moved to appropriate features
- [ ] All imports updated and corrected
- [ ] Application compiles without errors
- [ ] All tests pass (>95% success rate)
- [ ] No circular dependencies

### Part 3: Test Plan ✓
- [ ] Test plan document created (✓ Already done)
- [ ] 92+ test cases documented
- [ ] Test execution procedures defined
- [ ] Expected results specified

### Part 4: Regression Testing ✓
- [ ] All automated tests executed
- [ ] All manual tests completed
- [ ] Test results documented
- [ ] Issues found and recorded
- [ ] Fixes verified

### Part 5: Full Regression Report PDF
- [ ] Executive summary completed
- [ ] Project information filled in
- [ ] Refactoring summary documented
- [ ] Test results included
- [ ] Issues and fixes documented
- [ ] Metrics calculated
- [ ] Team signatures obtained
- [ ] Exported to PDF
- [ ] Filename: `FullRegressionReport_G4_UniGearTracker.pdf`

### Final Submission
- [ ] GitHub link provided
- [ ] Regression report PDF submitted
- [ ] All documentation complete
- [ ] Commit history reflects work done

---

## ⚠️ Common Issues & Solutions

### Issue 1: Import Errors After Moving Files
**Solution:** Update all package declarations and imports. Use IDE's refactor feature.

### Issue 2: Circular Dependencies
**Solution:** Review dependencies, move problematic code to shared package, or adjust imports.

### Issue 3: Tests Failing After Refactoring
**Solution:** Update test file package names and imports. May need to adjust test data paths.

### Issue 4: Application Won't Start
**Solution:** Check @ComponentScan includes all packages. Verify spring.datasource properties.

### Issue 5: PDF Tests Failing
**Solution:** Ensure database has correct BYTEA columns. Check file upload directory permissions.

---

## 📞 Support Resources

### Documentation
- **Refactoring Guide:** See `docs/VerticalSliceRefactoringGuide.md`
- **Test Plan:** See `docs/Comprehensive_Test_Plan.md`
- **Implementation Steps:** See `docs/Refactoring_Implementation_Checklist.md`
- **Report Template:** See `docs/FullRegressionReport_Template.md`

### Reference Information
- Spring Boot Docs: https://spring.io/projects/spring-boot
- Maven Documentation: https://maven.apache.org/
- JUnit 5 Guide: https://junit.org/junit5/docs/current/user-guide/

---

## 🎯 Assignment Summary

**What's Already Done For You:**
1. ✓ Fixed test file error
2. ✓ Created refactoring guide
3. ✓ Created comprehensive test plan
4. ✓ Created implementation checklist
5. ✓ Created regression report template

**What You Need to Complete:**
1. Create and push refactoring branch
2. Perform vertical slice refactoring (61 classes)
3. Execute comprehensive test plan (92+ tests)
4. Perform full regression testing
5. Document results in regression report PDF

**Total Effort:** ~21 hours over 5 days

**Deliverables:**
1. GitHub repository with refactor branch
2. Regression test report PDF

**Due:** May 9, 2026 (11:59 PM)

---

## ✨ Next Steps

1. **Start Today (May 4):**
   - Review `docs/VerticalSliceRefactoringGuide.md`
   - Set up refactoring branch
   - Begin understanding current structure

2. **May 5-6:**
   - Follow `docs/Refactoring_Implementation_Checklist.md`
   - Migrate features one by one
   - Keep verifying with Maven builds

3. **May 7:**
   - Finish refactoring
   - Fix any compilation errors
   - Ensure all tests pass

4. **May 8:**
   - Execute full test plan
   - Document results
   - Record issues

5. **May 9:**
   - Fill in regression report
   - Get approvals
   - Export to PDF
   - Submit

---

**Good Luck! You've got this! 🚀**

**Questions?** Refer to the comprehensive documentation in the `docs/` folder.
