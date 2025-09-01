import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider } from '@mui/material/styles';
import { CssBaseline, Box } from '@mui/material';

// Theme
import { iimTrichyTheme } from './theme/iimTrichyTheme';

// Context
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Components
import Header from './components/Layout/Header';
import Sidebar from './components/Layout/Sidebar';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import StudentDashboard from './pages/StudentDashboard';
import StaffDashboard from './pages/StaffDashboard';
import AdminDashboard from './pages/AdminDashboard';
import Tickets from './pages/Tickets';
import LandingPage from './pages/LandingPage';
import CreateTicket from './pages/CreateTicket';
import TicketDetails from './pages/TicketDetails';
import Reports from './pages/Reports';
import Notifications from './pages/Notifications';
import Profile from './pages/Profile';
import Settings from './pages/Settings';

// Theme is now imported from separate file

const DRAWER_WIDTH = 240;

// Main App Layout Component
function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { isAuthenticated, user } = useAuth();

  const handleSidebarToggle = () => {
    setSidebarOpen(!sidebarOpen);
  };

  // Redirect to appropriate dashboard based on user role
  const getDashboardComponent = () => {
    if (!user) return <Dashboard />;
    
    switch (user.role) {
      case 'STUDENT':
        return <StudentDashboard />;
      case 'STAFF':
        return <StaffDashboard />;
      case 'ADMIN':
        return <AdminDashboard />;
      default:
        return <Dashboard />;
    }
  };

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={<LandingPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Header onMenuToggle={handleSidebarToggle} />
      <Sidebar 
        open={sidebarOpen} 
        onClose={() => setSidebarOpen(false)} 
        width={DRAWER_WIDTH} 
      />
      
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          pt: 8, // Account for fixed header
          minHeight: '100vh',
          backgroundColor: 'grey.50'
        }}
      >
        <Routes>
          <Route path="/dashboard" element={getDashboardComponent()} />
          
          <Route 
            path="/tickets" 
            element={
              <ProtectedRoute requiredPermission="view_own_tickets">
                <Tickets />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/tickets/create" 
            element={
              <ProtectedRoute requiredPermission="create_ticket">
                <CreateTicket />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/tickets/:id" 
            element={
              <ProtectedRoute requiredPermission="view_own_tickets">
                <TicketDetails />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/tickets/:id/edit" 
            element={
              <ProtectedRoute requiredPermission="create_ticket">
                <CreateTicket />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/users" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/hostel-blocks" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <div>Hostel Blocks Management (Coming Soon)</div>
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/mappings" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AdminDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/categories" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <div>Categories Management (Coming Soon)</div>
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/settings" 
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <Settings />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/tickets/all" 
            element={
              <ProtectedRoute requiredPermission="view_all_tickets">
                <Tickets />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/staff" 
            element={
              <ProtectedRoute requiredRole="STAFF">
                <StaffDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/student" 
            element={
              <ProtectedRoute requiredRole="STUDENT">
                <StudentDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/reports" 
            element={
              <ProtectedRoute requiredPermission="generate_reports">
                <Reports />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/notifications" 
            element={
              <ProtectedRoute>
                <Notifications />
              </ProtectedRoute>
            } 
          />
          
          <Route path="/profile" element={<Profile />} />
          <Route path="/settings" element={<Settings />} />
          
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Box>
    </Box>
  );
}

function App() {
  return (
    <ThemeProvider theme={iimTrichyTheme}>
      <CssBaseline />
      <AuthProvider>
        <Router>
          <AppLayout />
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App; 