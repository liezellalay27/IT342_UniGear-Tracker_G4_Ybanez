import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Register from './components/Register.jsx';
import Login from './components/Login.jsx';
import OAuth2Callback from './components/OAuth2Callback.jsx';
import HomepageCatalog from './components/HomepageCatalog.jsx';
import EquipmentDetail from './components/EquipmentDetail.jsx';
import MyRequests from './components/MyRequests.jsx';
import Profile from './components/Profile.jsx';
import AdminDashboard from './components/Admin/AdminDashboard.jsx';
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
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
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
      </Routes>
    </Router>
  );
}

export default App;
