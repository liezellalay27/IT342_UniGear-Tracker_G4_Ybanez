import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/authService';
import './HomepageCatalog.css';
import logo from '../assets/UniGear Symbol.png';

function HomepageCatalog() {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');

  useEffect(() => {
    // Check if user is authenticated
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const categories = [
    'All',
    'Microscopes',
    'Glassware',
    'Electronics',
    'Safety Equipment',
    'Chemicals'
  ];

  // Sample equipment data (this will be replaced with actual data later)
  const equipment = [
    { 
      id: 1, 
      name: 'Microscope', 
      category: 'Microscopes', 
      status: 'Available',
      location: 'UniGear Office - Main Lobby',
      description: 'High-quality compound microscope suitable for biological research and laboratory work.',
      specifications: ['Magnification: 40x-1000x', 'LED illumination system']
    },
    { 
      id: 2, 
      name: 'Beaker Set', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Complete set of laboratory beakers in various sizes.',
      specifications: ['Sizes: 50ml, 100ml, 250ml, 500ml, 1000ml', 'Borosilicate glass']
    },
    { 
      id: 3, 
      name: 'Oscilloscope', 
      category: 'Electronics', 
      status: 'In Use',
      location: 'Electronics Lab - Workbench 3',
      description: 'Digital storage oscilloscope for measuring electronic signals.',
      specifications: ['Bandwidth: 100 MHz', '4 analog channels']
    },
    { 
      id: 4, 
      name: 'Lab Coat', 
      category: 'Safety Equipment', 
      status: 'Available',
      location: 'Safety Equipment Storage',
      description: 'Standard laboratory coat for personal protection.',
      specifications: ['Size: Large', '100% cotton fabric']
    },
    { 
      id: 5, 
      name: 'Test Tubes', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Set of 50 borosilicate glass test tubes.',
      specifications: ['Quantity: 50 pieces', 'Size: 18mm x 150mm']
    },
    { 
      id: 6, 
      name: 'Digital Multimeter', 
      category: 'Electronics', 
      status: 'Available',
      location: 'Electronics Lab - Tool Cabinet',
      description: 'Professional digital multimeter for accurate measurements.',
      specifications: ['AC/DC Voltage: 0-1000V', 'Auto-ranging']
    },
    { 
      id: 7, 
      name: 'Safety Goggles', 
      category: 'Safety Equipment', 
      status: 'Available',
      location: 'Safety Equipment Storage',
      description: 'Impact-resistant safety goggles with anti-fog coating.',
      specifications: ['Polycarbonate lenses', 'UV protection']
    },
    { 
      id: 8, 
      name: 'Compound Microscope', 
      category: 'Microscopes', 
      status: 'In Use',
      location: 'Biology Lab - Station 5',
      description: 'Advanced compound microscope with phase contrast.',
      specifications: ['Magnification: 40x-2000x', 'Phase contrast capability']
    },
    { 
      id: 9, 
      name: 'Flask Set', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Erlenmeyer flask set in multiple sizes.',
      specifications: ['Sizes: 125ml, 250ml, 500ml, 1000ml', 'Heat resistant']
    },
  ];

  // Filter equipment based on search query and selected category
  const filteredEquipment = equipment.filter((item) => {
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         item.category.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || 
                           item.category.toLowerCase() === selectedCategory.toLowerCase();
    return matchesSearch && matchesCategory;
  });

  return (
    <div className="homepage-container">
      {/* Navigation Header */}
      <header className="homepage-header">
        <div className="header-content">
          <div className="logo-section">
            <img src={logo} alt="Logo" className="header-logo" />
            <span className="header-title">UniGear Tracker</span>
          </div>
          <nav className="nav-links">
            <button onClick={() => navigate('/dashboard')} className="nav-link active">Catalog</button>
            <button onClick={() => navigate('/my-requests')} className="nav-link">My Requests</button>
            <button onClick={() => navigate('/profile')} className="nav-link">Profile</button>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </nav>
        </div>
      </header>

      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-text">
            <h1 className="hero-title">University Equipment Catalog</h1>
            <p className="hero-subtitle">Browse and borrow laboratory equipment for your research and projects</p>
            
            {/* Search Bar */}
            <div className="search-container">
              <input
                type="text"
                className="search-input"
                placeholder="Search for equipment..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
              <button className="search-button" aria-label="Search">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="11" cy="11" r="8"></circle>
                  <path d="m21 21-4.35-4.35"></path>
                </svg>
              </button>
            </div>

            {/* Quick Action Buttons */}
            <div className="quick-actions">
              <button className="action-btn">Browse Equipment</button>
              <button className="action-btn">My Requests</button>
              <button className="action-btn">Request History</button>
            </div>
          </div>
          
          <div className="hero-logo">
            <img src={logo} alt="University Logo" className="u-logo" />
          </div>
        </div>
      </section>

      {/* Main Content */}
      <main className="catalog-main">
        {/* Category Filter */}
        <div className="filter-section">
          <div className="filter-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"></polygon>
            </svg>
            <span className="filter-title">Filter by Category</span>
          </div>
          <div className="category-filters">
            {categories.map((category) => (
              <button
                key={category}
                className={`category-btn ${selectedCategory === category.toLowerCase() ? 'active' : ''}`}
                onClick={() => setSelectedCategory(category.toLowerCase())}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {/* Equipment Grid */}
        <div className="equipment-grid">
          {filteredEquipment.length > 0 ? (
            filteredEquipment.map((item) => (
              <div 
                key={item.id} 
                className="equipment-card"
                onClick={() => navigate(`/equipment/${item.id}`)}
              >
                <div className="equipment-image">
                  <div className="image-placeholder">
                    {item.name.charAt(0)}
                  </div>
                </div>
                <div className="equipment-info">
                  <h3 className="equipment-name">{item.name}</h3>
                  <p className="equipment-category">{item.category}</p>
                  <span className={`equipment-status ${item.status === 'Available' ? 'available' : 'in-use'}`}>
                    {item.status}
                  </span>
                </div>
              </div>
            ))
          ) : (
            <div className="no-results">
              <p>No equipment found matching your search criteria.</p>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

export default HomepageCatalog;
