
import React, { useEffect, useState } from 'react';
import { api } from '../api/api';
import './bookings.css';
import { useAuth } from '../auth/AuthContext';

type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELED';

export type BookingDto = {
  bookingId: string;
  userId: string;
  eventId: number;
  numberOfTickets: number;
  totalPrice: number;
  status: BookingStatus;
  createdAt: string;           
  sessionUrl?: string | null;
  message?: string | null;
  eventTitle: string | undefined;
  eventTime: string ;
  eventLocation: string | undefined;
};

export type PageResponse<T> = {
  content: T[];
  number: number;              // page index (0-based)
  size: number;
  totalElements: number;
  totalPages: number;
};

type SortKey = 'createdAt,desc' | 'createdAt,asc' | 'totalPrice,desc' | 'totalPrice,asc';

const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });
const dtFmt: Intl.DateTimeFormatOptions = { dateStyle: 'medium', timeStyle: 'short' };

const MyBookings: React.FC = () => {
  // Filters & paging

  const [status, setStatus] = useState<'ALL' | BookingStatus>('ALL');
  const [searchId, setSearchId] = useState('');
  const [sort, setSort] = useState<SortKey>('createdAt,desc');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const { user } = useAuth();
  const USER_ID = user?.appUserId || "";
  // Data
  const [data, setData] = useState<PageResponse<BookingDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // Cancel modal
  const [cancelId, setCancelId] = useState<string | null>(null);
  const [cancelLoading, setCancelLoading] = useState(false);

  // Fetch bookings
  useEffect(() => {
    let mounted = true;
     
    const fetchPage = async () => {
      setLoading(true);
      setErr(null);
      try {
        const res = await api.get<PageResponse<BookingDto>>('/bookings', {
          params: {
            page,
            size,
            sort,
            status: status === 'ALL' ? undefined : status,    
            q: searchId || undefined,  
            userId: USER_ID                      
          },
          withCredentials: true,
        });
        if (mounted) {
          setData(res.data);
        }
      } catch (e: any) {
        if (mounted) setErr(e?.response?.data?.message || 'Failed to load bookings');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    fetchPage();
    return () => { mounted = false; };
  }, [page, size, sort, status, searchId]);

  // Reset to page 0 when filters change (except page/size)
  useEffect(() => { setPage(0); }, [status, sort, searchId]);

  const total = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;
  const startIdx = total === 0 ? 0 : page * size + 1;
  const endIdx = total === 0 ? 0 : Math.min((page + 1) * size, total);
  const rows = data?.content ?? [];
  const onPrev = () => setPage(p => Math.max(0, p - 1));
  const onNext = () => setPage(p => Math.min(totalPages - 1, p + 1));
  const goToPage = (p: number) => setPage(Math.min(Math.max(0, p), Math.max(0, totalPages - 1)));

  const openPay = (url?: string | null) => {
    if (!url) {
      alert('No payment session available for this booking.');
      return;
    }
    window.location.href = url;
  };

  const confirmCancel = (id: string) => setCancelId(id);
  const closeModal = () => { if (!cancelLoading) setCancelId(null); };

  const doCancel = async () => {
    if (!cancelId) return;
    setCancelLoading(true);
    try {
      await api.put(`/bookings/cancel/${cancelId}`);
      // refresh current page
      const res = await api.get<PageResponse<BookingDto>>('/bookings', {
        params: { page, size, sort, status: status === 'ALL' ? undefined : status, q: searchId || undefined },
      });
      setData(res.data);

      setCancelId(null);
    } catch (e: any) {
      alert(e?.response?.data?.message || 'Failed to cancel booking');
    } finally {
      setCancelLoading(false);
    }
  };
 
  return (
    <div className="bklist-container">
      {/* Header */}
      <div className="bklist-header">
        <h1 className="bklist-title">My Bookings</h1>
        <p className="bklist-subtitle">Review and manage your ticket bookings</p>
      </div>

      {/* Status Tabs */}
    <div className="bklist-tabs" role="tablist">
      <button
        className={`bklist-tab ${status === 'ALL' ? 'active' : ''}`}
        onClick={() => {
          setStatus('ALL');
        }}
        role="tab"
        aria-selected={status === 'ALL'}
      >
        <span>All</span>
      
      </button>

      <button
        className={`bklist-tab ${status === 'PENDING' ? 'active' : ''}`}
        onClick={() => {
          setStatus('PENDING');
        }}
        role="tab"
        aria-selected={status === 'PENDING'}
      >
        <span>Pending</span>
      
      </button>

      <button
        className={`bklist-tab ${status === 'CONFIRMED' ? 'active' : ''}`}
        onClick={() => {
          setStatus('CONFIRMED');
        }}
        role="tab"
        aria-selected={status === 'CONFIRMED'}
      >
        <span>Paid</span>
      
      </button>

      <button
        className={`bklist-tab ${status === 'CANCELED' ? 'active' : ''}`}
        onClick={() => {
          setStatus('CANCELED');
        }}
        role="tab"
        aria-selected={status === 'CANCELED'}
      >
        <span>Canceled</span>
       
      </button>
    </div>


      {/* Filter Bar */}
      <div className="bklist-filterbar">
        <div className="bklist-filter-row">
          <div className="bklist-filter-group">
            <label className="bklist-filter-label" htmlFor="bk-id">Search</label>
            <input
              id="bk-id"
              className="bklist-input"
              placeholder="Search by Booking ID…"
              value={searchId}
              onChange={e => setSearchId(e.target.value.trim())}
            />
          </div>

          <div className="bklist-filter-group">
            <label className="bklist-filter-label" htmlFor="bk-sort">Sort</label>
            <select
              id="bk-sort"
              className="bklist-select"
              value={sort}
              onChange={e => setSort(e.target.value as SortKey)}
            >
              <option value="createdAt,desc">Created (Newest)</option>
              <option value="createdAt,asc">Created (Oldest)</option>
              <option value="totalPrice,desc">Total (Highest)</option>
              <option value="totalPrice,asc">Total (Lowest)</option>
            </select>
          </div>

          <div className="bklist-filter-group">
            <label className="bklist-filter-label" htmlFor="bk-size">Page Size</label>
            <select
              id="bk-size"
              className="bklist-select"
              value={size}
              onChange={e => setSize(Number(e.target.value))}
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results header */}
      <div className="bklist-results-head">
        <div className="bklist-results-count">
          {loading ? 'Loading…' : (total === 0 ? 'No bookings found' : `Showing ${startIdx}–${endIdx} of ${total} bookings`)}
        </div>
      </div>

      {err && <div className="bklist-error">{err}</div>}

      {/* Table (desktop) */}
      <div className="bklist-table-wrap">
        <table className="bklist-table">
          <thead>
            <tr>
              <th>Event title</th>
              <th>Event Start Time</th>
              <th>Event Location</th>
              <th>Number of Tickets</th>
              <th>Total</th>
              <th>Status</th>
              <th>Date</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {loading && Array.from({ length: Math.min(size, 8) }).map((_, i) => (
              <tr className="bklist-skeleton-row" key={`sk-${i}`}>
                <td colSpan={7}>&nbsp;</td>
              </tr>
            ))}

            {!loading && rows.length === 0 && (
              <tr><td colSpan={7} style={{ textAlign: 'center', padding: '1rem' }}>No data</td></tr>
            )}

            {!loading && rows.map(b => (
              <tr key={b.bookingId}>
                <td title={b.eventTitle} className="bklist-mono">{b.eventTitle}</td>
                <td>{new Date(b.eventTime).toLocaleString(undefined, dtFmt)}</td>
                <td>{b.eventLocation}</td>
                <td>{b.numberOfTickets}</td>
                <td>{gbp.format(b.totalPrice || 0)}</td>
                <td>
                  <span
                    className={`bklist-badge-status ${
                      b.status === 'PENDING' ? 'pending' :
                      b.status === 'CONFIRMED' ? 'paid' : 'canceled'
                    }`}
                  >
                    {b.status}
                  </span>
                </td>
                <td>{new Date(b.createdAt).toLocaleString(undefined, dtFmt)}</td>
                <td className="bklist-actions">
                  {b.status === 'PENDING' ? (
                    <>
                      <button className="bklist-btn primary" onClick={() => openPay(b.sessionUrl)}>Pay</button>
                      <button className="bklist-btn" onClick={() => confirmCancel(b.bookingId)}>Cancel</button>
                    </>
                  ) : (
                    <button className="bklist-btn" onClick={() => alert(`View ${b.bookingId}`)}>View</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Mobile cards */}
      <div className="bklist-cards">
        {!loading && rows.map(b => (
          <div className="bklist-card" key={`m-${b.bookingId}`}>
            <div className="bklist-card-head">
              <span className="bklist-mono">{b.bookingId}</span>
              <span className={`bklist-badge-status ${
                b.status === 'PENDING' ? 'pending' :
                b.status === 'CONFIRMED' ? 'paid' : 'canceled'
              }`}>{b.status}</span>
            </div>
            <div className="bklist-card-row"><strong>Event ID:</strong> {b.eventId}</div>
            <div className="bklist-card-row"><strong>Qty:</strong> {b.numberOfTickets}</div>
            <div className="bklist-card-row"><strong>Total:</strong> {gbp.format(b.totalPrice || 0)}</div>
            <div className="bklist-card-row"><strong>Created:</strong> {new Date(b.createdAt).toLocaleString(undefined, dtFmt)}</div>
            <div className="bklist-card-actions">
              {b.status === 'PENDING' ? (
                <>
                  <button className="bklist-btn primary" onClick={() => openPay(b.sessionUrl)}>Pay</button>
                  <button className="bklist-btn" onClick={() => confirmCancel(b.bookingId)}>Cancel</button>
                </>
              ) : (
                <button className="bklist-btn" onClick={() => alert(`View ${b.bookingId}`)}>View</button>
              )}
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="bklist-pagination">
        <div className="bklist-page-info">
          {total === 0 ? '—' : `Page ${page + 1} of ${totalPages}`}
        </div>
        <div className="bklist-page-controls">
          <button className="bklist-page-btn" disabled={page === 0 || loading} onClick={onPrev}>Previous</button>
          {/* compact numbers (first up to 7 pages) */}
          <div className="bklist-page-numbers">
            {Array.from({ length: Math.min(totalPages, 7) }).map((_, idx) => {
              const p = idx;
              return (
                <button
                  key={p}
                  className={`bklist-page-btn ${p === page ? 'active' : ''}`}
                  onClick={() => goToPage(p)}
                  disabled={loading}
                >
                  {p + 1}
                </button>
              );
            })}
            {totalPages > 7 && <span className="bklist-ellipsis">…</span>}
          </div>
          <button className="bklist-page-btn" disabled={page >= totalPages - 1 || loading} onClick={onNext}>Next</button>
          <select className="bklist-select small" value={size} onChange={e => setSize(Number(e.target.value))}>
            <option value={10}>10 / page</option>
            <option value={20}>20 / page</option>
            <option value={50}>50 / page</option>
          </select>
        </div>
      </div>

      {/* Cancel Modal */}
      <div className="bklist-modal-overlay" style={{ display: cancelId ? 'flex' : 'none' }}>
        <div className="bklist-modal" role="dialog" aria-modal="true" aria-labelledby="cancelTitle">
          <h3 id="cancelTitle" className="bklist-modal-title">Cancel booking?</h3>
          <p className="bklist-modal-text">Booking <span className="bklist-mono">{cancelId}</span> will be canceled. This action cannot be undone.</p>
          <div className="bklist-modal-actions">
            <button className="bklist-btn" onClick={closeModal} disabled={cancelLoading}>Close</button>
            <button className="bklist-btn danger" onClick={doCancel} disabled={cancelLoading}>
              {cancelLoading ? 'Canceling…' : 'Confirm Cancel'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
export default MyBookings;
