import { useLocation, useNavigate } from "react-router-dom";
import '../home/home.css';
import { api } from '../api/api';
import { AutoComplete, Button, Input, Select } from "antd";
import { useEffect, useState } from "react";
import { useAuth } from '../auth/AuthContext';
import {
  deleteSearch,
  fetchRecentSearches,
  saveSearch,
} from "../../service/service";
import { CloseOutlined, SearchOutlined } from "@ant-design/icons";

export default function Header({ onSearchChange }: any) {
  const { user, refresh } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const onLogo = () => {
    navigate(`/`);
  };

 const onLogout = async (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    try {
      const res = await api.post('/logout'); // Axios sends cookies + XSRF header
      console.log('logout status', res.status);
    } catch (err) {
      console.warn('logout failed', err);
    } finally {
      await refresh(); // /auth/me -> 401 -> user = null
      navigate('/login', { replace: true });
    }
  };

  const [search, setSearch] = useState("");
  const [recentSearch, setRecentSearch] = useState<any>();
  const handleSaveSearchPromp = async () => {
    try {
      (await saveSearch(search)) as any;
      handleGetSearch();
    } catch (error: any) {
      console.log(
        `${error.response?.data?.message || "Something went wrong."} `
      );
    }
  };
  const handleDeleteSearchPromp = async (id: any) => {
    try {
      (await deleteSearch(id)) as any;
      handleGetSearch();
    } catch (error: any) {
      console.log(
        `${error.response?.data?.message || "Something went wrong."} `
      );
    }
  };
  const handleGetSearch = async () => {
    try {
      const data = await fetchRecentSearches();
      if (data?.data?.searches?.length == 0) {
        setRecentSearch([]);
      } else
        setRecentSearch(
          data?.data?.searches.map((item: any) => {
            return {
              key: item.id,
              value: item.query,
            };
          })
        );
    } catch (error: any) {
      console.log(
        `${error.response?.data?.message || "Something went wrong."} `
      );
    }
  };
  const verifyLogin = localStorage.getItem("token") ?? false;
  useEffect(() => {
    handleGetSearch();
  }, []);
  return (
      <nav className="navbar">
        <div className="nav-container">
          <div className="logo cursor-pointer" onClick={()=>{
            navigate("/")
          }}>SoundWave</div>
          <ul className="nav-menu">
            <li><a href="/" className="nav-link">Home</a></li>

             <li><a href="/events" className="nav-link">Events</a></li>
            {/* <li><a href="/login" className="login-btn">Login / Register</a></li> */}
             {/* <li><a href="#" className="login-btn">Log out</a></li> */}
             {user ? (
            <>
              <li><span className="nav-link">Hi, {user.name || user.email}</span></li>
              <li><a href="/admin/events" className="nav-link">Admin Events</a></li>
                <li><a href="/admin/dashboard" className="nav-link">Admin DB</a></li>
              <li><a href="/bookings" className="nav-link">My Bookings</a></li>
              <li>
                <a href="#" className="login-btn" onClick={onLogout}>
                  Log out
                </a>
              </li>
            </>
          ) : (
                 <li><a href="/login" className="login-btn">Login / Register</a></li>
          )}
          </ul>
         
        </div>
      </nav> 
  );
}
