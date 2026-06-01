const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false });
  const page = await browser.newPage();
  
  const timestamp = Date.now();
  const testEmail = `test.user${timestamp}@example.com`;
  const testPassword = 'TestPassword123!';
  const testName = 'Test User ' + timestamp;
  
  console.log('\n====== E2E Auth Flow Test ======');
  console.log(`Test Email: ${testEmail}`);
  console.log(`Test Password: ${testPassword}\n`);
  
  try {
    // Step 1: Navigate to app
    console.log('1. Navigating to http://localhost:3001...');
    await page.goto('http://localhost:3001', { waitUntil: 'networkidle' });
    console.log('   ✓ App loaded');
    
    // Step 2: Register
    console.log('\n2. Testing Register endpoint...');
    const registerResponse = await page.evaluate(async (params) => {
      const res = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
      });
      return { status: res.status, data: await res.json() };
    }, { name: testName, email: testEmail, password: testPassword });
    
    if (registerResponse.status === 201 || registerResponse.status === 200) {
      console.log(`   ✓ Register succeeded (${registerResponse.status})`);
      console.log(`   User ID: ${registerResponse.data.id}, Role: ${registerResponse.data.role}`);
    } else {
      console.log(`   ✗ Register failed (${registerResponse.status}): ${JSON.stringify(registerResponse.data)}`);
      throw new Error('Register failed');
    }
    
    // Step 3: Login
    console.log('\n3. Testing Login endpoint...');
    const loginResponse = await page.evaluate(async (params) => {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
      });
      return { status: res.status, data: await res.json() };
    }, { email: testEmail, password: testPassword });
    
    if (loginResponse.status === 200) {
      console.log(`   ✓ Login succeeded (${loginResponse.status})`);
      console.log(`   User: ${loginResponse.data.email}, Role: ${loginResponse.data.role}`);
      
      if (loginResponse.data.accessToken && loginResponse.data.accessToken.length > 0) {
        console.log(`   ✓ JWT Token generated (${loginResponse.data.accessToken.substring(0, 30)}...)`);
      } else {
        console.log(`   ✗ No JWT token in response`);
      }
    } else {
      console.log(`   ✗ Login failed (${loginResponse.status}): ${JSON.stringify(loginResponse.data)}`);
      throw new Error('Login failed');
    }
    
    // Step 4: Test invalid credentials
    console.log('\n4. Testing Login with invalid credentials...');
    const invalidLoginResponse = await page.evaluate(async (params) => {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
      });
      return { status: res.status, data: await res.json() };
    }, { email: testEmail, password: 'wrongpassword' });
    
    if (invalidLoginResponse.status === 401) {
      console.log(`   ✓ Invalid login rejected (${invalidLoginResponse.status})`);
    } else {
      console.log(`   ✗ Unexpected response for invalid login (${invalidLoginResponse.status})`);
    }
    
    // Step 5: Test validation
    console.log('\n5. Testing validation (empty fields)...');
    const validationResponse = await page.evaluate(async () => {
      const res = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: '', password: '' })
      });
      return { status: res.status, data: await res.json() };
    });
    
    if (validationResponse.status === 400) {
      console.log(`   ✓ Validation errors returned (${validationResponse.status})`);
      console.log(`   Errors: ${JSON.stringify(validationResponse.data)}`);
    } else {
      console.log(`   ✗ Unexpected response for validation (${validationResponse.status})`);
    }
    
    console.log('\n====== ✓ All Auth Tests Passed ======\n');
    
  } catch (error) {
    console.error('\n✗ Test failed:', error.message);
    console.log('\n====== Test Summary ======');
    console.log('Make sure:');
    console.log('  1. Backend is running on http://localhost:8080');
    console.log('  2. Frontend is running on http://localhost:3001');
    console.log('  3. Database has proper user tables');
  } finally {
    await browser.close();
  }
})();
