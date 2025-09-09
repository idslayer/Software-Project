// import React, { ReactNode } from "react";
// import { Navigate } from "react-router-dom";

// interface ProtectedRouteProps {
//   children: ReactNode;
//   authenticated: boolean;
// }

// const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
//   children,
//   authenticated,
// }) => {
//   return authenticated ? children : <Navigate to="/login" replace />;
// };

// export default ProtectedRoute;


import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../pages/auth/AuthContext';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, loading } = useAuth();
  if (loading) return <div style={{ padding: 24 }}></div>;
  return user ? <>{children}</> : <Navigate to="/login" replace />;
};

export default ProtectedRoute;
