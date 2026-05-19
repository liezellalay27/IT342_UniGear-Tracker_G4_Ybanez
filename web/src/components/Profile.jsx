import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import UserProfilePanel from './Users/UserProfilePanel';
import styles from './Profile.module.css';
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
    const loadProfile = async () => {
      const token = localStorage.getItem('token');

      if (!token) {
        navigate('/login', { replace: true });
        return;
      }

      try {
        const response = await fetch(`${API_URL}/profile`, {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const data = await response.json();
          setProfile(data);
          setFormData({
            name: data.name,
            picture: data.picture || ''
          });
        } else if (response.status === 401) {
          localStorage.clear();
          navigate('/login', { replace: true });
        } else {
          const errorText = await response.text();
          setError(`Failed to fetch profile (${response.status}): ${errorText}`);
        }
      } catch (err) {
        setError(`Error connecting to server: ${err.message}`);
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [navigate]);

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
      <div className={styles.profileContainer} style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div className={styles.loading} style={{ color: '#EFBF04', fontSize: '20px', fontWeight: '600' }}>Loading profile...</div>
      </div>
    );
  }

  if (!profile) {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    return (
      <div className={styles.profileContainer} style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
        <div className={styles.error} style={{ textAlign: 'center', padding: '40px', maxWidth: '600px', background: 'white', borderRadius: '20px', boxShadow: '0 8px 32px rgba(0,0,0,0.3)' }}>
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
    <div className={styles.profileContainer}>
      <header className={styles.homepageHeader}>
        <div className={styles.headerContent}>
          <div className={styles.logoSection}>
            <img src={logo} alt="Logo" className={styles.headerLogo} />
            <span className={styles.headerTitle}>UniGear Tracker</span>
          </div>
          <div className={styles.navLinks}>
            <button onClick={() => navigate('/dashboard')} className={styles.navLink}>Dashboard</button>
            {profile.role === 'ADMIN' ? (
              <>
                <button onClick={() => navigate('/admin?tab=equipment')} className={styles.navLink}>Equipment</button>
                <button onClick={() => navigate('/admin?tab=users')} className={styles.navLink}>Users</button>
                <button onClick={() => navigate('/admin?tab=borrowed')} className={styles.navLink}>Borrowed</button>
                <button onClick={() => navigate('/admin?tab=requests')} className={styles.navLink}>Requests</button>
              </>
            ) : (
              <button onClick={() => navigate('/my-requests')} className={styles.navLink}>My Requests</button>
            )}
            <button onClick={() => navigate('/profile')} className={styles.navLinkActive}>Profile</button>
            <button onClick={handleLogout} className={styles.logoutBtn}>Logout</button>
          </div>
        </div>
      </header>

      <div className={styles.content}>
        <UserProfilePanel
          profile={profile}
          editing={editing}
          formData={formData}
          error={error}
          success={success}
          setEditing={setEditing}
          setFormData={setFormData}
          setError={setError}
          handleImageUpload={handleImageUpload}
          handleSubmit={handleSubmit}
        />
      </div>
    </div>
  );
}

export default Profile;


