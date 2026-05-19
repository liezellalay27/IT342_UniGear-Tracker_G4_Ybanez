import React, { useCallback, useEffect, useState } from 'react';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

function AdminEquipmentPanel({ activeTab = 'equipment', showTabs = true, onTabChange = () => {} }) {
  const [equipment, setEquipment] = useState([]);
  const [users, setUsers] = useState([]);
  const [borrowedRecords, setBorrowedRecords] = useState([]);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [decisionNotes, setDecisionNotes] = useState({});
  const [formData, setFormData] = useState({
    name: '',
    category: '',
    location: '',
    description: '',
    specificationsText: '',
    totalQuantity: 1,
    availableQuantity: 1,
    status: 'AVAILABLE'
  });

  const apiFetch = useCallback(async (path, options = {}) => {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        ...(options.headers || {})
      }
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || 'Request failed');
    }

    if (response.status === 204) {
      return null;
    }

    return response.json();
  }, []);

  const fetchAllAdminData = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [equipmentData, usersData, borrowedData, requestsData] = await Promise.all([
        apiFetch('/equipment'),
        apiFetch('/admin/users'),
        apiFetch('/admin/borrowed'),
        apiFetch('/admin/requests')
      ]);

      setEquipment(Array.isArray(equipmentData) ? equipmentData : []);
      setUsers(Array.isArray(usersData) ? usersData : []);
      setBorrowedRecords(Array.isArray(borrowedData) ? borrowedData : []);
      setRequests(Array.isArray(requestsData) ? requestsData : []);
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    } finally {
      setLoading(false);
    }
  }, [apiFetch]);

  useEffect(() => {
    fetchAllAdminData();
  }, [fetchAllAdminData]);

  const fetchEquipment = async () => {
    try {
      const data = await apiFetch('/equipment');
      setEquipment(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    const specifications = formData.specificationsText
      .split('\n')
      .map((item) => item.trim())
      .filter((item) => item.length > 0);

    if (specifications.length === 0) {
      setError('Please add at least one specification (one per line).');
      return;
    }

    try {
      const payload = {
        name: formData.name,
        category: formData.category,
        location: formData.location,
        description: formData.description,
        specifications,
        totalQuantity: Number(formData.totalQuantity),
        availableQuantity: Number(formData.availableQuantity),
        status: formData.status
      };

      await apiFetch('/equipment', {
        method: 'POST',
        body: JSON.stringify(payload)
      });

      setFormData({
        name: '',
        category: '',
        location: '',
        description: '',
        specificationsText: '',
        totalQuantity: 1,
        availableQuantity: 1,
        status: 'AVAILABLE'
      });
      setSuccess('Equipment added successfully.');
      fetchEquipment();
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  };

  const handleRequestDecision = async (requestId, status, returnedOnTime = null) => {
    setError('');
    setSuccess('');

    try {
      await apiFetch(`/admin/requests/${requestId}/status`, {
        method: 'PUT',
        body: JSON.stringify({
          status,
          notes: decisionNotes[requestId] || '',
          returnedOnTime
        })
      });

      setSuccess(`Request ${status.toLowerCase()} successfully.`);
      setDecisionNotes((prev) => ({ ...prev, [requestId]: '' }));
      fetchAllAdminData();
    } catch (err) {
      setError(err.message || 'Failed to update request status');
    }
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

  const pendingRequests = requests.filter((item) => item.status === 'PENDING');
  const lowStockItems = equipment.filter((item) => Number(item.availableQuantity) <= 2);
  const activeBorrowed = borrowedRecords.filter((item) => item.status === 'APPROVED');
  const overdueBorrowed = activeBorrowed.filter((item) => {
    if (!item.returnDate) {
      return false;
    }
    const dueDate = new Date(item.returnDate);
    const now = new Date();
    dueDate.setHours(0, 0, 0, 0);
    now.setHours(0, 0, 0, 0);
    return dueDate < now;
  });
  const recentPending = [...pendingRequests]
    .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    .slice(0, 5);

  const getReturnBadgeClass = (returnedOnTime) => {
    if (returnedOnTime === true) {
      return 'admin-return-badge on-time';
    }
    if (returnedOnTime === false) {
      return 'admin-return-badge late';
    }
    return 'admin-return-badge unknown';
  };

  return (
    <section className="admin-panel-card">
      <h2>Admin Control Center</h2>
      <p className="admin-subtitle">Manage inventory, users, borrow records, and approval decisions.</p>

      {error && <div className="error-message">{error}</div>}
      {success && <div className="success-message">{success}</div>}

      {showTabs && (
        <div className="admin-tabs">
          <button
            type="button"
            className={`admin-tab ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => onTabChange('overview')}
          >
            Overview
          </button>
          <button
            type="button"
            className={`admin-tab ${activeTab === 'equipment' ? 'active' : ''}`}
            onClick={() => onTabChange('equipment')}
          >
            Equipment Management
          </button>
          <button
            type="button"
            className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => onTabChange('users')}
          >
            User Management
          </button>
          <button
            type="button"
            className={`admin-tab ${activeTab === 'borrowed' ? 'active' : ''}`}
            onClick={() => onTabChange('borrowed')}
          >
            Borrowed Equipment
          </button>
          <button
            type="button"
            className={`admin-tab ${activeTab === 'requests' ? 'active' : ''}`}
            onClick={() => onTabChange('requests')}
          >
            Requests ({pendingRequests.length})
          </button>
        </div>
      )}

      {loading && <div className="loading">Loading admin data...</div>}

      {!loading && activeTab === 'overview' && (
        <>
          <div className="admin-overview-grid">
            <div className="admin-kpi-card kpi-alert">
              <div className="kpi-label">Pending Requests</div>
              <div className="kpi-value">{pendingRequests.length}</div>
              <div className="kpi-meta">Needs approval decisions</div>
            </div>
            <div className="admin-kpi-card kpi-neutral">
              <div className="kpi-label">Active Borrowed</div>
              <div className="kpi-value">{activeBorrowed.length}</div>
              <div className="kpi-meta">Currently out with users</div>
            </div>
            <div className="admin-kpi-card kpi-warning">
              <div className="kpi-label">Low Stock Items</div>
              <div className="kpi-value">{lowStockItems.length}</div>
              <div className="kpi-meta">Available quantity 2 or below</div>
            </div>
            <div className="admin-kpi-card kpi-danger">
              <div className="kpi-label">Overdue Returns</div>
              <div className="kpi-value">{overdueBorrowed.length}</div>
              <div className="kpi-meta">Borrowed past due date</div>
            </div>
          </div>

          <div className="admin-overview-actions">
            <button type="button" className="btn-primary" onClick={() => onTabChange('requests')}>
              Review Pending Requests
            </button>
            <button type="button" className="btn-cancel" onClick={() => onTabChange('borrowed')}>
              Check Borrowed Records
            </button>
            <button type="button" className="btn-cancel" onClick={() => onTabChange('equipment')}>
              Manage Inventory
            </button>
          </div>

          <div className="admin-overview-panels">
            <div className="admin-overview-panel">
              <h3>Urgent Pending Requests</h3>
              {recentPending.length === 0 ? (
                <div className="no-admin-items">No pending requests right now.</div>
              ) : (
                <div className="admin-mini-list">
                  {recentPending.map((item) => (
                    <div className="admin-mini-item" key={item.id}>
                      <div className="admin-item-title">{item.equipmentName}</div>
                      <div className="admin-item-meta">{item.requesterName || '-'} | Qty: {item.quantity}</div>
                      <div className="admin-item-meta">Borrow: {item.borrowDate || '-'} | Return: {item.returnDate || '-'}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="admin-overview-panel">
              <h3>Low Stock Watchlist</h3>
              {lowStockItems.length === 0 ? (
                <div className="no-admin-items">No low stock equipment.</div>
              ) : (
                <div className="admin-mini-list">
                  {lowStockItems.slice(0, 6).map((item) => (
                    <div className="admin-mini-item" key={item.id}>
                      <div className="admin-item-title">{item.name}</div>
                      <div className="admin-item-meta">{item.category}</div>
                      <div className="admin-item-meta">Available: {item.availableQuantity}/{item.totalQuantity}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      )}

      {!loading && activeTab === 'equipment' && (
        <>
          <form onSubmit={handleSubmit} className="admin-form-grid">
            <div className="form-group">
              <label>Name *</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label>Category *</label>
              <input
                type="text"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label>Location *</label>
              <input
                type="text"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label>Status</label>
              <select
                value={formData.status}
                onChange={(e) => setFormData({ ...formData, status: e.target.value })}
              >
                <option value="AVAILABLE">Available</option>
                <option value="IN_USE">In Use</option>
                <option value="MAINTENANCE">Maintenance</option>
              </select>
            </div>

            <div className="form-group">
              <label>Total Quantity *</label>
              <input
                type="number"
                min="1"
                value={formData.totalQuantity}
                onChange={(e) => setFormData({ ...formData, totalQuantity: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label>Available Quantity *</label>
              <input
                type="number"
                min="0"
                value={formData.availableQuantity}
                onChange={(e) => setFormData({ ...formData, availableQuantity: e.target.value })}
                required
              />
            </div>

            <div className="form-group admin-form-full">
              <label>Description *</label>
              <textarea
                rows="3"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
              />
            </div>

            <div className="form-group admin-form-full">
              <label>Specifications (one per line) *</label>
              <textarea
                rows="4"
                value={formData.specificationsText}
                onChange={(e) => setFormData({ ...formData, specificationsText: e.target.value })}
                required
              />
            </div>

            <div className="admin-form-full">
              <button type="submit" className="btn-primary">Add Equipment</button>
            </div>
          </form>

          <div className="admin-list-header">
            <h3>Current Equipment</h3>
            <button type="button" className="btn-cancel" onClick={fetchEquipment}>Refresh</button>
          </div>

          {equipment.length === 0 ? (
            <div className="no-admin-items">No equipment yet.</div>
          ) : (
            <div className="admin-equipment-grid">
              {equipment.map((item) => (
                <div key={item.id} className="admin-equipment-item">
                  <div className="admin-item-title">{item.name}</div>
                  <div className="admin-item-meta">{item.category} - {item.location}</div>
                  <div className="admin-item-meta">Status: {item.status} | Available: {item.availableQuantity}/{item.totalQuantity}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {!loading && activeTab === 'users' && (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {users.length === 0 ? (
                <tr>
                  <td colSpan="4">No users found.</td>
                </tr>
              ) : (
                users.map((item) => (
                  <tr key={item.id}>
                    <td>{item.name}</td>
                    <td>{item.email}</td>
                    <td>
                      <span className={`role-badge ${item.role === 'ADMIN' ? 'admin' : 'student'}`}>{item.role}</span>
                    </td>
                    <td>{item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {!loading && activeTab === 'borrowed' && (
        <div className="admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Borrower</th>
                <th>Email</th>
                <th>Equipment</th>
                <th>Category</th>
                <th>Qty</th>
                <th>Borrow Date</th>
                <th>Return Date</th>
                <th>Status</th>
                <th>PDF</th>
                <th>Return Check</th>
              </tr>
            </thead>
            <tbody>
              {borrowedRecords.length === 0 ? (
                <tr>
                  <td colSpan="10">No borrowed records yet.</td>
                </tr>
              ) : (
                borrowedRecords.map((item) => (
                  <tr key={item.id}>
                    <td>{item.requesterName || '-'}</td>
                    <td>{item.requesterEmail || '-'}</td>
                    <td>{item.equipmentName}</td>
                    <td>{item.category}</td>
                    <td>{item.quantity}</td>
                    <td>{item.borrowDate ? new Date(item.borrowDate).toLocaleDateString() : '-'}</td>
                    <td>{item.returnDate ? new Date(item.returnDate).toLocaleDateString() : '-'}</td>
                    <td>{item.status}</td>
                    <td>
                      {item.eventApprovalPdf ? (
                        <button
                          type="button"
                          className="btn-download-pdf-small"
                          onClick={() => handleDownloadPdf(item.id)}
                        >
                          📥 View
                        </button>
                      ) : (
                        <span className="no-pdf">-</span>
                      )}
                    </td>
                    <td>
                      {item.status === 'APPROVED' ? (
                        <div className="admin-inline-actions">
                          <button
                            type="button"
                            className="btn-primary btn-inline"
                            onClick={() => handleRequestDecision(item.id, 'COMPLETED', true)}
                          >
                            Returned On Time
                          </button>
                          <button
                            type="button"
                            className="btn-cancel btn-inline"
                            onClick={() => handleRequestDecision(item.id, 'COMPLETED', false)}
                          >
                            Returned Late
                          </button>
                        </div>
                      ) : (
                        <span className={getReturnBadgeClass(item.returnedOnTime)}>
                          {item.returnedOnTime === true
                            ? 'On Time'
                            : item.returnedOnTime === false
                              ? 'Late Return'
                              : 'Unknown'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {!loading && activeTab === 'requests' && (
        <div className="admin-request-list">
          {pendingRequests.length === 0 ? (
            <div className="no-admin-items">No pending requests.</div>
          ) : (
            pendingRequests.map((item) => (
              <div className="admin-request-card" key={item.id}>
                <div className="admin-request-head">
                  <div>
                    <div className="admin-item-title">{item.equipmentName}</div>
                    <div className="admin-item-meta">{item.requesterName || '-'} ({item.requesterEmail || '-'})</div>
                  </div>
                  <span className="role-badge student">{item.status}</span>
                </div>

                <div className="admin-request-grid">
                  <div>Category: {item.category}</div>
                  <div>Quantity: {item.quantity}</div>
                  <div>Requested: {item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</div>
                  <div>Borrow Date: {item.borrowDate ? new Date(item.borrowDate).toLocaleDateString() : '-'}</div>
                  <div>Return Date: {item.returnDate ? new Date(item.returnDate).toLocaleDateString() : '-'}</div>
                </div>

                {item.description && <p className="admin-request-desc">Purpose: {item.description}</p>}

                <textarea
                  rows="2"
                  placeholder="Optional notes for this decision"
                  value={decisionNotes[item.id] || ''}
                  onChange={(e) => setDecisionNotes((prev) => ({ ...prev, [item.id]: e.target.value }))}
                />

                <div className="admin-request-actions">
                  <button
                    type="button"
                    className="btn-primary"
                    onClick={() => handleRequestDecision(item.id, 'APPROVED')}
                  >
                    Approve
                  </button>
                  <button
                    type="button"
                    className="btn-cancel"
                    onClick={() => handleRequestDecision(item.id, 'REJECTED')}
                  >
                    Deny
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </section>
  );
}

export default AdminEquipmentPanel;
