// API Configuration for different environments

const getApiBaseUrl = (): string => {
  console.log('🌐 API URL Detection - FORCED PRODUCTION VERSION:');
  console.log('🌐 NODE_ENV:', process.env.NODE_ENV);
  console.log('🌐 REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
  console.log('🌐 window.location.hostname:', window.location.hostname);
  
  // FORCE PRODUCTION - Always use production URL unless explicitly localhost
  const isLocalhost = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '0.0.0.0';
  
  if (isLocalhost) {
    // Local development - use local backend
    const localUrl = 'http://localhost:8080/api';
    console.log('🌐 LOCAL DEV: Using local backend URL:', localUrl);
    return localUrl;
  } else {
    // ANYTHING ELSE - Force production backend
    const productionUrl = 'https://hostel-ticketing-portal.onrender.com/api';
    console.log('🌐 PRODUCTION FORCED: Using production backend:', productionUrl);
    console.log('🌐 Hostname detected:', window.location.hostname);
    return productionUrl;
  }
};

// TEMPORARY DEBUG: Force production URL 
export const API_BASE_URL = process.env.NODE_ENV === 'development' && window.location.hostname === 'localhost' 
  ? 'http://localhost:8080/api' 
  : 'https://hostel-ticketing-portal.onrender.com/api';

console.log('🌐 FINAL API_BASE_URL:', API_BASE_URL);

// Keep the function for debugging
const debugUrl = getApiBaseUrl();
console.log('🌐 Function would return:', debugUrl);

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
