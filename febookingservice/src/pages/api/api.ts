import axios from 'axios';

export const api = axios.create({
  baseURL:    (import.meta as any).env?.VITE_API_BASE_URL || 'https://phuong.tiktuzki.com',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',   // nếu BE dùng CookieCsrfTokenRepository
  xsrfHeaderName: 'X-XSRF-TOKEN', // Axios sẽ tự gắn header cho POST/PUT/DELETE
});
