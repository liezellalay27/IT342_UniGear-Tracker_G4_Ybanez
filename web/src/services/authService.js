import axios from 'axios';

const API_URL = `${process.env.REACT_APP_API_BASE_URL}/auth`;

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
    
    // Store user data in localStorage
    if (response.data) {
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    
    return { success: true, data: response.data };
  } catch (error) {
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
  return !!getCurrentUser();
};
