import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Typography,
  Box,
  Chip,
  Alert,
  CircularProgress
} from '@mui/material';
import {
  Update,
  Schedule,
  Assignment,
  BugReport,
  Pause,
  CheckCircle,
  Cancel,
  Refresh
} from '@mui/icons-material';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  status: string;
  priority: string;
  category: string;
}

interface TicketStatusDialogProps {
  open: boolean;
  onClose: () => void;
  ticket: Ticket | null;
  onStatusUpdate: () => void;
  userRole: string;
  currentUserId?: string;
}

const TicketStatusDialog: React.FC<TicketStatusDialogProps> = ({
  open,
  onClose,
  ticket,
  onStatusUpdate,
  userRole,
  currentUserId
}) => {
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getAvailableStatuses = () => {
    if (!ticket) return [];

    const currentStatus = ticket.status;
    
    // Define status transitions based on current status
    const statusTransitions: { [key: string]: string[] } = {
      'OPEN': ['ASSIGNED', 'CANCELLED'],
      'ASSIGNED': ['IN_PROGRESS', 'ON_HOLD', 'CANCELLED'],
      'IN_PROGRESS': ['ON_HOLD', 'RESOLVED', 'CANCELLED'],
      'ON_HOLD': ['IN_PROGRESS', 'RESOLVED', 'CANCELLED'],
      'RESOLVED': ['CLOSED', 'REOPENED'],
      'CLOSED': ['REOPENED'],
      'CANCELLED': ['OPEN'],
      'REOPENED': ['ASSIGNED', 'IN_PROGRESS', 'CANCELLED']
    };

    let availableStatuses = statusTransitions[currentStatus] || [];

    // Filter based on user role
    if (userRole === 'STUDENT') {
      // Students can only close resolved tickets or reopen closed tickets
      if (currentStatus === 'RESOLVED') {
        availableStatuses = ['CLOSED'];
      } else if (currentStatus === 'CLOSED') {
        availableStatuses = ['REOPENED'];
      } else {
        availableStatuses = []; // No status updates allowed for other states
      }
    } else if (userRole === 'STAFF') {
      // Staff can update tickets assigned to them
      availableStatuses = availableStatuses.filter(status => 
        status !== 'ASSIGNED' // Staff can't assign tickets to others
      );
    }
    // Admin can use all available statuses

    return availableStatuses;
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'OPEN': return <Schedule />;
      case 'ASSIGNED': return <Assignment />;
      case 'IN_PROGRESS': return <BugReport />;
      case 'ON_HOLD': return <Pause />;
      case 'RESOLVED': return <CheckCircle />;
      case 'CLOSED': return <Cancel />;
      case 'CANCELLED': return <Cancel />;
      case 'REOPENED': return <Refresh />;
      default: return <Update />;
    }
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

  const formatStatus = (status: string) => {
    return status.replace('_', ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  const getStatusDescription = (status: string) => {
    switch (status) {
      case 'ASSIGNED': return 'Ticket has been assigned to a staff member';
      case 'IN_PROGRESS': return 'Work is actively being done on this ticket';
      case 'ON_HOLD': return 'Ticket is temporarily paused';
      case 'RESOLVED': return 'Issue has been fixed, waiting for closure';
      case 'CLOSED': return 'Ticket is completed and closed';
      case 'CANCELLED': return 'Ticket has been cancelled';
      case 'REOPENED': return 'Ticket has been reopened for additional work';
      default: return '';
    }
  };

  const handleStatusUpdate = async () => {
    if (!ticket || !selectedStatus || !currentUserId) return;
    
    setSubmitting(true);
    setError(null);
    try {
      await axios.put(`${API_ENDPOINTS.TICKETS}/${ticket.id}/status?status=${selectedStatus}&updatedBy=${currentUserId}`);
      onStatusUpdate();
      onClose();
    } catch (error: any) {
      console.error('Error updating ticket status:', error);
      const errorMessage = error.response?.data?.message || 'Failed to update ticket status';
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  if (!ticket) return null;

  const availableStatuses = getAvailableStatuses();

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Update />
          <Typography variant="h6">
            Update Ticket Status
          </Typography>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box mb={3}>
          <Typography variant="subtitle2" color="textSecondary">Ticket</Typography>
          <Typography variant="body1" fontWeight="bold">{ticket.ticketNumber}</Typography>
          <Typography variant="body2">{ticket.title}</Typography>
          
          <Box display="flex" alignItems="center" gap={1} mt={1}>
            <Typography variant="subtitle2">Current Status:</Typography>
            <Chip 
              icon={getStatusIcon(ticket.status)}
              label={formatStatus(ticket.status)}
              sx={{ bgcolor: getStatusColor(ticket.status), color: 'white' }}
              size="small"
            />
          </Box>
        </Box>

        {availableStatuses.length === 0 ? (
          <Alert severity="info">
            No status updates are available for this ticket at the moment.
          </Alert>
        ) : (
          <>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>New Status</InputLabel>
              <Select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
                label="New Status"
              >
                {availableStatuses.map((status) => (
                  <MenuItem key={status} value={status}>
                    <Box display="flex" alignItems="center" gap={1}>
                      {getStatusIcon(status)}
                      <Typography>{formatStatus(status)}</Typography>
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {selectedStatus && (
              <Box>
                <Typography variant="subtitle2" gutterBottom>
                  Status Description
                </Typography>
                <Typography variant="body2" color="textSecondary">
                  {getStatusDescription(selectedStatus)}
                </Typography>
              </Box>
            )}
          </>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>
          Cancel
        </Button>
        
        {availableStatuses.length > 0 && (
          <Button
            onClick={handleStatusUpdate}
            variant="contained"
            disabled={submitting || !selectedStatus}
            startIcon={submitting ? <CircularProgress size={20} /> : <Update />}
          >
            Update Status
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default TicketStatusDialog;
