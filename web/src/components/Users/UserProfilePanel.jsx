import React from 'react';

function UserProfilePanel({
  profile,
  editing,
  formData,
  error,
  success,
  setEditing,
  setFormData,
  setError,
  handleImageUpload,
  handleSubmit
}) {
  return (
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
          <div className="info-row">
            <label>Role:</label>
            <span>{profile.role || 'STUDENT'}</span>
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
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
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
                Choose Image
              </button>
              <small style={{ color: '#666' }}>Select an image from your computer (max 2MB)</small>

              {formData.picture && (
                <div style={{ marginTop: '10px', textAlign: 'center' }}>
                  <p style={{ marginBottom: '10px', fontWeight: '600', color: '#550000' }}>Preview:</p>
                  <div
                    style={{
                      width: '120px',
                      height: '120px',
                      border: '3px solid #EFBF04',
                      borderRadius: '50%',
                      overflow: 'hidden',
                      margin: '0 auto',
                      boxShadow: '0 4px 12px rgba(239, 191, 4, 0.3)'
                    }}
                  >
                    <img
                      src={formData.picture}
                      alt="Preview"
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                  </div>
                  <button
                    type="button"
                    onClick={() => setFormData({ ...formData, picture: '' })}
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
            <button type="submit" className="btn-submit">
              Save Changes
            </button>
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
  );
}

export default UserProfilePanel;
