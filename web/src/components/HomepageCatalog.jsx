import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/authService';
import styles from './HomepageCatalog.module.css';
import logo from '../assets/UniGear Symbol.png';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

function HomepageCatalog() {
  const navigate = useNavigate();
  const currentUser = getCurrentUser();
  const isAdmin = currentUser?.role === 'ADMIN';
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [equipment, setEquipment] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadEquipment = async () => {
      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login', { replace: true });
        return;
      }

      setLoading(true);
      setError('');

      try {
        const response = await fetch(`${API_URL}/equipment`, {
          headers: {
            Authorization: `Bearer ${token}`
          }
        });

        if (response.status === 401) {
          logout();
          navigate('/login', { replace: true });
          return;
        }

        if (!response.ok) {
          const message = await response.text();
          throw new Error(message || 'Failed to load equipment');
        }

        const data = await response.json();
        setEquipment(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || 'Error connecting to server');
      } finally {
        setLoading(false);
      }
    };

    loadEquipment();
  }, [navigate]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const categories = [
    'All',
    ...Array.from(new Set(equipment.map((item) => item.category))).sort((a, b) => a.localeCompare(b))
  ];

  const toDisplayStatus = (status) => {
    if (!status) {
      return 'Unknown';
    }
    return status.replace('_', ' ').replace(/\b\w/g, (char) => char.toUpperCase());
  };

  // Filter equipment based on search query and selected category
  const filteredEquipment = equipment.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         item.category.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || 
                           item.category.toLowerCase() === selectedCategory.toLowerCase();
    return matchesSearch && matchesCategory;
  });

  if (loading) {
    return <div className={styles['loading']}>Loading equipment...</div>;
  }

  return (
    <div className={styles['homepage-container']}>
      {/* Navigation Header */}
      <header className={styles['homepage-header']}>
        <div className={styles['header-content']}>
          <div className={styles['logo-section']}>
            <img src={logo} alt="Logo" className={styles['header-logo']} />
            <span className={styles['header-title']}>UniGear Tracker</span>
          </div>
          <nav className={styles['nav-links']}>
            <button onClick={() => navigate('/dashboard')} className={`${styles['nav-link']} ${styles['active']}`}>Catalog</button>
            {isAdmin ? (
              <>
                <button onClick={() => navigate('/admin?tab=equipment')} className={styles['nav-link']}>Equipment</button>
                <button onClick={() => navigate('/admin?tab=users')} className={styles['nav-link']}>Users</button>
                <button onClick={() => navigate('/admin?tab=borrowed')} className={styles['nav-link']}>Borrowed</button>
                <button onClick={() => navigate('/admin?tab=requests')} className={styles['nav-link']}>Requests</button>
                <button onClick={() => navigate('/profile')} className={styles['nav-link']}>Profile</button>
              </>
            ) : (
              <>
                <button onClick={() => navigate('/my-requests')} className={styles['nav-link']}>My Requests</button>
                <button onClick={() => navigate('/profile')} className={styles['nav-link']}>Profile</button>
              </>
            )}
            <button onClick={handleLogout} className={styles['logout-btn']}>Logout</button>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className={styles['hero-section']}>
        <div className={styles['hero-content']}>
          <div className={styles['hero-text']}>
            <h1 className={styles['hero-title']}>University Equipment Catalog</h1>
            <p className={styles['hero-subtitle']}>Browse and borrow laboratory equipment for your research and projects</p>
            
            {/* Search Bar */}
            <div className={styles['search-container']}>
              <input
                type="text"
                className={styles['search-input']}
                placeholder="Search for equipment..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button className={styles['search-button']} aria-label="Search">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
              </button>
            </div>

            {/* Quick Action Buttons */}
            <div className={styles['quick-actions']}>
              <button className={styles['action-btn']} onClick={() => window.scrollTo({ top: 420, behavior: 'smooth' })}>Browse Equipment</button>
              <button className={styles['action-btn']} onClick={() => navigate('/my-requests')}>My Requests</button>
              <button className={styles['action-btn']} onClick={() => navigate('/my-requests')}>Request History</button>
            </div>
          </div>
          
          <div className={styles['hero-logo']}>
            <img src={logo} alt="University Logo" className={styles['u-logo']} />
          </div>
        </div>
      </section>

      {/* Main Content */}
      <main className={styles['catalog-main']}>
        {error && <div className={styles['error-message']}>{error}</div>}

        {/* Category Filter */}
        <div className={styles['filter-section']}>
          <div className={styles['filter-header']}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
            </svg>
            <span className={styles['filter-title']}>Filter by Category</span>
          </div>
          <div className={styles['category-filters']}>
            {categories.map((category) => (
              <button
                key={category}
                className={`${styles['category-btn']} ${selectedCategory === category.toLowerCase() ? styles.active : ''}`}
                onClick={() => setSelectedCategory(category.toLowerCase())}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {/* Equipment Grid */}
        <div className={styles['equipment-grid']}>
          {filteredEquipment.length > 0 ? (
            filteredEquipment.map((item) => (
              <div 
                key={item.id} 
                className={styles['equipment-card']}
                onClick={() => navigate(`/equipment/${item.id}`)}
              >
                <div className={styles['equipment-image']}>
                  <div className={styles['image-placeholder']}>
                    {item.name.charAt(0)}
                  </div>
                </div>
                <div className={styles['equipment-info']}>
                  <h3 className={styles['equipment-name']}>{item.name}</h3>
                  <p className={styles['equipment-category']}>{item.category}</p>
                  <span className={`${styles['equipment-status']} ${item.status === 'AVAILABLE' ? styles.available : styles['in-use']}`}>
                    {toDisplayStatus(item.status)}
                  </span>
                </div>
              </div>
            ))
          ) : (
            <div className={styles['no-results']}>
              <p>No equipment found matching your search criteria.</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default HomepageCatalog;


