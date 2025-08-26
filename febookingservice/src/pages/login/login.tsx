import React, { useState, useEffect, FormEvent, MouseEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import './login.css';

const API_BASE_URL = 'https://phuong.tiktuzki.com';


// const API_BASE_URL =
//   (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080';
const Login: React.FC = () => {
  const { refresh, user } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState(''); // nếu có login local sau này
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);

  useEffect(() => {
    if (user) navigate('/', { replace: true });
  }, [user, navigate]);

  useEffect(() => {
    const container = document.querySelector('.log-container') as HTMLElement | null;
    if (!container) return;
    const onMouseEnter = () => {
      container.style.transform = 'translateY(-2px)';
      container.style.boxShadow = '0 25px 70px rgba(0,0,0,0.12)';
    };
    const onMouseLeave = () => {
      container.style.transform = 'translateY(0)';
      container.style.boxShadow = '0 20px 60px rgba(0,0,0,0.08)';
    };
    container.addEventListener('mouseenter', onMouseEnter);
    container.addEventListener('mouseleave', onMouseLeave);
    return () => {
      container.removeEventListener('mouseenter', onMouseEnter);
      container.removeEventListener('mouseleave', onMouseLeave);
    };
  }, []);

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    // TODO: gọi API login nội bộ nếu cần
    setTimeout(async () => {
      setLoading(false);
      await refresh();
    }, 800);
  };

  const handleGoogle = (e: MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    setGoogleLoading(true);
    const base = API_BASE_URL.replace(/\/+$/, '');
    const googleAuthUrl = `${base}/oauth2/authorization/google`;
    window.location.assign(googleAuthUrl);
  };

  return (
    <div className="flex w-[100vw] items-center justify-center min-h-[1000px]">
      <div className="log-container">
        <div className="logo-l">
          <div className="logo-l-text">SoundWave</div>
          <div className="logo-l-tagline">Your gateway to live music</div>
        </div>

        <form onSubmit={handleSubmit} className="log-form">
          <div className="form-group">
            <label htmlFor="email" className="form-label">Email Address</label>
            <input
              type="email" id="email" name="email"
              className="form-input" placeholder="Enter your email"
              value={email} onChange={e => setEmail(e.target.value)} required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password" className="form-label">Password</label>
            <input
              type="password" id="password" name="password"
              className="form-input" placeholder="Enter your password"
              value={password} onChange={e => setPassword(e.target.value)} required
            />
          </div>

          <button type="submit" className={`log-btn ${loading ? 'btn-loading' : ''}`} disabled={loading}>
            {loading ? 'Signing in...' : 'Log In'}
          </button>
        </form>

        <div className="divider"><span>or</span></div>

        <button
          className={`google-btn ${googleLoading ? 'btn-loading' : ''}`}
          onClick={handleGoogle}
          disabled={googleLoading}
        >
          <div className="google-icon" />
          {googleLoading ? 'Connecting...' : 'Continue with Google'}
        </button>
      </div>
    </div>
  );
};

export default Login;
