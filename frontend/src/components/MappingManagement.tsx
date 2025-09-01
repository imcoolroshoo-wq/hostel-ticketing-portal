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
  Add,
  Edit,
  Delete,
  Map,
  Person,
  Home,
  Category,
  Save,
  Cancel,
  Refresh,
  Engineering,
  Build,
  Computer,
  CleaningServices,
  Security
} from '@mui/icons-material';
import axios from 'axios';
import { useAuth } from '../contexts/AuthContext';
import { API_ENDPOINTS } from '../config/api';

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

interface Hostel {
  value: string;
  displayName: string;
  code: string;
  fullName: string;
  isFemaleBlock: boolean;
}

interface CategoryStaffMapping {
  id: string;
  staffId: string;
  staffName: string;
  staffUsername: string;
  hostelBlock: string | null;
  category: string;
  categoryDisplayName: string;
  categoryIcon: string;
  priorityLevel: number;
  capacityWeight: number;
  expertiseLevel: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

interface MappingFormData {
  staffId: string;
  hostelBlock: string;
  category: string;
  priorityLevel: number;
  capacityWeight: number;
  expertiseLevel: number;
}

const MappingManagement: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [mappings, setMappings] = useState<CategoryStaffMapping[]>([]);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [hostels, setHostels] = useState<Hostel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Dialog states
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedMapping, setSelectedMapping] = useState<CategoryStaffMapping | null>(null);
  const [formData, setFormData] = useState<MappingFormData>({
    staffId: '',
    hostelBlock: '',
    category: '',
    priorityLevel: 1,
    capacityWeight: 1.0,
    expertiseLevel: 1
  });
  
  // Pagination
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);

  const categories = [
    { value: 'ELECTRICAL_ISSUES', label: 'Electrical Issues', icon: '⚡' },
    { value: 'PLUMBING_WATER', label: 'Plumbing & Water', icon: '🚰' },
    { value: 'HVAC', label: 'HVAC', icon: '❄️' },
    { value: 'STRUCTURAL_CIVIL', label: 'Structural & Civil', icon: '🏗️' },
    { value: 'FURNITURE_FIXTURES', label: 'Furniture & Fixtures', icon: '🪑' },
    { value: 'NETWORK_INTERNET', label: 'Network & Internet', icon: '🌐' },
    { value: 'COMPUTER_HARDWARE', label: 'Computer & Hardware', icon: '💻' },
    { value: 'AUDIO_VISUAL_EQUIPMENT', label: 'Audio/Visual Equipment', icon: '📺' },
    { value: 'SECURITY_SYSTEMS', label: 'Security Systems', icon: '📹' },
    { value: 'HOUSEKEEPING_CLEANLINESS', label: 'Housekeeping & Cleanliness', icon: '🧽' },
    { value: 'SAFETY_SECURITY', label: 'Safety & Security', icon: '🔒' },
    { value: 'LANDSCAPING_OUTDOOR', label: 'Landscaping & Outdoor', icon: '🌳' },
    { value: 'GENERAL', label: 'General', icon: '🔧' }
  ];

  useEffect(() => {
    if (hasPermission('view_mappings')) {
      fetchData();
    }
  }, [hasPermission]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [mappingsResponse, staffResponse, hostelsResponse] = await Promise.all([
        axios.get(API_ENDPOINTS.ADMIN_MAPPINGS),
        axios.get(API_ENDPOINTS.ADMIN_STAFF),
        axios.get(API_ENDPOINTS.ADMIN_HOSTELS)
      ]);
      
      setMappings(mappingsResponse.data || []);
      setStaff(staffResponse.data || []);
      setHostels(hostelsResponse.data || []);
      setError(null);
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to load mapping data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateMapping = () => {
    setSelectedMapping(null);
    setFormData({
      staffId: '',
      hostelBlock: '',
      category: '',
      priorityLevel: 1,
      capacityWeight: 1.0,
      expertiseLevel: 1
    });
    setDialogOpen(true);
  };

  const handleEditMapping = (mapping: CategoryStaffMapping) => {
    setSelectedMapping(mapping);
    setFormData({
      staffId: mapping.staffId,
      hostelBlock: mapping.hostelBlock || '',
      category: mapping.category,
      priorityLevel: mapping.priorityLevel,
      capacityWeight: mapping.capacityWeight,
      expertiseLevel: mapping.expertiseLevel
    });
    setDialogOpen(true);
  };

  const handleDeleteMapping = async (mappingId: string) => {
    if (!window.confirm('Are you sure you want to delete this mapping?')) {
      return;
    }

    try {
      await axios.delete(API_ENDPOINTS.DELETE_MAPPING(mappingId));
      fetchData();
    } catch (err) {
      console.error('Error deleting mapping:', err);
      setError('Failed to delete mapping');
    }
  };

  const handleSubmit = async () => {
    try {
      if (selectedMapping) {
        // Update existing mapping
        await axios.put(`http://localhost:8080/api/admin/mappings/${selectedMapping.id}`, formData);
      } else {
        // Create new mapping
        await axios.post('http://localhost:8080/api/admin/mappings', formData);
      }
      
      setDialogOpen(false);
      fetchData();
    } catch (err) {
      console.error('Error saving mapping:', err);
      setError('Failed to save mapping');
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
      default: return <Person />;
    }
  };

  const getCategoryIcon = (category: string) => {
    const categoryData = categories.find(c => c.value === category);
    return categoryData?.icon || '📋';
  };

  const formatCategory = (category: string) => {
    const categoryData = categories.find(c => c.value === category);
    return categoryData?.label || category;
  };

  // Check permissions
  if (!hasPermission('view_mappings')) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          You don't have permission to view staff mappings.
        </Alert>
      </Box>
    );
  }

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  const paginatedMappings = mappings.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Box>
          <Typography variant="h4" gutterBottom>
            Staff-Hostel-Category Mappings
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage staff assignments to hostel blocks and ticket categories for intelligent ticket routing
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button
            startIcon={<Refresh />}
            onClick={fetchData}
            variant="outlined"
          >
            Refresh
          </Button>
          {hasPermission('create_mappings') && (
            <Button
              startIcon={<Add />}
              onClick={handleCreateMapping}
              variant="contained"
            >
              Create Mapping
            </Button>
          )}
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {/* Mappings Table */}
      <Card>
        <CardContent>
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Staff Member</TableCell>
                  <TableCell>Hostel Block</TableCell>
                  <TableCell>Category</TableCell>
                  <TableCell>Priority</TableCell>
                  <TableCell>Weight/Level</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {paginatedMappings.map((mapping) => (
                  <TableRow key={mapping.id} hover>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={2}>
                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                          <Person />
                        </Avatar>
                        <Box>
                          <Typography variant="body2" fontWeight="bold">
                            {mapping.staffName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {mapping.staffUsername}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        icon={<Home />}
                        label={mapping.hostelBlock || 'All Blocks'}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography>{mapping.categoryIcon}</Typography>
                        <Typography variant="body2">
                          {mapping.categoryDisplayName}
                        </Typography>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={`Priority ${mapping.priorityLevel}`}
                        size="small"
                        color={mapping.priorityLevel === 1 ? 'primary' : 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">
                        Weight: {mapping.capacityWeight}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        Level: {mapping.expertiseLevel}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={mapping.active ? 'Active' : 'Inactive'}
                        size="small"
                        color={mapping.active ? 'success' : 'default'}
                      />
                    </TableCell>
                    <TableCell>
                      <Box display="flex" gap={1}>
                        {hasPermission('update_mappings') && (
                          <Tooltip title="Edit Mapping">
                            <IconButton
                              size="small"
                              onClick={() => handleEditMapping(mapping)}
                              color="primary"
                            >
                              <Edit />
                            </IconButton>
                          </Tooltip>
                        )}
                        {hasPermission('delete_mappings') && (
                          <Tooltip title="Delete Mapping">
                            <IconButton
                              size="small"
                              onClick={() => handleDeleteMapping(mapping.id)}
                              color="error"
                            >
                              <Delete />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>

          <TablePagination
            rowsPerPageOptions={[5, 10, 25]}
            component="div"
            count={mappings.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
            }}
          />
        </CardContent>
      </Card>

      {/* Create/Edit Mapping Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <Map />
            <Typography variant="h6">
              {selectedMapping ? 'Edit Mapping' : 'Create New Mapping'}
            </Typography>
          </Box>
        </DialogTitle>
        
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} md={6}>
              <FormControl fullWidth required>
                <InputLabel>Staff Member</InputLabel>
                <Select
                  value={formData.staffId}
                  onChange={(e) => setFormData(prev => ({ ...prev, staffId: e.target.value }))}
                  label="Staff Member"
                >
                  {staff.map((member) => (
                    <MenuItem key={member.id} value={member.id}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Avatar sx={{ width: 24, height: 24 }}>
                          {getVerticalIcon(member.staffVertical)}
                        </Avatar>
                        <Box>
                          <Typography variant="body2">
                            {member.firstName} {member.lastName}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            {member.staffVertical}
                          </Typography>
                        </Box>
                      </Box>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControl fullWidth required>
                <InputLabel>Hostel Block</InputLabel>
                <Select
                  value={formData.hostelBlock}
                  onChange={(e) => setFormData(prev => ({ ...prev, hostelBlock: e.target.value }))}
                  label="Hostel Block"
                >
                  <MenuItem value="">All Blocks</MenuItem>
                  {hostels.map((hostel) => (
                    <MenuItem key={hostel.value} value={hostel.displayName}>
                      {hostel.displayName}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12}>
              <FormControl fullWidth required>
                <InputLabel>Category</InputLabel>
                <Select
                  value={formData.category}
                  onChange={(e) => setFormData(prev => ({ ...prev, category: e.target.value }))}
                  label="Category"
                >
                  {categories.map((category) => (
                    <MenuItem key={category.value} value={category.value}>
                      <Box display="flex" alignItems="center" gap={1}>
                        <Typography>{category.icon}</Typography>
                        <Typography>{category.label}</Typography>
                      </Box>
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Priority Level"
                type="number"
                value={formData.priorityLevel}
                onChange={(e) => setFormData(prev => ({ ...prev, priorityLevel: parseInt(e.target.value) }))}
                inputProps={{ min: 1, max: 10 }}
                helperText="1 = Highest priority, 10 = Lowest priority"
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Capacity Weight"
                type="number"
                value={formData.capacityWeight}
                onChange={(e) => setFormData(prev => ({ ...prev, capacityWeight: parseFloat(e.target.value) }))}
                inputProps={{ min: 0.1, max: 2.0, step: 0.1 }}
                helperText="Workload multiplier (0.1 - 2.0)"
              />
            </Grid>

            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Expertise Level"
                type="number"
                value={formData.expertiseLevel}
                onChange={(e) => setFormData(prev => ({ ...prev, expertiseLevel: parseInt(e.target.value) }))}
                inputProps={{ min: 1, max: 5 }}
                helperText="1 = Beginner, 5 = Expert"
              />
            </Grid>
          </Grid>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={!formData.staffId || !formData.category}
            startIcon={<Save />}
          >
            {selectedMapping ? 'Update' : 'Create'} Mapping
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MappingManagement;
