import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styles from './MyRequests.module.css';
import logo from '../assets/UniGear Symbol.png';

const API_URL = 'http://localhost:8080/api';

function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('active');
  const navigate = useNavigate();
  const location = useLocation();
  
  const [formData, setFormData] = useState({
    equipmentName: '',
    category: '',
    description: '',
    quantity: 1,
    borrowDate: '',
    returnDate: '',
    studentName: '',
    schoolIdNumber: '',
    yearLevel: '',
    course: '',
    eventApprovalPdf: null
  });

  useEffect(() => {
    fetchRequests();
  }, []);

  useEffect(() => {
    if (!location.state) {
      return;
    }

    const prefill = {
      equipmentName: location.state.equipmentName || '',
      category: location.state.category || '',
      description: location.state.description || '',
      quantity: location.state.quantity || 1,
      borrowDate: location.state.borrowDate || '',
      returnDate: location.state.returnDate || '',
      studentName: location.state.studentName || '',
      schoolIdNumber: location.state.schoolIdNumber || '',
      yearLevel: location.state.yearLevel || '',
      course: location.state.course || '',
      eventApprovalPdf: null
    };

    if (prefill.equipmentName) {
      setFormData(prefill);
      setShowForm(true);
      navigate('/my-requests', { replace: true, state: null });
    }
  }, [location.state, navigate]);

  const fetchRequests = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setError('Session expired. Please login again.');
        navigate('/login');
        return;
      }

      const response = await fetch(`${API_URL}/requests`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setRequests(data);
      } else if (response.status === 401 || response.status === 403) {
        setError('Session expired. Please login again.');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
      } else {
        const errorData = await response.text();
        setError(errorData || 'Failed to fetch requests');
      }
    } catch (err) {
      setError('Error connecting to server');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    try {
      const token = localStorage.getItem('token');
      
      const formDataToSend = new FormData();
      formDataToSend.append('equipmentName', formData.equipmentName);
      formDataToSend.append('category', formData.category);
      formDataToSend.append('description', formData.description);
      formDataToSend.append('quantity', formData.quantity);
      formDataToSend.append('borrowDate', formData.borrowDate);
      formDataToSend.append('returnDate', formData.returnDate);
      formDataToSend.append('studentName', formData.studentName);
      formDataToSend.append('schoolIdNumber', formData.schoolIdNumber);
      formDataToSend.append('yearLevel', formData.yearLevel);
      formDataToSend.append('course', formData.course);
      
      if (formData.eventApprovalPdf) {
        formDataToSend.append('eventApprovalPdf', formData.eventApprovalPdf);
      }
      
      const response = await fetch(`${API_URL}/requests`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        body: formDataToSend
      });
      
      if (response.ok) {
        setShowForm(false);
        setFormData({
          equipmentName: '',
          category: '',
          description: '',
          quantity: 1,
          borrowDate: '',
          returnDate: '',
          studentName: '',
          schoolIdNumber: '',
          yearLevel: '',
          course: '',
          eventApprovalPdf: null
        });
        fetchRequests();
      } else if (response.status === 401 || response.status === 403) {
        setError('Session expired. Please login again.');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        navigate('/login');
      } else {
        const errorData = await response.text();
        setError(errorData || 'Failed to create request');
      }
    } catch (err) {
      setError('Error connecting to server');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this request?')) {
      return;
    }
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/requests/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        fetchRequests();
      } else {
        const errorData = await response.text();
        setError(errorData || 'Failed to delete request');
      }
    } catch (err) {
      setError('Error connecting to server');
    }
  };

  const getStatusClass = (status) => {
    return `status-${status.toLowerCase()}`;
  };

  const getBorderClass = (status) => {
    switch(status) {
      case 'APPROVED': return 'border-approved';
      case 'PENDING': return 'border-pending';
      case 'REJECTED': return 'border-rejected';
      case 'COMPLETED': return 'border-completed';
      default: return 'border-pending';
    }
  };

  const getReturnStatusClass = (returnedOnTime) => {
    if (returnedOnTime === true) {
      return 'return-badge on-time';
    }
    if (returnedOnTime === false) {
      return 'return-badge late';
    }
    return 'return-badge unknown';
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  const handleDownloadPdf = async (requestId) => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/requests/${requestId}/pdf`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (response.ok) {
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'event_approval.pdf';
        document.body.appendChild(link);
        link.click();
        window.URL.revokeObjectURL(url);
        document.body.removeChild(link);
      } else {
        setError('Failed to download PDF');
      }
    } catch (err) {
      setError('Error downloading PDF');
    }
  };

  // Calculate statistics
  const totalRequests = requests.length;
  const pendingCount = requests.filter(r => r.status === 'PENDING').length;
  const approvedCount = requests.filter(r => r.status === 'APPROVED').length;
  const completedCount = requests.filter(r => r.status === 'COMPLETED').length;
  
  // Filter requests based on active tab
  const activeRequests = requests.filter(r => r.status !== 'COMPLETED' && r.status !== 'REJECTED');
  const historyRequests = requests.filter(r => r.status === 'COMPLETED' || r.status === 'REJECTED');
  const displayedRequests = activeTab === 'active' ? activeRequests : historyRequests;

  if (loading) {
    return <div className={styles.loading}>Loading...</div>;
  }

  return (
    <div className={styles.myRequestsContainer}>
      {/* Navigation Header */}
      <header className={styles.homepageHeader}>
        <div className={styles.headerContent}>
          <div className={styles.logoSection}>
            <img src={logo} alt="Logo" className={styles.headerLogo} />
            <span className={styles.headerTitle}>UniGear Tracker</span>
          </div>
          <nav className={styles.navLinks}>
            <button onClick={() => navigate('/dashboard')} className={styles.navLink}>Catalog</button>
            <button onClick={() => navigate('/my-requests')} className={`${styles.navLink} ${styles.active}`}>My Requests</button>
            <button onClick={() => navigate('/profile')} className={styles.navLink}>Profile</button>
            <button onClick={handleLogout} className={styles.logoutBtn}>Logout</button>
          </nav>
        </div>
      </header>

      <div className={styles.content}>
        <div className={styles.pageHeader}>
          <div>
            <h1>My Requests</h1>
            <p className={styles.subtitle}>Track your equipment borrowing requests and history</p>
          </div>
          <button onClick={() => setShowForm(!showForm)} className={styles.btnNewRequest}>
            {showForm ? '✕ Cancel' : '+ New Request'}
          </button>
        </div>

        <div className={styles.statsGrid}>
          <div className={styles.statCard + " " + styles.statTotal}>
            <div className={styles.statIcon}>📊</div>
            <div className={styles.statNumber}>{totalRequests}</div>
            <div className={styles.statLabel}>Total Requests</div>
          </div>
          <div className={styles.statCard + " " + styles.statPending}>
            <div className={styles.statIcon}>⏱️</div>
            <div className={styles.statNumber}>{pendingCount}</div>
            <div className={styles.statLabel}>Pending</div>
          </div>
          <div className={styles.statCard + " " + styles.statApproved}>
            <div className={styles.statIcon}>✓</div>
            <div className={styles.statNumber}>{approvedCount}</div>
            <div className={styles.statLabel}>Approved</div>
          </div>
          <div className={`${styles.statCard} ${styles.statCompleted}`}>
            <div className={styles.statIcon}>✓</div>
            <div className={styles.statNumber}>{completedCount}</div>
            <div className={styles.statLabel}>Completed</div>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {showForm && (
          <div className={styles.requestForm}>
            <h2>Create New Request</h2>
            <form onSubmit={handleSubmit}>
              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label>Equipment Name *</label>
                  <input
                    type="text"
                    value={formData.equipmentName}
                    onChange={(e) => setFormData({...formData, equipmentName: e.target.value})}
                    required
                  />
                </div>
                
                <div className={styles.formGroup}>
                  <label>Category *</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({...formData, category: e.target.value})}
                    required
                  >
                    <option value="">Select Category</option>
                    <option value="Laptop">Laptop</option>
                    <option value="Desktop">Desktop</option>
                    <option value="Monitor">Monitor</option>
                    <option value="Keyboard">Keyboard</option>
                    <option value="Mouse">Mouse</option>
                    <option value="Headset">Headset</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                
                <div className={styles.formGroup}>
                  <label>Quantity *</label>
                  <input
                    type="number"
                    min="1"
                    value={formData.quantity}
                    onChange={(e) => setFormData({...formData, quantity: parseInt(e.target.value)})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Borrow Date *</label>
                  <input
                    type="date"
                    value={formData.borrowDate}
                    onChange={(e) => setFormData({...formData, borrowDate: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Return Date *</label>
                  <input
                    type="date"
                    value={formData.returnDate}
                    min={formData.borrowDate || undefined}
                    onChange={(e) => setFormData({...formData, returnDate: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Student Name *</label>
                  <input
                    type="text"
                    value={formData.studentName}
                    onChange={(e) => setFormData({...formData, studentName: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>School ID Number *</label>
                  <input
                    type="text"
                    placeholder="17-0635-488"
                    value={formData.schoolIdNumber}
                    pattern="\d{2}-\d{4}-\d{3}"
                    title="Use format 17-0635-488"
                    onChange={(e) => setFormData({...formData, schoolIdNumber: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Year *</label>
                  <input
                    type="text"
                    value={formData.yearLevel}
                    onChange={(e) => setFormData({...formData, yearLevel: e.target.value})}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label>Course *</label>
                  <input
                    type="text"
                    value={formData.course}
                    onChange={(e) => setFormData({...formData, course: e.target.value})}
                    required
                  />
                </div>
              </div>
              
              <div className={styles.formGroup}>
                <label>Purpose / Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  rows="4"
                  placeholder="Describe the purpose of your request..."
                />
              </div>

              <div className={styles.formGroup}>
                <label>Event Approval PDF (Optional)</label>
                <p className="form-label-hint">Upload a PDF file for event approval (if applicable)</p>
                <input
                  type="file"
                  accept=".pdf"
                  onChange={(e) => setFormData({...formData, eventApprovalPdf: e.target.files[0] || null})}
                  className="file-input"
                />
                {formData.eventApprovalPdf && (
                  <div className="file-selected">✓ {formData.eventApprovalPdf.name}</div>
                )}
              </div>
              
              <button type="submit" className="btn-submit">Submit Request</button>
            </form>
          </div>
        )}

        {/* Tabs */}
        <div className="tabs-container">
          <button 
            className={`tab ${activeTab === 'active' ? 'active' : ''}`}
            onClick={() => setActiveTab('active')}
          >
            Active Requests ({activeRequests.length})
          </button>
          <button 
            className={`tab ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            History ({historyRequests.length})
          </button>
        </div>

        {/* Requests List */}
        <div className="requests-grid">
          {displayedRequests.length === 0 ? (
            <div className="no-requests">
              <p>No {activeTab === 'active' ? 'active' : 'history'} requests found.</p>
              {activeTab === 'active' && (
                <button onClick={() => setShowForm(true)} className="btn-primary">Create Your First Request</button>
              )}
            </div>
          ) : (
            displayedRequests.map(request => (
              <div key={request.id} className={`request-card ${getBorderClass(request.status)}`}>
                <div className="request-card-header">
                  <h3 className="request-card-title">🎯 {request.equipmentName}</h3>
                  <span className={`status-badge ${getStatusClass(request.status)}`}>
                    {request.status === 'PENDING' && '⏱️ Pending'}
                    {request.status === 'APPROVED' && '✅ Approved'}
                    {request.status === 'REJECTED' && '❌ Rejected'}
                    {request.status === 'COMPLETED' && '🎉 Completed'}
                  </span>
                </div>

                <div className="request-meta">
                  <span className="meta-icon">📅</span>
                  {new Date(request.createdAt).toLocaleDateString('en-US', { 
                    month: 'short', 
                    day: 'numeric', 
                    year: 'numeric' 
                  })}
                  {request.updatedAt && request.updatedAt !== request.createdAt && (
                    <> - {new Date(request.updatedAt).toLocaleDateString('en-US', { 
                      month: 'short', 
                      day: 'numeric', 
                      year: 'numeric' 
                    })}</>
                  )}
                </div>

                {request.description && (
                  <div className="request-section">
                    <div className="section-label">📝 Purpose:</div>
                    <p className="section-content">{request.description}</p>
                  </div>
                )}

                <div className="request-details-grid">
                  <div className="detail-item">
                    <span className="detail-label">📦 Category</span>
                    <span className="detail-value">{request.category}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">🔢 Quantity</span>
                    <span className="detail-value">{request.quantity}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">📥 Borrow Date</span>
                    <span className="detail-value">
                      {request.borrowDate ? new Date(request.borrowDate).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric'
                      }) : '-'}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">📤 Return Date</span>
                    <span className="detail-value">
                      {request.returnDate ? new Date(request.returnDate).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric'
                      }) : '-'}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">👤 Student Name</span>
                    <span className="detail-value">{request.studentName || '-'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">🪪 School ID</span>
                    <span className="detail-value">{request.schoolIdNumber || '-'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">🎓 Year</span>
                    <span className="detail-value">{request.yearLevel || '-'}</span>
                  </div>
                  <div className="detail-item">
                    <span className="detail-label">📚 Course</span>
                    <span className="detail-value">{request.course || '-'}</span>
                  </div>
                  {request.status === 'COMPLETED' && (
                    <div className="detail-item">
                      <span className="detail-label">⏱ Return Status</span>
                      <span className={getReturnStatusClass(request.returnedOnTime)}>
                        {request.returnedOnTime === true ? 'On Time' : request.returnedOnTime === false ? 'Late Return' : 'Unknown'}
                      </span>
                    </div>
                  )}
                </div>

                {request.notes && (
                  <div className="request-section">
                    <div className="section-label">💬 Admin Notes:</div>
                    <p className="section-content admin-note">{request.notes}</p>
                  </div>
                )}

                {request.eventApprovalPdf && (
                  <div className="request-section">
                    <div className="section-label">📄 Event Approval PDF:</div>
                    <button 
                      onClick={() => handleDownloadPdf(request.id)}
                      className="btn-download-pdf"
                    >
                      📥 Download PDF
                    </button>
                  </div>
                )}

                <div className="request-footer">
                  <span className="submitted-text">
                    Submitted on {new Date(request.createdAt).toLocaleDateString('en-US', { 
                      month: 'short', 
                      day: 'numeric', 
                      year: 'numeric' 
                    })} at {new Date(request.createdAt).toLocaleTimeString('en-US', {
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </span>
                  <div className="card-actions">
                    {request.status === 'PENDING' && (
                      <button 
                        onClick={() => handleDelete(request.id)} 
                        className="btn-delete-small"
                      >
                        Delete
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

export default MyRequests;


