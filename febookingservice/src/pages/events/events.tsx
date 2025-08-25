import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import './events.css';
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
  pictureUrl: string;
};

export type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type SortKey =
  | 'startTime,asc'
  | 'startTime,desc'
  | 'createdAt,desc'
  | 'createdAt,asc'
  | 'ticketPrice,asc'
  | 'ticketPrice,desc';

const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });
const dtFmt: Intl.DateTimeFormatOptions = { dateStyle: 'medium', timeStyle: 'short' };

const EventsList: React.FC = () => {
  const navigate = useNavigate();

  // UI state
  const [searchInput, setSearchInput] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [sort, setSort] = useState<SortKey>('startTime,asc');
  const [page, setPage] = useState(0);          // 0-based for backend
  const [size, setSize] = useState(12);

  // Data state
  const [data, setData] = useState<PageResponse<EventDto> | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  // debounce search
  useEffect(() => {
    const id = window.setTimeout(() => setDebouncedSearch(searchInput.trim()), 400);
    return () => window.clearTimeout(id);
  }, [searchInput]);

  // fetch events
  useEffect(() => {
    let mounted = true;
    const fetchPage = async () => {
      setLoading(true);
      setErr(null);
      try {
        const res = await api.get<PageResponse<EventDto>>('/events', {
          params: {
            page,
            size,
            sort,             // backend supports sort=property,(asc|desc)
            title: debouncedSearch || undefined, // server-side title search (requires BE support)
          },
          withCredentials: true,
        });
        if (mounted) setData(res.data);
      } catch (e: any) {
        if (mounted) setErr(e?.response?.data?.message || 'Failed to load events');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    fetchPage();
    return () => { mounted = false; };
  }, [page, size, sort, debouncedSearch]);

  // reset to first page when search/sort change
  useEffect(() => { setPage(0); }, [debouncedSearch, sort]);

  const total = data?.totalElements ?? 0;
  const totalPages = data?.totalPages ?? 0;
  const startIdx = total === 0 ? 0 : page * size + 1;
  const endIdx = total === 0 ? 0 : Math.min((page + 1) * size, total);

  const results = data?.content ?? [];

  const onPrev = () => setPage(p => Math.max(0, p - 1));
  const onNext = () => setPage(p => Math.min(totalPages - 1, p + 1));
  const goToPage = (p: number) => setPage(Math.min(Math.max(0, p), Math.max(0, totalPages - 1)));

  const sortLabel = useMemo(() => {
    switch (sort) {
      case 'startTime,asc': return 'Event Date (Earliest)';
      case 'startTime,desc': return 'Event Date (Latest)';
      case 'createdAt,desc': return 'Created (Newest)';
      case 'createdAt,asc': return 'Created (Oldest)';
      case 'ticketPrice,asc': return 'Price (Lowest)';
      case 'ticketPrice,desc': return 'Price (Highest)';
      default: return 'Event Date (Earliest)';
    }
  }, [sort]);

  return (
    <div className="evlist-container">
      {/* Header */}
      <div className="evlist-header">
        <h1 className="evlist-title">Browse Events</h1>
        <p className="evlist-subtitle">Discover concerts and performances. Book your seat today.</p>
      </div>

      {/* Filter Bar */}
      <div className="evlist-filterbar">
        <div className="evlist-filter-row">
          <div className="evlist-filter-group">
            <label htmlFor="ev-search" className="evlist-filter-label">Search</label>
            <input
              id="ev-search"
              type="text"
              className="evlist-input"
              placeholder="Search by event title…"
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
            />
          </div>

          <div className="evlist-filter-group">
            <label htmlFor="ev-sort" className="evlist-filter-label">Sort By</label>
            <select
              id="ev-sort"
              className="evlist-select"
              value={sort}
              onChange={e => setSort(e.target.value as SortKey)}
            >
              <option value="startTime,asc">Event Date (Earliest)</option>
              <option value="startTime,desc">Event Date (Latest)</option>
              <option value="ticketPrice,asc">Price (Lowest)</option>
              <option value="ticketPrice,desc">Price (Highest)</option>
              <option value="createdAt,desc">Created (Newest)</option>
              <option value="createdAt,asc">Created (Oldest)</option>
            </select>
          </div>

          <div className="evlist-filter-group">
            <label htmlFor="ev-size" className="evlist-filter-label">Page Size</label>
            <select
              id="ev-size"
              className="evlist-select"
              value={size}
              onChange={e => setSize(Number(e.target.value))}
            >
              <option value={6}>6</option>
              <option value={12}>12</option>
              <option value={24}>24</option>
            </select>
          </div>
        </div>
      </div>

      {/* Results header */}
      <div className="evlist-results-head">
        <div className="evlist-results-count">
          {loading ? 'Loading…' : (total === 0 ? 'No events found' : `Showing ${startIdx}–${endIdx} of ${total} results`)}
        </div>
        {!loading && <div className="evlist-sort-label">Sorted by: {sortLabel}</div>}
      </div>

      {/* Error */}
      {err && <div className="evlist-error">{err}</div>}

      {/* Grid */}
      <div className="evlist-grid">
        {loading && Array.from({ length: size }).map((_, i) => (
          <div className="evlist-card evlist-skeleton" key={`sk-${i}`} />
        ))}

        {!loading && results.map(ev => (
          <div className="evlist-card" key={ev.id}>
            <img className="evlist-card-banner" src={ev.pictureUrl}>
            </img>
            <div className="evlist-card-body">
              <h3 className="evlist-card-title">{ev.title}</h3>

              <div className="evlist-meta">
                <div className="evlist-meta-row">
                  <span className="evlist-meta-ico">📅</span>
                  <span>{new Date(ev.startTime).toLocaleString(undefined, dtFmt)}</span>
                </div>
                <div className="evlist-meta-row">
                  <span className="evlist-meta-ico">📍</span>
                  <span title={ev.location}>{ev.location}</span>
                </div>
                <div className="evlist-meta-row">
                  <span className="evlist-badge">{ev.availableSeats} seats left</span>
                  <span className="evlist-price">{gbp.format(ev.ticketPrice || 0)}</span>
                </div>
              </div>

              <button
                className="evlist-btn"
                onClick={() => navigate(`/events/${ev.id}`)}
              >
                View Details
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="evlist-pagination">
        <div className="evlist-page-info">
          {total === 0 ? '—' : `Page ${page + 1} of ${totalPages}`}
        </div>

        <div className="evlist-page-controls">
          <button className="evlist-page-btn" disabled={page === 0 || loading} onClick={onPrev}>
            Previous
          </button>

          {/* simple numeric buttons (1..N) - compact for small N */}
          <div className="evlist-page-numbers">
            {Array.from({ length: totalPages }).slice(0, 7).map((_, idx) => {
              const p = idx; // show first up to 7 pages for simplicity
              return (
                <button
                  key={p}
                  className={`evlist-page-btn ${p === page ? 'active' : ''}`}
                  onClick={() => goToPage(p)}
                  disabled={loading}
                >
                  {p + 1}
                </button>
              );
            })}
            {totalPages > 7 && <span className="evlist-ellipsis">…</span>}
          </div>

          <button className="evlist-page-btn" disabled={page >= totalPages - 1 || loading} onClick={onNext}>
            Next
          </button>
        </div>
      </div>
    </div>
  );
};

export default EventsList;
