import React, { useState, useEffect } from 'react';
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
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  CircularProgress,
  Alert
} from '@mui/material';
import {
  Person,
  Assignment,
  Engineering,
  Security,
  CleaningServices,
  Computer,
  Build,
  Home
} from '@mui/icons-material';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface Staff {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  staffVertical?: string;
  staffId?: string;
  hostelBlock?: string;
}

interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  category: string;
  priority: string;
  status: string;
  hostelBlock: string;
  assignedTo?: Staff;
}

interface TicketAssignmentDialogProps {
  open: boolean;
  onClose: () => void;
  ticket: Ticket | null;
  onAssignmentComplete: () => void;
  userRole: string;
  currentUserId?: string;
}

const TicketAssignmentDialog: React.FC<TicketAssignmentDialogProps> = ({
  open,
  onClose,
  ticket,
  onAssignmentComplete,
  userRole,
  currentUserId
}) => {
  const [staff, setStaff] = useState<Staff[]>([]);
  const [selectedStaffId, setSelectedStaffId] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (open && userRole === 'ADMIN') {
      fetchStaff();
    }
  }, [open, userRole]);

  const fetchStaff = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(API_ENDPOINTS.ADMIN_STAFF);
      setStaff(response.data);
    } catch (error) {
      console.error('Error fetching staff:', error);
      setError('Failed to load staff members');
    } finally {
      setLoading(false);
    }
  };

  const handleAssignToSelf = async () => {
    if (!ticket || !currentUserId) return;
    
    setSubmitting(true);
    setError(null);
    try {
      await axios.post(`${API_ENDPOINTS.TICKETS}/${ticket.id}/assign/${currentUserId}?requestedBy=${currentUserId}`);
      onAssignmentComplete();
      onClose();
    } catch (error) {
      console.error('Error assigning ticket to self:', error);
      setError('Failed to assign ticket to yourself');
    } finally {
      setSubmitting(false);
    }
  };

  const handleAssignToStaff = async () => {
    if (!ticket || !selectedStaffId) return;
    
    setSubmitting(true);
    setError(null);
    try {
      await axios.post(`${API_ENDPOINTS.TICKETS}/${ticket.id}/assign/${selectedStaffId}?requestedBy=${currentUserId}`);
      onAssignmentComplete();
      onClose();
    } catch (error) {
      console.error('Error assigning ticket:', error);
      setError('Failed to assign ticket');
    } finally {
      setSubmitting(false);
    }
  };

  const getVerticalIcon = (vertical?: string) => {
    switch (vertical) {
      case 'ELECTRICAL': return <Engineering />;
      case 'PLUMBING': return <Build />;
      case 'HVAC': return <Engineering />;
      case 'CARPENTRY': return <Build />;
      case 'IT_SUPPORT': return <Computer />;
      case 'HOUSEKEEPING': return <CleaningServices />;
      case 'SECURITY': return <Security />;
      case 'BLOCK_A_WARDEN':
      case 'BLOCK_B_WARDEN':
      case 'BLOCK_C_WARDEN': return <Home />;
      default: return <Person />;
    }
  };

  const getVerticalColor = (vertical?: string) => {
    switch (vertical) {
      case 'ELECTRICAL': return '#ff9800';
      case 'PLUMBING': return '#2196f3';
      case 'HVAC': return '#9c27b0';
      case 'CARPENTRY': return '#795548';
      case 'IT_SUPPORT': return '#607d8b';
      case 'HOUSEKEEPING': return '#4caf50';
      case 'SECURITY': return '#f44336';
      case 'BLOCK_A_WARDEN':
      case 'BLOCK_B_WARDEN':
      case 'BLOCK_C_WARDEN': return '#3f51b5';
      default: return '#757575';
    }
  };

  const formatVerticalName = (vertical?: string) => {
    if (!vertical) return 'General';
    return vertical.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  };

  const getRecommendedStaff = () => {
    if (!ticket) return [];
    
    // Filter staff based on ticket category and building
    return staff.filter(member => {
      // Category-based filtering
      if (ticket.category === 'MAINTENANCE') {
        return ['ELECTRICAL', 'PLUMBING', 'HVAC', 'CARPENTRY', 'GENERAL_MAINTENANCE'].includes(member.staffVertical || '');
      } else if (ticket.category === 'HOUSEKEEPING') {
        return member.staffVertical === 'HOUSEKEEPING';
      } else if (ticket.category === 'SECURITY') {
        return member.staffVertical === 'SECURITY';
      } else if (ticket.category === 'FACILITIES') {
        return ['IT_SUPPORT', 'GENERAL_MAINTENANCE'].includes(member.staffVertical || '');
      } else if (ticket.category === 'STUDENT_SERVICES') {
        return ['BLOCK_A_WARDEN', 'BLOCK_B_WARDEN', 'BLOCK_C_WARDEN'].includes(member.staffVertical || '');
      }
      return true;
    });
  };

  if (!ticket) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" alignItems="center" gap={1}>
          <Assignment />
          <Typography variant="h6">
            Assign Ticket: {ticket.ticketNumber}
          </Typography>
        </Box>
      </DialogTitle>
      
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box mb={2}>
          <Typography variant="subtitle2" color="textSecondary">Ticket Details</Typography>
          <Typography variant="body1" fontWeight="bold">{ticket.title}</Typography>
          <Box display="flex" gap={1} mt={1}>
            <Chip label={ticket.category} size="small" />
            <Chip label={ticket.priority} size="small" color="warning" />
            <Chip label={ticket.hostelBlock} size="small" variant="outlined" />
          </Box>
        </Box>

        {userRole === 'STAFF' && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Assign to Yourself
            </Typography>
            <Typography variant="body2" color="textSecondary" mb={2}>
              Click the button below to assign this ticket to yourself.
            </Typography>
          </Box>
        )}

        {userRole === 'ADMIN' && (
          <Box>
            <Typography variant="h6" gutterBottom>
              Assign to Staff Member
            </Typography>
            
            {loading ? (
              <Box display="flex" justifyContent="center" p={2}>
                <CircularProgress />
              </Box>
            ) : (
              <>
                <FormControl fullWidth sx={{ mb: 2 }}>
                  <InputLabel>Select Staff Member</InputLabel>
                  <Select
                    value={selectedStaffId}
                    onChange={(e) => setSelectedStaffId(e.target.value)}
                    label="Select Staff Member"
                  >
                    {getRecommendedStaff().map((member) => (
                      <MenuItem key={member.id} value={member.id}>
                        <Box display="flex" alignItems="center" gap={1} width="100%">
                          <Avatar sx={{ bgcolor: getVerticalColor(member.staffVertical), width: 32, height: 32 }}>
                            {getVerticalIcon(member.staffVertical)}
                          </Avatar>
                          <Box flex={1}>
                            <Typography variant="body2">
                              {member.firstName} {member.lastName}
                            </Typography>
                            <Typography variant="caption" color="textSecondary">
                              {formatVerticalName(member.staffVertical)} • {member.staffId}
                            </Typography>
                          </Box>
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>

                {getRecommendedStaff().length > 0 && (
                  <Box>
                    <Typography variant="subtitle2" gutterBottom>
                      Recommended Staff ({getRecommendedStaff().length})
                    </Typography>
                    <List dense>
                      {getRecommendedStaff().slice(0, 3).map((member) => (
                        <ListItem 
                          key={member.id}
                          button
                          onClick={() => setSelectedStaffId(member.id)}
                          selected={selectedStaffId === member.id}
                        >
                          <ListItemAvatar>
                            <Avatar sx={{ bgcolor: getVerticalColor(member.staffVertical) }}>
                              {getVerticalIcon(member.staffVertical)}
                            </Avatar>
                          </ListItemAvatar>
                          <ListItemText
                            primary={`${member.firstName} ${member.lastName}`}
                            secondary={`${formatVerticalName(member.staffVertical)} • ${member.staffId}`}
                          />
                        </ListItem>
                      ))}
                    </List>
                  </Box>
                )}
              </>
            )}
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={submitting}>
          Cancel
        </Button>
        
        {userRole === 'STAFF' && (
          <Button
            onClick={handleAssignToSelf}
            variant="contained"
            disabled={submitting}
            startIcon={submitting ? <CircularProgress size={20} /> : <Assignment />}
          >
            Assign to Me
          </Button>
        )}
        
        {userRole === 'ADMIN' && (
          <Button
            onClick={handleAssignToStaff}
            variant="contained"
            disabled={submitting || !selectedStaffId}
            startIcon={submitting ? <CircularProgress size={20} /> : <Assignment />}
          >
            Assign Ticket
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
};

export default TicketAssignmentDialog;
