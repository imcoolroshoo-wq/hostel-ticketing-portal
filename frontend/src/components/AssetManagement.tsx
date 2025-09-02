import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Grid,
  Tab,
  Tabs,
  Badge,
  Tooltip,
  Avatar,
} from '@mui/material';
import {
  Inventory,
  Add,
  Edit,
  Delete,
  QrCode,
  Schedule,
  History,
  Refresh,
  Search,
  FilterList,
  Warning,
  CheckCircle,
  Build,
  Home,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';
import QRCodeScanner from './QRCodeScanner';

interface Asset {
  id: string;
  assetTag: string;
  name: string;
  description?: string;
  type: string;
  status: string;
  building: string;
  roomNumber?: string;
  location?: string;
  manufacturer?: string;
  model?: string;
  serialNumber?: string;
  purchaseDate?: string;
  warrantyExpiry?: string;
  purchasePrice?: number;
  currentValue?: number;
  assignedTo?: {
    id: string;
    firstName: string;
    lastName: string;
  };
  createdAt: string;
  updatedAt: string;
}

interface MaintenanceSchedule {
  id: string;
  asset: Asset;
  maintenanceType: string;
  scheduledDate: string;
  lastMaintenance?: string;
  nextMaintenance?: string;
  status: string;
  assignedTo?: {
    firstName: string;
    lastName: string;
  };
  notes?: string;
}

interface AssetMovement {
  id: string;
  asset: Asset;
  fromLocation: string;
  toLocation: string;
  movedBy: {
    firstName: string;
    lastName: string;
  };
  reason: string;
  movementDate: string;
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
      id={`asset-tabpanel-${index}`}
      aria-labelledby={`asset-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const AssetManagement: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [assets, setAssets] = useState<Asset[]>([]);
  const [maintenanceSchedules, setMaintenanceSchedules] = useState<MaintenanceSchedule[]>([]);
  const [assetMovements, setAssetMovements] = useState<AssetMovement[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  
  // Dialog states
  const [assetDialog, setAssetDialog] = useState(false);
  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
  const [qrScannerOpen, setQrScannerOpen] = useState(false);
  const [maintenanceDialog, setMaintenanceDialog] = useState(false);

  // Form data
  const [assetForm, setAssetForm] = useState({
    assetTag: '',
    name: '',
    description: '',
    type: 'FURNITURE',
    status: 'OPERATIONAL',
    building: '',
    roomNumber: '',
    location: '',
    manufacturer: '',
    model: '',
    serialNumber: '',
    purchaseDate: '',
    warrantyExpiry: '',
    purchasePrice: '',
    currentValue: ''
  });

  const [maintenanceForm, setMaintenanceForm] = useState({
    assetId: '',
    maintenanceType: 'ROUTINE',
    scheduledDate: '',
    notes: ''
  });

  // Search and filter
  const [searchTerm, setSearchTerm] = useState('');
  const [filters, setFilters] = useState({
    type: '',
    status: '',
    building: ''
  });

  const assetTypes = [
    { value: 'FURNITURE', label: 'Furniture' },
    { value: 'ELECTRONICS', label: 'Electronics' },
    { value: 'APPLIANCES', label: 'Appliances' },
    { value: 'TOOLS', label: 'Tools' },
    { value: 'EQUIPMENT', label: 'Equipment' },
    { value: 'VEHICLE', label: 'Vehicle' },
    { value: 'INFRASTRUCTURE', label: 'Infrastructure' },
    { value: 'OTHER', label: 'Other' }
  ];

  const assetStatuses = [
    { value: 'OPERATIONAL', label: 'Operational' },
    { value: 'MAINTENANCE', label: 'Under Maintenance' },
    { value: 'REPAIR', label: 'Needs Repair' },
    { value: 'DAMAGED', label: 'Damaged' },
    { value: 'DISPOSED', label: 'Disposed' },
    { value: 'MISSING', label: 'Missing' }
  ];

  const maintenanceTypes = [
    { value: 'ROUTINE', label: 'Routine Maintenance' },
    { value: 'REPAIR', label: 'Repair' },
    { value: 'INSPECTION', label: 'Inspection' },
    { value: 'CLEANING', label: 'Cleaning' },
    { value: 'UPGRADE', label: 'Upgrade' },
    { value: 'REPLACEMENT', label: 'Replacement' }
  ];

  useEffect(() => {
    if (hasPermission('view_all_tickets') || hasPermission('manage_users')) {
      fetchData();
    }
  }, [hasPermission]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [assetsResponse, maintenanceResponse, movementsResponse] = await Promise.all([
        axios.get(API_ENDPOINTS.ASSETS),
        axios.get(API_ENDPOINTS.MAINTENANCE_SCHEDULES),
        axios.get(API_ENDPOINTS.ASSET_MOVEMENTS)
      ]);
      
      setAssets(assetsResponse.data || []);
      setMaintenanceSchedules(maintenanceResponse.data || []);
      setAssetMovements(movementsResponse.data || []);
      setError(null);
    } catch (err) {
      console.error('Error fetching asset data:', err);
      setError('Failed to load asset data');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateAsset = async () => {
    try {
      const payload = {
        ...assetForm,
        purchasePrice: assetForm.purchasePrice ? parseFloat(assetForm.purchasePrice) : null,
        currentValue: assetForm.currentValue ? parseFloat(assetForm.currentValue) : null
      };
      
      if (selectedAsset) {
        await axios.put(`${API_ENDPOINTS.ASSETS}/${selectedAsset.id}`, payload);
      } else {
        await axios.post(API_ENDPOINTS.ASSETS, payload);
      }
      
      setAssetDialog(false);
      resetAssetForm();
      fetchData();
    } catch (err) {
      console.error('Error saving asset:', err);
      setError('Failed to save asset');
    }
  };

  const handleDeleteAsset = async (assetId: string) => {
    if (!window.confirm('Are you sure you want to delete this asset?')) return;
    
    try {
      await axios.delete(`${API_ENDPOINTS.ASSETS}/${assetId}`);
      fetchData();
    } catch (err) {
      console.error('Error deleting asset:', err);
      setError('Failed to delete asset');
    }
  };

  const handleScheduleMaintenance = async () => {
    try {
      await axios.post(API_ENDPOINTS.MAINTENANCE_SCHEDULES, maintenanceForm);
      setMaintenanceDialog(false);
      setMaintenanceForm({
        assetId: '',
        maintenanceType: 'ROUTINE',
        scheduledDate: '',
        notes: ''
      });
      fetchData();
    } catch (err) {
      console.error('Error scheduling maintenance:', err);
      setError('Failed to schedule maintenance');
    }
  };

  const resetAssetForm = () => {
    setAssetForm({
      assetTag: '',
      name: '',
      description: '',
      type: 'FURNITURE',
      status: 'OPERATIONAL',
      building: '',
      roomNumber: '',
      location: '',
      manufacturer: '',
      model: '',
      serialNumber: '',
      purchaseDate: '',
      warrantyExpiry: '',
      purchasePrice: '',
      currentValue: ''
    });
    setSelectedAsset(null);
  };

  const handleEditAsset = (asset: Asset) => {
    setSelectedAsset(asset);
    setAssetForm({
      assetTag: asset.assetTag,
      name: asset.name,
      description: asset.description || '',
      type: asset.type,
      status: asset.status,
      building: asset.building,
      roomNumber: asset.roomNumber || '',
      location: asset.location || '',
      manufacturer: asset.manufacturer || '',
      model: asset.model || '',
      serialNumber: asset.serialNumber || '',
      purchaseDate: asset.purchaseDate ? asset.purchaseDate.split('T')[0] : '',
      warrantyExpiry: asset.warrantyExpiry ? asset.warrantyExpiry.split('T')[0] : '',
      purchasePrice: asset.purchasePrice?.toString() || '',
      currentValue: asset.currentValue?.toString() || ''
    });
    setAssetDialog(true);
  };

  const handleQRScan = (result: any) => {
    if (result.assetId) {
      // Find and open asset details
      const asset = assets.find(a => a.id === result.assetId || a.assetTag === result.assetId);
      if (asset) {
        handleEditAsset(asset);
      }
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'OPERATIONAL': return 'success';
      case 'MAINTENANCE': return 'warning';
      case 'REPAIR': return 'error';
      case 'DAMAGED': return 'error';
      case 'DISPOSED': return 'default';
      case 'MISSING': return 'error';
      default: return 'default';
    }
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'FURNITURE': return '🪑';
      case 'ELECTRONICS': return '💻';
      case 'APPLIANCES': return '🔌';
      case 'TOOLS': return '🔧';
      case 'EQUIPMENT': return '⚙️';
      case 'VEHICLE': return '🚗';
      case 'INFRASTRUCTURE': return '🏗️';
      default: return '📦';
    }
  };

  // Filter assets based on search and filters
  const filteredAssets = assets.filter(asset => {
    const matchesSearch = !searchTerm || 
      asset.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      asset.assetTag.toLowerCase().includes(searchTerm.toLowerCase()) ||
      asset.description?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesType = !filters.type || asset.type === filters.type;
    const matchesStatus = !filters.status || asset.status === filters.status;
    const matchesBuilding = !filters.building || asset.building === filters.building;
    
    return matchesSearch && matchesType && matchesStatus && matchesBuilding;
  });

  // Get unique buildings for filter
  const uniqueBuildings = Array.from(new Set(assets.map(a => a.building)));

  if (!hasPermission('view_all_tickets') && !hasPermission('manage_users')) {
    return (
      <Alert severity="warning">
        You do not have permission to view asset management.
      </Alert>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Inventory />
          Asset Management
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<QrCode />}
            onClick={() => setQrScannerOpen(true)}
          >
            Scan QR
          </Button>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchData}
            disabled={loading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setAssetDialog(true)}
          >
            Add Asset
          </Button>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {/* Search and Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                size="small"
                placeholder="Search assets..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: <Search sx={{ mr: 1, color: 'text.secondary' }} />
                }}
              />
            </Grid>
            <Grid item xs={12} sm={4} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Type</InputLabel>
                <Select
                  value={filters.type}
                  onChange={(e) => setFilters(prev => ({ ...prev, type: e.target.value }))}
                >
                  <MenuItem value="">All Types</MenuItem>
                  {assetTypes.map(type => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status}
                  onChange={(e) => setFilters(prev => ({ ...prev, status: e.target.value }))}
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  {assetStatuses.map(status => (
                    <MenuItem key={status.value} value={status.value}>
                      {status.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Building</InputLabel>
                <Select
                  value={filters.building}
                  onChange={(e) => setFilters(prev => ({ ...prev, building: e.target.value }))}
                >
                  <MenuItem value="">All Buildings</MenuItem>
                  {uniqueBuildings.map(building => (
                    <MenuItem key={building} value={building}>
                      {building}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={4} md={2}>
              <Button
                fullWidth
                variant="outlined"
                onClick={() => {
                  setSearchTerm('');
                  setFilters({ type: '', status: '', building: '' });
                }}
              >
                Clear
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Asset Tabs */}
      <Card>
        <CardContent>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)}>
              <Tab 
                label={
                  <Badge badgeContent={filteredAssets.length} color="primary">
                    Assets
                  </Badge>
                } 
              />
              <Tab 
                label={
                  <Badge badgeContent={maintenanceSchedules.length} color="warning">
                    Maintenance
                  </Badge>
                } 
              />
              <Tab 
                label={
                  <Badge badgeContent={assetMovements.length} color="info">
                    Movements
                  </Badge>
                } 
              />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <TableContainer component={Paper} sx={{ mt: 2 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Asset</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Location</TableCell>
                    <TableCell>Assigned To</TableCell>
                    <TableCell>Value</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {filteredAssets.map((asset) => (
                    <TableRow key={asset.id} hover>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Typography variant="h6" component="span">
                            {getTypeIcon(asset.type)}
                          </Typography>
                          <Box>
                            <Typography variant="body2" fontWeight="bold">
                              {asset.name}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {asset.assetTag}
                            </Typography>
                          </Box>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Chip 
                          size="small" 
                          label={asset.type.replace('_', ' ')} 
                          color="default"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip 
                          size="small" 
                          label={asset.status} 
                          color={getStatusColor(asset.status)}
                        />
                      </TableCell>
                      <TableCell>
                        <Box>
                          <Typography variant="body2">
                            {asset.building}
                          </Typography>
                          {asset.roomNumber && (
                            <Typography variant="caption" color="text.secondary">
                              Room {asset.roomNumber}
                            </Typography>
                          )}
                        </Box>
                      </TableCell>
                      <TableCell>
                        {asset.assignedTo ? (
                          <Typography variant="body2">
                            {asset.assignedTo.firstName} {asset.assignedTo.lastName}
                          </Typography>
                        ) : (
                          <Chip size="small" label="Unassigned" color="default" />
                        )}
                      </TableCell>
                      <TableCell>
                        {asset.currentValue && (
                          <Typography variant="body2">
                            ₹{asset.currentValue.toLocaleString()}
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Tooltip title="Edit Asset">
                            <IconButton size="small" onClick={() => handleEditAsset(asset)}>
                              <Edit />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Schedule Maintenance">
                            <IconButton 
                              size="small" 
                              onClick={() => {
                                setMaintenanceForm(prev => ({ ...prev, assetId: asset.id }));
                                setMaintenanceDialog(true);
                              }}
                            >
                              <Schedule />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Delete Asset">
                            <IconButton 
                              size="small" 
                              color="error"
                              onClick={() => handleDeleteAsset(asset.id)}
                            >
                              <Delete />
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">Maintenance Schedules</Typography>
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={() => setMaintenanceDialog(true)}
              >
                Schedule Maintenance
              </Button>
            </Box>
            {/* Maintenance schedules table would go here */}
            <Alert severity="info">
              Maintenance scheduling functionality is being developed.
            </Alert>
          </TabPanel>

          <TabPanel value={tabValue} index={2}>
            <Typography variant="h6" sx={{ mb: 2 }}>Asset Movements</Typography>
            {/* Asset movements table would go here */}
            <Alert severity="info">
              Asset movement tracking functionality is being developed.
            </Alert>
          </TabPanel>
        </CardContent>
      </Card>

      {/* Asset Dialog */}
      <Dialog open={assetDialog} onClose={() => setAssetDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          {selectedAsset ? 'Edit Asset' : 'Add New Asset'}
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Asset Tag"
                value={assetForm.assetTag}
                onChange={(e) => setAssetForm(prev => ({ ...prev, assetTag: e.target.value }))}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Name"
                value={assetForm.name}
                onChange={(e) => setAssetForm(prev => ({ ...prev, name: e.target.value }))}
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Description"
                multiline
                rows={2}
                value={assetForm.description}
                onChange={(e) => setAssetForm(prev => ({ ...prev, description: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  value={assetForm.type}
                  onChange={(e) => setAssetForm(prev => ({ ...prev, type: e.target.value }))}
                >
                  {assetTypes.map(type => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={assetForm.status}
                  onChange={(e) => setAssetForm(prev => ({ ...prev, status: e.target.value }))}
                >
                  {assetStatuses.map(status => (
                    <MenuItem key={status.value} value={status.value}>
                      {status.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Building"
                value={assetForm.building}
                onChange={(e) => setAssetForm(prev => ({ ...prev, building: e.target.value }))}
                required
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Room Number"
                value={assetForm.roomNumber}
                onChange={(e) => setAssetForm(prev => ({ ...prev, roomNumber: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Manufacturer"
                value={assetForm.manufacturer}
                onChange={(e) => setAssetForm(prev => ({ ...prev, manufacturer: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Model"
                value={assetForm.model}
                onChange={(e) => setAssetForm(prev => ({ ...prev, model: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Serial Number"
                value={assetForm.serialNumber}
                onChange={(e) => setAssetForm(prev => ({ ...prev, serialNumber: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Purchase Price"
                type="number"
                value={assetForm.purchasePrice}
                onChange={(e) => setAssetForm(prev => ({ ...prev, purchasePrice: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Purchase Date"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={assetForm.purchaseDate}
                onChange={(e) => setAssetForm(prev => ({ ...prev, purchaseDate: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Warranty Expiry"
                type="date"
                InputLabelProps={{ shrink: true }}
                value={assetForm.warrantyExpiry}
                onChange={(e) => setAssetForm(prev => ({ ...prev, warrantyExpiry: e.target.value }))}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAssetDialog(false)}>Cancel</Button>
          <Button
            onClick={handleCreateAsset}
            variant="contained"
            disabled={!assetForm.assetTag || !assetForm.name || !assetForm.building}
          >
            {selectedAsset ? 'Update' : 'Create'} Asset
          </Button>
        </DialogActions>
      </Dialog>

      {/* Maintenance Dialog */}
      <Dialog open={maintenanceDialog} onClose={() => setMaintenanceDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Schedule Maintenance</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Maintenance Type</InputLabel>
                <Select
                  value={maintenanceForm.maintenanceType}
                  onChange={(e) => setMaintenanceForm(prev => ({ ...prev, maintenanceType: e.target.value }))}
                >
                  {maintenanceTypes.map(type => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Scheduled Date"
                type="datetime-local"
                InputLabelProps={{ shrink: true }}
                value={maintenanceForm.scheduledDate}
                onChange={(e) => setMaintenanceForm(prev => ({ ...prev, scheduledDate: e.target.value }))}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Notes"
                multiline
                rows={3}
                value={maintenanceForm.notes}
                onChange={(e) => setMaintenanceForm(prev => ({ ...prev, notes: e.target.value }))}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMaintenanceDialog(false)}>Cancel</Button>
          <Button
            onClick={handleScheduleMaintenance}
            variant="contained"
            disabled={!maintenanceForm.scheduledDate}
          >
            Schedule
          </Button>
        </DialogActions>
      </Dialog>

      {/* QR Scanner */}
      <QRCodeScanner
        open={qrScannerOpen}
        onClose={() => setQrScannerOpen(false)}
        onScan={handleQRScan}
        title="Scan Asset QR Code"
      />
    </Box>
  );
};

export default AssetManagement;
