import React, { createContext, useContext, useEffect, useState } from 'react';
import { api } from '../api/api';

type User = { name?: string; email?: string; picture?: string; appUserId?: number };
type AuthState = { user: User | null; loading: boolean; refresh: () => Promise<void> };

const Ctx = createContext<AuthState>({ user: null, loading: true, refresh: async () => {} });

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = async () => {
    try {
      const res = await api.get<User>('/auth/me');
      setUser(res.data);
    } catch {
      setUser(null);  // 401 → chưa đăng nhập/hết phiên
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { refresh(); }, []);

  // revalidate khi tab focus + định kỳ
  useEffect(() => {
    const onFocus = () => refresh();
    window.addEventListener('focus', onFocus);
    const id = window.setInterval(refresh, 5 * 60_000);
    return () => { window.removeEventListener('focus', onFocus); clearInterval(id); };
  }, []);

  // nếu có request trả 401 → clear user
  useEffect(() => {
    const i = api.interceptors.response.use(
      r => r,
      e => {
        if (e?.response?.status === 401) setUser(null);
        return Promise.reject(e);
      }
    );
    return () => api.interceptors.response.eject(i);
  }, []);

  return <Ctx.Provider value={{ user, loading, refresh }}>{children}</Ctx.Provider>;
};

export const useAuth = () => useContext(Ctx);
