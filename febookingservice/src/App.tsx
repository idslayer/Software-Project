
import { Box } from "@mui/material";
import { Route, BrowserRouter as Router, Routes } from "react-router-dom";
import "./App.css";
import Footer from "./pages/footer/footer";
import Header from "./pages/header/header";
import SignIn from "./pages/login/login";
import MyBookings from "./pages/booking/bookings";
import ProtectedRoute from "./utility/ProtectedRoute";
import Home from "./pages/home/home";
import EventDetail from "./pages/event/eventdetail";
import EventCreate from "./pages/admin/EventCreate";
import EventEdit from "./pages/admin/EventEdit";
import AdminEvent from "./pages/admin/AdminEvent";
import AdminDashboard from "./pages/admin/AdminDashboard";
import EventsList from "./pages/events/events";

import { AuthProvider } from "./pages/auth/AuthContext";
import PaymentSuccess from "./pages/payment/PaymentSuccess";

const App: React.FC = () => {
  



  return (
    <Router>
      <AuthProvider>
        <Box display="flex" flexDirection="column" minHeight="100vh" width={"100vw"}>
          <Header />
          <div className="flex flex-col bg-white min-h-[1000px]">
            <Routes>
              <Route
                path="/"
                element={
                    <Home />
                }
              />
               <Route
                path="/payment/success/:id"
                element={
                  <ProtectedRoute>
                    <PaymentSuccess />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/bookings"
                element={
                  <ProtectedRoute>
                    <MyBookings />
                  </ProtectedRoute>
                }
              />
              {/* Route chi tiết sự kiện có tham số id */}
              <Route
                path="/events/:id"
                element={
                  <ProtectedRoute>
                    <EventDetail />
                  </ProtectedRoute>
                }
              />
              <Route path="/login" element={<SignIn />}/>
              <Route path="/events" element={<ProtectedRoute>
                    <EventsList />
                  </ProtectedRoute>}/>
              <Route path="/admin/events/new" element={<ProtectedRoute>
                    <EventCreate />
                  </ProtectedRoute>}/>
              <Route path="/admin/events/:id/edit" element={<ProtectedRoute>
                    <EventEdit />
                  </ProtectedRoute>}/>
              <Route path="/admin/events/" element={<ProtectedRoute>
                    <AdminEvent />
                  </ProtectedRoute>}/>
              <Route path="/admin/dashboard" element={<ProtectedRoute>
                    <AdminDashboard />
                  </ProtectedRoute>}/>
            </Routes>
          </div>
          <Footer />
        </Box>
      </AuthProvider>
    </Router>
  );
};

export default App;
