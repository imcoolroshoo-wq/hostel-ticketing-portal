import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'STUDENT' | 'STAFF' | 'ADMIN';
  studentId?: string;
  roomNumber?: string;
  hostelBlock?: string;
  floorNumber?: number;
  employeeCode?: string;
  emergencyContact?: string;
  staffVertical?: string;
  phone?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
  isLoading: boolean;
  isAuthenticated: boolean;
  hasRole: (role: string) => boolean;
  hasPermission: (permission: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Initialize auth state from localStorage
  useEffect(() => {
    const savedToken = localStorage.getItem('authToken');
    const savedUser = localStorage.getItem('authUser');
    
    if (savedToken && savedUser) {
      try {
        setToken(savedToken);
        setUser(JSON.parse(savedUser));
        // Set default authorization header
        axios.defaults.headers.common['Authorization'] = `Bearer ${savedToken}`;
      } catch (error) {
        console.error('Error parsing saved user data:', error);
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUser');
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string): Promise<boolean> => {
    try {
      setIsLoading(true);
      const response = await axios.post(API_ENDPOINTS.LOGIN, {
        email,
        password
      });

      if (response.data.authenticated) {
        const userData = response.data.user;
        const authToken = response.data.token || `temp-token-${userData.id}`;
        
        setUser(userData);
        setToken(authToken);
        
        // Save to localStorage
        localStorage.setItem('authToken', authToken);
        localStorage.setItem('authUser', JSON.stringify(userData));
        
        // Set default authorization header
        axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`;
        
        return true;
      }
      return false;
    } catch (error) {
      console.error('Login error:', error);
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('authToken');
    localStorage.removeItem('authUser');
    delete axios.defaults.headers.common['Authorization'];
  };

  const hasRole = (role: string): boolean => {
    return user?.role === role;
  };

  const hasPermission = (permission: string): boolean => {
    if (!user) return false;
    
    // Strict role-based permissions as per Product Design Document
    const rolePermissions = {
      STUDENT: [
        'create_ticket',
        'view_own_tickets',
        'reopen_own_tickets',
        'close_own_tickets',
        'comment_on_own_tickets',
        'rate_completed_work',
        'update_own_profile'
      ],
      STAFF: [
        'view_assigned_tickets',
        'update_assigned_ticket_status',
        'comment_on_assigned_tickets',
        'request_reassignment',
        'update_own_profile',
        'view_knowledge_base'
      ],
      ADMIN: [
        // Complete system control as specified in product design
        'create_ticket',
        'view_all_tickets',
        'edit_all_tickets',
        'delete_all_tickets',
        'assign_tickets',
        'reassign_tickets',
        'update_any_ticket_status',
        'comment_on_any_ticket',
        'bulk_ticket_operations',
        
        // User management - Admin only
        'create_users',
        'manage_users',
        'update_users',
        'deactivate_users',
        'view_all_users',
        'bulk_user_operations',
        
        // Mapping management - Admin only
        'create_mappings',
        'update_mappings',
        'delete_mappings',
        'view_mappings',
        'manage_staff_assignments',
        
        // System administration
        'view_reports',
        'generate_reports',
        'system_settings',
        'escalate_tickets',
        'system_configuration',
        'audit_logs',
        'analytics_access'
      ]
    };

    return rolePermissions[user.role]?.includes(permission) || false;
  };

  const value: AuthContextType = {
    user,
    token,
    login,
    logout,
    isLoading,
    isAuthenticated: !!user,
    hasRole,
    hasPermission
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export default AuthContext;
