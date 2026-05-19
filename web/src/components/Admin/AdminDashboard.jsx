import React from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { logout } from '../../services/authService';
import AdminEquipmentPanel from './AdminEquipmentPanel.jsx';
import styles from '../Profile.module.css';
import logo from '../../assets/UniGear Symbol.png';

function AdminDashboard() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const activeTab = searchParams.get('tab') || 'overview';

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const goToTab = (tab) => {
    navigate(`/admin?tab=${tab}`);
  };

  return (
    <div className={styles['profile-container']}>
      <header className={styles['homepage-header']}>
        <div className={styles['header-content']}>
          <div className={styles['logo-section']}>
            <img src={logo} alt="Logo" className={styles['header-logo']} />
            <span className={styles['header-title']}>UniGear Tracker</span>
          </div>
          <div className={styles['nav-links']}>
            <button onClick={() => goToTab('overview')} className={`${styles['nav-link']} ${activeTab === 'overview' ? styles.active : ''}`}>Overview</button>
            <button onClick={() => goToTab('equipment')} className={`${styles['nav-link']} ${activeTab === 'equipment' ? styles.active : ''}`}>Equipment</button>
            <button onClick={() => goToTab('users')} className={`${styles['nav-link']} ${activeTab === 'users' ? styles.active : ''}`}>Users</button>
            <button onClick={() => goToTab('borrowed')} className={`${styles['nav-link']} ${activeTab === 'borrowed' ? styles.active : ''}`}>Borrowed</button>
            <button onClick={() => goToTab('requests')} className={`${styles['nav-link']} ${activeTab === 'requests' ? styles.active : ''}`}>Requests</button>
            <button onClick={() => navigate('/profile')} className={styles['nav-link']}>Profile</button>
            <button onClick={handleLogout} className={styles['logout-btn']}>Logout</button>
          </div>
        </div>
      </header>

      <div className={styles['admin-content']}>
        <AdminEquipmentPanel
          activeTab={activeTab}
          showTabs={false}
          onTabChange={goToTab}
        />
      </div>
    </div>
  );
}

export default AdminDashboard;


