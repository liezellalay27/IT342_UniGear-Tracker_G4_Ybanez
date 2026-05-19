import React, { useEffect, useState, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getCurrentUser, logout } from '../services/authService';
import styles from './EquipmentDetail.module.css';
import logo from '../assets/UniGear Symbol.png';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
const UNSPLASH_API_URL = 'https://api.unsplash.com';
const UNSPLASH_ACCESS_KEY = process.env.REACT_APP_UNSPLASH_ACCESS_KEY || '';
const FETCH_TIMEOUT = 30000; // 30 second timeout

function EquipmentDetail() {
  const navigate = useNavigate();
  const { id } = useParams();
  const user = getCurrentUser();
  const [equipment, setEquipment] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [images, setImages] = useState([]);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [imagesLoading, setImagesLoading] = useState(false);
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedStartDate, setSelectedStartDate] = useState(null);
  const [selectedEndDate, setSelectedEndDate] = useState(null);
  const [borrowedDates, setBorrowedDates] = useState([]);

  const handleLogout = useCallback(() => {
    logout();
    navigate('/login');
  }, [navigate]);

  const toDisplayStatus = useCallback((status) => {
    if (!status) {
      return 'Unknown';
    }
    return status.replace('_', ' ').replace(/\b\w/g, (char) => char.toUpperCase());
  }, []);

  // Fetch borrowed dates from API
  const fetchBorrowedDates = useCallback(async (equipmentName) => {
    try {
      const token = localStorage.getItem('token');
      if (!token || !equipmentName) return;

      // Fetch all requests
      const response = await fetch(`${API_URL}/requests`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!response.ok) {
        console.log('Could not fetch requests for availability');
        return;
      }

      const requests = await response.json();
      const dates = [];

      // Filter for approved/active requests of this equipment and extract date ranges
      requests.forEach((request) => {
        // Check if this request is for the current equipment and is approved/active
        if (
          request.equipmentName === equipmentName &&
          (request.status === 'APPROVED' || request.status === 'COMPLETED')
        ) {
          const borrowDate = new Date(request.borrowDate);
          const returnDate = new Date(request.returnDate);

          // Add all dates in the range
          for (let d = new Date(borrowDate); d <= returnDate; d.setDate(d.getDate() + 1)) {
            dates.push(new Date(d));
          }
        }
      });

      setBorrowedDates(dates);
    } catch (err) {
      console.log('Error fetching borrowed dates:', err.message);
      // Don't show error - just continue with empty borrowed dates
    }
  }, []);

  // Calendar helper functions
  const getDaysInMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  const isDateBorrowed = (date) => {
    if (!date) return false;
    return borrowedDates.some((borrowDate) => {
      const bDate = new Date(borrowDate);
      return (
        bDate.getDate() === date.getDate() &&
        bDate.getMonth() === date.getMonth() &&
        bDate.getFullYear() === date.getFullYear()
      );
    });
  };

  const isDateSelected = (date) => {
    if (!date || !selectedStartDate || !selectedEndDate) return false;
    return date >= selectedStartDate && date <= selectedEndDate;
  };

  const isStartDate = (date) => {
    if (!date || !selectedStartDate) return false;
    return (
      date.getDate() === selectedStartDate.getDate() &&
      date.getMonth() === selectedStartDate.getMonth() &&
      date.getFullYear() === selectedStartDate.getFullYear()
    );
  };

  const isEndDate = (date) => {
    if (!date || !selectedEndDate) return false;
    return (
      date.getDate() === selectedEndDate.getDate() &&
      date.getMonth() === selectedEndDate.getMonth() &&
      date.getFullYear() === selectedEndDate.getFullYear()
    );
  };

  const handleDateClick = (date) => {
    if (isDateBorrowed(date)) return;

    if (!selectedStartDate) {
      setSelectedStartDate(date);
    } else if (!selectedEndDate) {
      if (date < selectedStartDate) {
        setSelectedStartDate(date);
        setSelectedEndDate(null);
      } else {
        setSelectedEndDate(date);
      }
    } else {
      setSelectedStartDate(date);
      setSelectedEndDate(null);
    }
  };

  const generateCalendarDays = () => {
    const daysInMonth = getDaysInMonth(currentMonth);
    const firstDay = getFirstDayOfMonth(currentMonth);
    const days = [];

    // Empty cells for days before month starts
    for (let i = 0; i < firstDay; i++) {
      days.push(null);
    }

    // Days of the month
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(new Date(currentMonth.getFullYear(), currentMonth.getMonth(), i));
    }

    return days;
  };

  // Fetch images from Unsplash API
  const fetchUnsplashImages = useCallback(async (searchQuery) => {
    // Skip if no API key is provided
    if (!UNSPLASH_ACCESS_KEY) {
      console.log('Unsplash API key not configured. Skipping image fetch.');
      return;
    }

    setImagesLoading(true);
    try {
      const response = await fetch(
        `${UNSPLASH_API_URL}/search/photos?query=${encodeURIComponent(searchQuery)}&per_page=6&client_id=${UNSPLASH_ACCESS_KEY}`
      );

      if (!response.ok) {
        throw new Error('Failed to fetch images from Unsplash');
      }

      const data = await response.json();
      if (data.results && data.results.length > 0) {
        const imageData = data.results.map((result) => ({
          url: result.urls.regular,
          alt: result.alt_description || searchQuery,
          photographer: result.user.name,
          photographerUrl: result.user.links.html
        }));
        setImages(imageData);
      } else {
        console.log('No images found for:', searchQuery);
        setImages([]);
      }
    } catch (err) {
      console.log('Error fetching Unsplash images:', err.message);
      // Don't show error to user - gracefully handle API failure
      setImages([]);
    } finally {
      setImagesLoading(false);
    }
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), FETCH_TIMEOUT);

    const loadEquipmentDetail = async () => {
      const currentUser = getCurrentUser();
      if (!currentUser) {
        navigate('/login', { replace: true });
        return;
      }

      const token = localStorage.getItem('token');
      if (!token) {
        navigate('/login', { replace: true });
        return;
      }

      setLoading(true);
      setError('');

      try {
        const response = await fetch(`${API_URL}/equipment/${id}`, {
          headers: {
            Authorization: `Bearer ${token}`
          },
          signal: controller.signal
        });

        if (response.status === 401) {
          logout();
          navigate('/login', { replace: true });
          return;
        }

        if (!response.ok) {
          const message = await response.text();
          throw new Error(message || 'Failed to load equipment details');
        }

        const data = await response.json();
        setEquipment(data);
        // Fetch images from Unsplash for this equipment
        fetchUnsplashImages(data.name);
      } catch (err) {
        if (err.name === 'AbortError') {
          setError('Request timed out. Server is taking too long to respond.');
        } else {
          setError(err.message || 'Error connecting to server');
        }
      } finally {
        setLoading(false);
      }
    };

    loadEquipmentDetail();

    return () => {
      clearTimeout(timeoutId);
      controller.abort();
    };
  }, [id, navigate, fetchUnsplashImages]);

  // Fetch borrowed dates when equipment is loaded
  useEffect(() => {
    if (equipment && equipment.name) {
      fetchBorrowedDates(equipment.name);
    }
  }, [equipment, fetchBorrowedDates]);

  if (!user) {
    return null;
  }

  if (loading) {
    return <div className={styles.loading}>Loading equipment details...</div>;
  }

  if (!equipment) {
    return (
      <div className={styles.detailContainer}>
        <p>{error || 'Equipment not found'}</p>
        <button onClick={() => navigate('/dashboard')}>Back to Catalog</button>
      </div>
    );
  }

  return (
    <div className={styles.detailContainer}>
      {/* Navigation Header */}
      <header className={styles.detailHeader}>
        <div className={styles.headerContent}>
          <div className={styles.logoSection}>
            <img src={logo} alt="Logo" className={styles.headerLogo} />
            <span className={styles.headerTitle}>UniGear Tracker</span>
          </div>
          <nav className={styles.navLinks}>
            <button type="button" onClick={() => navigate('/dashboard')} className={styles.navLink}>Catalog</button>
            {user?.role === 'ADMIN' ? (
              <>
                <button type="button" onClick={() => navigate('/admin?tab=equipment')} className={styles.navLink}>Equipment</button>
                <button type="button" onClick={() => navigate('/admin?tab=users')} className={styles.navLink}>Users</button>
                <button type="button" onClick={() => navigate('/admin?tab=borrowed')} className={styles.navLink}>Borrowed</button>
                <button type="button" onClick={() => navigate('/admin?tab=requests')} className={styles.navLink}>Requests</button>
                <button type="button" onClick={() => navigate('/profile')} className={styles.navLink}>Profile</button>
              </>
            ) : (
              <>
                <button type="button" onClick={() => navigate('/my-requests')} className={styles.navLink}>My Requests</button>
                <button type="button" onClick={() => navigate('/profile')} className={styles.navLink}>Profile</button>
              </>
            )}
            <button onClick={handleLogout} className={styles.logoutBtn}>Logout</button>
          </nav>
        </div>
      </header>

      {/* Main Content */}
      <main className={styles.detailMain}>
        {/* Back Button */}
        <button className={styles.backButton} onClick={() => navigate('/dashboard')}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M19 12H5M12 19l-7-7 7-7"/>
          </svg>
          Back to Catalog
        </button>

        {/* Equipment Detail Card */}
        <div className={styles.detailCard}>
          {/* Image Section */}
          <div className={styles.detailImageSection}>
            {images.length > 0 ? (
              <div className={styles.detailImageGallery}>
                <div className={styles.galleryMain}>
                  <img
                    src={images[currentImageIndex].url}
                    alt={images[currentImageIndex].alt}
                    className={styles.galleryImage}
                  />
                  <div className={styles.galleryOverlay}>
                    <a
                      href={images[currentImageIndex].photographerUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className={styles.galleryCredit}
                    >
                      Photo by {images[currentImageIndex].photographer} on Unsplash
                    </a>
                  </div>
                </div>

                {images.length > 1 && (
                  <div className={styles.galleryControls}>
                    <button
                      className={styles.galleryNavBtn prev}
                      onClick={() => setCurrentImageIndex((prev) => (prev === 0 ? images.length - 1 : prev - 1))}
                      aria-label="Previous image"
                    >
                      ❮
                    </button>
                    <div className={styles.galleryThumbnails}>
                      {images.map((img, index) => (
                        <button
                          key={index}
                          className={`thumbnail ${index === currentImageIndex ? 'active' : ''}`}
                          onClick={() => setCurrentImageIndex(index)}
                          aria-label={`View image ${index + 1}`}
                        >
                          <img src={img.url} alt={`Thumbnail ${index + 1}`} />
                        </button>
                      ))}
                    </div>
                    <button
                      className={styles.galleryNavBtn next}
                      onClick={() => setCurrentImageIndex((prev) => (prev === images.length - 1 ? 0 : prev + 1))}
                      aria-label="Next image"
                    >
                      ❯
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className={styles.detailImagePlaceholder}>
                {equipment.name.charAt(0)}
              </div>
            )}
          </div>

          {/* Status Badge */}
          <div className={styles.detailInfoSection}>
            <span className={`detail-status-badge ${equipment.status === 'Available' ? 'available' : 'in-use'}`}>
              {toDisplayStatus(equipment.status)}
            </span>

            <h1 className={styles.detailTitle}>{equipment.name}</h1>
            
            <p className={styles.availabilityText}>
              {equipment.status === 'AVAILABLE' 
                ? 'This equipment is available for borrowing' 
                : 'This equipment is currently in use'}
            </p>

            <div className={styles.detailLocation}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
              </svg>
              <span>{equipment.location}</span>
            </div>

            <p className={styles.detailDescription}>{equipment.description}</p>

            <div className={styles.specificationsSection}>
              <h3>Technical Specifications</h3>
              <ul className={styles.specificationsList}>
                {(Array.isArray(equipment.specifications) ? equipment.specifications : []).map((spec, index) => (
                  <li key={index}>{spec}</li>
                ))}
              </ul>
            </div>

            {/* Availability Calendar */}
            <div className={styles.calendarSection}>
              <h3>Availability Calendar</h3>
              <p className={styles.calendarDescription}>
                Select dates to check availability before creating a request.
              </p>

              <div className={styles.calendarContainer}>
                <div className={styles.calendarHeader}>
                  <button
                    type="button"
                    className={styles.calendarNavBtn}
                    onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1))}
                  >
                    ← Prev
                  </button>
                  <h4 className={styles.calendarMonth}>
                    {currentMonth.toLocaleString('default', { month: 'long', year: 'numeric' })}
                  </h4>
                  <button
                    type="button"
                    className={styles.calendarNavBtn}
                    onClick={() => setCurrentMonth(new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1))}
                  >
                    Next →
                  </button>
                </div>

                <div className={styles.calendarWeekdays}>
                  {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((day) => (
                    <div key={day} className={styles.weekday}>
                      {day}
                    </div>
                  ))}
                </div>

                <div className={styles.calendarDays}>
                  {generateCalendarDays().map((date, index) => (
                    <button
                      key={index}
                      type="button"
                      className={`calendar-day ${
                        !date ? 'empty' : ''
                      } ${isDateBorrowed(date) ? 'borrowed' : ''} ${
                        isStartDate(date) ? 'start-date' : ''
                      } ${isEndDate(date) ? 'end-date' : ''} ${
                        isDateSelected(date) ? 'selected' : ''
                      }`}
                      onClick={() => date && handleDateClick(date)}
                      disabled={!date || isDateBorrowed(date)}
                      title={
                        date
                          ? isDateBorrowed(date)
                            ? 'Not available'
                            : date.toLocaleDateString()
                          : ''
                      }
                    >
                      {date ? date.getDate() : ''}
                    </button>
                  ))}
                </div>
              </div>

              {selectedStartDate && (
                <div className={styles.calendarSelectionInfo}>
                  <p>
                    <strong>Selected Period:</strong> {selectedStartDate.toLocaleDateString()} 
                    {selectedEndDate && ` to ${selectedEndDate.toLocaleDateString()}`}
                  </p>
                  {selectedEndDate && (
                    <p className={styles.duration}>
                      Duration: {Math.ceil((selectedEndDate - selectedStartDate) / (1000 * 60 * 60 * 24)) + 1} days
                    </p>
                  )}
                  <button
                    type="button"
                    className={styles.btnClearDates}
                    onClick={() => {
                      setSelectedStartDate(null);
                      setSelectedEndDate(null);
                    }}
                  >
                    Clear Selection
                  </button>
                </div>
              )}

              <div className={styles.calendarLegend}>
                <div className={styles.legendItem}>
                  <div className={styles.legendColor available}></div>
                  <span>Available</span>
                </div>
                <div className={styles.legendItem}>
                  <div className={styles.legendColor borrowed}></div>
                  <span>Not Available</span>
                </div>
                <div className={styles.legendItem}>
                  <div className={styles.legendColor selected}></div>
                  <span>Selected</span>
                </div>
              </div>
            </div>

            <button 
              className={styles.requestButton}
              disabled={equipment.status !== 'AVAILABLE'}
              onClick={() => navigate('/my-requests', {
                state: {
                  equipmentName: equipment.name,
                  category: equipment.category,
                  quantity: 1,
                  borrowDate: selectedStartDate ? selectedStartDate.toISOString().split('T')[0] : '',
                  returnDate: selectedEndDate ? selectedEndDate.toISOString().split('T')[0] : ''
                }
              })}
            >
              Request to Borrow
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

export default React.memo(EquipmentDetail);

