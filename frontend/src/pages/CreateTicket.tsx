import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  FormHelperText,
  Paper
} from '@mui/material';
import {
  Save,
  Cancel,
  AttachFile,
  LocationOn,
  PriorityHigh,
  Category,
  QrCodeScanner
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';
import QRCodeScanner, { QRScanResult } from '../components/QRCodeScanner';

interface CreateTicketForm {
  title: string;
  description: string;
  category: string;
  priority: string;
  hostelBlock: string;
  roomNumber: string;
  locationDetails: string;
}

const CreateTicket: React.FC = () => {
  const navigate = useNavigate();
  const { id: ticketId } = useParams();
  const { user } = useAuth();
  const isEditMode = Boolean(ticketId);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [qrScannerOpen, setQrScannerOpen] = useState(false);
  
  const [formData, setFormData] = useState<CreateTicketForm>({
    title: '',
    description: '',
    category: 'GENERAL', // default to a valid enum value
    priority: 'MEDIUM',
    hostelBlock: '',
    roomNumber: '',
    locationDetails: ''
  });

  const [errors, setErrors] = useState<Partial<CreateTicketForm>>({});
  const [customCategory, setCustomCategory] = useState('');
  const [showCustomCategory, setShowCustomCategory] = useState(false);

  // Load ticket data when in edit mode
  useEffect(() => {
    if (isEditMode && ticketId && user) {
      const loadTicketData = async () => {
        try {
          setLoading(true);
          const response = await axios.get(`${API_ENDPOINTS.TICKETS}/${ticketId}?userId=${user.id}`);
          const ticket = response.data;
          
          // Pre-fill form with ticket data
          setFormData({
            title: ticket.title,
            description: ticket.description,
            category: ticket.category || 'GENERAL',
            priority: ticket.priority,
            hostelBlock: ticket.hostelBlock || user.hostelBlock || '',
            roomNumber: ticket.roomNumber || user.roomNumber || '',
            locationDetails: ticket.locationDetails || ''
          });
          
          // Handle custom category
          if (ticket.customCategory) {
            setCustomCategory(ticket.customCategory);
            setShowCustomCategory(true);
            setFormData(prev => ({ ...prev, category: 'CUSTOM' }));
          }
        } catch (error) {
          console.error('Error loading ticket data:', error);
          setError('Failed to load ticket data');
        } finally {
          setLoading(false);
        }
      };
      
      loadTicketData();
    }
  }, [isEditMode, ticketId, user]);

  const categories = [
    // Infrastructure Categories
    { value: 'ELECTRICAL_ISSUES', label: 'Electrical Issues', color: '#ff9800', icon: '⚡' },
    { value: 'PLUMBING_WATER', label: 'Plumbing & Water', color: '#2196f3', icon: '🚰' },
    { value: 'HVAC', label: 'HVAC', color: '#00bcd4', icon: '❄️' },
    { value: 'STRUCTURAL_CIVIL', label: 'Structural & Civil', color: '#795548', icon: '🏗️' },
    { value: 'FURNITURE_FIXTURES', label: 'Furniture & Fixtures', color: '#607d8b', icon: '🪑' },
    
    // IT & Technology Categories
    { value: 'NETWORK_INTERNET', label: 'Network & Internet', color: '#3f51b5', icon: '🌐' },
    { value: 'COMPUTER_HARDWARE', label: 'Computer & Hardware', color: '#9c27b0', icon: '💻' },
    { value: 'AUDIO_VISUAL_EQUIPMENT', label: 'Audio/Visual Equipment', color: '#e91e63', icon: '📺' },
    { value: 'SECURITY_SYSTEMS', label: 'Security Systems', color: '#f44336', icon: '📹' },
    
    // General Maintenance Categories
    { value: 'HOUSEKEEPING_CLEANLINESS', label: 'Housekeeping & Cleanliness', color: '#4caf50', icon: '🧽' },
    { value: 'SAFETY_SECURITY', label: 'Safety & Security', color: '#d32f2f', icon: '🔒' },
    { value: 'LANDSCAPING_OUTDOOR', label: 'Landscaping & Outdoor', color: '#8bc34a', icon: '🌳' },
    { value: 'GENERAL', label: 'General', color: '#9e9e9e', icon: '🔧' },
    
    { value: 'CUSTOM', label: 'Custom Category', color: '#795548', icon: '📝' }
  ];

  const priorities = [
    { value: 'LOW', label: 'Low', color: '#2e7d32' },
    { value: 'MEDIUM', label: 'Medium', color: '#ed6c02' },
    { value: 'HIGH', label: 'High', color: '#d32f2f' },
    { value: 'EMERGENCY', label: 'Emergency', color: '#d32f2f' }
  ];

  const buildings = [
    'Hostel Block A', 'Hostel Block B', 'Hostel Block C', 'Hostel Block D',
    'Hostel Block E', 'Hostel Block F', 'Hostel Block G', 'Hostel Block H'
  ];

  const validateForm = (): boolean => {
    const newErrors: Partial<CreateTicketForm> = {};
    if (!formData.title || formData.title.length < 10) {
      newErrors.title = 'Title must be at least 10 characters';
    }
    if (!formData.description || formData.description.length < 20) {
      newErrors.description = 'Description must be at least 20 characters';
    }
    if (!formData.category) {
      newErrors.category = 'Category is required';
    }
    if (!formData.priority) {
      newErrors.priority = 'Priority is required';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (field: keyof CreateTicketForm, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    
    // Handle custom category selection
    if (field === 'category') {
      if (value === 'CUSTOM') {
        setShowCustomCategory(true);
      } else {
        setShowCustomCategory(false);
        setCustomCategory('');
      }
    }
    
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Get user ID from authentication context
      if (!user?.id) {
        setError('User not authenticated. Please log in again.');
        return;
      }
      const creatorId = user.id;
      
      const ticketData = {
        title: formData.title,
        description: formData.description,
        category: formData.category === 'CUSTOM' ? null : formData.category,
        customCategory: formData.category === 'CUSTOM' ? customCategory : null,
        priority: formData.priority,
        hostelBlock: formData.hostelBlock,
        roomNumber: formData.roomNumber,
        locationDetails: formData.locationDetails
      };

      let response;
      if (isEditMode) {
        // Update existing ticket
        response = await axios.put(`${API_ENDPOINTS.TICKETS}/${ticketId}?updatedBy=${creatorId}`, ticketData);
        setSuccess('Ticket updated successfully!');
      } else {
        // Create new ticket
        response = await axios.post(API_ENDPOINTS.TICKETS_SIMPLE(creatorId), ticketData);
        setSuccess('Ticket created successfully!');
      }
      
      // Redirect to tickets page after a short delay
      setTimeout(() => {
        navigate('/tickets');
      }, 2000);
      
    } catch (err: any) {
      console.error('Error creating ticket:', err);
      setError(err.response?.data?.message || 'Failed to create ticket. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleQRScan = (result: QRScanResult) => {
    setFormData(prev => ({
      ...prev,
      hostelBlock: result.hostelBlock,
      roomNumber: result.roomNumber,
      locationDetails: result.location
    }));
    setQrScannerOpen(false);
    
    // Clear any existing location errors
    setErrors(prev => ({
      ...prev,
      hostelBlock: '',
      roomNumber: '',
      locationDetails: ''
    }));
  };

  const handleCancel = () => {
    navigate('/tickets');
  };

  return (
    <Box sx={{ p: 3, maxWidth: 800, mx: 'auto' }}>
      <Typography variant="h4" component="h1" gutterBottom>
        {isEditMode ? 'Edit Ticket' : 'Create New Ticket'}
      </Typography>
      
      <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
        {isEditMode 
          ? 'Update your ticket details below. Note: You can only edit tickets that haven\'t been assigned to staff yet.'
          : 'Report a new issue or request for the hostel. Please provide as much detail as possible to help us resolve your concern quickly.'
        }
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <Grid container spacing={3}>
              {/* Title */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Issue Title"
                  value={formData.title}
                  onChange={(e) => handleInputChange('title', e.target.value)}
                  error={!!errors.title}
                  helperText={errors.title || 'Brief description of the issue (10-200 characters)'}
                  placeholder="e.g., Leaky faucet in bathroom, Broken window lock"
                  required
                />
              </Grid>

              {/* Description */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Detailed Description"
                  value={formData.description}
                  onChange={(e) => handleInputChange('description', e.target.value)}
                  error={!!errors.description}
                  helperText={errors.description || 'Provide detailed information about the issue (minimum 20 characters)'}
                  placeholder="Describe the problem in detail, when it started, any specific circumstances..."
                  multiline
                  rows={4}
                  required
                />
              </Grid>

              {/* Category and Priority */}
              <Grid item xs={12} md={6}>
                <FormControl fullWidth error={!!errors.category} required>
                  <InputLabel>Category</InputLabel>
                  <Select
                    value={formData.category}
                    onChange={(e) => handleInputChange('category', e.target.value)}
                    label="Category"
                  >
                    {categories.map((category) => (
                      <MenuItem key={category.value} value={category.value}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <span style={{ fontSize: '16px' }}>{category.icon}</span>
                          <Chip 
                            label={category.label} 
                            size="small" 
                            sx={{ backgroundColor: category.color, color: 'white' }}
                          />
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                  {errors.category && <FormHelperText>{errors.category}</FormHelperText>}
                </FormControl>
              </Grid>

              {/* Custom Category Input */}
              {showCustomCategory && (
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="Custom Category"
                    value={customCategory}
                    onChange={(e) => setCustomCategory(e.target.value)}
                    placeholder="Enter your custom category"
                    required
                    helperText="⚠️ Custom categories require manual admin assignment. Your ticket will remain unassigned until an admin reviews and assigns it to appropriate staff."
                  />
                </Grid>
              )}

              <Grid item xs={12} md={6}>
                <FormControl fullWidth error={!!errors.priority} required>
                  <InputLabel>Priority</InputLabel>
                  <Select
                    value={formData.priority}
                    onChange={(e) => handleInputChange('priority', e.target.value)}
                    label="Priority"
                  >
                    {priorities.map((priority) => (
                      <MenuItem key={priority.value} value={priority.value}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Chip 
                            label={priority.label} 
                            size="small" 
                            sx={{ backgroundColor: priority.color, color: 'white' }}
                          />
                        </Box>
                      </MenuItem>
                    ))}
                  </Select>
                  {errors.priority && <FormHelperText>{errors.priority}</FormHelperText>}
                </FormControl>
              </Grid>

              {/* Location Section with QR Scanner */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                  <LocationOn color="primary" />
                  <Typography variant="h6">Location Information</Typography>
                  <Button
                    variant="outlined"
                    startIcon={<QrCodeScanner />}
                    onClick={() => setQrScannerOpen(true)}
                    size="small"
                  >
                    Scan QR Code
                  </Button>
                </Box>
              </Grid>

              <Grid item xs={12} md={6}>
                <FormControl fullWidth error={!!errors.hostelBlock} required>
                  <InputLabel>Hostel Block</InputLabel>
                  <Select
                    value={formData.hostelBlock}
                    onChange={(e) => handleInputChange('hostelBlock', e.target.value)}
                    label="Hostel Block"
                  >
                    {buildings.map((building) => (
                      <MenuItem key={building} value={building}>
                        {building}
                      </MenuItem>
                    ))}
                  </Select>
                  {errors.hostelBlock && <FormHelperText>{errors.hostelBlock}</FormHelperText>}
                </FormControl>
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Room Number (Optional)"
                  value={formData.roomNumber}
                  onChange={(e) => handleInputChange('roomNumber', e.target.value)}
                  placeholder="e.g., 205, A101"
                  helperText="If the issue is in a specific room"
                />
              </Grid>

              {/* Location Details */}
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="Additional Location Details"
                  value={formData.locationDetails}
                  onChange={(e) => handleInputChange('locationDetails', e.target.value)}
                  placeholder="e.g., 2nd floor, near elevator, corner room, etc."
                  helperText="Any additional information about the location"
                  multiline
                  rows={2}
                />
              </Grid>

              {/* Action Buttons */}
              <Grid item xs={12}>
                <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                  <Button
                    variant="outlined"
                    onClick={handleCancel}
                    disabled={loading}
                    startIcon={<Cancel />}
                  >
                    Cancel
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    disabled={loading}
                    startIcon={loading ? <CircularProgress size={20} /> : <Save />}
                  >
                    {loading 
                      ? (isEditMode ? 'Updating...' : 'Creating...') 
                      : (isEditMode ? 'Update Ticket' : 'Create Ticket')
                    }
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </form>
        </CardContent>
      </Card>

      {/* Help Section */}
      <Paper sx={{ p: 3, mt: 3, bgcolor: 'grey.50' }}>
        <Typography variant="h6" gutterBottom>
          💡 Tips for Better Ticket Resolution
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Typography variant="body2" color="text.secondary">
              • <strong>Be specific:</strong> Include exact location, time, and details
            </Typography>
            <Typography variant="body2" color="text.secondary">
              • <strong>Add context:</strong> Mention if this is recurring or urgent
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography variant="body2" color="text.secondary">
              • <strong>Include photos:</strong> Visual evidence helps staff understand
            </Typography>
            <Typography variant="body2" color="text.secondary">
              • <strong>Set right priority:</strong> Use 'Urgent' only for safety issues
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      {/* QR Code Scanner */}
      <QRCodeScanner
        open={qrScannerOpen}
        onClose={() => setQrScannerOpen(false)}
        onScan={handleQRScan}
        title="Scan Location QR Code"
      />
    </Box>
  );
};

export default CreateTicket; 