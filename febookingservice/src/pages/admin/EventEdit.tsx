import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../api/api';
import './EventCreate.css'; // tái sử dụng CSS evcreate- từ trang tạo mới

type EventDto = {
  id: number;
  title: string;
  brief?: string;
  description: string;
  location: string;
  startTime: string;   // ISO-8601
  endTime: string;     // ISO-8601
  totalSeats: number;
  availableSeats: number;
  ticketPrice: number;
  pictureUrl?: string;
  createdAt: string;
};

type EventUpdateReq = {
  title: string;
  brief?: string;
  description: string;
  location: string;
  startTime: string;   // ISO-8601
  endTime: string;     // ISO-8601
  totalSeats: number;
  availableSeats: number;
  ticketPrice: number;
  pictureUrl?: string;
};

type FormState = {
  title: string;
  brief: string;
  description: string;
  location: string;
  startTimeLocal: string; // yyyy-MM-ddTHH:mm cho <input type="datetime-local">
  endTimeLocal: string;
  totalSeats: string;
  availableSeats: string;
  ticketPrice: string;
  pictureUrl: string;
};

type Errors = Partial<Record<keyof FormState, string>>;

const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });

function toIsoOrEmpty(local: string): string {
  if (!local) return '';
  const d = new Date(local);
  return isNaN(d.getTime()) ? '' : d.toISOString();
}

// ISO → "YYYY-MM-DDTHH:mm" cho <input type="datetime-local">
function isoToLocalInput(iso?: string): string {
  if (!iso) return '';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '';
  const pad = (n: number) => String(n).padStart(2, '0');
  const y = d.getFullYear();
  const m = pad(d.getMonth() + 1);
  const day = pad(d.getDate());
  const hh = pad(d.getHours());
  const mm = pad(d.getMinutes());
  return `${y}-${m}-${day}T${hh}:${mm}`;
}

const EventEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const eventId = Number(id);
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [loadErr, setLoadErr] = useState<string | null>(null);
  const [serverErr, setServerErr] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [form, setForm] = useState<FormState>({
    title: '',
    brief: '',
    description: '',
    location: '',
    startTimeLocal: '',
    endTimeLocal: '',
    totalSeats: '',
    availableSeats: '',
    ticketPrice: '',
    pictureUrl: ''
  });

  const pricePreview = useMemo(() => {
    const n = Number(form.ticketPrice);
    return isNaN(n) ? '—' : gbp.format(n);
  }, [form.ticketPrice]);

  // ===== FILL DỮ LIỆU TỪ /events/{id} =====
  useEffect(() => {
    let mounted = true;
    const load = async () => {
      if (isNaN(eventId)) {
        setLoadErr('Invalid event id');
        setLoading(false);
        return;
      }
      setLoading(true);
      setLoadErr(null);
      try {
        const res = await api.get<EventDto>(`/events/${eventId}`, { withCredentials: true });
        if (!mounted) return;
        const ev = res.data;
        setForm({
          title: ev.title ?? '',
          brief: ev.brief ?? '',
          description: ev.description ?? '',
          location: ev.location ?? '',
          startTimeLocal: isoToLocalInput(ev.startTime),
          endTimeLocal: isoToLocalInput(ev.endTime),
          totalSeats: ev.totalSeats != null ? String(ev.totalSeats) : '',
          availableSeats: ev.availableSeats != null ? String(ev.availableSeats) : '',
          ticketPrice: ev.ticketPrice != null ? String(ev.ticketPrice) : '',
          pictureUrl: ev.pictureUrl ?? ''
        });
      } catch (e: any) {
        setLoadErr(e?.response?.data?.message || 'Failed to load event');
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false; };
  }, [eventId]);

  const [errors, setErrors] = useState<Errors>({});

  const onChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { id, value } = e.target;
    setForm(prev => {
      if (id === 'totalSeats') {
        const next: FormState = { ...prev, totalSeats: value };
        if (!prev.availableSeats) next.availableSeats = value; // auto fill nếu đang trống
        return next;
      }
      return { ...prev, [id]: value };
    });
  };

  const validate = (): Errors => {
    const errs: Errors = {};
    if (!form.title.trim()) errs.title = 'Title is required';
    if (!form.description.trim()) errs.description = 'Description is required';
    if (!form.location.trim()) errs.location = 'Location is required';
    if (!form.startTimeLocal) errs.startTimeLocal = 'Start time is required';
    if (!form.endTimeLocal) errs.endTimeLocal = 'End time is required';

    const start = form.startTimeLocal ? new Date(form.startTimeLocal) : null;
    const end = form.endTimeLocal ? new Date(form.endTimeLocal) : null;
    if (start && end && end <= start) errs.endTimeLocal = 'End must be after start';

    const total = Number(form.totalSeats);
    const avail = Number(form.availableSeats);
    if (!form.totalSeats || isNaN(total) || total < 1) errs.totalSeats = 'Total seats must be >= 1';
    if (!form.availableSeats || isNaN(avail) || avail < 0) errs.availableSeats = 'Available seats must be >= 0';
    if (!errs.totalSeats && !errs.availableSeats && avail > total) {
      errs.availableSeats = 'Available cannot exceed total';
    }

    const price = Number(form.ticketPrice);
    if (form.ticketPrice === '' || isNaN(price) || price < 0) errs.ticketPrice = 'Ticket price must be >= 0';

    if (form.pictureUrl && !/^https?:\/\/|^\/|^data:image\//i.test(form.pictureUrl)) {
      errs.pictureUrl = 'Provide a valid URL or data URI';
    }
    return errs;
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setServerErr(null);
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length) return;

    const payload: EventUpdateReq = {
      title: form.title.trim(),
      brief: form.brief.trim() || undefined,
      description: form.description.trim(),
      location: form.location.trim(),
      startTime: toIsoOrEmpty(form.startTimeLocal),
      endTime: toIsoOrEmpty(form.endTimeLocal),
      totalSeats: Number(form.totalSeats),
      availableSeats: Number(form.availableSeats),
      ticketPrice: Number(form.ticketPrice),
      pictureUrl: form.pictureUrl.trim() || undefined
    };

    setSubmitting(true);
    try {
      await api.put(`/events/${eventId}`, payload, { withCredentials: true });
      navigate(`/events/${eventId}`); // về trang chi tiết sau khi lưu
    } catch (err: any) {
      setServerErr(err?.response?.data?.message || 'Failed to update event');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="evcreate-container">Loading…</div>;
  if (loadErr) return <div className="evcreate-container"><div className="evcreate-alert error">{loadErr}</div></div>;

  return (
    <div className="evcreate-container">
      <div className="evcreate-header">
        <h1 className="evcreate-title">Edit Event</h1>
        <p className="evcreate-subtitle">Update details for event #{eventId}</p>
      </div>

      {serverErr && <div className="evcreate-alert error">{serverErr}</div>}

      <form className="evcreate-form" onSubmit={onSubmit} noValidate>
        {/* Cột trái */}
        <div className="evcreate-col">
          <div className="evcreate-group">
            <label htmlFor="title" className="evcreate-label">Title *</label>
            <input
              id="title"
              className={`evcreate-input ${errors.title ? 'invalid' : ''}`}
              value={form.title}
              onChange={onChange}
              maxLength={200}
              required
            />
            {errors.title && <div className="evcreate-error">{errors.title}</div>}
          </div>

          <div className="evcreate-group">
            <label htmlFor="brief" className="evcreate-label">Brief</label>
            <input
              id="brief"
              className="evcreate-input"
              value={form.brief}
              onChange={onChange}
              maxLength={250}
            />
          </div>

          <div className="evcreate-group">
            <label htmlFor="description" className="evcreate-label">Description *</label>
            <textarea
              id="description"
              className={`evcreate-textarea ${errors.description ? 'invalid' : ''}`}
              value={form.description}
              onChange={onChange}
              rows={8}
              required
            />
            {errors.description && <div className="evcreate-error">{errors.description}</div>}
          </div>

          <div className="evcreate-group">
            <label htmlFor="location" className="evcreate-label">Location *</label>
            <input
              id="location"
              className={`evcreate-input ${errors.location ? 'invalid' : ''}`}
              value={form.location}
              onChange={onChange}
              maxLength={250}
              required
            />
            {errors.location && <div className="evcreate-error">{errors.location}</div>}
          </div>

          <div className="evcreate-row">
            <div className="evcreate-group">
              <label htmlFor="startTimeLocal" className="evcreate-label">Start Time *</label>
              <input
                id="startTimeLocal"
                type="datetime-local"
                className={`evcreate-input ${errors.startTimeLocal ? 'invalid' : ''}`}
                value={form.startTimeLocal}
                onChange={onChange}
                required
              />
              {errors.startTimeLocal && <div className="evcreate-error">{errors.startTimeLocal}</div>}
            </div>
            <div className="evcreate-group">
              <label htmlFor="endTimeLocal" className="evcreate-label">End Time *</label>
              <input
                id="endTimeLocal"
                type="datetime-local"
                className={`evcreate-input ${errors.endTimeLocal ? 'invalid' : ''}`}
                value={form.endTimeLocal}
                onChange={onChange}
                required
              />
              {errors.endTimeLocal && <div className="evcreate-error">{errors.endTimeLocal}</div>}
            </div>
          </div>
        </div>

        {/* Cột phải */}
        <div className="evcreate-col">
          <div className="evcreate-row">
            <div className="evcreate-group">
              <label htmlFor="totalSeats" className="evcreate-label">Total Seats *</label>
              <input
                id="totalSeats"
                type="number"
                min={1}
                step={1}
                className={`evcreate-input ${errors.totalSeats ? 'invalid' : ''}`}
                value={form.totalSeats}
                onChange={onChange}
                required
              />
              {errors.totalSeats && <div className="evcreate-error">{errors.totalSeats}</div>}
            </div>
            <div className="evcreate-group">
              <label htmlFor="availableSeats" className="evcreate-label">Available Seats *</label>
              <input
                id="availableSeats"
                type="number"
                min={0}
                step={1}
                className={`evcreate-input ${errors.availableSeats ? 'invalid' : ''}`}
                value={form.availableSeats}
                onChange={onChange}
                required
              />
              {errors.availableSeats && <div className="evcreate-error">{errors.availableSeats}</div>}
            </div>
          </div>

          <div className="evcreate-group">
            <label htmlFor="ticketPrice" className="evcreate-label">Ticket Price (GBP) *</label>
            <input
              id="ticketPrice"
              type="number"
              min={0}
              step="0.01"
              className={`evcreate-input ${errors.ticketPrice ? 'invalid' : ''}`}
              value={form.ticketPrice}
              onChange={onChange}
              required
            />
            <div className="evcreate-hint">Preview: <strong>{pricePreview}</strong></div>
            {errors.ticketPrice && <div className="evcreate-error">{errors.ticketPrice}</div>}
          </div>

          <div className="evcreate-group">
            <label htmlFor="pictureUrl" className="evcreate-label">Picture URL</label>
            <input
              id="pictureUrl"
              className={`evcreate-input ${errors.pictureUrl ? 'invalid' : ''}`}
              value={form.pictureUrl}
              onChange={onChange}
              placeholder="https://… or /images/…"
            />
            {errors.pictureUrl && <div className="evcreate-error">{errors.pictureUrl}</div>}
            {form.pictureUrl && !errors.pictureUrl && (
              <div className="evcreate-image-preview">
                <img
                  src={form.pictureUrl}
                  alt="Event preview"
                  onError={(e) => ((e.target as HTMLImageElement).style.display = 'none')}
                />
              </div>
            )}
          </div>

          <div className="evcreate-actions">
            <button
              type="button"
              className="evcreate-btn secondary"
              onClick={() => navigate(`/events/${eventId}`)}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="evcreate-btn primary"
              disabled={submitting}
            >
              {submitting ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default EventEdit;
