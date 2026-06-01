import React, { useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { requestPasswordReset, resetPassword } from '../services/authService';
import styles from './Auth.module.css';
import logo from '../assets/UniGear Symbol.png';

function ForgotPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = useMemo(() => searchParams.get('token') || '', [searchParams]);

  const [email, setEmail] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRequestReset = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    if (!email.trim()) {
      setError('Email is required');
      return;
    }

    setLoading(true);
    const result = await requestPasswordReset(email.trim());
    setLoading(false);

    if (result.success) {
      setSuccessMessage('Password reset instructions have been sent to your email.');
      setEmail('');
    } else {
      setError(result.error);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');

    if (!newPassword) {
      setError('New password is required');
      return;
    }

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    if (!token) {
      setError('Reset token is missing or invalid');
      return;
    }

    setLoading(true);
    const result = await resetPassword(token, newPassword);
    setLoading(false);

    if (result.success) {
      setSuccessMessage('Password reset successful. Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } else {
      setError(result.error);
    }
  };

  const isResetMode = Boolean(token);

  return (
    <div className={styles.authContainer}>
      <div className={styles.authCard}>
        <div className={styles.authHeader}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '12px', marginBottom: '15px' }}>
            <img src={logo} alt="Logo" style={{ height: '40px' }} />
            <h1 style={{ marginBottom: 0 }}>UniGear Tracker</h1>
          </div>
          <h2>{isResetMode ? 'Reset Password' : 'Forgot Password'}</h2>
          <p>{isResetMode ? 'Enter a new password for your account' : 'Enter your email and we will send a reset link'}</p>
        </div>

        {error && <div className={`${styles.errorMessage} ${styles.generalError}`}>{error}</div>}
        {successMessage && <div className={styles.successMessage}>{successMessage}</div>}

        {!isResetMode ? (
          <form onSubmit={handleRequestReset} className={styles.authForm}>
            <div className={styles.formGroup}>
              <label htmlFor="email">Email Address</label>
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                autoComplete="email"
              />
            </div>

            <button type="submit" className={styles.btnPrimary} disabled={loading}>
              {loading ? 'Sending...' : 'Send Reset Link'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword} className={styles.authForm}>
            <div className={styles.formGroup}>
              <label htmlFor="newPassword">New Password</label>
              <input
                type="password"
                id="newPassword"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                placeholder="Enter a new password"
                autoComplete="new-password"
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="confirmPassword">Confirm Password</label>
              <input
                type="password"
                id="confirmPassword"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                placeholder="Confirm your new password"
                autoComplete="new-password"
              />
            </div>

            <button type="submit" className={styles.btnPrimary} disabled={loading}>
              {loading ? 'Resetting...' : 'Reset Password'}
            </button>
          </form>
        )}

        <div className={styles.authFooter}>
          <p><Link to="/login">Back to login</Link></p>
        </div>
      </div>
    </div>
  );
}

export default ForgotPassword;