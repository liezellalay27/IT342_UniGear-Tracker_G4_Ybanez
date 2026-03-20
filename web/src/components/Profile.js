import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Profile.css';
import logo from '../assets/UniGear Symbol.png';

const API_URL = 'http://localhost:8080/api';

function Profile() {
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    name: '',
    picture: ''
  });

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    console.log('Profile useEffect - Token exists:', !!token);
    console.log('Profile useEffect - Token value:', token ? token.substring(0, 20) + '...' : 'null');
    
    if (!token) {
      console.log('No token found, redirecting to login');
      navigate('/login', { replace: true });
      return;
    }
    
    console.log('Token found, fetching profile...');
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      const token = localStorage.getItem('token');
      console.log('fetchProfile - Token exists:', !!token);
      console.log('fetchProfile - API URL:', `${API_URL}/profile`);
      
      const response = await fetch(`${API_URL}/profile`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      console.log('fetchProfile - Response status:', response.status);
      
      if (response.ok) {
        const data = await response.json();
        console.log('Profile data:', data);
        setProfile(data);
        setFormData({
          name: data.name,
          picture: data.picture || ''
        });
      } else if (response.status === 401) {
        // Unauthorized - token is invalid or expired
        console.error('Unauthorized - invalid token. Clearing storage and redirecting to login...');
        localStorage.clear(); // Clear all localStorage
        navigate('/login', { replace: true });
        return;
      } else {
        const errorText = await response.text();
        console.error('Profile fetch error:', response.status, errorText);
        setError(`Failed to fetch profile (${response.status}): ${errorText}`);
      }
    } catch (err) {
      console.error('Profile fetch error:', err);
      setError('Error connecting to server: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // Check file size (max 2MB)
      if (file.size > 2 * 1024 * 1024) {
        setError('Image size should be less than 2MB');
        return;
      }
      
      // Check file type
      if (!file.type.startsWith('image/')) {
        setError('Please select an image file');
        return;
      }
      
      // Convert to base64
      const reader = new FileReader();
      reader.onloadend = () => {
        setFormData({...formData, picture: reader.result});
        setError('');
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/profile`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        const data = await response.json();
        setProfile(data);
        setEditing(false);
        setSuccess('Profile updated successfully!');
        setTimeout(() => setSuccess(''), 3000);
      } else {
        const errorData = await response.text();
        setError(errorData || 'Failed to update profile');
      }
    } catch (err) {
      setError('Error connecting to server');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="profile-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div className="loading" style={{ color: '#EFBF04', fontSize: '20px', fontWeight: '600' }}>Loading profile...</div>
      </div>
    );
  }

  if (!profile) {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    return (
      <div className="profile-container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div className="error" style={{ textAlign: 'center', padding: '40px', maxWidth: '600px', background: 'white', borderRadius: '20px', boxShadow: '0 8px 32px rgba(0,0,0,0.3)' }}>
          <h2 style={{ color: '#550000', fontSize: '2rem', marginBottom: '20px' }}>Unable to load profile</h2>
          {error && <p style={{ marginTop: '10px', color: '#d32f2f', background: '#ffebee', padding: '15px', borderRadius: '8px', border: '2px solid #d32f2f' }}>{error}</p>}
          
          <div style={{ marginTop: '20px', padding: '15px', background: '#f5f5f5', borderRadius: '8px', textAlign: 'left' }}>
            <h3 style={{ color: '#550000' }}>Debug Info:</h3>
            <p><strong>Token exists:</strong> {token ? 'Yes' : 'No'}</p>
            <p><strong>User exists:</strong> {user ? 'Yes' : 'No'}</p>
            {token && <p><strong>Token preview:</strong> {token.substring(0, 20)}...</p>}
          </div>
          
          <div style={{ marginTop: '20px', display: 'flex', gap: '10px', justifyContent: 'center' }}>
            <button 
              onClick={() => navigate('/login')} 
              style={{ padding: '12px 24px', cursor: 'pointer', background: '#550000', color: 'white', border: 'none', borderRadius: '25px', fontWeight: '600', fontSize: '15px' }}
            >
              Go to Login
            </button>
            <button 
              onClick={() => window.location.reload()} 
              style={{ padding: '12px 24px', cursor: 'pointer', background: '#EFBF04', color: '#550000', border: 'none', borderRadius: '25px', fontWeight: '600', fontSize: '15px' }}
            >
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-container">
      <header className="homepage-header">
        <div className="header-content">
          <div className="logo-section">
            <img src={logo} alt="Logo" className="header-logo" />
            <span className="header-title">UniGear Tracker</span>
          </div>
          <div className="nav-links">
            <button onClick={() => navigate('/dashboard')} className="nav-link">Dashboard</button>
            <button onClick={() => navigate('/my-requests')} className="nav-link">My Requests</button>
            <button onClick={() => navigate('/profile')} className="nav-link active">Profile</button>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </div>
        </div>
      </header>

      <div className="content">
        <div className="profile-card">
          <h1>My Profile</h1>
          
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="profile-picture-section">
            <div className="profile-picture">
              {profile.picture ? (
                <img src={profile.picture} alt={profile.name} />
              ) : (
                <div className="profile-initials">
                  {profile.name.charAt(0).toUpperCase()}
                </div>
              )}
            </div>
            {!editing && (
              <button onClick={() => setEditing(true)} className="change-picture-btn">
                Change Picture
              </button>
            )}
          </div>

          {!editing ? (
            <div className="profile-info">
              <div className="info-row">
                <label>Name:</label>
                <span>{profile.name}</span>
              </div>
              <div className="info-row">
                <label>Email:</label>
                <span>{profile.email}</span>
              </div>
              <div className="info-row">
                <label>Member Since:</label>
                <span>{new Date(profile.createdAt).toLocaleDateString()}</span>
              </div>
              
              <button onClick={() => setEditing(true)} className="btn-primary">
                Edit Profile
              </button>
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="edit-form">
              <div className="form-group">
                <label>Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  value={profile.email}
                  disabled
                  className="disabled-input"
                />
                <small>Email cannot be changed</small>
              </div>
              
              <div className="form-group">
                <label>Profile Picture</label>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleImageUpload}
                    id="picture-upload"
                    style={{ display: 'none' }}
                  />
                  <button
                    type="button"
                    onClick={() => document.getElementById('picture-upload').click()}
                    style={{
                      background: '#EFBF04',
                      color: '#550000',
                      border: 'none',
                      padding: '12px 24px',
                      borderRadius: '25px',
                      fontSize: '15px',
                      fontWeight: '600',
                      cursor: 'pointer',
                      transition: 'all 0.3s ease',
                      alignSelf: 'flex-start'
                    }}
                    onMouseOver={(e) => {
                      e.target.style.background = '#550000';
                      e.target.style.color = '#EFBF04';
                    }}
                    onMouseOut={(e) => {
                      e.target.style.background = '#EFBF04';
                      e.target.style.color = '#550000';
                    }}
                  >
                    📷 Choose Image
                  </button>
                  <small style={{ color: '#666' }}>Select an image from your computer (max 2MB)</small>
                  
                  {/* Picture Preview */}
                  {formData.picture && (
                    <div style={{ marginTop: '10px', textAlign: 'center' }}>
                      <p style={{ marginBottom: '10px', fontWeight: '600', color: '#550000' }}>Preview:</p>
                      <div style={{ 
                        width: '120px', 
                        height: '120px', 
                        border: '3px solid #EFBF04', 
                        borderRadius: '50%', 
                        overflow: 'hidden',
                        margin: '0 auto',
                        boxShadow: '0 4px 12px rgba(239, 191, 4, 0.3)'
                      }}>
                        <img 
                          src={formData.picture} 
                          alt="Preview" 
                          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        />
                      </div>
                      <button
                        type="button"
                        onClick={() => setFormData({...formData, picture: ''})}
                        style={{
                          marginTop: '10px',
                          background: 'transparent',
                          color: '#d32f2f',
                          border: '1px solid #d32f2f',
                          padding: '6px 16px',
                          borderRadius: '15px',
                          fontSize: '13px',
                          cursor: 'pointer'
                        }}
                      >
                        Remove Picture
                      </button>
                    </div>
                  )}
                </div>
              </div>
              
              <div className="button-group">
                <button type="submit" className="btn-submit">Save Changes</button>
                <button 
                  type="button" 
                  onClick={() => {
                    setEditing(false);
                    setFormData({
                      name: profile.name,
                      picture: profile.picture || ''
                    });
                    setError('');
                  }} 
                  className="btn-cancel"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default Profile;
