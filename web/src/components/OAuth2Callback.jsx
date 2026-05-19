import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Auth.css';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

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
      const syncProfileAndRedirect = async () => {
        try {
          localStorage.setItem('token', token);

          const response = await fetch(`${API_URL}/profile`, {
            headers: {
              Authorization: `Bearer ${token}`
            }
          });

          if (response.ok) {
            const profile = await response.json();
            localStorage.setItem('user', JSON.stringify({
              id: profile.id,
              name: profile.name,
              email: profile.email,
              role: profile.role,
              picture: profile.picture || '',
              accessToken: token
            }));

            if (profile.role === 'ADMIN') {
              navigate('/admin?tab=overview');
            } else {
              navigate('/dashboard');
            }
          } else {
            localStorage.setItem('user', JSON.stringify({ accessToken: token }));
            navigate('/dashboard');
          }
        } catch (err) {
          localStorage.setItem('user', JSON.stringify({ accessToken: token }));
          navigate('/dashboard');
        }
      };

      syncProfileAndRedirect();
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
