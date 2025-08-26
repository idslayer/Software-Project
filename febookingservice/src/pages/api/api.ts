import axios from 'axios';

export const api = axios.create({
  baseURL:  'https://phuong.tiktuzki.com',
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',   // nếu BE dùng CookieCsrfTokenRepository
  xsrfHeaderName: 'X-XSRF-TOKEN', // Axios sẽ tự gắn header cho POST/PUT/DELETE
});
