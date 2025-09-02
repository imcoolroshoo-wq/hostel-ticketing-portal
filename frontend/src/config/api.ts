// API Configuration for different environments

const getApiBaseUrl = (): string => {
  console.log('🌐 Environment Detection:');
  console.log('🌐 NODE_ENV:', process.env.NODE_ENV);
  console.log('🌐 REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
  console.log('🌐 window.location.hostname:', window.location.hostname);
  console.log('🌐 window.location.href:', window.location.href);
  
  // First priority: Use environment variable if provided
  if (process.env.REACT_APP_API_URL) {
    console.log('🌐 Using REACT_APP_API_URL:', process.env.REACT_APP_API_URL);
    return process.env.REACT_APP_API_URL;
  }
  
  // Second priority: Determine based on hostname
  const isLocalhost = window.location.hostname === 'localhost' || 
                     window.location.hostname === '127.0.0.1' ||
                     window.location.hostname === '0.0.0.0';
  
  if (isLocalhost) {
    const localUrl = 'http://localhost:8080/api';
    console.log('🌐 LOCAL DEVELOPMENT: Using local backend:', localUrl);
    return localUrl;
  } else {
    const productionUrl = 'https://hostel-ticketing-portal.onrender.com/api';
    console.log('🌐 PRODUCTION: Using production backend:', productionUrl);
    console.log('🌐 Detected hostname:', window.location.hostname);
    return productionUrl;
  }
};

export const API_BASE_URL = getApiBaseUrl();
console.log('🌐 FINAL API_BASE_URL:', API_BASE_URL);

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
  
  // Escalation management
  ESCALATIONS: `${API_BASE_URL}/escalations`,
  ESCALATION_STATISTICS: `${API_BASE_URL}/escalations/statistics`,
  MANUAL_ESCALATION: `${API_BASE_URL}/escalations/manual`,
  PROCESS_AUTO_ESCALATIONS: `${API_BASE_URL}/escalations/process-automatic`,
  TICKET_ESCALATIONS: (ticketId: string) => `${API_BASE_URL}/escalations/ticket/${ticketId}`,
  USER_ESCALATIONS: (userId: string) => `${API_BASE_URL}/escalations/user/${userId}`,
  OVERDUE_ESCALATIONS: `${API_BASE_URL}/escalations/overdue`,
  
  // Analytics and reports
  ANALYTICS: `${API_BASE_URL}/analytics`,
  ADVANCED_ANALYTICS: `${API_BASE_URL}/analytics/advanced`,
  REPORTS: `${API_BASE_URL}/reports`,
  
  // Bulk operations
  BULK_OPERATIONS: `${API_BASE_URL}/bulk`,
  BULK_UPDATE_STATUS: `${API_BASE_URL}/bulk/update-status`,
  BULK_ASSIGN: `${API_BASE_URL}/bulk/assign`,
  BULK_EXPORT: `${API_BASE_URL}/bulk/export`,
  
  // Quality assurance
  QA: `${API_BASE_URL}/qa`,
  QA_VERIFICATION: `${API_BASE_URL}/qa/verification`,
  QA_QUALITY_CHECKS: `${API_BASE_URL}/qa/quality-checks`,
  
  // Asset management
  ASSETS: `${API_BASE_URL}/assets`,
  ASSET_MOVEMENTS: `${API_BASE_URL}/assets/movements`,
  MAINTENANCE_SCHEDULES: `${API_BASE_URL}/assets/maintenance`,
  
  // System configuration
  SYSTEM_CONFIG: `${API_BASE_URL}/system`,
  SYSTEM_HEALTH: `${API_BASE_URL}/system/health`,
  SYSTEM_PARAMETERS: `${API_BASE_URL}/system/parameters`,
} as const;

export default API_BASE_URL;
