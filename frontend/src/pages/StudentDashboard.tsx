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
  Alert,
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
  MenuItem
} from '@mui/material';
import {
  Add,
  BugReport,
  CheckCircle,
  Schedule,
  Warning,
  TrendingUp,
  Home,
  Phone,
  Email,
  Room,
  Refresh,
  Visibility,
  Edit,
  Close,
  Send,
  Plumbing,
  ElectricalServices,
  CleaningServices,
  Security,
  Chair,
  ReportProblem
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

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

interface QuickAction {
  title: string;
  description: string;
  icon: React.ReactNode;
  category: string;
  priority: string;
  color: string;
}

const StudentDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [quickActionOpen, setQuickActionOpen] = useState(false);
  const [selectedAction, setSelectedAction] = useState<QuickAction | null>(null);
  const [quickTicketData, setQuickTicketData] = useState({
    title: '',
    description: '',
    locationDetails: ''
  });

  // Fetch user's tickets
  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/tickets');
        const allTickets = response.data.tickets || response.data;
        // Filter tickets created by current user
        const userTickets = allTickets.filter((ticket: Ticket) => 
          ticket.createdBy?.id === user?.id
        );
        setTickets(userTickets);
      } catch (error) {
        console.error('Error fetching tickets:', error);
      } finally {
        setLoading(false);
      }
    };

    if (user) {
      fetchTickets();
    }
  }, [user]);

  const stats = {
    totalTickets: tickets.length,
    openTickets: tickets.filter(t => t.status === 'OPEN').length,
    inProgress: tickets.filter(t => t.status === 'IN_PROGRESS').length,
    resolved: tickets.filter(t => t.status === 'RESOLVED').length,
    urgent: tickets.filter(t => t.priority === 'EMERGENCY').length
  };

  const quickActions: QuickAction[] = [
    {
      title: 'Plumbing Issue',
      description: 'Leaky faucet, clogged drain, or water pressure problems',
      icon: <Plumbing />,
      category: 'PLUMBING_WATER',
      priority: 'MEDIUM',
      color: '#2196f3'
    },
    {
      title: 'Electrical Problem',
      description: 'Power outage, faulty outlets, or lighting issues',
      icon: <ElectricalServices />,
      category: 'ELECTRICAL_ISSUES',
      priority: 'HIGH',
      color: '#ff9800'
    },
    {
      title: 'Room Cleaning',
      description: 'Request deep cleaning or report cleanliness issues',
      icon: <CleaningServices />,
      category: 'HOUSEKEEPING_CLEANLINESS',
      priority: 'LOW',
      color: '#4caf50'
    },
    {
      title: 'Security Concern',
      description: 'Safety issues, broken locks, or suspicious activity',
      icon: <Security />,
      category: 'SAFETY_SECURITY',
      priority: 'HIGH',
      color: '#f44336'
    },
    {
      title: 'Furniture Repair',
      description: 'Broken bed, chair, desk, or other furniture',
      icon: <Chair />,
      category: 'FURNITURE_FIXTURES',
      priority: 'MEDIUM',
      color: '#9c27b0'
    },
    {
      title: 'Emergency',
      description: 'Urgent issues requiring immediate attention',
      icon: <ReportProblem />,
      category: 'SAFETY_SECURITY',
      priority: 'EMERGENCY',
      color: '#d32f2f'
    }
  ];

  const recentTickets = tickets
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 5);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'primary';
      case 'IN_PROGRESS': return 'info';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'default';
      default: return 'default';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'LOW': return 'success';
      case 'MEDIUM': return 'warning';
      case 'HIGH': return 'error';
      case 'URGENT': return 'error';
      default: return 'default';
    }
  };

  const handleQuickAction = (action: QuickAction) => {
    setSelectedAction(action);
    setQuickTicketData({
      title: action.title,
      description: action.description,
      locationDetails: user?.hostelBlock && user?.roomNumber 
        ? `${user.hostelBlock} - Room ${user.roomNumber}` 
        : ''
    });
    setQuickActionOpen(true);
  };

  const handleQuickTicketSubmit = async () => {
    if (!selectedAction || !user) return;

    try {
      const ticketData = {
        title: quickTicketData.title,
        description: quickTicketData.description,
        category: selectedAction.category,
        priority: selectedAction.priority,
        hostelBlock: user.hostelBlock || 'Not specified',
        roomNumber: user.roomNumber || 'Not specified',
        locationDetails: quickTicketData.locationDetails
      };

      console.log('Creating ticket with data:', ticketData);
      const response = await axios.post(API_ENDPOINTS.TICKETS_SIMPLE(user.id), ticketData);
      console.log('Ticket created successfully:', response.data);
      
      setQuickActionOpen(false);
      setSelectedAction(null);
      setQuickTicketData({ title: '', description: '', locationDetails: '' });
      
      // Refresh tickets list
      const ticketsResponse = await axios.get(API_ENDPOINTS.TICKETS);
      const allTickets = ticketsResponse.data.tickets || ticketsResponse.data;
      const userTickets = allTickets.filter((ticket: Ticket) => 
        ticket.createdBy?.id === user?.id
      );
      setTickets(userTickets);
      
      // Show success message
      alert('Ticket created successfully!');
    } catch (error) {
      console.error('Error creating ticket:', error);
      alert('Error creating ticket. Please try again.');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <LinearProgress sx={{ width: '50%' }} />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Welcome Section */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Welcome back, {user?.firstName}! 👋
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage your hostel tickets and requests from your personalized dashboard
        </Typography>
      </Box>

      {/* User Info Card */}
      <Card sx={{ mb: 4, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
        <CardContent>
          <Grid container spacing={3} alignItems="center">
            <Grid item>
              <Avatar sx={{ width: 80, height: 80, bgcolor: 'rgba(255,255,255,0.2)' }}>
                <Typography variant="h4">
                  {user?.firstName[0]}{user?.lastName[0]}
                </Typography>
              </Avatar>
            </Grid>
            <Grid item xs>
              <Typography variant="h5" gutterBottom>
                {user?.firstName} {user?.lastName}
              </Typography>
              <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Email fontSize="small" />
                  <Typography variant="body2">{user?.email}</Typography>
                </Box>
                {user?.phone && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Phone fontSize="small" />
                    <Typography variant="body2">{user.phone}</Typography>
                  </Box>
                )}
                {user?.hostelBlock && user?.roomNumber && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Room fontSize="small" />
                    <Typography variant="body2">{user.hostelBlock} - Room {user.roomNumber}</Typography>
                  </Box>
                )}
                {user?.studentId && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2">Student ID: {user.studentId}</Typography>
                  </Box>
                )}
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                  <BugReport />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.totalTickets}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'warning.main', mr: 2 }}>
                  <Schedule />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.openTickets}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Open Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'info.main', mr: 2 }}>
                  <TrendingUp />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.inProgress}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    In Progress
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'success.main', mr: 2 }}>
                  <CheckCircle />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.resolved}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Resolved
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        {/* Quick Actions */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Actions
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Report common issues with pre-filled templates
              </Typography>
              
              <Grid container spacing={2}>
                {quickActions.map((action, index) => (
                  <Grid item xs={12} sm={6} key={index}>
                    <Paper
                      sx={{
                        p: 2,
                        cursor: 'pointer',
                        transition: 'all 0.2s',
                        border: `2px solid ${action.color}20`,
                        '&:hover': {
                          bgcolor: `${action.color}10`,
                          transform: 'translateY(-2px)',
                          boxShadow: 2
                        }
                      }}
                      onClick={() => handleQuickAction(action)}
                    >
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                        <Avatar sx={{ bgcolor: action.color, width: 32, height: 32 }}>
                          {action.icon}
                        </Avatar>
                        <Typography variant="subtitle2" fontWeight="bold">
                          {action.title}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        {action.description}
                      </Typography>
                    </Paper>
                  </Grid>
                ))}
              </Grid>

              <Button
                fullWidth
                variant="outlined"
                startIcon={<Add />}
                onClick={() => navigate('/tickets/create')}
                sx={{ mt: 2 }}
              >
                Create Custom Ticket
              </Button>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Tickets */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h6">
                  Recent Tickets
                </Typography>
                <Box>
                  <Tooltip title="Refresh">
                    <IconButton size="small" onClick={() => window.location.reload()}>
                      <Refresh />
                    </IconButton>
                  </Tooltip>
                  <Button
                    variant="text"
                    size="small"
                    onClick={() => navigate('/tickets')}
                  >
                    View All
                  </Button>
                </Box>
              </Box>
              
              {recentTickets.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body2" color="text.secondary">
                    No tickets yet. Create your first ticket to get started!
                  </Typography>
                </Box>
              ) : (
                <List>
                  {recentTickets.map((ticket, index) => (
                    <React.Fragment key={ticket.id}>
                      <ListItem sx={{ px: 0 }}>
                        <ListItemAvatar>
                          <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
                            <BugReport fontSize="small" />
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                              <Typography variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                                {ticket.title}
                              </Typography>
                              <Chip 
                                label={ticket.status.replace('_', ' ')} 
                                size="small" 
                                color={getStatusColor(ticket.status) as any}
                              />
                            </Box>
                          }
                          secondary={
                            <Box>
                              <Typography variant="body2" color="text.secondary">
                                {ticket.ticketNumber} • {formatDate(ticket.createdAt)}
                              </Typography>
                              <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
                                <Chip 
                                  label={ticket.priority} 
                                  size="small" 
                                  color={getPriorityColor(ticket.priority) as any}
                                  variant="outlined"
                                />
                                <Chip 
                                  label={ticket.category} 
                                  size="small" 
                                  variant="outlined"
                                />
                              </Box>
                            </Box>
                          }
                        />
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          <Tooltip title="View Details">
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/tickets/${ticket.id}`)}
                            >
                              <Visibility fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </ListItem>
                      {index < recentTickets.length - 1 && <Divider />}
                    </React.Fragment>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Quick Action Dialog */}
      <Dialog 
        open={quickActionOpen} 
        onClose={() => setQuickActionOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {selectedAction && (
              <Avatar sx={{ bgcolor: selectedAction.color }}>
                {selectedAction.icon}
              </Avatar>
            )}
            <Box>
              <Typography variant="h6">Create Ticket</Typography>
              <Typography variant="body2" color="text.secondary">
                {selectedAction?.title}
              </Typography>
            </Box>
          </Box>
        </DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Title"
            value={quickTicketData.title}
            onChange={(e) => setQuickTicketData(prev => ({ ...prev, title: e.target.value }))}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Description"
            multiline
            rows={3}
            value={quickTicketData.description}
            onChange={(e) => setQuickTicketData(prev => ({ ...prev, description: e.target.value }))}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Location Details"
            value={quickTicketData.locationDetails}
            onChange={(e) => setQuickTicketData(prev => ({ ...prev, locationDetails: e.target.value }))}
            margin="normal"
            helperText="Specific location within your room or building"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setQuickActionOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleQuickTicketSubmit}
            variant="contained"
            startIcon={<Send />}
            disabled={!quickTicketData.title || !quickTicketData.description}
          >
            Create Ticket
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default StudentDashboard;
