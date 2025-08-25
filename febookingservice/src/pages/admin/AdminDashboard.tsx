import React, { useEffect, useMemo, useState } from 'react';
import { api } from '../api/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Line, Doughnut } from 'react-chartjs-2';
import './AdminDashboard.css';

// Register Chart.js parts
ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, ArcElement, Tooltip, Legend);

/* ================== Types ================== */
type Summary = {
  grossRevenue: number;
  netRevenue?: number;
  ticketsSold: number;
  ordersPaid: number;
  aov: number;
  avgTicketsPerOrder: number;
  conversionRatePct: number;
  abandonedRatePct: number;
  refunds?: number;
};

type TimeseriesPoint = {
  day: string;            // e.g. "2025-08-01"
  grossRevenue: number;
  ticketsSold: number;
  ordersPaid: number;
};

type PaymentBreakdown = {
  paid: number;
  pending: number;
  canceled: number;
};

type TopEvent = {
  eventId: number;
  title: string;
  revenue: number;
  tickets: number;
};

type OccupancyItem = {
  eventId: number;
  title: string;
  totalSeats: number;
  availableSeats: number;
  occupancyPct: number;
};

/* ================== Helpers ================== */
const COLORS = {
  revenue: { border: '#6366f1', fill: 'rgba(99,102,241,0.15)' },  // indigo
  tickets: { border: '#10b981', fill: 'rgba(16,185,129,0.15)' },  // emerald
  paid: '#10b981',                                                // emerald
  pending: '#f59e0b',                                            // amber
  canceled: '#ef4444',                                           // red
};


const gbp = new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' });
const fmtInt = (n: number | undefined) => (n ?? 0).toLocaleString();

function toISODate(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

function addDays(d: Date, delta: number) {
  const dd = new Date(d);
  dd.setDate(dd.getDate() + delta);
  return dd;
}

/* ================== Component ================== */
const AdminDashboard: React.FC = () => {
  // Default range: last 30 days
  const today = useMemo(() => new Date(), []);
  const [from, setFrom] = useState(toISODate(addDays(today, -29)));
  const [to, setTo] = useState(toISODate(today));

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [summary, setSummary] = useState<Summary | null>(null);
  const [series, setSeries] = useState<TimeseriesPoint[]>([]);
  const [breakdown, setBreakdown] = useState<PaymentBreakdown | null>(null);
  const [topEvents, setTopEvents] = useState<TopEvent[]>([]);
  const [occupancy, setOccupancy] = useState<OccupancyItem[]>([]);

  const quickRange = (days: number) => {
    const t = new Date();
    setTo(toISODate(t));
    setFrom(toISODate(addDays(t, -(days - 1))));
  };

  const fetchAll = async () => {
    setLoading(true);
    setError(null);
    try {
      const [s, ts, pb, te, oc] = await Promise.all([
        api.get<Summary>('/admin/dashboard/summary', { params: { from, to }, withCredentials: true }),
        api.get<TimeseriesPoint[]>('/admin/dashboard/timeseries', { params: { from, to }, withCredentials: true }),
        api.get<PaymentBreakdown>('/admin/dashboard/payment-breakdown', { params: { from, to }, withCredentials: true }),
        api.get<TopEvent[]>('/admin/dashboard/top-events', { params: { from, to, limit: 10 }, withCredentials: true }),
        api.get<OccupancyItem[]>('/admin/dashboard/occupancy', { params: { from, to }, withCredentials: true }),
      ]);

      setSummary(s.data);
      setSeries(ts.data);
      setBreakdown(pb.data);
      setTopEvents(te.data);
      setOccupancy(oc.data);
    } catch (e: any) {
      setError(e?.response?.data?.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [from, to]);

  /* ===== Charts Data ===== */
  const lineLabels = useMemo(
    () => (series.length ? series.map(p => p.day) : []),
    [series]
  );
  const revenueData = useMemo(() => series.map(p => p.grossRevenue), [series]);
  const ticketsData = useMemo(() => series.map(p => p.ticketsSold), [series]);

const lineData = useMemo(() => ({
  labels: lineLabels,
  datasets: [
    {
      label: 'Revenue (GBP)',
      data: revenueData,
      borderColor: COLORS.revenue.border,
      backgroundColor: COLORS.revenue.fill,
      fill: true,
      borderWidth: 2,
      tension: 0.3,
      pointRadius: 2,
      pointHoverRadius: 4,
      yAxisID: 'yRevenue',
    },
    {
      label: 'Tickets Sold',
      data: ticketsData,
      borderColor: COLORS.tickets.border,
      backgroundColor: COLORS.tickets.fill,
      fill: true,
      borderDash: [6, 3],
      borderWidth: 2,
      tension: 0.3,
      pointRadius: 2,
      pointHoverRadius: 4,
      yAxisID: 'yTickets',
    },
  ],
}), [lineLabels, revenueData, ticketsData]);

  const lineOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false as const,
    plugins: {
      legend: { position: 'top' as const },
      tooltip: {
        callbacks: {
          label: (ctx: any) => {
            const label = ctx.dataset.label || '';
            const val = ctx.parsed.y ?? 0;
            if (ctx.dataset.yAxisID === 'yRevenue') return `${label}: ${gbp.format(val)}`;
            return `${label}: ${fmtInt(val)}`;
          }
        }
      }
    },
    scales: {
      yRevenue: {
        type: 'linear' as const,
        position: 'left' as const,
        ticks: { callback: (v: any) => gbp.format(Number(v)) }
      },
      yTickets: {
        type: 'linear' as const,
        position: 'right' as const,
        grid: { drawOnChartArea: false },
        ticks: { callback: (v: any) => fmtInt(Number(v)) }
      },
      x: { ticks: { autoSkip: true, maxRotation: 0 } }
    }
  }), []);

 const donutData = useMemo(() => {
  const p = breakdown?.paid ?? 0;
  const q = breakdown?.pending ?? 0;
  const c = breakdown?.canceled ?? 0;
  return {
    labels: ['Paid', 'Pending', 'Canceled'],
    datasets: [{
      data: [p, q, c],
      backgroundColor: [COLORS.paid, COLORS.pending, COLORS.canceled],
      hoverBackgroundColor: ['#34d399', '#fbbf24', '#f87171'], // lighter hovers
      borderColor: '#ffffff',
      borderWidth: 2,
      hoverOffset: 6,
    }],
  };
}, [breakdown]);

//

//

  const donutOptions = useMemo(() => ({
    responsive: true,
    maintainAspectRatio: false as const,
    plugins: {
      legend: { position: 'bottom' as const }
    }
  }), []);

  return (
    <div className="dash-wrap">
      {/* Top: Header + Date Filters */}
      <div className="dash-top">
        <div>
          <h1 className="dash-title">Sales Dashboard</h1>
          <p className="dash-subtitle">Revenue, tickets and performance overview</p>
        </div>

        <div className="dash-filters">
          <div className="dash-date">
            <label className="dash-label">From</label>
            <input type="date" value={from} onChange={e => setFrom(e.target.value)} className="dash-input" />
          </div>
          <div className="dash-date">
            <label className="dash-label">To</label>
            <input type="date" value={to} onChange={e => setTo(e.target.value)} className="dash-input" />
          </div>
          <div className="dash-quick">
            <button className="dash-btn" onClick={() => quickRange(7)}>Last 7d</button>
            <button className="dash-btn" onClick={() => quickRange(30)}>Last 30d</button>
            <button className="dash-btn" onClick={() => quickRange(90)}>Last 90d</button>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="dash-kpi">
        <div className="dash-card">
          <div className="dash-kpi-label">Gross Revenue</div>
          <div className="dash-kpi-value">{summary ? gbp.format(summary.grossRevenue ?? 0) : '—'}</div>
        </div>
        <div className="dash-card">
          <div className="dash-kpi-label">Orders Paid</div>
          <div className="dash-kpi-value">{summary ? fmtInt(summary.ordersPaid) : '—'}</div>
        </div>
        <div className="dash-card">
          <div className="dash-kpi-label">Tickets Sold</div>
          <div className="dash-kpi-value">{summary ? fmtInt(summary.ticketsSold) : '—'}</div>
        </div>
        <div className="dash-card">
          <div className="dash-kpi-label">AOV</div>
          <div className="dash-kpi-value">{summary ? gbp.format(summary.aov ?? 0) : '—'}</div>
        </div>
        <div className="dash-card">
          <div className="dash-kpi-label">Conversion</div>
          <div className="dash-kpi-value">{summary ? `${(summary.conversionRatePct ?? 0).toFixed(1)}%` : '—'}</div>
        </div>
        <div className="dash-card">
          <div className="dash-kpi-label">Abandoned</div>
          <div className="dash-kpi-value">{summary ? `${(summary.abandonedRatePct ?? 0).toFixed(1)}%` : '—'}</div>
        </div>
      </div>

      {error && <div className="dash-alert">{error}</div>}

      {/* Charts Row */}
      <div className="dash-row">
        <div className="dash-panel">
          <div className="dash-panel-title">Revenue & Tickets</div>
          <div className="dash-chart">
            {loading ? <div className="dash-skel" /> : <Line data={lineData} options={lineOptions} />}
          </div>
        </div>

        <div className="dash-panel small">
          <div className="dash-panel-title">Payment Status</div>
          <div className="dash-chart">
            {loading ? <div className="dash-skel" /> : <Doughnut data={donutData} options={donutOptions} />}
          </div>
        </div>
      </div>

      {/* Tables Row */}
      <div className="dash-row">
        <div className="dash-panel">
          <div className="dash-panel-title">Top Events by Revenue</div>
          <div className="dash-table-wrap">
            <table className="dash-table">
              <thead>
                <tr>
                  <th style={{ width: '56px' }}>#</th>
                  <th>Event</th>
                  <th className="right">Revenue</th>
                  <th className="right">Tickets</th>
                </tr>
              </thead>
              <tbody>
                {loading && Array.from({ length: 5 }).map((_, i) => (
                  <tr key={`sk-te-${i}`} className="dash-skel-row"><td colSpan={4} /></tr>
                ))}
                {!loading && topEvents.length === 0 && (
                  <tr><td colSpan={4} className="empty">No data</td></tr>
                )}
                {!loading && topEvents.map((ev, idx) => (
                  <tr key={ev.eventId}>
                    <td>{idx + 1}</td>
                    <td className="ellipsis" title={ev.title}>{ev.title}</td>
                    <td className="right">{gbp.format(ev.revenue || 0)}</td>
                    <td className="right">{fmtInt(ev.tickets)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="dash-panel">
          <div className="dash-panel-title">Occupancy (Sell-through)</div>
          <div className="dash-table-wrap">
            <table className="dash-table">
              <thead>
                <tr>
                  <th style={{ width: '56px' }}>#</th>
                  <th>Event</th>
                  <th className="right">Sold / Total</th>
                  <th className="right">Occupancy</th>
                </tr>
              </thead>
              <tbody>
                {loading && Array.from({ length: 5 }).map((_, i) => (
                  <tr key={`sk-oc-${i}`} className="dash-skel-row"><td colSpan={4} /></tr>
                ))}
                {!loading && occupancy.length === 0 && (
                  <tr><td colSpan={4} className="empty">No data</td></tr>
                )}
                {!loading && occupancy.map((it, idx) => {
                  const sold = Math.max((it.totalSeats ?? 0) - (it.availableSeats ?? 0), 0);
                  return (
                    <tr key={it.eventId}>
                      <td>{idx + 1}</td>
                      <td className="ellipsis" title={it.title}>{it.title}</td>
                      <td className="right">{fmtInt(sold)}/{fmtInt(it.totalSeats)}</td>
                      <td className="right">{(it.occupancyPct ?? 0).toFixed(1)}%</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
