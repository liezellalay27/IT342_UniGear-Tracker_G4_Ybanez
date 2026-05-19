import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
const API_URL = `${API_BASE_URL}/auth`;
const BACKEND_BASE_URL = API_BASE_URL.replace(/\/api\/?$/, '');

// Debug: Log the API URL to see if environment variable is loaded
console.log('Environment Variable:', process.env.REACT_APP_API_BASE_URL);
console.log('API_BASE_URL:', API_BASE_URL);
console.log('API_URL:', API_URL);

/**
 * Authentication Service
 * Handles all authentication-related API calls
 */

// Register a new user
export const register = async (name, email, password) => {
  try {
    const response = await axios.post(`${API_URL}/register`, {
      name,
      email,
      password
    });
    
    // Store user data and token in localStorage
    if (response.data) {
      localStorage.setItem('user', JSON.stringify(response.data));
      // Store token separately for easier access
      if (response.data.accessToken) {
        localStorage.setItem('token', response.data.accessToken);
      }
    }
    
    return { success: true, data: response.data };
  } catch (error) {
    return {
      success: false,
      error: error.response?.data?.error || error.response?.data || 'Registration failed'
    };
  }
};

// Login user
export const login = async (email, password) => {
  try {
    const response = await axios.post(`${API_URL}/login`, {
      email,
      password
    });
    
    // Store user data and token in localStorage
    if (response.data) {
      localStorage.setItem('user', JSON.stringify(response.data));
      // Store token separately for easier access
      if (response.data.accessToken) {
        localStorage.setItem('token', response.data.accessToken);
      }
    }
    
    return { success: true, data: response.data };
  } catch (error) {
  localStorage.removeItem('token');
    return {
      success: false,
      error: error.response?.data?.error || 'Login failed'
    };
  }
};

// Logout user
export const logout = () => {
  localStorage.removeItem('user');
  localStorage.removeItem('token');
};

// Get current user from localStorage
export const getCurrentUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

// Check if user is logged in
export const isAuthenticated = () => {
  const token = localStorage.getItem('token');
  return !!token;
};

// Google OAuth2 Login
export const loginWithGoogle = () => {
  // Use dynamic frontend origin so OAuth works on any local port (3000/3001/etc.)
  const redirectUri = encodeURIComponent(window.location.origin);
  window.location.href = `${BACKEND_BASE_URL}/api/auth/mobile/google?redirect_uri=${redirectUri}`;
};

// Get JWT token from current user
export const getAuthToken = () => {
  return localStorage.getItem('token');
};
