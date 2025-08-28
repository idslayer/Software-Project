import { useNavigate } from "react-router-dom";
import '../home/home.css';
import { api } from '../api/api';
import { useAuth } from '../auth/AuthContext';

export default function Header({ }: any) {
  const { user, refresh } = useAuth();
  const navigate = useNavigate();


 const onLogout = async (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    try {
      const res = await api.post('/logout'); // Axios sends cookies + XSRF header
      localStorage.removeItem("token");
      console.log('logout status', res.status);
    } catch (err) {
      console.warn('logout failed', err);
    } finally {
      await refresh(); // /auth/me -> 401 -> user = null
      navigate('/login', { replace: true });
    }
  };

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

               {/* Admin links: chỉ hiện khi role là admin */}
                {user.role === "ADMIN" && (
                  <>
                    <li><a href="/admin/events" className="nav-link">Admin Events</a></li>
                    <li><a href="/admin/dashboard" className="nav-link">Admin DB</a></li>
                  </>
                )}
            
              <li><a href="/bookings" className="nav-link">My Bookings</a></li>
              <li>
                <a href="#" className="login-btn" onClick={onLogout}>
                  Log out
                </a>
              </li>
            </>
          ) : (
                 <li><a href="/login" className="login-btn">Login</a></li>
          )}
          </ul>
         
        </div>
      </nav> 
  );
}
