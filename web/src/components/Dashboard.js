import React from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/authService';
import './Dashboard.css';
import logo from '../assets/UniGear Symbol.png';

function Dashboard() {
  const navigate = useNavigate();
  const user = getCurrentUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) {
    navigate('/login');
    return null;
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <img src={logo} alt="Logo" style={{ height: '45px', filter: 'brightness(0) invert(1)' }} />
            <h1>UniGear Tracker</h1>
          </div>
          <button onClick={handleLogout} className="btn-logout">
            Logout
          </button>
        </div>
      </header>

      <main className="dashboard-main">
        <div className="welcome-section">
          <h2>Welcome, {user.name}!</h2>
          <p className="user-email">Logged in as: {user.email}</p>
          <div className="success-badge">
            ✓ Successfully logged in
          </div>
        </div>

        <div className="dashboard-content">
          <div className="info-card">
            <h3>🎉 Authentication Successful</h3>
            <p>You have successfully completed Phase 1 - User Registration and Login</p>
            <div className="feature-list">
              <div className="feature-item">
                <span className="check">✓</span>
                <span>User Registration Implemented</span>
              </div>
              <div className="feature-item">
                <span className="check">✓</span>
                <span>User Login Implemented</span>
              </div>
              <div className="feature-item">
                <span className="check">✓</span>
                <span>Secure Password Storage (BCrypt)</span>
              </div>
              <div className="feature-item">
                <span className="check">✓</span>
                <span>Input Validation</span>
              </div>
              <div className="feature-item">
                <span className="check">✓</span>
                <span>Duplicate Email Prevention</span>
              </div>
            </div>
          </div>

          <div className="user-info-card">
            <h3>Your Account Information</h3>
            <div className="info-row">
              <span className="label">User ID:</span>
              <span className="value">{user.id}</span>
            </div>
            <div className="info-row">
              <span className="label">Name:</span>
              <span className="value">{user.name}</span>
            </div>
            <div className="info-row">
              <span className="label">Email:</span>
              <span className="value">{user.email}</span>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

export default Dashboard;
