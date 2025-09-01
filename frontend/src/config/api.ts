// API Configuration for different environments

const getApiBaseUrl = (): string => {
  // In production, use environment variable (Render deployment)
  if (process.env.NODE_ENV === 'production') {
    return process.env.REACT_APP_API_URL || window.location.origin + '/api';
  }
  
  // In development, use localhost
  return process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
};

export const API_BASE_URL = getApiBaseUrl();

// API endpoints
export const API_ENDPOINTS = {
  // Authentication
  LOGIN: `${API_BASE_URL}/users/authenticate`,
  
  // Tickets
  TICKETS: `${API_BASE_URL}/tickets`,
  TICKETS_SIMPLE: (creatorId: string) => `${API_BASE_URL}/tickets/simple?creatorId=${creatorId}`,
  
  // Admin
  ADMIN_USERS: `${API_BASE_URL}/admin/users`,
  ADMIN_MAPPINGS: `${API_BASE_URL}/admin/mappings`,
  ADMIN_STAFF: `${API_BASE_URL}/admin/staff`,
  ADMIN_HOSTELS: `${API_BASE_URL}/admin/hostels`,
  
  // User management
  DELETE_MAPPING: (mappingId: string) => `${API_BASE_URL}/admin/mappings/${mappingId}`,
} as const;

export default API_BASE_URL;
