import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './MyRequests.css';
import logo from '../assets/UniGear Symbol.png';

const API_URL = 'http://localhost:8080/api';

function MyRequests() {
  const [requests, setRequests] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeTab, setActiveTab] = useState('active');
  const navigate = useNavigate();
  
  const [formData, setFormData] = useState({
    equipmentName: '',
    category: '',
    description: '',
    quantity: 1
  });

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_URL}/requests`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        setRequests(data);
      } else {
        setError('Failed to fetch requests');
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
      const response = await fetch(`${API_URL}/requests`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(formData)
      });
      
      if (response.ok) {
        setShowForm(false);
        setFormData({
          equipmentName: '',
          category: '',
          description: '',
          quantity: 1
        });
        fetchRequests();
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

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
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
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="my-requests-container">
      {/* Navigation Header */}
      <header className="homepage-header">
        <div className="header-content">
          <div className="logo-section">
            <img src={logo} alt="Logo" className="header-logo" />
            <span className="header-title">UniGear Tracker</span>
          </div>
          <nav className="nav-links">
            <button onClick={() => navigate('/dashboard')} className="nav-link">Catalog</button>
            <button onClick={() => navigate('/my-requests')} className="nav-link active">My Requests</button>
            <button onClick={() => navigate('/profile')} className="nav-link">Profile</button>
            <button onClick={handleLogout} className="logout-btn">Logout</button>
          </nav>
        </div>
      </header>

      <div className="content">
        <div className="page-header">
          <div>
            <h1>My Requests</h1>
            <p className="subtitle">Track your equipment borrowing requests and history</p>
          </div>
          <button onClick={() => setShowForm(!showForm)} className="btn-new-request">
            {showForm ? '✕ Cancel' : '+ New Request'}
          </button>
        </div>

        <div className="stats-grid">
          <div className="stat-card stat-total">
            <div className="stat-icon">📊</div>
            <div className="stat-number">{totalRequests}</div>
            <div className="stat-label">Total Requests</div>
          </div>
          <div className="stat-card stat-pending">
            <div className="stat-icon">⏱️</div>
            <div className="stat-number">{pendingCount}</div>
            <div className="stat-label">Pending</div>
          </div>
          <div className="stat-card stat-approved">
            <div className="stat-icon">✓</div>
            <div className="stat-number">{approvedCount}</div>
            <div className="stat-label">Approved</div>
          </div>
          <div className="stat-card stat-completed">
            <div className="stat-icon">✓</div>
            <div className="stat-number">{completedCount}</div>
            <div className="stat-label">Completed</div>
          </div>
        </div>

        {error && <div className="error-message">{error}</div>}

        {showForm && (
          <div className="request-form">
            <h2>Create New Request</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Equipment Name *</label>
                  <input
                    type="text"
                    value={formData.equipmentName}
                    onChange={(e) => setFormData({...formData, equipmentName: e.target.value})}
                    required
                  />
                </div>
                
                <div className="form-group">
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
                
                <div className="form-group">
                  <label>Quantity *</label>
                  <input
                    type="number"
                    min="1"
                    value={formData.quantity}
                    onChange={(e) => setFormData({...formData, quantity: parseInt(e.target.value)})}
                    required
                  />
                </div>
              </div>
              
              <div className="form-group">
                <label>Purpose / Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({...formData, description: e.target.value})}
                  rows="4"
                  placeholder="Describe the purpose of your request..."
                />
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
                </div>

                {request.notes && (
                  <div className="request-section">
                    <div className="section-label">💬 Admin Notes:</div>
                    <p className="section-content admin-note">{request.notes}</p>
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
