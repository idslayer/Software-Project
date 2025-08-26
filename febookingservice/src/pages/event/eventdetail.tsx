import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import './eventdetail.css';
import { useAuth } from '../auth/AuthContext';

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
  pictureUrl: string;
};

type BookingResponse = {
  bookingId: number;
  userId: number | string;
  eventId: number;
  numberOfTickets: number;
  totalPrice?: number;
  status?: string;
  createdAt?: string;
  sessionUrl: string;
  message: string;
};


type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

// const serviceFee = 12.5; // GBP
const maxTickets = 8;

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://172.187.193.117:8080',
  withCredentials: true
});

// Helper: build query params with multiple sort keys (sort=field,dir)
const buildPageParams = (page: number, size: number, sorts: string[]) => {
  const p = new URLSearchParams();
  p.append('page', String(page));
  p.append('size', String(size));
  sorts.forEach(s => p.append('sort', s));
  return p;
};

const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });

const EventDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [event, setEvent] = useState<EventDto | null>(null);
  const [related, setRelated] = useState<EventDto[]>([]);
  const [quantity, setQuantity] = useState(1);
  // const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [bookingLoading, setBookingLoading] = useState(false);
  const [bookingErr, setBookingErr] = useState<string | null>(null);

  const carouselRef = useRef<HTMLDivElement>(null);
  const isDragging = useRef(false);
  const startX = useRef(0);
  const scrollLeft = useRef(0);

 
  const GUEST_USER_ID = user?.appUserId || "";

  // Fetch event detail + related (paginated + sorted)
  useEffect(() => {
    if (!id) return;
    let mounted = true;

    (async () => {
      setLoading(true);
      setErr(null);
      try {
        // 1) Chi tiết sự kiện
        const eventRes = await api.get<EventDto>(`/events/${id}`);
        if (!mounted) return;

        setEvent(eventRes.data);
        // setTotal(eventRes.data.ticketPrice * quantity);

        // 2) More like this — lấy 8 bản ghi, sort theo startTime asc + title asc
        const params = buildPageParams(0, 8, ['startTime,asc', 'title,asc']);
        const pageRes = await api.get<PageResponse<EventDto>>('/events', { params });
        if (!mounted) return;

        const items = (pageRes.data.content || [])
          .filter(e => e.id !== Number(id))
          .slice(0, 4);
        setRelated(items);
      } catch (e: any) {
        setErr(e?.response?.data?.message || 'Không thể tải dữ liệu sự kiện');
      } finally {
        if (mounted) setLoading(false);
      }
    })();

    return () => { mounted = false; };
  }, [id, quantity]);

  // Fade-in
  useEffect(() => {
    const observer = new IntersectionObserver(
      entries => entries.forEach(entry => {
        if (entry.isIntersecting) {
          (entry.target as HTMLElement).style.animation = 'fadeInUp 0.6s ease forwards';
        }
      }),
      { threshold: 0.1, rootMargin: '0px 0px -50px 0px' }
    );
    document.querySelectorAll('.event-fade-in').forEach(el => observer.observe(el));
    return () => observer.disconnect();
  }, [loading]);

  // Carousel drag
  useEffect(() => {
    const carousel = carouselRef.current;
    if (!carousel) return;

    const onMouseDown = (e: any) => {
      isDragging.current = true;
      startX.current = e.pageX - carousel.offsetLeft;
      scrollLeft.current = carousel.scrollLeft;
      carousel.style.cursor = 'grabbing';
    };
    const onMouseUp = () => {
      isDragging.current = false;
      carousel.style.cursor = 'grab';
    };
    const onMouseMove = (e: any) => {
      if (!isDragging.current) return;
      e.preventDefault();
      const x = e.pageX - carousel.offsetLeft;
      const walk = (x - startX.current) * 2;
      carousel.scrollLeft = scrollLeft.current - walk;
    };

    carousel.addEventListener('mousedown', onMouseDown);
    carousel.addEventListener('mouseup', onMouseUp);
    carousel.addEventListener('mouseleave', onMouseUp);
    carousel.addEventListener('mousemove', onMouseMove);

    return () => {
      carousel.removeEventListener('mousedown', onMouseDown);
      carousel.removeEventListener('mouseup', onMouseUp);
      carousel.removeEventListener('mouseleave', onMouseUp);
      carousel.removeEventListener('mousemove', onMouseMove);
    };
  }, []);

  const changeQuantity = (delta: number) => {
    setQuantity(prev => Math.min(Math.max(prev + delta, 1), maxTickets));
  };

  const fmtDateRange = (startIso?: string, endIso?: string) => {
    if (!startIso) return '';
    const start = new Date(startIso);
    const end = endIso ? new Date(endIso) : undefined;
    const opts: Intl.DateTimeFormatOptions = { dateStyle: 'medium', timeStyle: 'short' };
    return end
      ? `${start.toLocaleString(undefined, opts)} – ${end.toLocaleString(undefined, opts)}`
      : start.toLocaleString(undefined, opts);
  };

  // Gọi POST /bookings khi bấm nút
  const handleBooking = async () => {
  if (!event) return;
  setBookingLoading(true);
  setBookingErr(null);

  try {
    const payload = {
      userId: GUEST_USER_ID,         // tạm thời gán cứng
      eventId: event.id,             // id sự kiện hiện tại
      numberOfTickets: quantity
    };

    const res = await api.post<BookingResponse>('/bookings', payload, {
      headers: { 'Content-Type': 'application/json' }
    });

    const sessionUrl = res.data?.sessionUrl;
    if (sessionUrl && typeof sessionUrl === 'string') {
      // Chuyển trang sang cổng thanh toán
      window.location.assign(sessionUrl);
      // hoặc: window.location.href = sessionUrl;
      return;
    }

    // Không có sessionUrl -> báo lỗi dễ hiểu
    setBookingErr(res.data?.message || 'Không nhận được sessionUrl từ máy chủ.');
  } catch (e: any) {
    const msg = e?.response?.data?.message || e?.message || 'Đặt vé thất bại';
    setBookingErr(msg);
  } finally {
    // Lưu ý: nếu đã redirect, dòng dưới sẽ không còn chạy. Không sao.
    setBookingLoading(false);
  }
};

  if (loading) return <div className="main-content">Đang tải…</div>;
  if (err) return <div className="main-content" style={{ color: '#ef4444' }}>{err}</div>;
  if (!event) return <div className="main-content">Không tìm thấy sự kiện.</div>;

  return (
    <>
      <main className="main-content">
        <div className={`content-grid`}>
          <div className="div">
            <img src={`${event.pictureUrl}`} className=''/>
          <div className="event-details event-fade-in">
            <h2 className="event-title">{event.title}</h2>

            <div className="event-meta">
              <div className="meta-item">
                <svg className="meta-icon" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11zM7 10h5v5H7z"/>
                </svg>
                <div className="meta-content">
                  <div className="meta-label">Date & Time</div>
                  <div className="meta-value">{fmtDateRange(event.startTime, event.endTime)}</div>
                </div>
              </div>

              <div className="meta-item">
                <svg className="meta-icon" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                </svg>
                <div className="meta-content">
                  <div className="meta-label">Location</div>
                  <div className="meta-value">{event.location}</div>
                </div>
              </div>
            </div>
            <p className="event-description font-semibold">{event.brief}</p>

            <p className="event-description">{event.description}</p>
          </div>
          </div>
          <div className="ticket-panel event-fade-in">
            <div className="ticket-header">
              <h3 className="ticket-title">Get Your Tickets</h3>
              <div className="ticket-availability">
                <span className="availability-dot" /> {event.availableSeats} tickets available
              </div>
            </div>
            <div className="quantity-section">
              <div className="quantity-label">Number of Tickets</div>
              <div className="quantity-selector">
                <button className="quantity-btn" onClick={() => changeQuantity(-1)} disabled={quantity <= 1}>−</button>
                <input type="number" className="quantity-input" value={quantity} readOnly />
                <button className="quantity-btn" onClick={() => changeQuantity(1)} disabled={quantity >= maxTickets}>+</button>
              </div>
            </div>

            <div className="price-section">
              <div className="price-row">
                <span className="price-label">Price per ticket</span>
                <span className="price-value">{gbp.format(event.ticketPrice)}</span>
              </div>
              {/* <div className="price-row">
                <span className="price-label">Service fee</span>
                <span className="price-value">{gbp.format(serviceFee)}</span>
              </div> */}
              <div className="price-row total-price-row">
                <span className="price-label total-label">Total</span>
                <span className="price-value total-price">
                  {gbp.format(event.ticketPrice * quantity)}
                </span>
              </div>
            </div>

          <button
              className="book-btn"
              onClick={handleBooking}
              disabled={bookingLoading}
            >
              {bookingLoading ? 'Processing…' : 'Book Tickets Now'}
            </button>
            {bookingErr && (
              <div style={{ marginTop: 12, color: '#ef4444', fontWeight: 600 }}>
                {bookingErr}
              </div>
            )}
          </div>
        </div>

        <section className="related-section event-fade-in">
          <h2 className="section-title">More like this</h2>
          <div className="carousel-container">
            <div className="carousel" ref={carouselRef}>
              {related.map(evt => (
                <div
                  key={evt.id}
                  className="related-card"
                  onClick={() => navigate(`/events/${evt.id}`)}
                >
                  <img className="card-image" src={evt.pictureUrl}/>
                  <div className="card-content">
                    <h3 className="card-title">{evt.title}</h3>
                    <p className="card-date">
                      {new Date(evt.startTime).toLocaleDateString(undefined, { dateStyle: 'medium' })}
                    </p>
                  </div>
                </div>
              ))}
              {related.length === 0 && <div style={{ padding: '1rem' }}>No related events.</div>}
            </div>
          </div>
        </section>
      </main>
    </>
  );
};

export default EventDetail;
