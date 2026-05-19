import React, { useCallback, useEffect, useState } from 'react';
import styles from './Admin.module.css';

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
    <section className={styles.adminPanelCard}>
      <h2>Admin Control Center</h2>
      <p className={styles.adminSubtitle}>Manage inventory, users, borrow records, and approval decisions.</p>

      {error && <div className={styles.errorMessage}>{error}</div>}
      {success && <div className={styles.successMessage}>{success}</div>}

      {showTabs && (
        <div className={styles.adminTabs}>
          <button
            type="button"
            className={`${styles.adminTab} ${activeTab === 'overview' ? styles.active : ''}`}
            onClick={() => onTabChange('overview')}
          >
            Overview
          </button>
          <button
            type="button"
            className={`${styles.adminTab} ${activeTab === 'equipment' ? styles.active : ''}`}
            onClick={() => onTabChange('equipment')}
          >
            Equipment Management
          </button>
          <button
            type="button"
            className={`${styles.adminTab} ${activeTab === 'users' ? styles.active : ''}`}
            onClick={() => onTabChange('users')}
          >
            User Management
          </button>
          <button
            type="button"
            className={`${styles.adminTab} ${activeTab === 'borrowed' ? styles.active : ''}`}
            onClick={() => onTabChange('borrowed')}
          >
            Borrowed Equipment
          </button>
          <button
            type="button"
            className={`${styles.adminTab} ${activeTab === 'requests' ? styles.active : ''}`}
            onClick={() => onTabChange('requests')}
          >
            Requests ({pendingRequests.length})
          </button>
        </div>
      )}

      {loading && (() => {
        const LoadingSpinner = require('../LoadingSpinner').default;
        return <LoadingSpinner message="Loading admin data..." showTimer={false} />;
      })()}

      {!loading && activeTab === 'overview' && (
        <>
          <div className={styles.adminOverviewGrid}>
            <div className={`${styles.adminKpiCard} ${styles.kpiAlert}`}>
              <div className={styles.kpiLabel}>Pending Requests</div>
              <div className={styles.kpiValue}>{pendingRequests.length}</div>
              <div className={styles.kpiMeta}>Needs approval decisions</div>
            </div>
            <div className={`${styles.adminKpiCard} ${styles.kpiNeutral}`}>
              <div className={styles.kpiLabel}>Active Borrowed</div>
              <div className={styles.kpiValue}>{activeBorrowed.length}</div>
              <div className={styles.kpiMeta}>Currently out with users</div>
            </div>
            <div className={`${styles.adminKpiCard} ${styles.kpiWarning}`}>
              <div className={styles.kpiLabel}>Low Stock Items</div>
              <div className={styles.kpiValue}>{lowStockItems.length}</div>
              <div className={styles.kpiMeta}>Available quantity 2 or below</div>
            </div>
            <div className={`${styles.adminKpiCard} ${styles.kpiDanger}`}>
              <div className={styles.kpiLabel}>Overdue Returns</div>
              <div className={styles.kpiValue}>{overdueBorrowed.length}</div>
              <div className={styles.kpiMeta}>Borrowed past due date</div>
            </div>
          </div>

          <div className={styles.adminOverviewActions}>
            <button type="button" className={styles.btnPrimary} onClick={() => onTabChange('requests')}>
              Review Pending Requests
            </button>
            <button type="button" className={styles.btnCancel} onClick={() => onTabChange('borrowed')}>
              Check Borrowed Records
            </button>
            <button type="button" className={styles.btnCancel} onClick={() => onTabChange('equipment')}>
              Manage Inventory
            </button>
          </div>

          <div className={styles.adminOverviewPanels}>
            <div className={styles.adminOverviewPanel}>
              <h3>Urgent Pending Requests</h3>
              {recentPending.length === 0 ? (
                <div className={styles.noAdminItems}>No pending requests right now.</div>
              ) : (
                <div className={styles.adminMiniList}>
                  {recentPending.map((item) => (
                    <div className={styles.adminMiniItem} key={item.id}>
                      <div className={styles.adminItemTitle}>{item.equipmentName}</div>
                      <div className={styles.adminItemMeta}>{item.requesterName || '-'} | Qty: {item.quantity}</div>
                      <div className={styles.adminItemMeta}>Borrow: {item.borrowDate || '-'} | Return: {item.returnDate || '-'}</div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className={styles.adminOverviewPanel}>
              <h3>Low Stock Watchlist</h3>
              {lowStockItems.length === 0 ? (
                <div className={styles.noAdminItems}>No low stock equipment.</div>
              ) : (
                <div className={styles.adminMiniList}>
                  {lowStockItems.slice(0, 6).map((item) => (
                    <div className={styles.adminMiniItem} key={item.id}>
                      <div className={styles.adminItemTitle}>{item.name}</div>
                      <div className={styles.adminItemMeta}>{item.category}</div>
                      <div className={styles.adminItemMeta}>Available: {item.availableQuantity}/{item.totalQuantity}</div>
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
          <form onSubmit={handleSubmit} className={styles.adminFormGrid}>
            <div className={styles.formGroup}>
              <label>Name *</label>
              <input
                type="text"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label>Category *</label>
              <input
                type="text"
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label>Location *</label>
              <input
                type="text"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                required
              />
            </div>

            <div className={styles.formGroup}>
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

            <div className={styles.formGroup}>
              <label>Total Quantity *</label>
              <input
                type="number"
                min="1"
                value={formData.totalQuantity}
                onChange={(e) => setFormData({ ...formData, totalQuantity: e.target.value })}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label>Available Quantity *</label>
              <input
                type="number"
                min="0"
                value={formData.availableQuantity}
                onChange={(e) => setFormData({ ...formData, availableQuantity: e.target.value })}
                required
              />
            </div>

            <div className={`${styles.formGroup} ${styles.adminFormFull}`}>
              <label>Description *</label>
              <textarea
                rows="3"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                required
              />
            </div>

            <div className={`${styles.formGroup} ${styles.adminFormFull}`}>
              <label>Specifications (one per line) *</label>
              <textarea
                rows="4"
                value={formData.specificationsText}
                onChange={(e) => setFormData({ ...formData, specificationsText: e.target.value })}
                required
              />
            </div>

            <div className={styles.adminFormFull}>
              <button type="submit" className={styles.btnPrimary}>Add Equipment</button>
            </div>
          </form>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3>Current Equipment</h3>
            <button type="button" className={styles.btnCancel} onClick={fetchEquipment}>Refresh</button>
          </div>

          {equipment.length === 0 ? (
            <div className={styles.noAdminItems}>No equipment yet.</div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem', marginTop: '1rem' }}>
              {equipment.map((item) => (
                <div key={item.id} className={styles.adminMiniItem}>
                  <div className={styles.adminItemTitle}>{item.name}</div>
                  <div className={styles.adminItemMeta}>{item.category} - {item.location}</div>
                  <div className={styles.adminItemMeta}>Status: {item.status} | Available: {item.availableQuantity}/{item.totalQuantity}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {!loading && activeTab === 'users' && (
        <div style={{ overflowX: 'auto' }}>
          <table className={styles.adminTable}>
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
        <div style={{ overflowX: 'auto' }}>
          <table className={styles.adminTable}>
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
                          className={styles.btnDownloadPdfSmall}
                          onClick={() => handleDownloadPdf(item.id)}
                        >
                          📥 View
                        </button>
                      ) : (
                        <span className={styles.noPdf}>-</span>
                      )}
                    </td>
                    <td>
                      {item.status === 'APPROVED' ? (
                        <div className={styles.adminInlineActions}>
                          <button
                            type="button"
                            className={`${styles.btnPrimary} ${styles.btnInline}`}
                            onClick={() => handleRequestDecision(item.id, 'COMPLETED', true)}
                          >
                            Returned On Time
                          </button>
                          <button
                            type="button"
                            className={`${styles.btnCancel} ${styles.btnInline}`}
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
        <div className={styles.adminRequestList}>
          {requests.length === 0 ? (
            <div className={styles.noAdminItems}>No pending requests.</div>
          ) : (
            pendingRequests.map((item) => (
              <div className={styles.adminRequestCard} key={item.id}>
                <div className={styles.adminRequestHead}>
                  <div>
                    <div className={styles.adminItemTitle}>{item.equipmentName}</div>
                    <div className={styles.adminItemMeta}>{item.requesterName || '-'} ({item.requesterEmail || '-'})</div>
                  </div>
                  <span className={`${styles.roleBadge} ${styles.roleBadgeStudent}`}>{item.status}</span>
                </div>

                <div className={styles.adminRequestGrid}>
                  <div>Category: {item.category}</div>
                  <div>Quantity: {item.quantity}</div>
                  <div>Requested: {item.createdAt ? new Date(item.createdAt).toLocaleString() : '-'}</div>
                  <div>Borrow Date: {item.borrowDate ? new Date(item.borrowDate).toLocaleDateString() : '-'}</div>
                  <div>Return Date: {item.returnDate ? new Date(item.returnDate).toLocaleDateString() : '-'}</div>
                </div>

                {item.description && <p className={styles.adminRequestDesc}>Purpose: {item.description}</p>}

                <textarea
                  rows="2"
                  placeholder="Optional notes for this decision"
                  value={decisionNotes[item.id] || ''}
                  onChange={(e) => setDecisionNotes((prev) => ({ ...prev, [item.id]: e.target.value }))}
                />

                <div className={styles.adminRequestActions}>
                  <button
                    type="button"
                    className={styles.btnPrimary}
                    onClick={() => handleRequestDecision(item.id, 'APPROVED')}
                  >
                    Approve
                  </button>
                  <button
                    type="button"
                    className={styles.btnCancel}
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
