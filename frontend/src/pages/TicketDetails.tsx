import React, { useState, useEffect } from 'react';
import { API_BASE_URL } from '../config/api';
import { useAuth } from '../contexts/AuthContext';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Chip,
  Button,
  CircularProgress,
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
  Alert,
  CircularProgress,
  Paper
} from '@mui/material';

import {
  Edit,
  Comment,
  Assignment,
  History,
  AttachFile,
  Send,
  ArrowBack,
  Update,
  PriorityHigh,
  Category,
  LocationOn,
  Schedule
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
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
  createdAt: string;
  updatedAt: string;
  createdBy: {
    firstName: string;
    lastName: string;
    email: string;
  };
  assignedTo?: {
    firstName: string;
    lastName: string;
    email: string;
  };
  comments: Array<{
    id: string;
    comment: string;
    createdAt: string;
    user: {
      firstName: string;
      lastName: string;
      role: string;
    };
    isInternal: boolean;
  }>;
  history: Array<{
    id: string;
    fieldName: string;
    oldValue: string;
    newValue: string;
    changedAt: string;
    changedBy: {
      firstName: string;
      lastName: string;
    };
  }>;
}

const TicketDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [newComment, setNewComment] = useState('');
  const [submittingComment, setSubmittingComment] = useState(false);
  const [showEditForm, setShowEditForm] = useState(false);
  const [unassigning, setUnassigning] = useState(false);

  useEffect(() => {
    if (id) {
      fetchTicketDetails();
    }
  }, [id]);

  const fetchTicketDetails = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/tickets/${id}`);
      setTicket(response.data);
    } catch (err: any) {
      console.error('Error fetching ticket:', err);
      setError('Failed to load ticket details');
    } finally {
      setLoading(false);
    }
  };

  const handleAddComment = async () => {
    if (!newComment.trim() || !ticket) return;

    setSubmittingComment(true);
    try {
      // Mock user ID - in real app this would come from authentication context
      const userId = '550e8400-e29b-41d4-a716-446655440000';
      
      await axios.patch(`${API_BASE_URL}/tickets/${id}/status`, {
        newStatus: ticket.status,
        comment: newComment,
        userId: userId
      });

      setNewComment('');
      fetchTicketDetails(); // Refresh to get new comment
    } catch (err: any) {
      console.error('Error adding comment:', err);
      setError('Failed to add comment');
    } finally {
      setSubmittingComment(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    if (!ticket) return;

    try {
      // Mock user ID - in real app this would come from authentication context
      const userId = '550e8400-e29b-41d4-a716-446655440000';
      
      await axios.patch(`${API_BASE_URL}/tickets/${id}/status`, {
        newStatus: newStatus,
        userId: userId
      });

      fetchTicketDetails(); // Refresh ticket data
    } catch (err: any) {
      console.error('Error updating status:', err);
      setError('Failed to update ticket status');
    }
  };

  const handleUnassignTicket = async () => {
    if (!ticket || !user || user.role !== 'ADMIN') return;

    setUnassigning(true);
    try {
      const response = await axios.patch(`${API_BASE_URL}/tickets/${id}/unassign?adminId=${user.id}`);
      console.log('Unassign response:', response.data);
      
      // Refresh ticket details to show updated status
      fetchTicketDetails();
    } catch (err: any) {
      console.error('Error unassigning ticket:', err);
      setError('Failed to unassign ticket');
    } finally {
      setUnassigning(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPEN': return 'default';
      case 'IN_PROGRESS': return 'primary';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'secondary';
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

  const getCategoryColor = (category: string) => {
    switch (category) {
      case 'MAINTENANCE': return '#1976d2';
      case 'HOUSEKEEPING': return '#2e7d32';
      case 'SECURITY': return '#d32f2f';
      case 'FACILITIES': return '#ed6c02';
      case 'STUDENT_SERVICES': return '#9c27b0';
      case 'EMERGENCY': return '#d32f2f';
      default: return '#757575';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '50vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error || !ticket) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          {error || 'Ticket not found'}
        </Alert>
        <Button
          variant="outlined"
          onClick={() => navigate('/tickets')}
          sx={{ mt: 2 }}
          startIcon={<ArrowBack />}
        >
          Back to Tickets
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, maxWidth: 1200, mx: 'auto' }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Ticket #{ticket.ticketNumber}
          </Typography>
          <Typography variant="h6" color="text.secondary">
            {ticket.title}
          </Typography>
        </Box>
        <Button
          variant="outlined"
          onClick={() => navigate('/tickets')}
          startIcon={<ArrowBack />}
        >
          Back to Tickets
        </Button>
      </Box>

      <Grid container spacing={3}>
        {/* Main Ticket Information */}
        <Grid item xs={12} md={8}>
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Issue Details
              </Typography>
              <Typography variant="body1" paragraph>
                {ticket.description}
              </Typography>

              <Grid container spacing={2} sx={{ mt: 2 }}>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    <Category sx={{ fontSize: 16, mr: 1, verticalAlign: 'middle' }} />
                    Category
                  </Typography>
                  <Chip 
                    label={ticket.category.replace('_', ' ')} 
                    sx={{ backgroundColor: getCategoryColor(ticket.category), color: 'white' }}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    <PriorityHigh sx={{ fontSize: 16, mr: 1, verticalAlign: 'middle' }} />
                    Priority
                  </Typography>
                  <Chip 
                    label={ticket.priority} 
                    color={getPriorityColor(ticket.priority) as any}
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    <LocationOn sx={{ fontSize: 16, mr: 1, verticalAlign: 'middle' }} />
                    Location
                  </Typography>
                  <Typography variant="body2">
                    {ticket.hostelBlock} {ticket.roomNumber && `- Room ${ticket.roomNumber}`}
                  </Typography>
                  {ticket.locationDetails && (
                    <Typography variant="body2" color="text.secondary">
                      {ticket.locationDetails}
                    </Typography>
                  )}
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography variant="subtitle2" color="text.secondary">
                    <Schedule sx={{ fontSize: 16, mr: 1, verticalAlign: 'middle' }} />
                    Status
                  </Typography>
                  <Chip 
                    label={ticket.status.replace('_', ' ')} 
                    color={getStatusColor(ticket.status) as any}
                  />
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          {/* Comments Section */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Comments & Updates
              </Typography>
              
              {/* Add Comment */}
              <Box sx={{ mb: 3 }}>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  placeholder="Add a comment or update..."
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  sx={{ mb: 2 }}
                />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Button
                    variant="outlined"
                    startIcon={<AttachFile />}
                    size="small"
                    onClick={() => alert('File attachment feature coming soon!')}
                  >
                    Attach File
                  </Button>
                  <Button
                    variant="contained"
                    onClick={handleAddComment}
                    disabled={!newComment.trim() || submittingComment}
                    startIcon={submittingComment ? <CircularProgress size={20} /> : <Send />}
                  >
                    {submittingComment ? 'Adding...' : 'Add Comment'}
                  </Button>
                </Box>
              </Box>

              {/* Comments List */}
              <List>
                {ticket.comments && ticket.comments.length > 0 ? (
                  ticket.comments.map((comment, index) => (
                    <React.Fragment key={comment.id}>
                      <ListItem alignItems="flex-start">
                        <ListItemAvatar>
                          <Avatar sx={{ bgcolor: 'primary.main' }}>
                            {comment.user.firstName.charAt(0)}
                          </Avatar>
                        </ListItemAvatar>
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Typography variant="subtitle2">
                                {comment.user.firstName} {comment.user.lastName}
                              </Typography>
                              <Chip 
                                label={comment.user.role} 
                                size="small" 
                                variant="outlined"
                              />
                              {comment.isInternal && (
                                <Chip 
                                  label="Internal" 
                                  size="small" 
                                  color="warning"
                                />
                              )}
                            </Box>
                          }
                          secondary={
                            <>
                              <Typography variant="body2" sx={{ mt: 1 }}>
                                {comment.comment}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {new Date(comment.createdAt).toLocaleString()}
                              </Typography>
                            </>
                          }
                        />
                      </ListItem>
                      {index < ticket.comments.length - 1 && <Divider />}
                    </React.Fragment>
                  ))
                ) : (
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
                    No comments yet. Be the first to add one!
                  </Typography>
                )}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* Sidebar */}
        <Grid item xs={12} md={4}>
          {/* Status Management */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Status Management
              </Typography>
              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Change Status</InputLabel>
                <Select
                  value={ticket.status}
                  onChange={(e) => handleStatusChange(e.target.value)}
                  label="Change Status"
                >
                  <MenuItem value="OPEN">Open</MenuItem>
                  <MenuItem value="IN_PROGRESS">In Progress</MenuItem>
                  <MenuItem value="RESOLVED">Resolved</MenuItem>
                  <MenuItem value="CLOSED">Closed</MenuItem>
                </Select>
              </FormControl>
              <Button
                variant="outlined"
                fullWidth
                startIcon={<Edit />}
                onClick={() => setShowEditForm(!showEditForm)}
              >
                Edit Ticket
              </Button>
            </CardContent>
          </Card>

          {/* Ticket Information */}
          <Card sx={{ mb: 3 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Ticket Information
              </Typography>
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  Created By
                </Typography>
                <Typography variant="body2">
                  {ticket.createdBy.firstName} {ticket.createdBy.lastName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {ticket.createdBy.email}
                </Typography>
              </Box>
              
              {ticket.assignedTo && (
                <Box sx={{ mb: 2 }}>
                  <Typography variant="subtitle2" color="text.secondary">
                    Assigned To
                  </Typography>
                  <Typography variant="body2">
                    {ticket.assignedTo.firstName} {ticket.assignedTo.lastName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {ticket.assignedTo.email}
                  </Typography>
                </Box>
              )}
              
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  Created
                </Typography>
                <Typography variant="body2">
                  {new Date(ticket.createdAt).toLocaleString()}
                </Typography>
              </Box>
              
              <Box>
                <Typography variant="subtitle2" color="text.secondary">
                  Last Updated
                </Typography>
                <Typography variant="body2">
                  {new Date(ticket.updatedAt).toLocaleString()}
                </Typography>
              </Box>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Actions
              </Typography>
              <Button
                variant="outlined"
                fullWidth
                sx={{ mb: 1 }}
                startIcon={<Assignment />}
                onClick={() => alert('Reassignment feature coming soon!')}
              >
                Reassign Ticket
              </Button>
              <Button
                variant="outlined"
                fullWidth
                sx={{ mb: 1 }}
                startIcon={<History />}
                onClick={() => alert('Ticket history feature coming soon!')}
              >
                View History
              </Button>
              <Button
                variant="outlined"
                fullWidth
                startIcon={<AttachFile />}
                onClick={() => alert('File attachment feature coming soon!')}
              >
                Add Attachment
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Admin Actions - Only for Admin users */}
      {user && user.role === 'ADMIN' && ticket && ticket.assignedTo && (
        <Card sx={{ mt: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Admin Actions
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
              <Button
                variant="outlined"
                color="warning"
                onClick={handleUnassignTicket}
                disabled={unassigning}
                startIcon={unassigning ? <CircularProgress size={16} /> : null}
              >
                {unassigning ? 'Unassigning...' : 'Unassign Ticket'}
              </Button>
              <Typography variant="body2" color="text.secondary">
                Currently assigned to: {ticket.assignedTo.firstName} {ticket.assignedTo.lastName} ({ticket.assignedTo.email})
              </Typography>
            </Box>
          </CardContent>
        </Card>
      )}

      {/* Ticket History */}
      {ticket.history && ticket.history.length > 0 && (
        <Card sx={{ mt: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Ticket History
            </Typography>
            <List>
              {ticket.history.map((item, index) => (
                <React.Fragment key={item.id}>
                  <ListItem>
                    <ListItemAvatar>
                      <Avatar sx={{ bgcolor: 'primary.main', width: 32, height: 32 }}>
                        <History />
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Typography variant="subtitle2">
                          {item.changedBy.firstName} {item.changedBy.lastName}
                        </Typography>
                      }
                      secondary={
                        <>
                          <Typography variant="body2">
                            Changed {item.fieldName} from "{item.oldValue}" to "{item.newValue}"
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {new Date(item.changedAt).toLocaleString()}
                          </Typography>
                        </>
                      }
                    />
                  </ListItem>
                  {index < ticket.history.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};

export default TicketDetails; 