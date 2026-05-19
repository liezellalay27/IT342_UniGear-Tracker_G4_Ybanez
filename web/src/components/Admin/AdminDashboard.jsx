import React from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { logout } from '../../services/authService';
import AdminEquipmentPanel from './AdminEquipmentPanel.jsx';
import logo from '../../assets/UniGear Symbol.png';

function AdminDashboard() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const activeTab = searchParams.get('tab') || 'overview';
  const currentPath = location.pathname;

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const goToTab = (tab) => {
    navigate(`/admin?tab=${tab}`);
  };

  return (
    <div className="profile-container">
      <header className="homepage-header">
        <div className="header-content">
          <div className="logo-section">
            <img src={logo} alt="Logo" className="header-logo" />
            <span className="header-title">UniGear Tracker</span>
          </div>
          <div className="nav-links">
            <button onClick={() => goToTab('overview')} className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`}>Overview</button>
            <button onClick={() => goToTab('equipment')} className={`nav-link ${activeTab === 'equipment' ? 'active' : ''}`}>Equipment</button>
            <button onClick={() => goToTab('users')} className={`nav-link ${activeTab === 'users' ? 'active' : ''}`}>Users</button>
            <button onClick={() => goToTab('borrowed')} className={`nav-link ${activeTab === 'borrowed' ? 'active' : ''}`}>Borrowed</button>
            <button onClick={() => goToTab('requests')} className={`nav-link ${activeTab === 'requests' ? 'active' : ''}`}>Requests</button>
            <button onClick={() => navigate('/profile')} className={`nav-link ${currentPath === '/profile' ? 'active' : ''}`}>Profile</button>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </div>
        </div>
      </header>

      <div className="content">
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


