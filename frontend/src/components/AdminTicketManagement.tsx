import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Chip,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Divider,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination
} from '@mui/material';
import {
  Assignment,
  FilterList,
  Refresh,
  Visibility,
  Edit,
  Person,
  Schedule,
  CheckCircle,
  Cancel,
  BugReport,
  Pause,
  Update,
  Search,
  Clear
} from '@mui/icons-material';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';
import { API_ENDPOINTS } from '../config/api';
import TicketAssignmentDialog from './TicketAssignmentDialog';
import TicketStatusDialog from './TicketStatusDialog';

interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  description: string;
  category: string;
  customCategory?: string;
  priority: string;
  status: string;
  hostelBlock: string;
  roomNumber: string;
  locationDetails: string;
  createdBy: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
  };
  assignedTo?: {
    id: string;
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
    staffVertical?: string;
    staffId?: string;
  };
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string;
}

interface Staff {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  staffVertical?: string;
  staffId?: string;
}

interface FilterState {
  status: string;
  category: string;
  priority: string;
  hostelBlock: string;
  assignedTo: string;
  dateRange: string;
  search: string;
}

const AdminTicketManagement: React.FC = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [filteredTickets, setFilteredTickets] = useState<Ticket[]>([]);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  
  // Filters
  const [filters, setFilters] = useState<FilterState>({
    status: 'all',
    category: 'all',
    priority: 'all',
    hostelBlock: 'all',
    assignedTo: 'all',
    dateRange: 'all',
    search: ''
  });
  
  // Dialogs
  const [assignmentDialogOpen, setAssignmentDialogOpen] = useState(false);
  const [statusDialogOpen, setStatusDialogOpen] = useState(false);
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null);

  // Fetch data
  useEffect(() => {
    fetchTickets();
    fetchStaff();
  }, []);

  // Apply filters
  useEffect(() => {
    applyFilters();
  }, [tickets, filters]);

  const fetchTickets = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/tickets');
      const ticketsData = response.data.tickets || response.data;
      setTickets(Array.isArray(ticketsData) ? ticketsData : []);
      setError(null);
    } catch (err) {
      console.error('Error fetching tickets:', err);
      setError('Failed to load tickets');
    } finally {
      setLoading(false);
    }
  };

  const fetchStaff = async () => {
    try {
      const response = await axios.get(API_ENDPOINTS.ADMIN_STAFF);
      setStaff(response.data || []);
    } catch (err) {
      console.error('Error fetching staff:', err);
    }
  };

  const applyFilters = () => {
    let filtered = [...tickets];

    // Status filter
    if (filters.status !== 'all') {
      filtered = filtered.filter(ticket => ticket.status === filters.status);
    }

    // Category filter
    if (filters.category !== 'all') {
      filtered = filtered.filter(ticket => {
        const effectiveCategory = ticket.customCategory || ticket.category;
        return effectiveCategory === filters.category;
      });
    }

    // Priority filter
    if (filters.priority !== 'all') {
      filtered = filtered.filter(ticket => ticket.priority === filters.priority);
    }

    // Hostel Block filter
    if (filters.hostelBlock !== 'all') {
      filtered = filtered.filter(ticket => ticket.hostelBlock === filters.hostelBlock);
    }

    // Assigned to filter
    if (filters.assignedTo !== 'all') {
      if (filters.assignedTo === 'unassigned') {
        filtered = filtered.filter(ticket => !ticket.assignedTo);
      } else {
        filtered = filtered.filter(ticket => ticket.assignedTo?.id === filters.assignedTo);
      }
    }

    // Date range filter
    if (filters.dateRange !== 'all') {
      const now = new Date();
      const filterDate = new Date();
      
      switch (filters.dateRange) {
        case 'today':
          filterDate.setHours(0, 0, 0, 0);
          break;
        case 'week':
          filterDate.setDate(now.getDate() - 7);
          break;
        case 'month':
          filterDate.setMonth(now.getMonth() - 1);
          break;
      }
      
      filtered = filtered.filter(ticket => 
        new Date(ticket.createdAt) >= filterDate
      );
    }

    // Search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(ticket =>
        ticket.title.toLowerCase().includes(searchLower) ||
        ticket.description.toLowerCase().includes(searchLower) ||
        ticket.ticketNumber.toLowerCase().includes(searchLower) ||
        ticket.createdBy.firstName.toLowerCase().includes(searchLower) ||
        ticket.createdBy.lastName.toLowerCase().includes(searchLower)
      );
    }

    setFilteredTickets(filtered);
    setPage(0); // Reset to first page when filters change
  };

  const handleFilterChange = (field: keyof FilterState, value: string) => {
    setFilters(prev => ({ ...prev, [field]: value }));
  };

  const clearFilters = () => {
    setFilters({
      status: 'all',
      category: 'all',
      priority: 'all',
      hostelBlock: 'all',
      assignedTo: 'all',
      dateRange: 'all',
      search: ''
    });
  };

  const handleAssignTicket = (ticket: Ticket) => {
    setSelectedTicket(ticket);
    setAssignmentDialogOpen(true);
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
    fetchTickets(); // Refresh tickets after assignment
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return '#1976d2';
      case 'ASSIGNED': return '#0288d1';
      case 'IN_PROGRESS': return '#ed6c02';
      case 'ON_HOLD': return '#9e9e9e';
      case 'RESOLVED': return '#2e7d32';
      case 'CLOSED': return '#757575';
      case 'CANCELLED': return '#d32f2f';
      case 'REOPENED': return '#1976d2';
      default: return '#757575';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'LOW': return '#2e7d32';
      case 'MEDIUM': return '#ed6c02';
      case 'HIGH': return '#d32f2f';
      case 'URGENT': return '#d32f2f';
      default: return '#757575';
    }
  };

  const formatStatus = (status: string) => {
    return status.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  const getUniqueValues = (field: keyof Ticket) => {
    const values = tickets.map(ticket => {
      if (field === 'category') {
        return ticket.customCategory || ticket.category;
      }
      return ticket[field];
    }).filter(Boolean) as string[];
    return Array.from(new Set(values)).sort();
  };

  // Pagination
  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  const paginatedTickets = filteredTickets.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Ticket Management
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} mb={2}>
            <FilterList />
            <Typography variant="h6">Filters</Typography>
            <Box flexGrow={1} />
            <Button
              startIcon={<Clear />}
              onClick={clearFilters}
              variant="outlined"
              size="small"
            >
              Clear All
            </Button>
            <Button
              startIcon={<Refresh />}
              onClick={fetchTickets}
              variant="outlined"
              size="small"
            >
              Refresh
            </Button>
          </Box>

          <Grid container spacing={2}>
            {/* Search */}
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="Search"
                value={filters.search}
                onChange={(e) => handleFilterChange('search', e.target.value)}
                placeholder="Search tickets..."
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />
                }}
              />
            </Grid>

            {/* Status Filter */}
            <Grid item xs={12} md={2}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status}
                  onChange={(e) => handleFilterChange('status', e.target.value)}
                  label="Status"
                >
                  <MenuItem value="all">All Statuses</MenuItem>
                  <MenuItem value="OPEN">Open</MenuItem>
                  <MenuItem value="ASSIGNED">Assigned</MenuItem>
                  <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                  <MenuItem value="ON_HOLD">On Hold</MenuItem>
                  <MenuItem value="RESOLVED">Resolved</MenuItem>
                  <MenuItem value="CLOSED">Closed</MenuItem>
                  <MenuItem value="CANCELLED">Cancelled</MenuItem>
                  <MenuItem value="REOPENED">Reopened</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* Category Filter */}
            <Grid item xs={12} md={2}>
              <FormControl fullWidth>
                <InputLabel>Category</InputLabel>
                <Select
                  value={filters.category}
                  onChange={(e) => handleFilterChange('category', e.target.value)}
                  label="Category"
                >
                  <MenuItem value="all">All Categories</MenuItem>
                  {getUniqueValues('category').map(category => (
                    <MenuItem key={category} value={category}>
                      {category}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            {/* Priority Filter */}
            <Grid item xs={12} md={2}>
              <FormControl fullWidth>
                <InputLabel>Priority</InputLabel>
                <Select
                  value={filters.priority}
                  onChange={(e) => handleFilterChange('priority', e.target.value)}
                  label="Priority"
                >
                  <MenuItem value="all">All Priorities</MenuItem>
                  <MenuItem value="LOW">Low</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="HIGH">High</MenuItem>
                  <MenuItem value="URGENT">Urgent</MenuItem>
                </Select>
              </FormControl>
            </Grid>

            {/* Assigned To Filter */}
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Assigned To</InputLabel>
                <Select
                  value={filters.assignedTo}
                  onChange={(e) => handleFilterChange('assignedTo', e.target.value)}
                  label="Assigned To"
                >
                  <MenuItem value="all">All Assignments</MenuItem>
                  <MenuItem value="unassigned">Unassigned</MenuItem>
                  {staff.map(member => (
                    <MenuItem key={member.id} value={member.id}>
                      {member.firstName} {member.lastName} ({member.staffVertical})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
          </Grid>

          <Box mt={2} display="flex" alignItems="center" gap={2}>
            <Typography variant="body2" color="text.secondary">
              Showing {filteredTickets.length} of {tickets.length} tickets
            </Typography>
          </Box>
        </CardContent>
      </Card>

      {/* Tickets Table */}
      <Card>
        <CardContent>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Ticket #</TableCell>
                  <TableCell>Title</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Assigned To</TableCell>
                  <TableCell>Created</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedTickets.map((ticket) => (
                  <TableRow key={ticket.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight="bold">
                        {ticket.ticketNumber}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box>
                        <Typography variant="body2" fontWeight="bold">
                          {ticket.title}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {ticket.hostelBlock} - Room {ticket.roomNumber}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={ticket.customCategory || ticket.category}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={ticket.priority}
                        size="small"
                        sx={{
                          backgroundColor: getPriorityColor(ticket.priority),
                          color: 'white'
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={formatStatus(ticket.status)}
                        size="small"
                        sx={{
                          backgroundColor: getStatusColor(ticket.status),
                          color: 'white'
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      {ticket.assignedTo ? (
                        <Box>
                          <Typography variant="body2">
                            {ticket.assignedTo.firstName} {ticket.assignedTo.lastName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {ticket.assignedTo.staffVertical}
                          </Typography>
                        </Box>
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          Unassigned
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {new Date(ticket.createdAt).toLocaleDateString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        <Tooltip title="Assign Ticket">
                          <IconButton
                            size="small"
                            onClick={() => handleAssignTicket(ticket)}
                            color="primary"
                          >
                            <Assignment />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Update Status">
                          <IconButton
                            size="small"
                            onClick={() => handleUpdateStatus(ticket)}
                            color="secondary"
                          >
                            <Update />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="View Details">
                          <IconButton
                            size="small"
                            color="default"
                          >
                            <Visibility />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25, 50]}
            component="div"
            count={filteredTickets.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
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

export default AdminTicketManagement;
