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
  Alert
} from '@mui/material';
import {
  Assignment,
  BugReport,
  CheckCircle,
  Schedule,
  Warning,
  TrendingUp,
  Refresh,
  Visibility,
  Edit,
  PlayArrow,
  Pause,
  Done,
  Close,
  AssignmentInd,
  Timeline,
  Notifications,
  FilterList,
  Search,
  MoreVert
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

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
      id={`simple-tabpanel-${index}`}
      aria-labelledby={`simple-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const StaffDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);
  const [actionDialogOpen, setActionDialogOpen] = useState(false);
  const [actionType, setActionType] = useState<'assign' | 'status' | 'comment'>('status');
  const [actionData, setActionData] = useState({
    status: '',
    comment: '',
    assigneeId: ''
  });

  // Fetch tickets based on staff role - only assigned tickets as per Product Design
  useEffect(() => {
    const fetchTickets = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/tickets');
        const allTickets = response.data.tickets || response.data;
        
        // Staff can only see tickets assigned to them as per Product Design Document
        const staffTickets = allTickets.filter((ticket: any) => 
          ticket.assignedTo && ticket.assignedTo.id === user?.id
        );
        
        setTickets(staffTickets);
      } catch (error) {
        console.error('Error fetching tickets:', error);
      } finally {
        setLoading(false);
      }
    };

    if (user?.id) {
      fetchTickets();
    }
  }, [user?.id]);

  // Filter tickets based on current tab - Staff can only see assigned tickets
  const getFilteredTickets = () => {
    // All tickets are already filtered to show only assigned tickets
    switch (tabValue) {
      case 0: // All assigned tickets
        return tickets;
      case 1: // High Priority assigned tickets
        return tickets.filter(t => t.priority === 'HIGH' || t.priority === 'URGENT');
      case 2: // Recent assigned tickets
        return tickets
          .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
          .slice(0, 20);
      default:
        return tickets;
    }
  };

  const filteredTickets = getFilteredTickets();

  const stats = {
    totalTickets: tickets.length, // Only assigned tickets
    assignedToMe: tickets.length, // All tickets are assigned to this staff member
    unassigned: 0, // Staff can't see unassigned tickets
    highPriority: tickets.filter(t => t.priority === 'HIGH' || t.priority === 'URGENT').length,
    openTickets: tickets.filter(t => t.status === 'OPEN').length,
    inProgress: tickets.filter(t => t.status === 'IN_PROGRESS').length,
    resolved: tickets.filter(t => t.status === 'RESOLVED').length,
    overdue: tickets.filter(t => {
      const createdDate = new Date(t.createdAt);
      const now = new Date();
      const hoursDiff = (now.getTime() - createdDate.getTime()) / (1000 * 60 * 60);
      return t.status !== 'RESOLVED' && t.status !== 'CLOSED' && hoursDiff > 24;
    }).length
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'primary';
      case 'IN_PROGRESS': return 'info';
      case 'PENDING': return 'warning';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'default';
      case 'CANCELLED': return 'error';
      default: return 'default';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'LOW': return 'success';
      case 'MEDIUM': return 'warning';
      case 'HIGH': return 'error';
      case 'EMERGENCY': return 'error';
      default: return 'default';
    }
  };

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'ELECTRICAL_ISSUES': return '⚡';
      case 'PLUMBING_WATER': return '🚰';
      case 'HVAC': return '❄️';
      case 'STRUCTURAL_CIVIL': return '🏗️';
      case 'FURNITURE_FIXTURES': return '🪑';
      case 'NETWORK_INTERNET': return '🌐';
      case 'COMPUTER_HARDWARE': return '💻';
      case 'AUDIO_VISUAL_EQUIPMENT': return '📺';
      case 'SECURITY_SYSTEMS': return '📹';
      case 'HOUSEKEEPING_CLEANLINESS': return '🧽';
      case 'SAFETY_SECURITY': return '🔒';
      case 'LANDSCAPING_OUTDOOR': return '🌳';
      case 'GENERAL': return '🔧';
      default: return '📋';
    }
  };

  const handleTicketAction = (ticket: Ticket, action: 'assign' | 'status' | 'comment') => {
    setSelectedTicket(ticket);
    setActionType(action);
    setActionData({
      status: ticket.status,
      comment: '',
      assigneeId: ticket.assignedTo?.id || ''
    });
    setActionDialogOpen(true);
  };

  const handleActionSubmit = async () => {
    if (!selectedTicket) return;

    try {
      // This would typically call different endpoints based on action type
      // For now, we'll simulate the action
      console.log('Action submitted:', {
        ticketId: selectedTicket.id,
        actionType,
        actionData
      });

      setActionDialogOpen(false);
      setSelectedTicket(null);
      
      // Refresh tickets
      window.location.reload();
    } catch (error) {
      console.error('Error performing action:', error);
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

  const isOverdue = (ticket: Ticket) => {
    const createdDate = new Date(ticket.createdAt);
    const now = new Date();
    const hoursDiff = (now.getTime() - createdDate.getTime()) / (1000 * 60 * 60);
    return ticket.status !== 'RESOLVED' && ticket.status !== 'CLOSED' && hoursDiff > 24;
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
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Staff Dashboard 🛠️
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Manage and resolve hostel tickets efficiently
        </Typography>
      </Box>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                  <Assignment />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.assignedToMe}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Assigned to Me
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
                    {stats.unassigned}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Unassigned
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
                <Avatar sx={{ bgcolor: 'error.main', mr: 2 }}>
                  <Warning />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.highPriority}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    High Priority
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
                  <Timeline />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.overdue}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Overdue
                  </Typography>
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
            Quick Actions
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            <Button
              variant="contained"
              startIcon={<Warning />}
              onClick={() => setTabValue(1)}
            >
              Review High Priority
            </Button>
            <Button
              variant="outlined"
              startIcon={<Timeline />}
              onClick={() => setTabValue(2)}
            >
              View Recent Work
            </Button>
            <Button
              variant="outlined"
              startIcon={<Refresh />}
              onClick={() => window.location.reload()}
            >
              Refresh Data
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Ticket Management Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab 
              label={
                <Badge badgeContent={stats.totalTickets} color="primary">
                  My Assigned Tickets
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={stats.highPriority} color="error">
                  High Priority
                </Badge>
              } 
            />
            <Tab label="Recent Work" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={tabValue}>
          {filteredTickets.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <Typography variant="body1" color="text.secondary">
                No tickets found in this category.
              </Typography>
            </Box>
          ) : (
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Ticket</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell>Priority</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Assigned To</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredTickets.map((ticket) => (
                    <TableRow 
                      key={ticket.id}
                      sx={{ 
                        bgcolor: isOverdue(ticket) ? 'error.light' : 'inherit',
                        opacity: isOverdue(ticket) ? 0.8 : 1
                      }}
                    >
                      <TableCell>
                        <Box>
                          <Typography variant="subtitle2" fontWeight="bold">
                            {ticket.title}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {ticket.ticketNumber}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {ticket.hostelBlock} - Room {ticket.roomNumber}
                          </Typography>
                          {isOverdue(ticket) && (
                            <Chip 
                              label="OVERDUE" 
                              size="small" 
                              color="error" 
                              sx={{ ml: 1 }}
                            />
                          )}
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography>{getCategoryIcon(ticket.category)}</Typography>
                          <Typography variant="body2">{ticket.category}</Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={ticket.priority} 
                          size="small" 
                          color={getPriorityColor(ticket.priority) as any}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={ticket.status.replace('_', ' ')} 
                          size="small" 
                          color={getStatusColor(ticket.status) as any}
                        />
                      </TableCell>
                      <TableCell>
                        {ticket.assignedTo ? (
                          <Box>
                            <Typography variant="body2">
                              {ticket.assignedTo.firstName} {ticket.assignedTo.lastName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {ticket.assignedTo.email}
                            </Typography>
                          </Box>
                        ) : (
                          <Chip label="Unassigned" size="small" variant="outlined" />
                        )}
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2">
                          {formatDate(ticket.createdAt)}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          by {ticket.createdBy?.firstName} {ticket.createdBy?.lastName}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 0.5 }}>
                          <Tooltip title="View Details">
                            <IconButton
                              size="small"
                              onClick={() => navigate(`/tickets/${ticket.id}`)}
                            >
                              <Visibility fontSize="small" />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Assign">
                            <IconButton
                              size="small"
                              onClick={() => handleTicketAction(ticket, 'assign')}
                            >
                              <AssignmentInd fontSize="small" />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Update Status">
                            <IconButton
                              size="small"
                              onClick={() => handleTicketAction(ticket, 'status')}
                            >
                              <Edit fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </TabPanel>
      </Card>

      {/* Action Dialog */}
      <Dialog 
        open={actionDialogOpen} 
        onClose={() => setActionDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>
          {actionType === 'assign' && 'Assign Ticket'}
          {actionType === 'status' && 'Update Status'}
          {actionType === 'comment' && 'Add Comment'}
        </DialogTitle>
        <DialogContent>
          {selectedTicket && (
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2">{selectedTicket.title}</Typography>
              <Typography variant="body2" color="text.secondary">
                {selectedTicket.ticketNumber}
              </Typography>
            </Box>
          )}

          {actionType === 'status' && (
            <FormControl fullWidth margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                value={actionData.status}
                onChange={(e) => setActionData(prev => ({ ...prev, status: e.target.value }))}
                label="Status"
              >
                <MenuItem value="OPEN">Open</MenuItem>
                <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                <MenuItem value="PENDING">Pending</MenuItem>
                <MenuItem value="RESOLVED">Resolved</MenuItem>
                <MenuItem value="CLOSED">Closed</MenuItem>
                <MenuItem value="CANCELLED">Cancelled</MenuItem>
              </Select>
            </FormControl>
          )}

          {actionType === 'assign' && (
            <FormControl fullWidth margin="normal">
              <InputLabel>Assign To</InputLabel>
              <Select
                value={actionData.assigneeId}
                onChange={(e) => setActionData(prev => ({ ...prev, assigneeId: e.target.value }))}
                label="Assign To"
              >
                <MenuItem value="">Unassigned</MenuItem>
                <MenuItem value={user?.id || ''}>{user?.firstName} {user?.lastName} (Me)</MenuItem>
                {/* Add other staff members here */}
              </Select>
            </FormControl>
          )}

          <TextField
            fullWidth
            label="Comment"
            multiline
            rows={3}
            value={actionData.comment}
            onChange={(e) => setActionData(prev => ({ ...prev, comment: e.target.value }))}
            margin="normal"
            helperText="Add a comment about this action (optional)"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setActionDialogOpen(false)}>Cancel</Button>
          <Button 
            onClick={handleActionSubmit}
            variant="contained"
          >
            Update
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default StaffDashboard;
