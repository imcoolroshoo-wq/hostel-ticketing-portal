import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Grid,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Divider,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  IconButton,
  Tooltip,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  Add,
  Search,
  FilterList,
  Edit,
  Visibility,
  BugReport,
  Schedule,
  CheckCircle,
  Cancel,
  Assignment,
  Update
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';
import { useAuth } from '../contexts/AuthContext';
import TicketAssignmentDialog from '../components/TicketAssignmentDialog';
import TicketStatusDialog from '../components/TicketStatusDialog';

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
  createdBy: {
    id: string;
    username: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  assignedTo: any;
  createdAt: string;
  updatedAt: string;
  resolvedAt: string | null;
}

const Tickets: React.FC = () => {
  const navigate = useNavigate();
  const { user, hasPermission } = useAuth();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [priorityFilter, setPriorityFilter] = useState('all');
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Dialog states
  const [assignmentDialogOpen, setAssignmentDialogOpen] = useState(false);
  const [statusDialogOpen, setStatusDialogOpen] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);

  // Fetch tickets from API
  useEffect(() => {
    const fetchTickets = async () => {
      try {
        setLoading(true);
        const response = await axios.get(API_ENDPOINTS.TICKETS);
        // Handle both paginated and direct array responses
        const ticketsData = response.data.tickets || response.data;
        setTickets(Array.isArray(ticketsData) ? ticketsData : []);
        setError(null);
      } catch (err) {
        console.error('Error fetching tickets:', err);
        setError('Failed to load tickets. Please try again.');
      } finally {
        setLoading(false);
      }
    };

    fetchTickets();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'primary';
      case 'ASSIGNED': return 'info';
      case 'IN_PROGRESS': return 'warning';
      case 'ON_HOLD': return 'secondary';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'default';
      case 'CANCELLED': return 'error';
      case 'REOPENED': return 'primary';
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

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'OPEN': return <Schedule />;
      case 'ASSIGNED': return <Edit />;
      case 'IN_PROGRESS': return <BugReport />;
      case 'ON_HOLD': return <Schedule />;
      case 'RESOLVED': return <CheckCircle />;
      case 'CLOSED': return <Cancel />;
      case 'CANCELLED': return <Cancel />;
      case 'REOPENED': return <Schedule />;
      default: return <Schedule />;
    }
  };

  const formatStatus = (status: string) => {
    return status.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatPriority = (priority: string) => {
    return priority.toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString();
  };

  // Dialog handlers
  const handleAssignTicket = async (ticket: Ticket) => {
    if (user?.role === 'STAFF') {
      // Staff self-assignment
      try {
        await axios.post(`${API_ENDPOINTS.TICKETS}/${ticket.id}/assign/${user.id}?requestedBy=${user.id}`);
        // Refresh tickets
        await handleAssignmentComplete();
      } catch (error) {
        console.error('Error assigning ticket to self:', error);
      }
    } else {
      // Admin assignment - open dialog
      setSelectedTicket(ticket);
      setAssignmentDialogOpen(true);
    }
  };

  const handleUpdateStatus = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setStatusDialogOpen(true);
  };

  const handleDialogClose = () => {
    setAssignmentDialogOpen(false);
    setStatusDialogOpen(false);
    setSelectedTicket(null);
  };

  const handleAssignmentComplete = () => {
    // Refresh tickets after assignment
    const fetchTickets = async () => {
      try {
        const response = await axios.get(API_ENDPOINTS.TICKETS);
        const ticketsData = response.data.tickets || response.data;
        setTickets(Array.isArray(ticketsData) ? ticketsData : []);
      } catch (err) {
        console.error('Error refreshing tickets:', err);
      }
    };
    fetchTickets();
  };

  // Check if user can assign tickets - Strict access control per Product Design
  const canAssignTickets = (ticket: Ticket) => {
    if (!user) return false;
    
    // Admin can assign tickets to any staff
    if (user.role === 'ADMIN' && hasPermission('assign_tickets')) {
      return true;
    }
    
    // Staff can assign unassigned tickets to themselves only
    if (user.role === 'STAFF' && !ticket.assignedTo && ticket.status === 'OPEN') {
      return true;
    }
    
    // Students cannot assign tickets
    return false;
  };

  // Check if user can update ticket status - Strict access control per Product Design
  const canUpdateStatus = (ticket: Ticket) => {
    if (!user) return false;
    
    // Admin can update any ticket status
    if (user.role === 'ADMIN' && hasPermission('update_any_ticket_status')) {
      return true;
    }
    
    // Staff can only update status of tickets assigned to them
    if (user.role === 'STAFF' && hasPermission('update_assigned_ticket_status')) {
      return ticket.assignedTo && ticket.assignedTo.id === user.id;
    }
    
    // Students can only update status of their own tickets (close/reopen only)
    if (user.role === 'STUDENT' && ticket.createdBy && ticket.createdBy.id === user.id) {
      // Students can only close resolved tickets or reopen closed tickets
      return (ticket.status === 'RESOLVED' || ticket.status === 'CLOSED') &&
             (hasPermission('reopen_own_tickets') || hasPermission('close_own_tickets'));
    }
    
    return false;
  };

  // Check if user can view this ticket - Strict access control per Product Design
  const canViewTicket = (ticket: Ticket) => {
    if (!user) return false;
    
    // Admin can view all tickets
    if (user.role === 'ADMIN' && hasPermission('view_all_tickets')) {
      return true;
    }
    
    // Staff can only view tickets assigned to them
    if (user.role === 'STAFF' && hasPermission('view_assigned_tickets')) {
      return ticket.assignedTo && ticket.assignedTo.id === user.id;
    }
    
    // Students can only view their own tickets
    if (user.role === 'STUDENT' && hasPermission('view_own_tickets')) {
      return ticket.createdBy && ticket.createdBy.id === user.id;
    }
    
    return false;
  };

  const filteredTickets = tickets.filter(ticket => {
    // First check if user can view this ticket (strict access control)
    if (!canViewTicket(ticket)) {
      return false;
    }
    
    const matchesSearch = ticket.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         ticket.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         ticket.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = statusFilter === 'all' || ticket.status === statusFilter;
    const matchesPriority = priorityFilter === 'all' || ticket.priority === priorityFilter;
    
    return matchesSearch && matchesStatus && matchesPriority;
  });

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          {user?.role === 'ADMIN' ? 'All Tickets' : 
           user?.role === 'STAFF' ? 'Assigned Tickets' : 
           'My Tickets'}
        </Typography>
        {hasPermission('create_ticket') && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => navigate('/tickets/create')}
          >
            Create Ticket
          </Button>
        )}
      </Box>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Search tickets"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={statusFilter}
                  label="Status"
                  onChange={(e: SelectChangeEvent) => setStatusFilter(e.target.value)}
                >
                  <MenuItem value="all">All Statuses</MenuItem>
                  <MenuItem value="OPEN">Open</MenuItem>
                  <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                  <MenuItem value="RESOLVED">Resolved</MenuItem>
                  <MenuItem value="CLOSED">Closed</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Priority</InputLabel>
                <Select
                  value={priorityFilter}
                  label="Priority"
                  onChange={(e: SelectChangeEvent) => setPriorityFilter(e.target.value)}
                >
                  <MenuItem value="all">All Priorities</MenuItem>
                  <MenuItem value="LOW">Low</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HIGH">High</MenuItem>
                  <MenuItem value="URGENT">Urgent</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<FilterList />}
                onClick={() => {
                  setSearchTerm('');
                  setStatusFilter('all');
                  setPriorityFilter('all');
                }}
              >
                Clear
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Tickets List */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Tickets ({loading ? '...' : filteredTickets.length})
          </Typography>
          
          {loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : filteredTickets.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body1" color="text.secondary">
                {tickets.length === 0 ? 'No tickets found. Create your first ticket!' : 'No tickets found matching your criteria.'}
              </Typography>
            </Box>
          ) : (
            <List>
              {filteredTickets.map((ticket, index) => (
                <React.Fragment key={ticket.id}>
                  <ListItem>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'primary.main' }}>
                        {getStatusIcon(ticket.status)}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                          <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>
                            {ticket.title}
                          </Typography>
                          <Chip 
                            label={formatStatus(ticket.status)} 
                            size="small" 
                            color={getStatusColor(ticket.status) as any}
                          />
                          <Chip 
                            label={formatPriority(ticket.priority)} 
                            size="small" 
                            color={getPriorityColor(ticket.priority) as any}
                          />
                          <Chip 
                            label={ticket.category} 
                            size="small" 
                            variant="outlined"
                          />
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary" paragraph>
                            {ticket.description}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                            <Typography variant="body2" color="text.secondary">
                              <strong>Ticket #:</strong> {ticket.ticketNumber}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              <strong>Location:</strong> {ticket.hostelBlock} - Room {ticket.roomNumber}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              <strong>Created:</strong> {formatDate(ticket.createdAt)}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              <strong>Created by:</strong> {ticket.createdBy.firstName} {ticket.createdBy.lastName}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              <strong>Assigned to:</strong> {ticket.assignedTo ? `${ticket.assignedTo.firstName} ${ticket.assignedTo.lastName}` : 'Unassigned'}
                            </Typography>
                          </Box>
                        </Box>
                      }
                    />
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Tooltip title="View Details">
                        <IconButton
                          size="small"
                          onClick={() => navigate(`/tickets/${ticket.id}`)}
                        >
                          <Visibility />
                        </IconButton>
                      </Tooltip>
                      
                      {canAssignTickets(ticket) && (
                        <Tooltip title={user?.role === 'ADMIN' ? 'Assign to Staff' : 'Assign to Me'}>
                          <IconButton
                            size="small"
                            onClick={() => handleAssignTicket(ticket)}
                            color="primary"
                          >
                            <Assignment />
                          </IconButton>
                        </Tooltip>
                      )}
                      
                      {canUpdateStatus(ticket) && (
                        <Tooltip title="Update Status">
                          <IconButton
                            size="small"
                            onClick={() => handleUpdateStatus(ticket)}
                            color="secondary"
                          >
                            <Update />
                          </IconButton>
                        </Tooltip>
                      )}
                      
                      {/* Edit button: Students can only edit their own unassigned tickets */}
                      {((user?.role === 'STUDENT' && ticket.createdBy.id === user.id && !ticket.assignedTo) ||
                        (user?.role === 'ADMIN') ||
                        (user?.role === 'STAFF' && ticket.assignedTo?.id === user.id)) && (
                        <Tooltip title="Edit Ticket">
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/tickets/${ticket.id}/edit`)}
                          >
                            <Edit />
                          </IconButton>
                        </Tooltip>
                      )}
                    </Box>
                  </ListItem>
                  {index < filteredTickets.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          )}
        </CardContent>
      </Card>

      {/* Assignment Dialog */}
      <TicketAssignmentDialog
        open={assignmentDialogOpen}
        onClose={handleDialogClose}
        ticket={selectedTicket}
        onAssignmentComplete={handleAssignmentComplete}
        userRole={user?.role || ''}
        currentUserId={user?.id}
      />

      {/* Status Update Dialog */}
      <TicketStatusDialog
        open={statusDialogOpen}
        onClose={handleDialogClose}
        ticket={selectedTicket}
        onStatusUpdate={handleAssignmentComplete}
        userRole={user?.role || ''}
        currentUserId={user?.id}
      />
    </Box>
  );
};

export default Tickets; 