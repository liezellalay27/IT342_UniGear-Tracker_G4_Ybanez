import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
const API_URL = `${API_BASE_URL}/auth`;

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
  // Redirect to backend OAuth2 authorization endpoint (Spring Security default)
  // Note: OAuth2 paths are NOT under /api/
  window.location.href = `http://localhost:8080/oauth2/authorization/google`;
};

// Get JWT token from current user
export const getAuthToken = () => {
  return localStorage.getItem('token');
};
