import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Register from './components/Register';
import Login from './components/Login';
import OAuth2Callback from './components/OAuth2Callback';
import HomepageCatalog from './components/HomepageCatalog';
import EquipmentDetail from './components/EquipmentDetail';
import MyRequests from './components/MyRequests';
import Profile from './components/Profile';
import { isAuthenticated } from './services/authService';

// Protected Route Component
function ProtectedRoute({ children }) {
  return isAuthenticated() ? children : <Navigate to="/login" />;
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
            <ProtectedRoute>
              <HomepageCatalog />
            </ProtectedRoute>
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
      </Routes>
    </Router>
  );
}

export default App;
