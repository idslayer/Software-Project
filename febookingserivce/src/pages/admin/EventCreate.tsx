import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
import './EventCreate.css';

type EventCreateReq = {
  title: string;
  brief?: string;
  description: string;
  location: string;
  startTime: string;   // ISO-8601 for backend (Instant)
  endTime: string;     // ISO-8601
  totalSeats: number;
  availableSeats: number;
  ticketPrice: number; // GBP
  pictureUrl?: string;
};

type FormState = {
  title: string;
  brief: string;
  description: string;
  location: string;
  startTimeLocal: string; // yyyy-MM-ddTHH:mm (for <input type="datetime-local">)
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
  // <input type="datetime-local"> returns local time without timezone.
  // Convert to Date and send ISO string (UTC) for backend Instant.
  const d = new Date(local);
  return isNaN(d.getTime()) ? '' : d.toISOString();
}

const EventCreate: React.FC = () => {
  const navigate = useNavigate();

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

  const [submitting, setSubmitting] = useState(false);
  const [serverErr, setServerErr] = useState<string | null>(null);
  const [serverOk, setServerOk] = useState<string | null>(null);

  const pricePreview = useMemo(() => {
    const n = Number(form.ticketPrice);
    return isNaN(n) ? '—' : gbp.format(n);
  }, [form.ticketPrice]);

  const onChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { id, value } = e.target;
    setForm(prev => {
      // auto-fill availableSeats if empty when totalSeats changes
      if (id === 'totalSeats') {
        const next: FormState = { ...prev, totalSeats: value };
        if (!prev.availableSeats) next.availableSeats = value;
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

    // Picture is optional, but if provided, do a simple check
    if (form.pictureUrl && !/^https?:\/\/|^\/|^data:image\//i.test(form.pictureUrl)) {
      errs.pictureUrl = 'Provide a valid URL or data URI';
    }
    return errs;
  };

  const [errors, setErrors] = useState<Errors>({});

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setServerErr(null);
    setServerOk(null);
    const errs = validate();
    setErrors(errs);
    if (Object.keys(errs).length) return;

    const payload: EventCreateReq = {
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
      const res = await api.post('/events', payload, { withCredentials: true });
      setServerOk('Event created successfully.');
      // If backend returns created object with id: navigate to details
      const id = (res?.data && (res.data.id ?? res.data.eventId)) as number | undefined;
      if (typeof id === 'number') {
        navigate(`/events/${id}`);
      } else {
        // fallback: to list
        navigate('/events');
      }
    } catch (err: any) {
      setServerErr(err?.response?.data?.message || 'Failed to create event');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="evcreate-container">
      <div className="evcreate-header">
        <h1 className="evcreate-title">Create Event</h1>
        <p className="evcreate-subtitle">Fill out the details below and publish your new event.</p>
      </div>

      {serverErr && <div className="evcreate-alert error">{serverErr}</div>}
      {serverOk && <div className="evcreate-alert success">{serverOk}</div>}

      <form className="evcreate-form" onSubmit={onSubmit} noValidate>
        {/* Left column */}
        <div className="evcreate-col">
          {/* Title */}
          <div className="evcreate-group">
            <label htmlFor="title" className="evcreate-label">Title *</label>
            <input
              id="title"
              className={`evcreate-input ${errors.title ? 'invalid' : ''}`}
              placeholder="Event title"
              value={form.title}
              onChange={onChange}
              maxLength={200}
              required
            />
            {errors.title && <div className="evcreate-error">{errors.title}</div>}
          </div>

          {/* Brief */}
          <div className="evcreate-group">
            <label htmlFor="brief" className="evcreate-label">Brief</label>
            <input
              id="brief"
              className="evcreate-input"
              placeholder="Short tagline"
              value={form.brief}
              onChange={onChange}
              maxLength={250}
            />
          </div>

          {/* Description */}
          <div className="evcreate-group">
            <label htmlFor="description" className="evcreate-label">Description *</label>
            <textarea
              id="description"
              className={`evcreate-textarea ${errors.description ? 'invalid' : ''}`}
              placeholder="Full event description"
              value={form.description}
              onChange={onChange}
              rows={8}
              required
            />
            {errors.description && <div className="evcreate-error">{errors.description}</div>}
          </div>

          {/* Location */}
          <div className="evcreate-group">
            <label htmlFor="location" className="evcreate-label">Location *</label>
            <input
              id="location"
              className={`evcreate-input ${errors.location ? 'invalid' : ''}`}
              placeholder="Venue and city"
              value={form.location}
              onChange={onChange}
              maxLength={250}
              required
            />
            {errors.location && <div className="evcreate-error">{errors.location}</div>}
          </div>

          {/* Time */}
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

        {/* Right column */}
        <div className="evcreate-col">
          {/* Seats */}
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

          {/* Price */}
          <div className="evcreate-group">
            <label htmlFor="ticketPrice" className="evcreate-label">Ticket Price (GBP) *</label>
            <input
              id="ticketPrice"
              type="number"
              min={0}
              step="0.01"
              className={`evcreate-input ${errors.ticketPrice ? 'invalid' : ''}`}
              placeholder="e.g. 89.00"
              value={form.ticketPrice}
              onChange={onChange}
              required
            />
            <div className="evcreate-hint">Preview: <strong>{pricePreview}</strong></div>
            {errors.ticketPrice && <div className="evcreate-error">{errors.ticketPrice}</div>}
          </div>

          {/* Picture */}
          <div className="evcreate-group">
            <label htmlFor="pictureUrl" className="evcreate-label">Picture URL</label>
            <input
              id="pictureUrl"
              className={`evcreate-input ${errors.pictureUrl ? 'invalid' : ''}`}
              placeholder="https://… or /images/…"
              value={form.pictureUrl}
              onChange={onChange}
            />
            {errors.pictureUrl && <div className="evcreate-error">{errors.pictureUrl}</div>}
            {form.pictureUrl && !errors.pictureUrl && (
              <div className="evcreate-image-preview">
                <img src={form.pictureUrl} alt="Event preview" onError={(e) => ((e.target as HTMLImageElement).style.display = 'none')} />
              </div>
            )}
          </div>

          {/* Actions */}
          <div className="evcreate-actions">
            <button
              type="button"
              className="evcreate-btn secondary"
              onClick={() => navigate('/events')}
              disabled={submitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="evcreate-btn primary"
              disabled={submitting}
            >
              {submitting ? 'Creating…' : 'Create Event'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default EventCreate;
