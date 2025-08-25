import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, Link, useLocation } from 'react-router-dom';
import { api } from '../api/api'; // Axios instance with withCredentials + CSRF
import './styles.css';

const REDIRECT_SECONDS = 4;

type State = 'updating' | 'ok' | 'error';

const PaymentSuccess: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const [state, setState] = useState<State>('updating');
  const [seconds, setSeconds] = useState(REDIRECT_SECONDS);
  const [error, setError] = useState<string | null>(null);

  // Optional: show bookingId from query if provided (?bookingId=...)
  const bookingId = useMemo(() => {
    console.log(id,"paymentId")
    const q = new URLSearchParams(location.search);
    return q.get('bookingId') || undefined;
  }, [location.search]);

  const callUpdate = async () => {
    if (!id) {
      setError('Missing payment id in URL.');
      setState('error');
      return;
    }
    try {
      setError(null);
      setState('updating');
      await api.put(`/product/v1/payment/success/${encodeURIComponent(id)}`, null, {
        withCredentials: true,
      });
      setState('ok');
      setSeconds(REDIRECT_SECONDS);
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to update payment status.');
      setState('error');
    }
  };

  // Call update once on mount/when paymentId changes
  useEffect(() => {
    callUpdate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  // Auto redirect countdown when ok
  useEffect(() => {
    if (state !== 'ok') return;
    if (seconds <= 0) {
      navigate('/', { replace: true });
      return;
    }
    const t = setTimeout(() => setSeconds(s => s - 1), 1000);
    return () => clearTimeout(t);
  }, [state, seconds, navigate]);

  const Content = () => {
    if (state === 'updating') {
      return (
        <>
          <div className="payok-icon">
            <div className="payok-spinner" aria-hidden />
          </div>
          <h1 className="payok-title">Finalizing your payment…</h1>
          <p className="payok-subtitle">We’re confirming your transaction with our payment provider.</p>
        </>
      );
    }

    if (state === 'error') {
      return (
        <>
          <div className="payok-icon">
            <svg viewBox="0 0 72 72" className="payok-error">
              <circle cx="36" cy="36" r="32" className="payok-error-circle" />
              <path d="M24 24 L48 48 M48 24 L24 48" className="payok-error-x" />
            </svg>
          </div>
          <h1 className="payok-title">We couldn’t confirm the payment</h1>
          <p className="payok-subtitle">{error}</p>
          <div className="payok-actions">
            <button className="payok-btn primary" onClick={callUpdate}>Try again</button>
            <Link to="/" className="payok-btn ghost">Go to Home</Link>
          </div>
        </>
      );
    }

    // state === 'ok'
    return (
      <>
        <div className="payok-icon">
          {/* Animated checkmark */}
          <svg viewBox="0 0 72 72" className="payok-check">
            <circle className="payok-check-circle" cx="36" cy="36" r="32" />
            <path className="payok-check-tick" d="M22 38 L32 48 L52 26" />
          </svg>
        </div>
        <h1 className="payok-title">Payment successful</h1>
        <p className="payok-subtitle">
          Thank you for your purchase{bookingId ? <>. Booking ID: <span className="payok-id">{bookingId}</span></> : ''}.
          You’ll be redirected to the home page shortly.
        </p>

        <div className="payok-countdown">
          Redirecting in <span className="payok-seconds">{seconds}</span> seconds…
        </div>
        <div className="payok-progress">
          <div
            className="payok-progress-bar"
            style={{ width: `${((REDIRECT_SECONDS - seconds) / REDIRECT_SECONDS) * 100}%` }}
          />
        </div>

        <div className="payok-actions">
          <button className="payok-btn primary" onClick={() => navigate('/', { replace: true })}>
            Go to Home now
          </button>
          <Link to="/bookings" className="payok-btn ghost">View my bookings</Link>
        </div>
      </>
    );
  };

  return (
    <div className="payok-wrap">
      <div className="payok-card">
        <Content />
      </div>
    </div>
  );
};

export default PaymentSuccess;
