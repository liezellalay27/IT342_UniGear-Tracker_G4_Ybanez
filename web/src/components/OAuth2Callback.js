import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Auth.css';

function OAuth2Callback() {
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState('');

  useEffect(() => {
    // Parse query parameters
    const params = new URLSearchParams(location.search);
    const token = params.get('token');
    const errorParam = params.get('error');

    if (errorParam) {
      setError(decodeURIComponent(errorParam));
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } else if (token) {
      // Store the JWT token
      const userData = {
        accessToken: token,
        // You can decode the token to get user info if needed
      };
      localStorage.setItem('user', JSON.stringify(userData));
      // Store token separately for easier access
      localStorage.setItem('token', token);
      
      // Redirect to dashboard
      navigate('/dashboard');
    } else {
      setError('Authentication failed. No token received.');
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    }
  }, [location, navigate]);

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h2>{error ? 'Authentication Failed' : 'Authenticating...'}</h2>
        </div>
        {error ? (
          <div className="error-message general-error" style={{ marginTop: '20px' }}>
            {error}
            <p style={{ marginTop: '10px', fontSize: '14px' }}>Redirecting to login...</p>
          </div>
        ) : (
          <div style={{ textAlign: 'center', padding: '40px 20px' }}>
            <div className="spinner" style={{
              border: '4px solid #f3f3f3',
              borderTop: '4px solid #3498db',
              borderRadius: '50%',
              width: '40px',
              height: '40px',
              animation: 'spin 1s linear infinite',
              margin: '0 auto'
            }}></div>
            <p style={{ marginTop: '20px', color: '#666' }}>
              Processing authentication...
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

export default OAuth2Callback;
