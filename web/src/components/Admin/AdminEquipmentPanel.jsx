import React, { useCallback, useEffect, useState } from 'react';

const API_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

function AdminEquipmentPanel({ activeTab = 'equipment', showTabs = true, onTabChange = () => {} }) {
  const [equipment, setEquipment] = useState([]);
  const [users, setUsers] = useState([]);
  const [borrowedRecords, setBorrowedRecords] = useState([]);
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadedTabs, setLoadedTabs] = useState({});
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
  const [editingId, setEditingId] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [page, setPage] = useState(1);
  const PAGE_SIZE = 10;
  const [recentlyDeleted, setRecentlyDeleted] = useState([]); // {id, data, timeoutId}
  const [conflictMessage, setConflictMessage] = useState('');

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

  const fetchEquipment = useCallback(async () => {
    const data = await apiFetch('/equipment');
    setEquipment(Array.isArray(data) ? data : []);
  }, [apiFetch]);

  const fetchUsers = useCallback(async () => {
    const data = await apiFetch('/admin/users');
    setUsers(Array.isArray(data) ? data : []);
  }, [apiFetch]);

  const fetchBorrowed = useCallback(async () => {
    const data = await apiFetch('/admin/borrowed');
    setBorrowedRecords(Array.isArray(data) ? data : []);
  }, [apiFetch]);

  const fetchRequests = useCallback(async () => {
    const data = await apiFetch('/admin/requests');
    setRequests(Array.isArray(data) ? data : []);
  }, [apiFetch]);

  const refreshEquipment = useCallback(async () => {
    try {
      setError('');
      await fetchEquipment();
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  }, [fetchEquipment]);

  const refreshBorrowed = useCallback(async () => {
    try {
      setError('');
      await fetchBorrowed();
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  }, [fetchBorrowed]);

  const refreshRequests = useCallback(async () => {
    try {
      setError('');
      await fetchRequests();
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  }, [fetchRequests]);

  const fetchOverviewData = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      await Promise.all([fetchEquipment(), fetchBorrowed(), fetchRequests()]);
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    } finally {
      setLoading(false);
    }
  }, [fetchBorrowed, fetchEquipment, fetchRequests]);

  const loadTabData = useCallback(async (tab) => {
    if (tab === 'overview') {
      if (loadedTabs.overview) {
        setLoading(false);
        return;
      }
      await fetchOverviewData();
      setLoadedTabs((prev) => ({ ...prev, overview: true }));
      return;
    }

    if (loadedTabs[tab]) {
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');

    try {
      if (tab === 'equipment') {
        await fetchEquipment();
      } else if (tab === 'users') {
        await fetchUsers();
      } else if (tab === 'borrowed') {
        await fetchBorrowed();
      } else if (tab === 'requests') {
        await fetchRequests();
      }

      setLoadedTabs((prev) => ({ ...prev, [tab]: true }));
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    } finally {
      setLoading(false);
    }
  }, [fetchBorrowed, fetchEquipment, fetchRequests, fetchUsers, fetchOverviewData, loadedTabs]);

  useEffect(() => {
    loadTabData(activeTab);
  }, [activeTab, loadTabData]);

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

      if (editingId) {
        await apiFetch(`/equipment/${editingId}`, {
          method: 'PUT',
          body: JSON.stringify(payload)
        });
        setSuccess('Equipment updated successfully.');
        setEditingId(null);
      } else {
        await apiFetch('/equipment', {
          method: 'POST',
          body: JSON.stringify(payload)
        });
        setSuccess('Equipment added successfully.');
      }

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
      await fetchEquipment();
      setLoadedTabs((prev) => ({ ...prev, equipment: true, overview: true }));
    } catch (err) {
      setError(err.message || 'Error connecting to server');
    }
  };

  const handleEdit = (item) => {
    setEditingId(item.id);
    setFormData({
      name: item.name || '',
      category: item.category || '',
      location: item.location || '',
      description: item.description || '',
      specificationsText: (item.specifications || []).join('\n'),
      totalQuantity: item.totalQuantity || 1,
      availableQuantity: item.availableQuantity || 1,
      status: item.status || 'AVAILABLE'
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCancelEdit = () => {
    setEditingId(null);
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
  };

  const doDeleteRequest = async (id) => {
    const token = localStorage.getItem('token');
    const res = await fetch(`${API_URL}/equipment/${id}`, {
      method: 'DELETE',
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    const text = await res.text();
    return { status: res.status, ok: res.ok, text };
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this equipment? This cannot be undone.')) return;

    // optimistic remove and keep data for undo
    const item = equipment.find((it) => it.id === id);
    setEquipment((prev) => prev.filter((it) => it.id !== id));
    if (editingId === id) handleCancelEdit();

    try {
      const r = await doDeleteRequest(id);
      if (r.status === 409) {
        // show conflict modal and restore locally
        setConflictMessage(r.text || 'Conflict - cannot delete');
        setEquipment((prev) => [item, ...prev]);
        return;
      }

      if (!r.ok) {
        setError(r.text || 'Failed to delete equipment');
        setEquipment((prev) => [item, ...prev]);
        return;
      }

      setSuccess('Equipment deleted. Undo?');
      // keep for undo for 10s
      const timeoutId = setTimeout(() => {
        setRecentlyDeleted((prev) => prev.filter((x) => x.id !== id));
      }, 10000);
      setRecentlyDeleted((prev) => [{ id, data: item, timeoutId }, ...prev]);
      setLoadedTabs((prev) => ({ ...prev, equipment: false, overview: false }));
      fetchEquipment().catch(() => {});
    } catch (err) {
      setError(err.message || 'Failed to delete equipment');
      setEquipment((prev) => [item, ...prev]);
    }
  };

  const toggleSelect = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const selectAllOnPage = (list) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      const start = (page - 1) * PAGE_SIZE;
      const pageItems = list.slice(start, start + PAGE_SIZE);
      const allSelected = pageItems.every((it) => next.has(it.id));
      if (allSelected) {
        pageItems.forEach((it) => next.delete(it.id));
      } else {
        pageItems.forEach((it) => next.add(it.id));
      }
      return next;
    });
  };

  const handleBulkDelete = async () => {
    if (selectedIds.size === 0) return;
    if (!window.confirm(`Delete ${selectedIds.size} selected equipment items? This cannot be undone.`)) return;
    const ids = Array.from(selectedIds);
    const failed = [];
    const deletedItems = [];
    for (const id of ids) {
      const item = equipment.find((it) => it.id === id);
      if (item) deletedItems.push(item);
      // optimistic remove
      setEquipment((prev) => prev.filter((it) => it.id !== id));
      try {
        const r = await doDeleteRequest(id);
        if (r.status === 409) {
          // conflict: restore this item and show modal
          setConflictMessage(r.text || 'Conflict - cannot delete some items');
          setEquipment((prev) => [item, ...prev]);
          failed.push(id);
          continue;
        }
        if (!r.ok) {
          failed.push(id);
          setEquipment((prev) => [item, ...prev]);
        } else {
          const timeoutId = setTimeout(() => {
            setRecentlyDeleted((prev) => prev.filter((x) => x.id !== id));
          }, 10000);
          setRecentlyDeleted((prev) => [{ id, data: item, timeoutId }, ...prev]);
        }
      } catch (err) {
        failed.push(id);
        setEquipment((prev) => [item, ...prev]);
      }
    }

    if (failed.length === 0) setSuccess(`Deleted ${ids.length} items. Undo available for 10s.`);
    else setError(`Failed to delete ${failed.length} items.`);

    setSelectedIds(new Set());
    setLoadedTabs((prev) => ({ ...prev, equipment: false, overview: false }));
    fetchEquipment().catch(() => {});
  };

  const undoDelete = async (id) => {
    const entry = recentlyDeleted.find((x) => x.id === id);
    if (!entry) return;
    clearTimeout(entry.timeoutId);
    try {
      const payload = {
        name: entry.data.name,
        category: entry.data.category,
        location: entry.data.location,
        description: entry.data.description,
        specifications: (entry.data.specifications || '').split('\n').filter(Boolean),
        totalQuantity: entry.data.totalQuantity || 1,
        availableQuantity: entry.data.availableQuantity || 1,
        status: entry.data.status || 'AVAILABLE'
      };
      await apiFetch('/equipment', { method: 'POST', body: JSON.stringify(payload) });
      setRecentlyDeleted((prev) => prev.filter((x) => x.id !== id));
      await fetchEquipment();
      setSuccess('Restore successful');
    } catch (err) {
      setError(err.message || 'Failed to restore equipment');
    }
  };

  const handleRequestDecision = async (requestId, status, returnedOnTime = null) => {
    setError('');
    setSuccess('');

    try {
      const decisionNote = (decisionNotes[requestId] || '').trim();
      await apiFetch(`/admin/requests/${requestId}/status`, {
        method: 'PUT',
        body: JSON.stringify({
          status,
          notes: decisionNote,
          returnedOnTime
        })
      });

      const statusLabel = status === 'APPROVED'
        ? 'approved'
        : status === 'REJECTED'
          ? 'rejected'
          : 'updated';
      const noteLabel = decisionNote
        ? status === 'APPROVED'
          ? ' Approval note saved.'
          : status === 'REJECTED'
            ? ' Rejection note saved.'
            : ' Admin note saved.'
        : '';

      setSuccess(`Request ${statusLabel} successfully.${noteLabel}`);
      setDecisionNotes((prev) => ({ ...prev, [requestId]: '' }));
      setLoadedTabs((prev) => ({ ...prev, requests: false, borrowed: false, overview: false }));
      await loadTabData(activeTab);
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
  const [requestsDisplayLimit, setRequestsDisplayLimit] = useState(10);

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
      {conflictMessage && (
        <div className="error-modal">
          <div className="error-modal-content">
            <h3>Cannot delete</h3>
            <p>{conflictMessage}</p>
            <button className="btn-cancel" onClick={() => setConflictMessage('')}>Close</button>
          </div>
        </div>
      )}

      {/* Recently deleted toast (undo) */}
      {recentlyDeleted.length > 0 && (
        <div className="undo-toast">
          {recentlyDeleted.map((entry) => (
            <div key={entry.id} className="undo-item">
              <span>Deleted: {entry.data.name}</span>
              <button className="btn-primary" onClick={() => undoDelete(entry.id)}>Undo</button>
            </div>
          ))}
        </div>
      )}

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
              <button type="submit" className="btn-primary">{editingId ? 'Update Equipment' : 'Add Equipment'}</button>
              {editingId && (
                <button type="button" className="btn-cancel" onClick={handleCancelEdit} style={{ marginLeft: '0.5rem' }}>
                  Cancel
                </button>
              )}
            </div>
          </form>

          <div className="admin-list-header">
            <h3>Current Equipment</h3>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <input
                  type="search"
                  placeholder="Search equipment by name or category"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ padding: '0.35rem', borderRadius: '4px', border: '1px solid #ccc' }}
                />
                <button type="button" className="btn-cancel" onClick={refreshEquipment}>Refresh</button>
                <button type="button" className="btn-cancel" onClick={() => {
                  const q = searchTerm.trim().toLowerCase();
                  const list = q ? equipment.filter((it) => ((it.name||'').toLowerCase().includes(q) || (it.category||'').toLowerCase().includes(q))) : equipment;
                  selectAllOnPage(list);
                }} style={{ marginLeft: '0.5rem' }}>
                  Toggle Select Page
                </button>
                <button type="button" className="btn-cancel" onClick={handleBulkDelete} disabled={selectedIds.size===0} style={{ marginLeft: '0.5rem' }}>
                  Delete Selected ({selectedIds.size})
                </button>
              </div>
          </div>

          {equipment.length === 0 ? (
            <div className="no-admin-items">No equipment yet.</div>
          ) : (
            <div className="admin-equipment-grid">
              {(() => {
                const q = searchTerm.trim().toLowerCase();
                const list = q
                  ? equipment.filter((it) => (
                      (it.name || '').toLowerCase().includes(q) ||
                      (it.category || '').toLowerCase().includes(q)
                    ))
                  : equipment;
                const start = (page - 1) * PAGE_SIZE;
                const paged = list.slice(start, start + PAGE_SIZE);
                return paged.map((item) => (
                <div key={item.id} className="admin-equipment-item">
                  <div style={{ position: 'absolute', top: '0.5rem', left: '0.5rem' }}>
                    <input type="checkbox" checked={selectedIds.has(item.id)} onChange={() => toggleSelect(item.id)} />
                  </div>
                  <div className="admin-item-title">{item.name}</div>
                  <div className="admin-item-meta">{item.category} - {item.location}</div>
                  <div className="admin-item-meta">Status: {item.status} | Available: {item.availableQuantity}/{item.totalQuantity}</div>
                  <div style={{ marginTop: '0.5rem' }} className="admin-inline-actions">
                    <button type="button" className="btn-primary btn-inline" onClick={() => handleEdit(item)}>Edit</button>
                    <button type="button" className="btn-cancel btn-inline" onClick={() => handleDelete(item.id)}>Delete</button>
                  </div>
                </div>
                ));
              })()}
              {/* Pagination controls */}
              <div style={{ display: 'flex', justifyContent: 'center', marginTop: '1rem', gap: '0.5rem' }}>
                <button type="button" className="btn-cancel" onClick={() => setPage((p) => Math.max(1, p-1))}>
                  Prev
                </button>
                <span>Page {page}</span>
                <button type="button" className="btn-cancel" onClick={() => setPage((p) => p+1)}>
                  Next
                </button>
              </div>
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
          <div className="admin-list-header">
            <h3>Borrowed Equipment</h3>
            <button type="button" className="btn-cancel" onClick={refreshBorrowed}>
              Refresh
            </button>
          </div>
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
          <div className="admin-list-header">
            <h3>Pending Requests</h3>
            <button type="button" className="btn-cancel" onClick={refreshRequests}>
              Refresh
            </button>
          </div>
          {pendingRequests.length === 0 ? (
            <div className="no-admin-items">No pending requests.</div>
          ) : (
            <>
              {pendingRequests.slice(0, requestsDisplayLimit).map((item) => (
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
                    placeholder="Add an approval or rejection note for the requester"
                    value={decisionNotes[item.id] || ''}
                    onChange={(e) => setDecisionNotes((prev) => ({ ...prev, [item.id]: e.target.value }))}
                  />
                  <p style={{ margin: '0.45rem 0 0', fontSize: '0.85rem', color: '#666' }}>
                    This note is saved to the request record and shown to the student.
                  </p>

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
              ))}

              {pendingRequests.length > requestsDisplayLimit && (
                <div className="admin-list-footer">
                  <button
                    type="button"
                    className="btn-cancel"
                    onClick={() => setRequestsDisplayLimit(pendingRequests.length)}
                  >
                    Show more
                  </button>
                </div>
              )}

              {requestsDisplayLimit > 10 && (
                <div className="admin-list-footer">
                  <button
                    type="button"
                    className="btn-cancel"
                    onClick={() => setRequestsDisplayLimit(10)}
                  >
                    Show less
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </section>
  );
}

export default AdminEquipmentPanel;
