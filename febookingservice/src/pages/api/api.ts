import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://172.187.193.117:8080',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',   // nếu BE dùng CookieCsrfTokenRepository
  xsrfHeaderName: 'X-XSRF-TOKEN', // Axios sẽ tự gắn header cho POST/PUT/DELETE
});
