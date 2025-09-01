import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  LinearProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Button,
  Tab,
  Tabs,
  Divider,
} from '@mui/material';
import {
  Analytics,
  TrendingUp,
  TrendingDown,
  Assessment,
  Speed,
  CheckCircle,
  Warning,
  Schedule,
  People,
  ConfirmationNumber,
  Refresh,
  Download,
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

interface AnalyticsData {
  overview: {
    totalTickets: number;
    activeTickets: number;
    resolvedTickets: number;
    avgResolutionTime: number;
    satisfactionRate: number;
    overdueTickets: number;
  };
  trends: {
    ticketVolumeChange: number;
    resolutionTimeChange: number;
    satisfactionChange: number;
  };
  performance: {
    staffPerformance: StaffPerformance[];
    categoryPerformance: CategoryPerformance[];
    slaCompliance: number;
    firstCallResolution: number;
  };
  distribution: {
    byPriority: { [key: string]: number };
    byStatus: { [key: string]: number };
    byCategory: { [key: string]: number };
    byBuilding: { [key: string]: number };
  };
  operational: {
    peakHours: Array<{ hour: number; count: number }>;
    monthlyTrends: Array<{ month: string; created: number; resolved: number }>;
    escalationRate: number;
    reopenRate: number;
  };
}

interface StaffPerformance {
  id: string;
  name: string;
  role: string;
  assignedTickets: number;
  resolvedTickets: number;
  avgResolutionTime: number;
  satisfactionRating: number;
  workload: number;
}

interface CategoryPerformance {
  category: string;
  totalTickets: number;
  avgResolutionTime: number;
  satisfactionRating: number;
  escalationRate: number;
  trend: number;
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
      id={`analytics-tabpanel-${index}`}
      aria-labelledby={`analytics-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const AdvancedAnalyticsDashboard: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tabValue, setTabValue] = useState(0);
  const [timeRange, setTimeRange] = useState('30'); // days

  useEffect(() => {
    if (hasPermission('analytics_access') || hasPermission('view_reports')) {
      fetchAnalyticsData();
    }
  }, [hasPermission, timeRange]);

  const fetchAnalyticsData = async () => {
    try {
      setLoading(true);
      const response = await axios.get(`${API_ENDPOINTS.ADVANCED_ANALYTICS}/dashboard/operational`, {
        params: { timeRange }
      });
      setAnalyticsData(response.data);
      setError(null);
    } catch (err) {
      console.error('Error fetching analytics data:', err);
      setError('Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  };

  const formatTrend = (value: number) => {
    const isPositive = value >= 0;
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
        {isPositive ? <TrendingUp color="success" /> : <TrendingDown color="error" />}
        <Typography color={isPositive ? 'success.main' : 'error.main'}>
          {isPositive ? '+' : ''}{value.toFixed(1)}%
        </Typography>
      </Box>
    );
  };

  const getPerformanceColor = (value: number, good: number, excellent: number) => {
    if (value >= excellent) return 'success';
    if (value >= good) return 'warning';
    return 'error';
  };

  if (!hasPermission('analytics_access') && !hasPermission('view_reports')) {
    return (
      <Alert severity="warning">
        You do not have permission to view analytics.
      </Alert>
    );
  }

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          Loading Analytics...
        </Typography>
        <LinearProgress />
      </Box>
    );
  }

  if (error || !analyticsData) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error || 'No analytics data available'}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Analytics />
          Advanced Analytics
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Time Range</InputLabel>
            <Select
              value={timeRange}
              onChange={(e) => setTimeRange(e.target.value)}
            >
              <MenuItem value="7">Last 7 days</MenuItem>
              <MenuItem value="30">Last 30 days</MenuItem>
              <MenuItem value="90">Last 3 months</MenuItem>
              <MenuItem value="365">Last year</MenuItem>
            </Select>
          </FormControl>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={fetchAnalyticsData}
          >
            Refresh
          </Button>
          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={() => alert('Export functionality coming soon')}
          >
            Export
          </Button>
        </Box>
      </Box>

      {/* Key Metrics Overview */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Total Tickets
              </Typography>
              <Typography variant="h4">
                {analyticsData.overview.totalTickets}
              </Typography>
              {formatTrend(analyticsData.trends.ticketVolumeChange)}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Active Tickets
              </Typography>
              <Typography variant="h4" color="warning.main">
                {analyticsData.overview.activeTickets}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {Math.round((analyticsData.overview.activeTickets / analyticsData.overview.totalTickets) * 100)}% of total
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Avg Resolution
              </Typography>
              <Typography variant="h4" color="info.main">
                {analyticsData.overview.avgResolutionTime}h
              </Typography>
              {formatTrend(analyticsData.trends.resolutionTimeChange)}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Satisfaction
              </Typography>
              <Typography variant="h4" color="success.main">
                {analyticsData.overview.satisfactionRate}%
              </Typography>
              {formatTrend(analyticsData.trends.satisfactionChange)}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                SLA Compliance
              </Typography>
              <Typography variant="h4" color={getPerformanceColor(analyticsData.performance.slaCompliance, 80, 95)}>
                {analyticsData.performance.slaCompliance}%
              </Typography>
              <LinearProgress
                variant="determinate"
                value={analyticsData.performance.slaCompliance}
                color={getPerformanceColor(analyticsData.performance.slaCompliance, 80, 95)}
                sx={{ mt: 1 }}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} md={2}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom variant="body2">
                Overdue Tickets
              </Typography>
              <Typography variant="h4" color="error.main">
                {analyticsData.overview.overdueTickets}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {Math.round((analyticsData.overview.overdueTickets / analyticsData.overview.activeTickets) * 100)}% of active
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Detailed Analytics Tabs */}
      <Card>
        <CardContent>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tabValue} onChange={(e, newValue) => setTabValue(newValue)}>
              <Tab label="Performance" />
              <Tab label="Distribution" />
              <Tab label="Staff Analysis" />
              <Tab label="Operational Insights" />
            </Tabs>
          </Box>

          <TabPanel value={tabValue} index={0}>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              {/* Category Performance */}
              <Grid item xs={12} lg={8}>
                <Typography variant="h6" gutterBottom>
                  Category Performance
                </Typography>
                <TableContainer component={Paper}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Category</TableCell>
                        <TableCell align="right">Total Tickets</TableCell>
                        <TableCell align="right">Avg Resolution (hrs)</TableCell>
                        <TableCell align="right">Satisfaction</TableCell>
                        <TableCell align="right">Escalation Rate</TableCell>
                        <TableCell align="right">Trend</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {analyticsData.performance.categoryPerformance.map((category) => (
                        <TableRow key={category.category}>
                          <TableCell>
                            <Typography variant="body2" fontWeight="bold">
                              {category.category.replace('_', ' ')}
                            </Typography>
                          </TableCell>
                          <TableCell align="right">{category.totalTickets}</TableCell>
                          <TableCell align="right">
                            <Chip
                              size="small"
                              label={`${category.avgResolutionTime}h`}
                              color={getPerformanceColor(category.avgResolutionTime, 24, 12)}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <Chip
                              size="small"
                              label={`${category.satisfactionRating}%`}
                              color={getPerformanceColor(category.satisfactionRating, 80, 90)}
                            />
                          </TableCell>
                          <TableCell align="right">
                            <Chip
                              size="small"
                              label={`${category.escalationRate}%`}
                              color={getPerformanceColor(100 - category.escalationRate, 80, 95)}
                            />
                          </TableCell>
                          <TableCell align="right">
                            {formatTrend(category.trend)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {/* Key Performance Indicators */}
              <Grid item xs={12} lg={4}>
                <Typography variant="h6" gutterBottom>
                  Key Performance Indicators
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="body2" color="text.secondary">
                        First Call Resolution
                      </Typography>
                      <Typography variant="h5">
                        {analyticsData.performance.firstCallResolution}%
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={analyticsData.performance.firstCallResolution}
                        color={getPerformanceColor(analyticsData.performance.firstCallResolution, 70, 85)}
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                  </Card>

                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="body2" color="text.secondary">
                        Escalation Rate
                      </Typography>
                      <Typography variant="h5" color="warning.main">
                        {analyticsData.operational.escalationRate}%
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={analyticsData.operational.escalationRate}
                        color="warning"
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                  </Card>

                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="body2" color="text.secondary">
                        Reopen Rate
                      </Typography>
                      <Typography variant="h5" color="error.main">
                        {analyticsData.operational.reopenRate}%
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={analyticsData.operational.reopenRate}
                        color="error"
                        sx={{ mt: 1 }}
                      />
                    </CardContent>
                  </Card>
                </Box>
              </Grid>
            </Grid>
          </TabPanel>

          <TabPanel value={tabValue} index={1}>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              {/* Priority Distribution */}
              <Grid item xs={12} md={6}>
                <Typography variant="h6" gutterBottom>
                  Priority Distribution
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {Object.entries(analyticsData.distribution.byPriority).map(([priority, count]) => (
                    <Box key={priority} sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Typography variant="body2" sx={{ minWidth: 80 }}>
                        {priority}
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={(count / analyticsData.overview.totalTickets) * 100}
                        sx={{ flexGrow: 1 }}
                        color={priority === 'EMERGENCY' ? 'error' : priority === 'HIGH' ? 'warning' : 'primary'}
                      />
                      <Typography variant="body2" sx={{ minWidth: 40 }}>
                        {count}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              </Grid>

              {/* Status Distribution */}
              <Grid item xs={12} md={6}>
                <Typography variant="h6" gutterBottom>
                  Status Distribution
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {Object.entries(analyticsData.distribution.byStatus).map(([status, count]) => (
                    <Box key={status} sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                      <Typography variant="body2" sx={{ minWidth: 80 }}>
                        {status.replace('_', ' ')}
                      </Typography>
                      <LinearProgress
                        variant="determinate"
                        value={(count / analyticsData.overview.totalTickets) * 100}
                        sx={{ flexGrow: 1 }}
                        color={status === 'RESOLVED' ? 'success' : status === 'OPEN' ? 'error' : 'primary'}
                      />
                      <Typography variant="body2" sx={{ minWidth: 40 }}>
                        {count}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              </Grid>

              {/* Building Distribution */}
              <Grid item xs={12}>
                <Typography variant="h6" gutterBottom>
                  Building Distribution
                </Typography>
                <Grid container spacing={2}>
                  {Object.entries(analyticsData.distribution.byBuilding).map(([building, count]) => (
                    <Grid item xs={12} sm={6} md={4} key={building}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" color="primary">
                            {building}
                          </Typography>
                          <Typography variant="h4">
                            {count}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {Math.round((count / analyticsData.overview.totalTickets) * 100)}% of total
                          </Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              </Grid>
            </Grid>
          </TabPanel>

          <TabPanel value={tabValue} index={2}>
            <Typography variant="h6" gutterBottom sx={{ mt: 1 }}>
              Staff Performance Analysis
            </Typography>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Staff Member</TableCell>
                    <TableCell>Role</TableCell>
                    <TableCell align="right">Assigned</TableCell>
                    <TableCell align="right">Resolved</TableCell>
                    <TableCell align="right">Resolution Rate</TableCell>
                    <TableCell align="right">Avg Time (hrs)</TableCell>
                    <TableCell align="right">Satisfaction</TableCell>
                    <TableCell align="right">Workload</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {analyticsData.performance.staffPerformance.map((staff) => (
                    <TableRow key={staff.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="bold">
                          {staff.name}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip size="small" label={staff.role} />
                      </TableCell>
                      <TableCell align="right">{staff.assignedTickets}</TableCell>
                      <TableCell align="right">{staff.resolvedTickets}</TableCell>
                      <TableCell align="right">
                        <Chip
                          size="small"
                          label={`${Math.round((staff.resolvedTickets / staff.assignedTickets) * 100)}%`}
                          color={getPerformanceColor((staff.resolvedTickets / staff.assignedTickets) * 100, 70, 85)}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Chip
                          size="small"
                          label={`${staff.avgResolutionTime}h`}
                          color={getPerformanceColor(staff.avgResolutionTime, 24, 12)}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Chip
                          size="small"
                          label={`${staff.satisfactionRating}%`}
                          color={getPerformanceColor(staff.satisfactionRating, 80, 90)}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <LinearProgress
                          variant="determinate"
                          value={staff.workload}
                          color={staff.workload > 80 ? 'error' : staff.workload > 60 ? 'warning' : 'success'}
                          sx={{ width: 60 }}
                        />
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </TabPanel>

          <TabPanel value={tabValue} index={3}>
            <Grid container spacing={3} sx={{ mt: 1 }}>
              {/* Peak Hours Analysis */}
              <Grid item xs={12} md={6}>
                <Typography variant="h6" gutterBottom>
                  Peak Hours Analysis
                </Typography>
                <TableContainer component={Paper}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Hour</TableCell>
                        <TableCell align="right">Tickets</TableCell>
                        <TableCell align="right">Load</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {analyticsData.operational.peakHours.slice(0, 10).map((hour) => (
                        <TableRow key={hour.hour}>
                          <TableCell>{hour.hour}:00</TableCell>
                          <TableCell align="right">{hour.count}</TableCell>
                          <TableCell align="right">
                            <LinearProgress
                              variant="determinate"
                              value={(hour.count / Math.max(...analyticsData.operational.peakHours.map(h => h.count))) * 100}
                              sx={{ width: 60 }}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>

              {/* Monthly Trends */}
              <Grid item xs={12} md={6}>
                <Typography variant="h6" gutterBottom>
                  Monthly Trends
                </Typography>
                <TableContainer component={Paper}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Month</TableCell>
                        <TableCell align="right">Created</TableCell>
                        <TableCell align="right">Resolved</TableCell>
                        <TableCell align="right">Ratio</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {analyticsData.operational.monthlyTrends.map((trend) => (
                        <TableRow key={trend.month}>
                          <TableCell>{trend.month}</TableCell>
                          <TableCell align="right">{trend.created}</TableCell>
                          <TableCell align="right">{trend.resolved}</TableCell>
                          <TableCell align="right">
                            <Chip
                              size="small"
                              label={`${Math.round((trend.resolved / trend.created) * 100)}%`}
                              color={getPerformanceColor((trend.resolved / trend.created) * 100, 80, 95)}
                            />
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            </Grid>
          </TabPanel>
        </CardContent>
      </Card>
    </Box>
  );
};

export default AdvancedAnalyticsDashboard;
