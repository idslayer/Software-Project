import React, { useEffect, useState } from 'react';
import './home.css';
import { useNavigate } from 'react-router-dom';
import { api } from '../api/api';
type EventDto = {
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
  pictureUrl:string;
};

type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

// Helper: build query params with multiple sort keys
const buildPageParams = (page: number, size: number, sorts: string[]) => {
  const p = new URLSearchParams();
  p.append('page', String(page));
  p.append('size', String(size));
  sorts.forEach(s => p.append('sort', s));
  return p;
};

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [events, setEvents] = useState<EventDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    // Scroll animations
    const observerOptions = { threshold: 0.1, rootMargin: '0px 0px -50px 0px' };
    const observer = new IntersectionObserver((entries) => {
      entries.forEach(entry => { if (entry.isIntersecting) entry.target.classList.add('visible'); });
    }, observerOptions);
    document.querySelectorAll('.fade-in').forEach(el => observer.observe(el));

    // Navbar scroll effect
    const onScroll = () => {
      const navbar = document.querySelector('.navbar') as HTMLElement | null;
      if (!navbar) return;
      if (window.scrollY > 50) {
        navbar.style.background = 'rgba(255, 255, 255, 0.98)';
        navbar.style.boxShadow = '0 2px 20px rgba(0, 0, 0, 0.1)';
      } else {
        navbar.style.background = 'rgba(255, 255, 255, 0.95)';
        navbar.style.boxShadow = 'none';
      }
    };
    window.addEventListener('scroll', onScroll);

    return () => {
      observer.disconnect();
      window.removeEventListener('scroll', onScroll);
    };
  }, []);

  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      setErr(null);
      try {
        // Lấy 3 sự kiện sắp tới: sort theo startTime asc, fallback title asc
        const params = buildPageParams(0, 3, ['startTime,asc', 'title,asc']);
        const res = await api.get<PageResponse<EventDto>>('/events', { params });
        if (!mounted) return;
        setEvents(res.data.content || []);
      } catch (e: any) {
        if (!mounted) return;
        setErr(e?.response?.data?.message || 'Không thể tải danh sách sự kiện');
      } finally {
        if (mounted) setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const fmtDateTime = (iso?: string) => {
    if (!iso) return '';
    return new Date(iso).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
  };

  return (
    <>
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-content">
          <h1>Experience Live Music Like Never Before</h1>
          <p>Discover amazing concerts, festivals, and live performances. Book your tickets instantly and join thousands of music lovers worldwide.</p>
          {/* <a href="/events" className="book-now-btn">Book Now</a> */}
           <button
                  className="book-now-btn"
                  onClick={() => navigate(`/events`)}
                >
                  Book Now
                </button>
        </div>
      </section>

      {/* Events Section */}
      <section className="events">
        <div className="section-header fade-in">
          <h2 className="section-title">Upcoming Events</h2>
          <p className="section-subtitle">Don't miss out on these incredible live performances happening near you</p>
        </div>

        {loading && <div className="fade-in" style={{ textAlign: 'center' }}>Đang tải…</div>}
        {err && <div className="fade-in" style={{ color: '#ef4444', textAlign: 'center' }}>{err}</div>}

        <div className="events-grid">
          {events.map(evt => (
            <div key={evt.id} className="event-card fade-in">
              <img className="event-image" src={evt.pictureUrl}/>
              <div className="event-info">
                <h3 className="event-title">{evt.title}</h3>
                <div className="event-meta">
                  <div className="meta-item">
                    <span className="meta-icon">📅</span>
                    <span>{fmtDateTime(evt.startTime)}</span>
                  </div>
                  <div className="meta-item">
                    <span className="meta-icon">📍</span>
                    <span>{evt.location}</span>
                  </div>
                  <div className="seats-available">
                    {evt.availableSeats} seats available
                  </div>
                </div>
                <button
                  className="view-details-btn"
                  onClick={() => navigate(`/events/${evt.id}`)}
                >
                  View Details
                </button>
              </div>
            </div>
          ))}

          {!loading && !err && events.length === 0 && (
            <div className="fade-in" style={{ gridColumn: '1 / -1', textAlign: 'center' }}>
              Chưa có sự kiện.
            </div>
          )}
        </div>
      </section>
    </>
  );
};

export default Home;
