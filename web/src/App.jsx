import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Register from './components/Register.jsx';
import Login from './components/Login.jsx';
import ForgotPassword from './components/ForgotPassword.jsx';
import OAuth2Callback from './components/OAuth2Callback.jsx';
import LandingPage from './components/LandingPage.jsx';
import HomepageCatalog from './components/Users/HomepageCatalog.jsx';
import EquipmentDetail from './components/EquipmentDetail.jsx';
import MyRequests from './components/Users/MyRequests.jsx';
import Profile from './components/Users/Profile.jsx';
import AdminDashboard from './components/Admin/AdminDashboard.jsx';
import './adminStyles.css';
import { getCurrentUser, isAuthenticated } from './services/authService';

// Protected Route Component
function ProtectedRoute({ children }) {
  return isAuthenticated() ? children : <Navigate to="/login" />;
}

function DashboardEntry() {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }

  const user = getCurrentUser();
  if (user?.role === 'ADMIN') {
    return <Navigate to="/admin?tab=overview" replace />;
  }

  return <HomepageCatalog />;
}

function AdminRoute({ children }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }

  const user = getCurrentUser();
  if (user?.role !== 'ADMIN') {
    return <Navigate to="/dashboard" />;
  }

  return children;
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/oauth2/callback" element={<OAuth2Callback />} />
        <Route 
          path="/dashboard" 
          element={
            <DashboardEntry />
          } 
        />
        <Route 
          path="/equipment/:id" 
          element={
            <ProtectedRoute>
              <EquipmentDetail />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/my-requests" 
          element={
            <ProtectedRoute>
              <MyRequests />
            </ProtectedRoute>
          } 
        />
        <Route 
          path="/profile" 
          element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          } 
        />
        <Route
          path="/admin"
          element={
            <AdminRoute>
              <AdminDashboard />
            </AdminRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
