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
  Badge,
  Tooltip,
  Tab,
  Tabs,
} from '@mui/material';
import {
  Escalator,
  Warning,
  CheckCircle,
  Schedule,
  Person,
  Assignment,
  TrendingUp,
  Refresh,
  Add,
  Visibility,
  Close,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface Escalation {
  id: string;
  ticket: {
    id: string;
    ticketNumber: string;
    title: string;
    priority: string;
    status: string;
  };
  escalatedFrom: {
    id: string;
    firstName: string;
    lastName: string;
    role: string;
  };
  escalatedTo: {
    id: string;
    firstName: string;
    lastName: string;
    role: string;
  };
  reason: string;
  escalationLevel: number;
  isAutoEscalated: boolean;
  escalatedAt: string;
  resolvedAt?: string;
}

interface Staff {
  id: string;
  firstName: string;
  lastName: string;
  role: string;
  staffVertical?: string;
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
      id={`escalation-tabpanel-${index}`}
      aria-labelledby={`escalation-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const EscalationManagement: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [escalations, setEscalations] = useState<Escalation[]>([]);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  
  // Dialog states
  const [manualEscalationDialog, setManualEscalationDialog] = useState(false);
  const [selectedTicketId, setSelectedTicketId] = useState<string>('');
  const [escalationForm, setEscalationForm] = useState({
    ticketId: '',
    escalatedToId: '',
    reason: '',
    level: 'HIGH_NO_PROGRESS'
  });

  // Statistics
  const [statistics, setStatistics] = useState({
    totalEscalations: 0,
    activeEscalations: 0,
    escalationsByLevel: {}
  });

  useEffect(() => {
    if (hasPermission('escalate_tickets') || hasPermission('view_all_tickets')) {
      fetchData();
    }
  }, [hasPermission]);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [escalationsResponse, staffResponse, statsResponse] = await Promise.all([
        axios.get(API_ENDPOINTS.ESCALATIONS),
        axios.get(API_ENDPOINTS.ADMIN_STAFF),
        axios.get(API_ENDPOINTS.ESCALATION_STATISTICS)
      ]);
      
      setEscalations(escalationsResponse.data.escalations || []);
      setStaff(staffResponse.data || []);
      setStatistics(statsResponse.data || {});
      setError(null);
    } catch (err) {
      console.error('Error fetching escalation data:', err);
      setError('Failed to load escalation data');
    } finally {
      setLoading(false);
    }
  };

  const handleManualEscalation = async () => {
    try {
      await axios.post(API_ENDPOINTS.MANUAL_ESCALATION, escalationForm);
      setManualEscalationDialog(false);
      setEscalationForm({
        ticketId: '',
        escalatedToId: '',
        reason: '',
        level: 'HIGH_NO_PROGRESS'
      });
      fetchData();
    } catch (err) {
      console.error('Error escalating ticket:', err);
      setError('Failed to escalate ticket');
    }
  };

  const handleResolveEscalation = async (escalationId: string) => {
    try {
      await axios.post(`${API_ENDPOINTS.ESCALATIONS}/${escalationId}/resolve`, {
        reason: 'Resolved by admin'
      });
      fetchData();
    } catch (err) {
      console.error('Error resolving escalation:', err);
      setError('Failed to resolve escalation');
    }
  };

  const getEscalationLevelText = (level: number) => {
    const levels = {
      1: 'Emergency Unassigned',
      2: 'High No Progress',
      3: 'Medium Unassigned',
      4: 'Low Unassigned',
      5: 'SLA Breach'
    };
    return levels[level] || 'Unknown';
  };

  const getEscalationLevelColor = (level: number) => {
    const colors = {
      1: 'error',
      2: 'warning',
      3: 'info',
      4: 'default',
      5: 'error'
    };
    return colors[level] || 'default';
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

  const activeEscalations = escalations.filter(e => !e.resolvedAt);
  const resolvedEscalations = escalations.filter(e => e.resolvedAt);
  const overdueEscalations = escalations.filter(e => {
    if (e.resolvedAt) return false;
    const escalatedDate = new Date(e.escalatedAt);
    const hoursOld = (Date.now() - escalatedDate.getTime()) / (1000 * 60 * 60);
    return hoursOld > 24; // Consider overdue after 24 hours
  });

  if (!hasPermission('escalate_tickets') && !hasPermission('view_all_tickets')) {
    return (
      <Alert severity="warning">
        You do not have permission to view escalation management.
      </Alert>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Escalator />
          Escalation Management
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchData}
            disabled={loading}
          >
            Refresh
          </Button>
          {hasPermission('escalate_tickets') && (
            <Button
              variant="contained"
              startIcon={<Add />}
              onClick={() => setManualEscalationDialog(true)}
            >
              Manual Escalation
            </Button>
          )}
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Escalations
              </Typography>
              <Typography variant="h4">
                {statistics.totalEscalations}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Active Escalations
              </Typography>
              <Typography variant="h4" color="warning.main">
                {statistics.activeEscalations}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Overdue Escalations
              </Typography>
              <Typography variant="h4" color="error.main">
                {overdueEscalations.length}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Resolution Rate
              </Typography>
              <Typography variant="h4" color="success.main">
                {escalations.length > 0 
                  ? Math.round((resolvedEscalations.length / escalations.length) * 100)
                  : 0}%
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Escalation Tabs */}
      <Card>
        <CardContent>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)}>
              <Tab 
                label={
                  <Badge badgeContent={activeEscalations.length} color="warning">
                    Active Escalations
                  </Badge>
                } 
              />
              <Tab 
                label={
                  <Badge badgeContent={overdueEscalations.length} color="error">
                    Overdue
                  </Badge>
                } 
              />
              <Tab label="Resolved" />
              <Tab label="All Escalations" />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <EscalationTable 
              escalations={activeEscalations}
              onResolve={handleResolveEscalation}
              getEscalationLevelText={getEscalationLevelText}
              getEscalationLevelColor={getEscalationLevelColor}
              getPriorityColor={getPriorityColor}
              showResolveAction={true}
            />
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <EscalationTable 
              escalations={overdueEscalations}
              onResolve={handleResolveEscalation}
              getEscalationLevelText={getEscalationLevelText}
              getEscalationLevelColor={getEscalationLevelColor}
              getPriorityColor={getPriorityColor}
              showResolveAction={true}
              isOverdue={true}
            />
          </TabPanel>

          <TabPanel value={tabValue} index={2}>
            <EscalationTable 
              escalations={resolvedEscalations}
              onResolve={handleResolveEscalation}
              getEscalationLevelText={getEscalationLevelText}
              getEscalationLevelColor={getEscalationLevelColor}
              getPriorityColor={getPriorityColor}
              showResolveAction={false}
            />
          </TabPanel>

          <TabPanel value={tabValue} index={3}>
            <EscalationTable 
              escalations={escalations}
              onResolve={handleResolveEscalation}
              getEscalationLevelText={getEscalationLevelText}
              getEscalationLevelColor={getEscalationLevelColor}
              getPriorityColor={getPriorityColor}
              showResolveAction={true}
            />
          </TabPanel>
        </CardContent>
      </Card>

      {/* Manual Escalation Dialog */}
      <Dialog 
        open={manualEscalationDialog} 
        onClose={() => setManualEscalationDialog(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Manual Escalation</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 1 }}>
            <TextField
              fullWidth
              label="Ticket ID"
              value={escalationForm.ticketId}
              onChange={(e) => setEscalationForm(prev => ({ ...prev, ticketId: e.target.value }))}
              sx={{ mb: 2 }}
            />
            
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Escalate To</InputLabel>
              <Select
                value={escalationForm.escalatedToId}
                onChange={(e) => setEscalationForm(prev => ({ ...prev, escalatedToId: e.target.value }))}
              >
                {staff.map((staffMember) => (
                  <MenuItem key={staffMember.id} value={staffMember.id}>
                    {staffMember.firstName} {staffMember.lastName} ({staffMember.role})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Escalation Level</InputLabel>
              <Select
                value={escalationForm.level}
                onChange={(e) => setEscalationForm(prev => ({ ...prev, level: e.target.value }))}
              >
                <MenuItem value="EMERGENCY_UNASSIGNED">Emergency Unassigned</MenuItem>
                <MenuItem value="HIGH_NO_PROGRESS">High No Progress</MenuItem>
                <MenuItem value="MEDIUM_UNASSIGNED">Medium Unassigned</MenuItem>
                <MenuItem value="LOW_UNASSIGNED">Low Unassigned</MenuItem>
                <MenuItem value="SLA_BREACH">SLA Breach</MenuItem>
              </Select>
            </FormControl>

            <TextField
              fullWidth
              label="Reason"
              multiline
              rows={3}
              value={escalationForm.reason}
              onChange={(e) => setEscalationForm(prev => ({ ...prev, reason: e.target.value }))}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setManualEscalationDialog(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleManualEscalation}
            variant="contained"
            disabled={!escalationForm.ticketId || !escalationForm.escalatedToId || !escalationForm.reason}
          >
            Escalate
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

// Separate component for escalation table
interface EscalationTableProps {
  escalations: Escalation[];
  onResolve: (id: string) => void;
  getEscalationLevelText: (level: number) => string;
  getEscalationLevelColor: (level: number) => any;
  getPriorityColor: (priority: string) => any;
  showResolveAction: boolean;
  isOverdue?: boolean;
}

const EscalationTable: React.FC<EscalationTableProps> = ({
  escalations,
  onResolve,
  getEscalationLevelText,
  getEscalationLevelColor,
  getPriorityColor,
  showResolveAction,
  isOverdue = false
}) => {
  if (escalations.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', py: 4 }}>
        <Typography variant="body1" color="text.secondary">
          No escalations found
        </Typography>
      </Box>
    );
  }

  return (
    <TableContainer component={Paper} sx={{ mt: 2 }}>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Ticket</TableCell>
            <TableCell>Level</TableCell>
            <TableCell>From</TableCell>
            <TableCell>To</TableCell>
            <TableCell>Reason</TableCell>
            <TableCell>Escalated</TableCell>
            <TableCell>Status</TableCell>
            {showResolveAction && <TableCell>Actions</TableCell>}
          </TableRow>
        </TableHead>
        <TableBody>
          {escalations.map((escalation) => (
            <TableRow 
              key={escalation.id}
              sx={isOverdue ? { backgroundColor: 'error.50' } : {}}
            >
              <TableCell>
                <Box>
                  <Typography variant="body2" fontWeight="bold">
                    {escalation.ticket.ticketNumber}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {escalation.ticket.title}
                  </Typography>
                  <Box sx={{ mt: 0.5 }}>
                    <Chip 
                      size="small" 
                      label={escalation.ticket.priority} 
                      color={getPriorityColor(escalation.ticket.priority)}
                    />
                  </Box>
                </Box>
              </TableCell>
              <TableCell>
                <Chip 
                  size="small"
                  label={getEscalationLevelText(escalation.escalationLevel)}
                  color={getEscalationLevelColor(escalation.escalationLevel)}
                />
              </TableCell>
              <TableCell>
                <Box>
                  <Typography variant="body2">
                    {escalation.escalatedFrom.firstName} {escalation.escalatedFrom.lastName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {escalation.escalatedFrom.role}
                  </Typography>
                </Box>
              </TableCell>
              <TableCell>
                <Box>
                  <Typography variant="body2">
                    {escalation.escalatedTo.firstName} {escalation.escalatedTo.lastName}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {escalation.escalatedTo.role}
                  </Typography>
                </Box>
              </TableCell>
              <TableCell>
                <Tooltip title={escalation.reason}>
                  <Typography variant="body2" sx={{ 
                    maxWidth: 200, 
                    overflow: 'hidden', 
                    textOverflow: 'ellipsis',
                    whiteSpace: 'nowrap'
                  }}>
                    {escalation.reason}
                  </Typography>
                </Tooltip>
              </TableCell>
              <TableCell>
                <Typography variant="body2">
                  {new Date(escalation.escalatedAt).toLocaleDateString()}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {new Date(escalation.escalatedAt).toLocaleTimeString()}
                </Typography>
                {escalation.isAutoEscalated && (
                  <Chip size="small" label="Auto" color="info" sx={{ ml: 1 }} />
                )}
              </TableCell>
              <TableCell>
                {escalation.resolvedAt ? (
                  <Chip size="small" label="Resolved" color="success" icon={<CheckCircle />} />
                ) : (
                  <Chip size="small" label="Active" color="warning" icon={<Schedule />} />
                )}
              </TableCell>
              {showResolveAction && (
                <TableCell>
                  {!escalation.resolvedAt && (
                    <Tooltip title="Resolve Escalation">
                      <IconButton
                        size="small"
                        onClick={() => onResolve(escalation.id)}
                        color="success"
                      >
                        <CheckCircle />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              )}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default EscalationManagement;
