// API Configuration for different environments

const getApiBaseUrl = (): string => {
  console.log('🌐 API URL Detection:');
  console.log('🌐 NODE_ENV:', process.env.NODE_ENV);
  console.log('🌐 REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
  console.log('🌐 window.location.hostname:', window.location.hostname);
  
  // Force production URL if running on Render
  const isOnRender = window.location.hostname.includes('onrender.com') || 
                    window.location.hostname.includes('render.com');
  
  if (isOnRender) {
    const renderUrl = 'https://hostel-ticketing-portal.onrender.com/api';
    console.log('🌐 FORCED: Detected Render deployment, using production backend:', renderUrl);
    return renderUrl;
  }
  
  // If REACT_APP_API_URL is explicitly set, use it
  if (process.env.REACT_APP_API_URL) {
    console.log('🌐 Using explicit REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
    return process.env.REACT_APP_API_URL;
  }
  
  // Check if we're running on localhost (local development)
  const isLocalhost = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '0.0.0.0';
  
  console.log('🌐 Is localhost?', isLocalhost);
  
  if (isLocalhost) {
    // Local development - use local backend
    const localUrl = 'http://localhost:8080/api';
    console.log('🌐 Using local backend URL:', localUrl);
    return localUrl;
  } else {
    // Remote deployment - use Render backend (FALLBACK)
    const remoteUrl = 'https://hostel-ticketing-portal.onrender.com/api';
    console.log('🌐 Using remote backend URL:', remoteUrl);
    return remoteUrl;
  }
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
