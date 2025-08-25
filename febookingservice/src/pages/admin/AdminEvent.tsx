import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import './AdminEvent.css';

export type EventDto = {
  id: number;
  title: string;
  brief?: string;
  description: string;
  location: string;
  startTime: string;
  endTime: string;
  totalSeats: number;
  availableSeats: number;
  ticketPrice: number;
  createdAt: string;
};

export type PageResponse<T> = {
  content: T[];
  number: number;          // 0-based
  size: number;
  totalElements: number;
  totalPages: number;
};

type SortKey =
  | 'createdAt,desc'
  | 'createdAt,asc'
  | 'startTime,desc'
  | 'startTime,asc'
  | 'title,asc'
  | 'title,desc';

const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });
const dtFmt: Intl.DateTimeFormatOptions = { dateStyle: 'medium', timeStyle: 'short' };

const AdminEvents: React.FC = () => {
  const navigate = useNavigate();

  // filters & paging
  const [title, setTitle] = useState('');
  const [sort, setSort] = useState<SortKey>('createdAt,desc');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  // data
  const [data, setData] = useState<PageResponse<EventDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // actions
  const [menuOpenId, setMenuOpenId] = useState<number | null>(null);
  const [confirmId, setConfirmId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

  const total = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;
  const rows = data?.content ?? [];
  const startIdx = total === 0 ? 0 : page * size + 1;
  const endIdx = total === 0 ? 0 : Math.min((page + 1) * size, total);

  // fetch page
  const fetchPage = async () => {
    setLoading(true);
    setErr(null);
    try {
      const res = await api.get<PageResponse<EventDto>>('/events', {
        params: { page, size, sort, title: title || undefined },
        withCredentials: true,
      });
      setData(res.data);
    } catch (e: any) {
      setErr(e?.response?.data?.message || 'Failed to load events');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchPage(); }, [page, size, sort, title]);
  useEffect(() => { setPage(0); }, [sort, title]); // reset page when filters change

  // close dropdown on outside click
  useEffect(() => {
    const onDocClick = (e: MouseEvent) => {
      const el = e.target as HTMLElement;
      if (!el.closest('.admev-menu') && !el.closest('.admev-menu-trigger')) {
        setMenuOpenId(null);
      }
    };
    document.addEventListener('mousedown', onDocClick);
    return () => document.removeEventListener('mousedown', onDocClick);
  }, []);

  const onPrev = () => setPage(p => Math.max(0, p - 1));
  const onNext = () => setPage(p => Math.min(totalPages - 1, p + 1));
  const goToPage = (p: number) => setPage(Math.min(Math.max(0, p), Math.max(0, totalPages - 1)));

  const openMenu = (id: number) => setMenuOpenId(prev => (prev === id ? null : id));
  const confirmDelete = (id: number) => { setMenuOpenId(null); setConfirmId(id); };

  const doDelete = async () => {
    if (!confirmId) return;
    setDeleting(true);
    try {
      await api.delete(`/events/${confirmId}`, { withCredentials: true });
      setConfirmId(null);
      // if current page becomes empty after delete, go back one page
      const remaining = (rows.length ?? 1) - 1;
      if (remaining <= 0 && page > 0) {
        setPage(p => p - 1);
      } else {
        await fetchPage();
      }
    } catch (e: any) {
      alert(e?.response?.data?.message || 'Delete failed');
    } finally {
      setDeleting(false);
    }
  };

  const fmtDate = (iso: string) => new Date(iso).toLocaleString(undefined, dtFmt);

  return (
    <div className="admev-wrap">
      {/* Top bar */}
      <div className="admev-topbar">
        <div className="admev-titlebox">
          <h1 className="admev-title">Manage Events</h1>
          <p className="admev-subtitle">Create, edit, and monitor your events</p>
        </div>
        <div className="admev-actions">
          <button className="admev-btn primary" onClick={() => navigate('/admin/events/new')}>
            + New Event
          </button>
        </div>
      </div>

      {/* Filter bar */}
      <div className="admev-filterbar">
        <div className="admev-filter-row">
          <div className="admev-filter-group">
            <label htmlFor="admev-title" className="admev-label">Search by Title</label>
            <input
              id="admev-title"
              className="admev-input"
              placeholder="Type to search…"
              value={title}
              onChange={e => setTitle(e.target.value)}
            />
          </div>

          <div className="admev-filter-group">
            <label htmlFor="admev-sort" className="admev-label">Sort</label>
            <select
              id="admev-sort"
              className="admev-select"
              value={sort}
              onChange={e => setSort(e.target.value as SortKey)}
            >
              <option value="createdAt,desc">Created (Newest)</option>
              <option value="createdAt,asc">Created (Oldest)</option>
              <option value="startTime,desc">Start Time (Latest)</option>
              <option value="startTime,asc">Start Time (Earliest)</option>
              <option value="title,asc">Title (A→Z)</option>
              <option value="title,desc">Title (Z→A)</option>
            </select>
          </div>

          <div className="admev-filter-group">
            <label htmlFor="admev-size" className="admev-label">Page Size</label>
            <select
              id="admev-size"
              className="admev-select"
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

      {/* Result header */}
      <div className="admev-reshead">
        <div className="admev-rescount">
          {loading ? 'Loading…' : (total === 0 ? 'No events found' : `Showing ${startIdx}–${endIdx} of ${total} events`)}
        </div>
        {err && <div className="admev-error">{err}</div>}
      </div>

      {/* Table */}
      <div className="admev-tablewrap">
        <table className="admev-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Start</th>
              <th>End</th>
              <th>Location</th>
              <th>Seats</th>
              <th>Price</th>
              <th>Created</th>
              <th className="admev-col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading && Array.from({ length: Math.min(size, 8) }).map((_, i) => (
              <tr className="admev-skel" key={`sk-${i}`}>
                <td colSpan={8}>&nbsp;</td>
              </tr>
            ))}

            {!loading && rows.length === 0 && (
              <tr><td colSpan={8} style={{ textAlign: 'center', padding: '1rem' }}>No data</td></tr>
            )}

            {!loading && rows.map(ev => (
              <tr key={ev.id}>
                <td className="admev-ellipsis" title={ev.title}>{ev.title}</td>
                <td>{fmtDate(ev.startTime)}</td>
                <td>{fmtDate(ev.endTime)}</td>
                <td className="admev-ellipsis" title={ev.location}>{ev.location}</td>
                <td>{ev.availableSeats}/{ev.totalSeats}</td>
                <td>{gbp.format(ev.ticketPrice || 0)}</td>
                <td>{fmtDate(ev.createdAt)}</td>
                <td className="admev-actions-cell">
                  <button
                    className="admev-menu-trigger"
                    onClick={() => openMenu(ev.id)}
                    aria-haspopup="menu"
                    aria-expanded={menuOpenId === ev.id}
                    title="Actions"
                  >
                    ⋮
                  </button>
                  {menuOpenId === ev.id && (
                    <div className="admev-menu" role="menu">
                      <button role="menuitem" onClick={() => { setMenuOpenId(null); navigate(`/events/${ev.id}`); }}>
                        View details
                      </button>
                      <button role="menuitem" onClick={() => { setMenuOpenId(null); navigate(`/admin/events/${ev.id}/edit`); }}>
                        Edit
                      </button>
                      <button role="menuitem" className="danger" onClick={() => confirmDelete(ev.id)}>
                        Delete
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      <div className="admev-pager">
        <div className="admev-pageinfo">{total === 0 ? '—' : `Page ${page + 1} of ${totalPages}`}</div>
        <div className="admev-pagectrls">
          <button className="admev-pagebtn" onClick={onPrev} disabled={page === 0 || loading}>Previous</button>
          <div className="admev-pagenums">
            {Array.from({ length: Math.min(totalPages, 7) }).map((_, i) => {
              const p = i;
              return (
                <button
                  key={p}
                  className={`admev-pagebtn ${p === page ? 'active' : ''}`}
                  onClick={() => goToPage(p)}
                  disabled={loading}
                >
                  {p + 1}
                </button>
              );
            })}
            {totalPages > 7 && <span className="admev-ellipsis-txt">…</span>}
          </div>
          <button className="admev-pagebtn" onClick={onNext} disabled={page >= totalPages - 1 || loading}>Next</button>
        </div>
      </div>

      {/* Delete modal */}
      <div className="admev-modal-overlay" style={{ display: confirmId ? 'flex' : 'none' }}>
        <div className="admev-modal" role="dialog" aria-modal="true" aria-labelledby="admev-del-title">
          <h3 id="admev-del-title" className="admev-modal-title">Delete event?</h3>
          <p className="admev-modal-text">
            This will permanently delete event <strong>#{confirmId}</strong>.
          </p>
          <div className="admev-modal-actions">
            <button className="admev-btn" onClick={() => setConfirmId(null)} disabled={deleting}>Close</button>
            <button className="admev-btn danger" onClick={doDelete} disabled={deleting}>
              {deleting ? 'Deleting…' : 'Confirm Delete'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminEvents;
