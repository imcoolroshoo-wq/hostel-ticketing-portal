import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  LinearProgress,
  Avatar,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Paper,
  Divider,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Badge,
  Alert,
  Switch,
  FormControlLabel
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  People,
  BugReport,
  Analytics,
  Settings,
  TrendingUp,
  Warning,
  CheckCircle,
  Schedule,
  PersonAdd,
  Edit,
  Delete,
  Visibility,
  Block,
  CheckBox,
  Assessment,
  Notifications,
  Security,
  Build,
  School,
  AdminPanelSettings
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';
import AdminTicketManagement from '../components/AdminTicketManagement';
import MappingManagement from '../components/MappingManagement';


interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
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

interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  description: string;
  category: string;
  priority: string;
  status: string;
  hostelBlock: string;
  roomNumber: string;
  locationDetails: string;
  createdBy: any;
  assignedTo: any;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`admin-tabpanel-${index}`}
      aria-labelledby={`admin-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

interface Hostel {
  value: string;
  displayName: string;
  code: string;
  fullName: string;
  isFemaleBlock: boolean;
}

const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [users, setUsers] = useState<User[]>([]);
  const [hostels, setHostels] = useState<Hostel[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [userDialogOpen, setUserDialogOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [userFilter, setUserFilter] = useState<'all' | 'active' | 'inactive'>('all');
  const [newUserData, setNewUserData] = useState({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    role: 'STUDENT',
    studentId: '',
    roomNumber: '',
    hostelBlock: '',
    phone: '',
    password: ''
  });

  // Fetch users function
  const fetchUsers = async () => {
    try {
      console.log('Fetching users...');
      const response = await axios.get(API_ENDPOINTS.ADMIN_USERS);
      console.log('Users fetched successfully:', response.data);
      setUsers(response.data || []);
    } catch (error: any) {
      console.error('Error fetching users:', error);
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Failed to fetch users';
      alert(`Error fetching users: ${errorMessage}`);
    }
  };

  // Fetch tickets function
  const fetchTickets = async () => {
    try {
      console.log('Fetching tickets...');
      const response = await axios.get(API_ENDPOINTS.TICKETS);
      console.log('Tickets fetched successfully:', response.data);
      setTickets(response.data.tickets || response.data);
    } catch (error: any) {
      console.error('Error fetching tickets:', error);
    }
  };

  // Fetch hostels function
  const fetchHostels = async () => {
    try {
      console.log('Fetching hostels...');
      const response = await axios.get(API_ENDPOINTS.ADMIN_HOSTELS);
      console.log('Hostels fetched successfully:', response.data);
      setHostels(response.data || []);
    } catch (error: any) {
      console.error('Error fetching hostels:', error);
    }
  };

  // Fetch data
  useEffect(() => {
    const fetchData = async () => {
      try {
        await Promise.all([fetchTickets(), fetchUsers(), fetchHostels()]);
      } catch (error) {
        console.error('Error fetching data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Calculate comprehensive statistics
  const stats = {
    // User Statistics
    totalUsers: users.length,
    activeUsers: users.filter(u => u.isActive).length,
    students: users.filter(u => u.role === 'STUDENT').length,
    staff: users.filter(u => u.role === 'STAFF').length,
    admins: users.filter(u => u.role === 'ADMIN').length,
    
    // Ticket Statistics
    totalTickets: tickets.length,
    openTickets: tickets.filter(t => t.status === 'OPEN').length,
    inProgress: tickets.filter(t => t.status === 'IN_PROGRESS').length,
    resolved: tickets.filter(t => t.status === 'RESOLVED').length,
    closed: tickets.filter(t => t.status === 'CLOSED').length,
    
    // Priority Statistics
    emergencyTickets: tickets.filter(t => t.priority === 'EMERGENCY').length,
    highPriority: tickets.filter(t => t.priority === 'HIGH').length,
    
    // Category Statistics
    electricalTickets: tickets.filter(t => t.category === 'ELECTRICAL_ISSUES').length,
    plumbingTickets: tickets.filter(t => t.category === 'PLUMBING_WATER').length,
    hvacTickets: tickets.filter(t => t.category === 'HVAC').length,
    housekeepingTickets: tickets.filter(t => t.category === 'HOUSEKEEPING_CLEANLINESS').length,
    securityTickets: tickets.filter(t => t.category === 'SAFETY_SECURITY').length,
    
    // Performance Metrics
    avgResolutionTime: calculateAvgResolutionTime(tickets),
    satisfactionRate: 85, // This would come from feedback data
    
    // Recent Activity
    todayTickets: tickets.filter(t => {
      const today = new Date();
      const ticketDate = new Date(t.createdAt);
      return ticketDate.toDateString() === today.toDateString();
    }).length,
    
    weekTickets: tickets.filter(t => {
      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      return new Date(t.createdAt) > weekAgo;
    }).length
  };

  function calculateAvgResolutionTime(tickets: Ticket[]): number {
    const resolvedTickets = tickets.filter(t => t.resolvedAt);
    if (resolvedTickets.length === 0) return 0;
    
    const totalHours = resolvedTickets.reduce((sum, ticket) => {
      const created = new Date(ticket.createdAt);
      const resolved = new Date(ticket.resolvedAt!);
      return sum + (resolved.getTime() - created.getTime()) / (1000 * 60 * 60);
    }, 0);
    
    return Math.round(totalHours / resolvedTickets.length);
  }

  const getRoleIcon = (role: string) => {
    switch (role) {
      case 'STUDENT': return <School />;
      case 'STAFF': return <Build />;
      case 'ADMIN': return <AdminPanelSettings />;
      default: return <People />;
    }
  };

  const getRoleColor = (role: string) => {
    switch (role) {
      case 'STUDENT': return 'primary';
      case 'STAFF': return 'secondary';
      case 'ADMIN': return 'error';
      default: return 'default';
    }
  };

  const handleCreateUser = () => {
    console.log('handleCreateUser called'); // Debug log
    try {
      setSelectedUser(null);
      setNewUserData({
        username: '',
        email: '',
        firstName: '',
        lastName: '',
        role: 'STUDENT',
        studentId: '',
        roomNumber: '',
        hostelBlock: '',
        phone: '',
        password: ''
      });
      console.log('Setting userDialogOpen to true'); // Debug log
      setUserDialogOpen(true);
    } catch (error) {
      console.error('Error in handleCreateUser:', error);
    }
  };

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setNewUserData({
      username: user.username,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      role: user.role,
      studentId: user.studentId || '',
      roomNumber: user.roomNumber || '',
      hostelBlock: user.hostelBlock || '',
      phone: user.phone || '',
      password: ''
    });
    setUserDialogOpen(true);
  };

  const validateUserData = () => {
    const errors = [];
    
    if (!newUserData.firstName.trim()) errors.push('First Name is required');
    if (!newUserData.lastName.trim()) errors.push('Last Name is required');
    
    if (!selectedUser) {
      // For new users, these fields are required
      if (!newUserData.username.trim()) errors.push('Username is required');
      if (!newUserData.email.trim()) errors.push('Email is required');
      if (!newUserData.password.trim()) errors.push('Password is required');
    }
    
    if (newUserData.role === 'STUDENT' && !newUserData.studentId.trim()) {
      errors.push('Student ID is required for students');
    }
    
    return errors;
  };

  const handleUserSubmit = async () => {
    console.log('handleUserSubmit called with data:', newUserData);
    
    // Validate form data
    const validationErrors = validateUserData();
    if (validationErrors.length > 0) {
      alert('Please fix the following errors:\n' + validationErrors.join('\n'));
      return;
    }
    
    try {
      if (selectedUser) {
        // Update existing user - use correct admin endpoint
        console.log('Updating user:', selectedUser.id);
        const updatePayload = {
          firstName: newUserData.firstName,
          lastName: newUserData.lastName,
          phone: newUserData.phone,
          hostelBlock: newUserData.hostelBlock,
          roomNumber: newUserData.roomNumber,
          studentId: newUserData.studentId,
          // Only include password if it's provided
          ...(newUserData.password && { password: newUserData.password })
        };
        console.log('Sending update payload:', updatePayload);
        const response = await axios.put(`http://localhost:8080/api/admin/users/${selectedUser.id}`, updatePayload);
        console.log('User updated successfully:', response.data);
        alert('User updated successfully!');
      } else {
        // Create new user
        console.log('Creating new user');
        const userPayload = {
          username: newUserData.username,
          email: newUserData.email,
          firstName: newUserData.firstName,
          lastName: newUserData.lastName,
          role: newUserData.role,
          phone: newUserData.phone,
          hostelBlock: newUserData.hostelBlock,
          roomNumber: newUserData.roomNumber,
          studentId: newUserData.studentId,
          password: newUserData.password
        };
        console.log('Sending create payload:', userPayload);
        const response = await axios.post('http://localhost:8080/api/admin/users', userPayload);
        console.log('User created successfully:', response.data);
        alert('User created successfully!');
      }
      
      setUserDialogOpen(false);
      // Refresh users list
      await fetchUsers();
    } catch (error: any) {
      console.error('Error saving user:', error);
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Unknown error occurred';
      alert(`Error saving user: ${errorMessage}`);
    }
  };

  const handleToggleUserStatus = async (userId: string, currentStatus: boolean) => {
    try {
      console.log(`Toggling user status for ${userId} from ${currentStatus} to ${!currentStatus}`);
      
      // Optimistically update the UI immediately
      setUsers(prevUsers => 
        prevUsers.map(user => 
          user.id === userId 
            ? { ...user, isActive: !currentStatus }
            : user
        )
      );
      
      // Call the API
      const response = await axios.put(`http://localhost:8080/api/admin/users/${userId}/status`);
      console.log('User status updated successfully:', response.data);
      
      // Refresh users list to ensure consistency
      await fetchUsers();
    } catch (error: any) {
      console.error('Error updating user status:', error);
      
      // Revert the optimistic update on error
      setUsers(prevUsers => 
        prevUsers.map(user => 
          user.id === userId 
            ? { ...user, isActive: currentStatus }
            : user
        )
      );
      
      const errorMessage = error.response?.data?.message || 
                          error.response?.data?.error || 
                          error.message || 
                          'Failed to update user status';
      alert(`Error updating user status: ${errorMessage}`);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  };

  // Filter users based on selected filter
  const filteredUsers = users.filter(user => {
    switch (userFilter) {
      case 'active':
        return user.isActive;
      case 'inactive':
        return !user.isActive;
      case 'all':
      default:
        return true;
    }
  });



  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <LinearProgress sx={{ width: '50%' }} />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Admin Dashboard 👑
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Comprehensive system management and analytics
        </Typography>
      </Box>



      {/* Key Metrics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <People />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.totalUsers}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Total Users
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <BugReport />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.totalTickets}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Total Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <TrendingUp />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.avgResolutionTime}h
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Avg Resolution
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <CheckCircle />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.satisfactionRate}%
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Satisfaction Rate
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Detailed Statistics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                User Distribution
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={4}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Avatar sx={{ bgcolor: 'primary.main', mx: 'auto', mb: 1 }}>
                      <School />
                    </Avatar>
                    <Typography variant="h5">{stats.students}</Typography>
                    <Typography variant="body2" color="text.secondary">Students</Typography>
                  </Box>
                </Grid>
                <Grid item xs={4}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Avatar sx={{ bgcolor: 'secondary.main', mx: 'auto', mb: 1 }}>
                      <Build />
                    </Avatar>
                    <Typography variant="h5">{stats.staff}</Typography>
                    <Typography variant="body2" color="text.secondary">Staff</Typography>
                  </Box>
                </Grid>
                <Grid item xs={4}>
                  <Box sx={{ textAlign: 'center' }}>
                    <Avatar sx={{ bgcolor: 'error.main', mx: 'auto', mb: 1 }}>
                      <AdminPanelSettings />
                    </Avatar>
                    <Typography variant="h5">{stats.admins}</Typography>
                    <Typography variant="body2" color="text.secondary">Admins</Typography>
                  </Box>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Ticket Status Overview
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2">Open ({stats.openTickets})</Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={(stats.openTickets / stats.totalTickets) * 100} 
                    sx={{ width: '60%' }}
                    color="primary"
                  />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2">In Progress ({stats.inProgress})</Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={(stats.inProgress / stats.totalTickets) * 100} 
                    sx={{ width: '60%' }}
                    color="info"
                  />
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="body2">Resolved ({stats.resolved})</Typography>
                  <LinearProgress 
                    variant="determinate" 
                    value={(stats.resolved / stats.totalTickets) * 100} 
                    sx={{ width: '60%' }}
                    color="success"
                  />
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Quick Actions */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Admin Actions
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            {hasPermission('create_users') ? (
              <Button
                variant="contained"
                startIcon={<PersonAdd />}
                onClick={handleCreateUser}
                sx={{ 
                  zIndex: 1000,
                  pointerEvents: 'auto',
                  position: 'relative'
                }}
              >
                Add New User
              </Button>
            ) : (
              <div>No permission to create users</div>
            )}
            <Button
              variant="outlined"
              startIcon={<Assessment />}
              onClick={() => navigate('/reports')}
            >
              Generate Reports
            </Button>
            <Button
              variant="outlined"
              startIcon={<Settings />}
              onClick={() => navigate('/settings')}
            >
              System Settings
            </Button>
            <Button
              variant="outlined"
              startIcon={<Security />}
              onClick={() => navigate('/security')}
            >
              Security Logs
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Management Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab label="Users Management" />
            <Tab label="Tickets Overview" />
            <Tab label="Staff Mappings" />
            <Tab label="System Analytics" />
            <Tab label="Settings" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          {/* Users Management */}
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Box>
              <Typography variant="h6">Users Management</Typography>
              <Typography variant="body2" color="text.secondary">
                Showing {filteredUsers.length} of {users.length} users
              </Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
              <FormControl size="small" sx={{ minWidth: 120 }}>
                <InputLabel>Filter Users</InputLabel>
                <Select
                  value={userFilter}
                  onChange={(e) => setUserFilter(e.target.value as 'all' | 'active' | 'inactive')}
                  label="Filter Users"
                >
                  <MenuItem value="all">All Users</MenuItem>
                  <MenuItem value="active">Active Only</MenuItem>
                  <MenuItem value="inactive">Inactive Only</MenuItem>
                </Select>
              </FormControl>
              {hasPermission('create_users') ? (
                <Button
                  variant="contained"
                  startIcon={<PersonAdd />}
                  onClick={handleCreateUser}
                  sx={{ 
                    zIndex: 1000,
                    pointerEvents: 'auto',
                    position: 'relative'
                  }}
                >
                  Add User
                </Button>
              ) : (
                <div>No permission to create users</div>
              )}
            </Box>
          </Box>
          
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>User</TableCell>
                  <TableCell>Role</TableCell>
                  <TableCell>Contact</TableCell>
                  <TableCell>Location</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Joined</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredUsers.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        <Avatar sx={{ bgcolor: getRoleColor(user.role) + '.main' }}>
                          {getRoleIcon(user.role)}
                        </Avatar>
                        <Box>
                          <Typography variant="subtitle2">
                            {user.firstName} {user.lastName}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            @{user.username}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        label={user.role} 
                        size="small" 
                        color={getRoleColor(user.role) as any}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{user.email}</Typography>
                      {user.phone && (
                        <Typography variant="caption" color="text.secondary">
                          {user.phone}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      {user.hostelBlock && user.roomNumber ? (
                        <Typography variant="body2">
                          {user.hostelBlock} - Room {user.roomNumber}
                        </Typography>
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          Not specified
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={user.isActive}
                            onChange={() => handleToggleUserStatus(user.id, user.isActive)}
                            size="small"
                          />
                        }
                        label={user.isActive ? 'Active' : 'Inactive'}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {formatDate(user.createdAt)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 0.5 }}>
                        <Tooltip title="Edit User">
                          <IconButton
                            size="small"
                            onClick={() => handleEditUser(user)}
                          >
                            <Edit fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="View Profile">
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/users/${user.id}`)}
                          >
                            <Visibility fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          {/* Enhanced Tickets Management */}
          <AdminTicketManagement />
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          {/* Staff Mappings Management */}
          <MappingManagement />
        </TabPanel>

        <TabPanel value={tabValue} index={3}>
          {/* System Analytics */}
          <Typography variant="h6" gutterBottom>System Analytics</Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Category Distribution</Typography>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>Electrical</Typography>
                      <Typography>{stats.electricalTickets}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>Plumbing</Typography>
                      <Typography>{stats.plumbingTickets}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>HVAC</Typography>
                      <Typography>{stats.hvacTickets}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>Housekeeping</Typography>
                      <Typography>{stats.housekeepingTickets}</Typography>
                    </Box>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                      <Typography>Security</Typography>
                      <Typography>{stats.securityTickets}</Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
            <Grid item xs={12} md={6}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Performance Metrics</Typography>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    <Box>
                      <Typography variant="body2">Average Resolution Time</Typography>
                      <Typography variant="h5">{stats.avgResolutionTime} hours</Typography>
                    </Box>
                    <Box>
                      <Typography variant="body2">Customer Satisfaction</Typography>
                      <Typography variant="h5">{stats.satisfactionRate}%</Typography>
                    </Box>
                    <Box>
                      <Typography variant="body2">Weekly Tickets</Typography>
                      <Typography variant="h5">{stats.weekTickets}</Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={4}>
          {/* Settings */}
          <Typography variant="h6" gutterBottom>System Settings</Typography>
          <Alert severity="info" sx={{ mb: 2 }}>
            System configuration and settings will be available in the next update.
          </Alert>
        </TabPanel>
      </Card>

      {/* User Dialog */}
      <Dialog 
        open={userDialogOpen} 
        onClose={() => setUserDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          {selectedUser ? 'Edit User' : 'Create New User'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="First Name"
                value={newUserData.firstName}
                onChange={(e) => setNewUserData(prev => ({ ...prev, firstName: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Last Name"
                value={newUserData.lastName}
                onChange={(e) => setNewUserData(prev => ({ ...prev, lastName: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Username"
                value={newUserData.username}
                onChange={(e) => setNewUserData(prev => ({ ...prev, username: e.target.value }))}
                disabled={!!selectedUser}
                helperText={selectedUser ? "Username cannot be changed" : ""}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Email"
                type="email"
                value={newUserData.email}
                onChange={(e) => setNewUserData(prev => ({ ...prev, email: e.target.value }))}
                disabled={!!selectedUser}
                helperText={selectedUser ? "Email cannot be changed" : ""}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Role</InputLabel>
                <Select
                  value={newUserData.role}
                  onChange={(e) => setNewUserData(prev => ({ ...prev, role: e.target.value }))}
                  label="Role"
                >
                  <MenuItem value="STUDENT">Student</MenuItem>
                  <MenuItem value="STAFF">Staff</MenuItem>
                  <MenuItem value="ADMIN">Admin</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Phone"
                value={newUserData.phone}
                onChange={(e) => setNewUserData(prev => ({ ...prev, phone: e.target.value }))}
              />
            </Grid>
            {newUserData.role === 'STUDENT' && (
              <>
                <Grid item xs={12} sm={4}>
                  <TextField
                    fullWidth
                    label="Student ID"
                    value={newUserData.studentId}
                    onChange={(e) => setNewUserData(prev => ({ ...prev, studentId: e.target.value }))}
                  />
                </Grid>
                <Grid item xs={12} sm={4}>
                  <FormControl fullWidth>
                    <InputLabel>Hostel Block</InputLabel>
                    <Select
                      value={newUserData.hostelBlock}
                      onChange={(e) => setNewUserData(prev => ({ ...prev, hostelBlock: e.target.value }))}
                      label="Hostel Block"
                    >
                      <MenuItem value="">None</MenuItem>
                      {hostels.map((hostel) => (
                        <MenuItem key={hostel.value} value={hostel.displayName}>
                          {hostel.displayName} ({hostel.fullName})
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <TextField
                    fullWidth
                    label="Room Number"
                    value={newUserData.roomNumber}
                    onChange={(e) => setNewUserData(prev => ({ ...prev, roomNumber: e.target.value }))}
                  />
                </Grid>
              </>
            )}
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Password"
                type="password"
                value={newUserData.password}
                onChange={(e) => setNewUserData(prev => ({ ...prev, password: e.target.value }))}
                helperText={selectedUser ? "Leave empty to keep current password" : "Required for new users"}
                required={!selectedUser}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUserDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleUserSubmit}
            variant="contained"
            disabled={!newUserData.firstName || !newUserData.lastName || !newUserData.email}
          >
            {selectedUser ? 'Update' : 'Create'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminDashboard;
