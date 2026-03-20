import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/authService';
import './EquipmentDetail.css';
import logo from '../assets/UniGear Symbol.png';

function EquipmentDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const user = getCurrentUser();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) {
    navigate('/login');
    return null;
  }

  // Sample equipment data (will be replaced with API call later)
  const allEquipment = [
    { 
      id: 1, 
      name: 'Microscope', 
      category: 'Microscopes', 
      status: 'Available',
      location: 'UniGear Office - Main Lobby',
      description: 'High-quality compound microscope suitable for biological research and laboratory work. Perfect for observing cellular structures and microorganisms.',
      specifications: [
        'Magnification: 40x-1000x',
        'LED illumination system',
        'Coaxial coarse and fine focus',
        'Dual viewing head option available'
      ]
    },
    { 
      id: 2, 
      name: 'Beaker Set', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Complete set of laboratory beakers in various sizes. Made from high-quality borosilicate glass for chemical resistance and durability.',
      specifications: [
        'Sizes: 50ml, 100ml, 250ml, 500ml, 1000ml',
        'Borosilicate glass (3.3)',
        'Graduated markings',
        'Temperature resistant up to 500°C'
      ]
    },
    { 
      id: 3, 
      name: 'Oscilloscope', 
      category: 'Electronics', 
      status: 'In Use',
      location: 'Electronics Lab - Workbench 3',
      description: 'Digital storage oscilloscope for measuring and analyzing electronic signals. Essential tool for circuit debugging and signal analysis.',
      specifications: [
        'Bandwidth: 100 MHz',
        'Sample Rate: 1 GSa/s',
        '4 analog channels',
        '7-inch color display'
      ]
    },
    { 
      id: 4, 
      name: 'Lab Coat', 
      category: 'Safety Equipment', 
      status: 'Available',
      location: 'Safety Equipment Storage',
      description: 'Standard laboratory coat for personal protection. Made from durable, chemical-resistant fabric with multiple pockets for convenience.',
      specifications: [
        'Size: Large',
        '100% cotton fabric',
        'Chemical resistant coating',
        'Three front pockets',
        'Snap button closure'
      ]
    },
    { 
      id: 5, 
      name: 'Test Tubes', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Set of 50 borosilicate glass test tubes with rubber stoppers. Ideal for chemical experiments and sample storage.',
      specifications: [
        'Quantity: 50 pieces',
        'Size: 18mm x 150mm',
        'Borosilicate glass',
        'Includes rubber stoppers',
        'Autoclavable'
      ]
    },
    { 
      id: 6, 
      name: 'Digital Multimeter', 
      category: 'Electronics', 
      status: 'Available',
      location: 'Electronics Lab - Tool Cabinet',
      description: 'Professional digital multimeter for accurate electrical measurements. Features auto-ranging and multiple measurement modes.',
      specifications: [
        'AC/DC Voltage: 0-1000V',
        'AC/DC Current: 0-10A',
        'Resistance: 0-40MΩ',
        'Auto-ranging',
        'Backlit LCD display'
      ]
    },
    { 
      id: 7, 
      name: 'Safety Goggles', 
      category: 'Safety Equipment', 
      status: 'Available',
      location: 'Safety Equipment Storage',
      description: 'Impact-resistant safety goggles with anti-fog coating. Provides full eye protection for laboratory work.',
      specifications: [
        'Polycarbonate lenses',
        'Anti-fog coating',
        'UV protection',
        'Adjustable strap',
        'Fits over prescription glasses'
      ]
    },
    { 
      id: 8, 
      name: 'Compound Microscope', 
      category: 'Microscopes', 
      status: 'In Use',
      location: 'Biology Lab - Station 5',
      description: 'Advanced compound microscope with phase contrast capability. Ideal for detailed cellular and microbiological studies.',
      specifications: [
        'Magnification: 40x-2000x',
        'Phase contrast capability',
        'Trinocular head',
        'Digital camera compatible',
        'Halogen illumination'
      ]
    },
    { 
      id: 9, 
      name: 'Flask Set', 
      category: 'Glassware', 
      status: 'Available',
      location: 'Chemistry Lab - Storage Room',
      description: 'Erlenmeyer flask set in multiple sizes. Perfect for mixing, heating, and storing chemical solutions.',
      specifications: [
        'Sizes: 125ml, 250ml, 500ml, 1000ml',
        'Borosilicate glass',
        'Narrow neck design',
        'Graduated markings',
        'Heat resistant'
      ]
    },
  ];

  const equipment = allEquipment.find(item => item.id === parseInt(id));

  if (!equipment) {
    return (
      <div className="detail-container">
        <p>Equipment not found</p>
        <button onClick={() => navigate('/dashboard')}>Back to Catalog</button>
      </div>
    );
  }

  return (
    <div className="detail-container">
      {/* Navigation Header */}
      <header className="detail-header">
        <div className="header-content">
          <div className="logo-section">
            <img src={logo} alt="Logo" className="header-logo" />
            <span className="header-title">UniGear Tracker</span>
          </div>
          <nav className="nav-links">
            <a href="#catalog" onClick={(e) => { e.preventDefault(); navigate('/dashboard'); }} className="nav-link">Catalog</a>
            <a href="#requests" className="nav-link">My Request</a>
            <a href="#profile" className="nav-link">Profile</a>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className="detail-main">
        {/* Back Button */}
        <button className="back-button" onClick={() => navigate('/dashboard')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Back to Catalog
        </button>

        {/* Equipment Detail Card */}
        <div className="detail-card">
          {/* Image Section */}
          <div className="detail-image-section">
            <div className="detail-image-placeholder">
              {equipment.name.charAt(0)}
            </div>
          </div>

          {/* Status Badge */}
          <div className="detail-info-section">
            <span className={`detail-status-badge ${equipment.status === 'Available' ? 'available' : 'in-use'}`}>
              {equipment.status}
            </span>

            <h1 className="detail-title">{equipment.name}</h1>
            
            <p className="availability-text">
              {equipment.status === 'Available' 
                ? 'This equipment is available for borrowing' 
                : 'This equipment is currently in use'}
            </p>

            <div className="detail-location">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>{equipment.location}</span>
            </div>

            <p className="detail-description">{equipment.description}</p>

            <div className="specifications-section">
              <h3>Technical Specifications</h3>
              <ul className="specifications-list">
                {equipment.specifications.map((spec, index) => (
                  <li key={index}>{spec}</li>
                ))}
              </ul>
            </div>

            <button 
              className="request-button"
              disabled={equipment.status !== 'Available'}
            >
              Request to Borrow
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default EquipmentDetail;
