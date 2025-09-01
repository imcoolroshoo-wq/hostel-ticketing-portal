import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Chip,
  LinearProgress,
  Grid,
  TextField,
  Tab,
  Tabs,
  Tooltip,
  IconButton,
} from '@mui/material';
import {
  SelectAll,
  Update,
  Assignment,
  Download,
  Upload,
  Delete,
  Refresh,
  FilterList,
  Clear,
  CheckCircle,
  Warning,
  Info,
} from '@mui/icons-material';
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
  createdBy: {
    firstName: string;
    lastName: string;
  };
  assignedTo?: {
    id: string;
    firstName: string;
    lastName: string;
  };
  createdAt: string;
  updatedAt: string;
}

interface Staff {
  id: string;
  firstName: string;
  lastName: string;
  role: string;
  staffVertical?: string;
}

interface BulkOperationResult {
  success: boolean;
  message: string;
  successCount: number;
  errorCount: number;
  errors: string[];
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
      id={`bulk-tabpanel-${index}`}
      aria-labelledby={`bulk-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const BulkOperations: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [selectedTickets, setSelectedTickets] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(true);
  const [operationLoading, setOperationLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);

  // Dialog states
  const [statusUpdateDialog, setStatusUpdateDialog] = useState(false);
  const [assignmentDialog, setAssignmentDialog] = useState(false);
  const [confirmDialog, setConfirmDialog] = useState(false);
  const [operationDetails, setOperationDetails] = useState<any>({});

  // Filter states
  const [filters, setFilters] = useState({
    status: '',
    priority: '',
    category: '',
    assignedTo: '',
    dateRange: {
      start: '',
      end: ''
    }
  });

  // Bulk operation data
  const [bulkData, setBulkData] = useState({
    newStatus: '',
    assignToUser: '',
    comment: ''
  });

  useEffect(() => {
    if (hasPermission('bulk_ticket_operations')) {
      fetchData();
    }
  }, [hasPermission]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [ticketsResponse, staffResponse] = await Promise.all([
        axios.get(API_ENDPOINTS.TICKETS),
        axios.get(API_ENDPOINTS.ADMIN_STAFF)
      ]);
      
      setTickets(ticketsResponse.data.tickets || ticketsResponse.data || []);
      setStaff(staffResponse.data || []);
      setError(null);
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTicket = (ticketId: string) => {
    const newSelected = new Set(selectedTickets);
    if (newSelected.has(ticketId)) {
      newSelected.delete(ticketId);
    } else {
      newSelected.add(ticketId);
    }
    setSelectedTickets(newSelected);
  };

  const handleSelectAll = () => {
    if (selectedTickets.size === filteredTickets.length) {
      setSelectedTickets(new Set());
    } else {
      setSelectedTickets(new Set(filteredTickets.map(t => t.id)));
    }
  };

  const handleBulkStatusUpdate = async () => {
    try {
      setOperationLoading(true);
      const response = await axios.post(API_ENDPOINTS.BULK_UPDATE_STATUS, {
        ticketIds: Array.from(selectedTickets),
        newStatus: bulkData.newStatus,
        comment: bulkData.comment
      });

      const result: BulkOperationResult = response.data;
      
      if (result.success) {
        setSuccess(`Successfully updated ${result.successCount} tickets`);
        setSelectedTickets(new Set());
        setBulkData({ newStatus: '', assignToUser: '', comment: '' });
        fetchData();
      } else {
        setError(`Operation completed with errors: ${result.message}`);
      }
      
      setStatusUpdateDialog(false);
    } catch (err) {
      console.error('Error updating tickets:', err);
      setError('Failed to update tickets');
    } finally {
      setOperationLoading(false);
    }
  };

  const handleBulkAssignment = async () => {
    try {
      setOperationLoading(true);
      const response = await axios.post(API_ENDPOINTS.BULK_ASSIGN, {
        ticketIds: Array.from(selectedTickets),
        assignToUserId: bulkData.assignToUser,
        comment: bulkData.comment
      });

      const result: BulkOperationResult = response.data;
      
      if (result.success) {
        setSuccess(`Successfully assigned ${result.successCount} tickets`);
        setSelectedTickets(new Set());
        setBulkData({ newStatus: '', assignToUser: '', comment: '' });
        fetchData();
      } else {
        setError(`Operation completed with errors: ${result.message}`);
      }
      
      setAssignmentDialog(false);
    } catch (err) {
      console.error('Error assigning tickets:', err);
      setError('Failed to assign tickets');
    } finally {
      setOperationLoading(false);
    }
  };

  const handleBulkExport = async () => {
    try {
      setOperationLoading(true);
      const response = await axios.post(API_ENDPOINTS.BULK_EXPORT, {
        ticketIds: Array.from(selectedTickets),
        format: 'csv'
      }, {
        responseType: 'blob'
      });

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `tickets_export_${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      setSuccess(`Successfully exported ${selectedTickets.size} tickets`);
    } catch (err) {
      console.error('Error exporting tickets:', err);
      setError('Failed to export tickets');
    } finally {
      setOperationLoading(false);
    }
  };

  // Apply filters to tickets
  const filteredTickets = tickets.filter(ticket => {
    if (filters.status && ticket.status !== filters.status) return false;
    if (filters.priority && ticket.priority !== filters.priority) return false;
    if (filters.category && ticket.category !== filters.category) return false;
    if (filters.assignedTo && ticket.assignedTo?.id !== filters.assignedTo) return false;
    
    if (filters.dateRange.start) {
      const ticketDate = new Date(ticket.createdAt);
      const startDate = new Date(filters.dateRange.start);
      if (ticketDate < startDate) return false;
    }
    
    if (filters.dateRange.end) {
      const ticketDate = new Date(ticket.createdAt);
      const endDate = new Date(filters.dateRange.end);
      if (ticketDate > endDate) return false;
    }
    
    return true;
  });

  const clearFilters = () => {
    setFilters({
      status: '',
      priority: '',
      category: '',
      assignedTo: '',
      dateRange: { start: '', end: '' }
    });
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'EMERGENCY': return 'error';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      case 'LOW': return 'success';
      default: return 'default';
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'error';
      case 'ASSIGNED': return 'warning';
      case 'IN_PROGRESS': return 'info';
      case 'ON_HOLD': return 'default';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'default';
      case 'CANCELLED': return 'default';
      default: return 'default';
    }
  };

  if (!hasPermission('bulk_ticket_operations')) {
    return (
      <Alert severity="warning">
        You do not have permission to perform bulk operations.
      </Alert>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <SelectAll />
          Bulk Operations
        </Typography>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={fetchData}
          disabled={loading}
        >
          Refresh
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
          {success}
        </Alert>
      )}

      {/* Selection Summary */}
      {selectedTickets.size > 0 && (
        <Card sx={{ mb: 3, backgroundColor: 'primary.50' }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              {selectedTickets.size} tickets selected
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                startIcon={<Update />}
                onClick={() => setStatusUpdateDialog(true)}
                disabled={operationLoading}
              >
                Update Status
              </Button>
              <Button
                variant="contained"
                startIcon={<Assignment />}
                onClick={() => setAssignmentDialog(true)}
                disabled={operationLoading}
              >
                Bulk Assign
              </Button>
              <Button
                variant="outlined"
                startIcon={<Download />}
                onClick={handleBulkExport}
                disabled={operationLoading}
              >
                Export
              </Button>
              <Button
                variant="outlined"
                startIcon={<Clear />}
                onClick={() => setSelectedTickets(new Set())}
              >
                Clear Selection
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <FilterList />
            Filters
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status}
                  onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                >
                  <MenuItem value="">All</MenuItem>
                  <MenuItem value="OPEN">Open</MenuItem>
                  <MenuItem value="ASSIGNED">Assigned</MenuItem>
                  <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                  <MenuItem value="ON_HOLD">On Hold</MenuItem>
                  <MenuItem value="RESOLVED">Resolved</MenuItem>
                  <MenuItem value="CLOSED">Closed</MenuItem>
                  <MenuItem value="CANCELLED">Cancelled</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Priority</InputLabel>
                <Select
                  value={filters.priority}
                  onChange={(e) => setFilters(prev => ({ ...prev, priority: e.target.value }))}
                >
                  <MenuItem value="">All</MenuItem>
                  <MenuItem value="EMERGENCY">Emergency</MenuItem>
                  <MenuItem value="HIGH">High</MenuItem>
                  <MenuItem value="MEDIUM">Medium</MenuItem>
                  <MenuItem value="LOW">Low</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Assigned To</InputLabel>
                <Select
                  value={filters.assignedTo}
                  onChange={(e) => setFilters(prev => ({ ...prev, assignedTo: e.target.value }))}
                >
                  <MenuItem value="">All</MenuItem>
                  <MenuItem value="unassigned">Unassigned</MenuItem>
                  {staff.map((staffMember) => (
                    <MenuItem key={staffMember.id} value={staffMember.id}>
                      {staffMember.firstName} {staffMember.lastName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                fullWidth
                size="small"
                label="Start Date"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={filters.dateRange.start}
                onChange={(e) => setFilters(prev => ({ 
                  ...prev, 
                  dateRange: { ...prev.dateRange, start: e.target.value } 
                }))}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <TextField
                fullWidth
                size="small"
                label="End Date"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={filters.dateRange.end}
                onChange={(e) => setFilters(prev => ({ 
                  ...prev, 
                  dateRange: { ...prev.dateRange, end: e.target.value } 
                }))}
              />
            </Grid>
            <Grid item xs={12} sm={6} md={2}>
              <Button
                fullWidth
                variant="outlined"
                startIcon={<Clear />}
                onClick={clearFilters}
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Progress bar for operations */}
      {operationLoading && (
        <Box sx={{ mb: 3 }}>
          <LinearProgress />
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Processing bulk operation...
          </Typography>
        </Box>
      )}

      {/* Tickets Table */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">
              Tickets ({filteredTickets.length} total)
            </Typography>
            <Button
              variant="outlined"
              startIcon={<SelectAll />}
              onClick={handleSelectAll}
            >
              {selectedTickets.size === filteredTickets.length ? 'Deselect All' : 'Select All'}
            </Button>
          </Box>

          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox">
                    <Checkbox
                      indeterminate={selectedTickets.size > 0 && selectedTickets.size < filteredTickets.length}
                      checked={filteredTickets.length > 0 && selectedTickets.size === filteredTickets.length}
                      onChange={handleSelectAll}
                    />
                  </TableCell>
                  <TableCell>Ticket</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell>Assigned To</TableCell>
                  <TableCell>Created</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredTickets.map((ticket) => (
                  <TableRow 
                    key={ticket.id} 
                    hover
                    selected={selectedTickets.has(ticket.id)}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedTickets.has(ticket.id)}
                        onChange={() => handleSelectTicket(ticket.id)}
                      />
                    </TableCell>
                    <TableCell>
                      <Box>
                        <Typography variant="body2" fontWeight="bold">
                          {ticket.ticketNumber}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {ticket.title}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip 
                        size="small" 
                        label={ticket.priority} 
                        color={getPriorityColor(ticket.priority)}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip 
                        size="small" 
                        label={ticket.status.replace('_', ' ')} 
                        color={getStatusColor(ticket.status)}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {ticket.category.replace('_', ' ')}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {ticket.assignedTo ? (
                        <Typography variant="body2">
                          {ticket.assignedTo.firstName} {ticket.assignedTo.lastName}
                        </Typography>
                      ) : (
                        <Chip size="small" label="Unassigned" color="default" />
                      )}
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        {new Date(ticket.createdAt).toLocaleDateString()}
                      </Typography>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      {/* Status Update Dialog */}
      <Dialog open={statusUpdateDialog} onClose={() => setStatusUpdateDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Bulk Status Update</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Update status for {selectedTickets.size} selected tickets
          </Typography>
          
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>New Status</InputLabel>
            <Select
              value={bulkData.newStatus}
              onChange={(e) => setBulkData(prev => ({ ...prev, newStatus: e.target.value }))}
            >
              <MenuItem value="ASSIGNED">Assigned</MenuItem>
              <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
              <MenuItem value="ON_HOLD">On Hold</MenuItem>
              <MenuItem value="RESOLVED">Resolved</MenuItem>
              <MenuItem value="CLOSED">Closed</MenuItem>
              <MenuItem value="CANCELLED">Cancelled</MenuItem>
            </Select>
          </FormControl>

          <TextField
            fullWidth
            label="Comment (optional)"
            multiline
            rows={3}
            value={bulkData.comment}
            onChange={(e) => setBulkData(prev => ({ ...prev, comment: e.target.value }))}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStatusUpdateDialog(false)}>Cancel</Button>
          <Button
            onClick={handleBulkStatusUpdate}
            variant="contained"
            disabled={!bulkData.newStatus || operationLoading}
          >
            Update Status
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assignment Dialog */}
      <Dialog open={assignmentDialog} onClose={() => setAssignmentDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Bulk Assignment</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Assign {selectedTickets.size} selected tickets
          </Typography>
          
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>Assign To</InputLabel>
            <Select
              value={bulkData.assignToUser}
              onChange={(e) => setBulkData(prev => ({ ...prev, assignToUser: e.target.value }))}
            >
              {staff.map((staffMember) => (
                <MenuItem key={staffMember.id} value={staffMember.id}>
                  {staffMember.firstName} {staffMember.lastName} ({staffMember.role})
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <TextField
            fullWidth
            label="Comment (optional)"
            multiline
            rows={3}
            value={bulkData.comment}
            onChange={(e) => setBulkData(prev => ({ ...prev, comment: e.target.value }))}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAssignmentDialog(false)}>Cancel</Button>
          <Button
            onClick={handleBulkAssignment}
            variant="contained"
            disabled={!bulkData.assignToUser || operationLoading}
          >
            Assign Tickets
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default BulkOperations;
