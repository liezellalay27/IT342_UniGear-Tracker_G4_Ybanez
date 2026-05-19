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
    <div className={styles.profileContainer}>
      <header className={styles.homepageHeader}>
        <div className={styles.headerContent}>
          <div className={styles.logoSection}>
            <img src={logo} alt="Logo" className={styles.headerLogo} />
            <span className={styles.headerTitle}>UniGear Tracker</span>
          </div>
          <div className={styles.navLinks}>
            <button onClick={() => goToTab('overview')} className={`${styles.navLink} ${activeTab === 'overview' ? styles.active : ""}`}>Overview</button>
            <button onClick={() => goToTab('equipment')} className={`${styles.navLink} ${activeTab === 'equipment' ? styles.active : ""}`}>Equipment</button>
            <button onClick={() => goToTab('users')} className={`${styles.navLink} ${activeTab === 'users' ? styles.active : ""}`}>Users</button>
            <button onClick={() => goToTab('borrowed')} className={`${styles.navLink} ${activeTab === 'borrowed' ? styles.active : ""}`}>Borrowed</button>
            <button onClick={() => goToTab('requests')} className={`${styles.navLink} ${activeTab === 'requests' ? styles.active : ""}`}>Requests</button>
            <button onClick={() => navigate('/profile')} className={styles.navLink}>Profile</button>
            <button onClick={handleLogout} className={styles.logoutBtn}>Logout</button>
          </div>
        </div>
      </header>

      <div className={styles.content}>
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


