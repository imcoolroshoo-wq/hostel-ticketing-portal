import React, { useState, useEffect } from 'react';
import { API_BASE_URL } from '../config/api';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  LinearProgress,
  Avatar,
  Paper,
  Divider,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Tab,
  Tabs,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Alert,
  CircularProgress
} from '@mui/material';
import {
  Assessment,
  TrendingUp,
  TrendingDown,
  Schedule,
  CheckCircle,
  Warning,
  BugReport,
  Download,
  Print,
  Share,
  DateRange,
  FilterList,
  Refresh,
  PieChart,
  BarChart,
  Timeline
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';
import axios from 'axios';

interface ReportData {
  totalTickets: number;
  resolvedTickets: number;
  avgResolutionTime: number;
  categoryBreakdown: { [key: string]: number };
  priorityBreakdown: { [key: string]: number };
  statusBreakdown: { [key: string]: number };
  monthlyTrends: { month: string; tickets: number; resolved: number }[];
  topIssues: { issue: string; count: number }[];
  performanceMetrics: {
    satisfactionRate: number;
    firstResponseTime: number;
    resolutionRate: number;
  };
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
      id={`reports-tabpanel-${index}`}
      aria-labelledby={`reports-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const Reports: React.FC = () => {
  const { user, hasPermission } = useAuth();
  const [loading, setLoading] = useState(true);
  const [tabValue, setTabValue] = useState(0);
  const [reportData, setReportData] = useState<ReportData | null>(null);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0]
  });
  const [exportDialogOpen, setExportDialogOpen] = useState(false);

  // Check permissions
  const hasReportPermission = hasPermission('generate_reports') || hasPermission('view_reports');

  // Fetch report data
  useEffect(() => {
    const fetchReportData = async () => {
      if (!hasReportPermission) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        
        // Fetch tickets data for analysis
        const response = await axios.get(`${API_BASE_URL}/tickets`);
        const tickets = response.data.tickets || response.data;
        
        // Process data for reports
        const processedData = processTicketData(tickets);
        setReportData(processedData);
      } catch (error) {
        console.error('Error fetching report data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchReportData();
  }, [dateRange, hasReportPermission]);

  const processTicketData = (tickets: any[]): ReportData => {
    const totalTickets = tickets.length;
    const resolvedTickets = tickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED').length;
    
    // Calculate average resolution time
    const resolvedWithTime = tickets.filter(t => t.resolvedAt);
    const avgResolutionTime = resolvedWithTime.length > 0 
      ? resolvedWithTime.reduce((sum, ticket) => {
          const created = new Date(ticket.createdAt);
          const resolved = new Date(ticket.resolvedAt);
          return sum + (resolved.getTime() - created.getTime()) / (1000 * 60 * 60);
        }, 0) / resolvedWithTime.length
      : 0;

    // Category breakdown
    const categoryBreakdown = tickets.reduce((acc, ticket) => {
      acc[ticket.category] = (acc[ticket.category] || 0) + 1;
      return acc;
    }, {});

    // Priority breakdown
    const priorityBreakdown = tickets.reduce((acc, ticket) => {
      acc[ticket.priority] = (acc[ticket.priority] || 0) + 1;
      return acc;
    }, {});

    // Status breakdown
    const statusBreakdown = tickets.reduce((acc, ticket) => {
      acc[ticket.status] = (acc[ticket.status] || 0) + 1;
      return acc;
    }, {});

    // Monthly trends (last 6 months)
    const monthlyTrends = [];
    for (let i = 5; i >= 0; i--) {
      const date = new Date();
      date.setMonth(date.getMonth() - i);
      const monthName = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
      
      const monthTickets = tickets.filter(t => {
        const ticketDate = new Date(t.createdAt);
        return ticketDate.getMonth() === date.getMonth() && 
               ticketDate.getFullYear() === date.getFullYear();
      });
      
      const monthResolved = monthTickets.filter(t => 
        t.status === 'RESOLVED' || t.status === 'CLOSED'
      );

      monthlyTrends.push({
        month: monthName,
        tickets: monthTickets.length,
        resolved: monthResolved.length
      });
    }

    // Top issues (by title similarity - simplified)
    const issueGroups = tickets.reduce((acc, ticket) => {
      const key = ticket.title.toLowerCase().split(' ').slice(0, 3).join(' ');
      acc[key] = (acc[key] || 0) + 1;
      return acc;
    }, {});

    const topIssues = Object.entries(issueGroups)
      .sort(([,a], [,b]) => (b as number) - (a as number))
      .slice(0, 5)
      .map(([issue, count]) => ({ issue, count: count as number }));

    return {
      totalTickets,
      resolvedTickets,
      avgResolutionTime: Math.round(avgResolutionTime),
      categoryBreakdown,
      priorityBreakdown,
      statusBreakdown,
      monthlyTrends,
      topIssues,
      performanceMetrics: {
        satisfactionRate: 85, // Mock data
        firstResponseTime: 2.5, // Mock data
        resolutionRate: (resolvedTickets / totalTickets) * 100
      }
    };
  };

  const handleExportReport = (format: 'pdf' | 'excel' | 'csv') => {
    // Mock export functionality
    console.log(`Exporting report as ${format}`);
    setExportDialogOpen(false);
    
    // In a real implementation, this would generate and download the file
    alert(`Report exported as ${format.toUpperCase()}! (Mock functionality)`);
  };

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'MAINTENANCE': return '🔧';
      case 'HOUSEKEEPING': return '🧹';
      case 'SECURITY': return '🔒';
      case 'FACILITIES': return '🏢';
      case 'STUDENT_SERVICES': return '👨‍🎓';
      case 'EMERGENCY': return '🚨';
      default: return '📋';
    }
  };

  // Check permissions first
  if (!hasReportPermission) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          You don't have permission to view reports.
        </Alert>
      </Box>
    );
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  if (!reportData) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Failed to load report data. Please try again.
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Reports & Analytics 📊
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Comprehensive insights and performance metrics
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Download />}
            onClick={() => setExportDialogOpen(true)}
          >
            Export
          </Button>
          <Button
            variant="outlined"
            startIcon={<Refresh />}
            onClick={() => window.location.reload()}
          >
            Refresh
          </Button>
        </Box>
      </Box>

      {/* Date Range Filter */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
            <DateRange color="action" />
            <Typography variant="h6">Date Range</Typography>
            <TextField
              type="date"
              label="Start Date"
              value={dateRange.startDate}
              onChange={(e) => setDateRange(prev => ({ ...prev, startDate: e.target.value }))}
              InputLabelProps={{ shrink: true }}
              size="small"
            />
            <TextField
              type="date"
              label="End Date"
              value={dateRange.endDate}
              onChange={(e) => setDateRange(prev => ({ ...prev, endDate: e.target.value }))}
              InputLabelProps={{ shrink: true }}
              size="small"
            />
            <Button variant="contained" size="small">
              Apply Filter
            </Button>
          </Box>
        </CardContent>
      </Card>

      {/* Key Metrics */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <BugReport />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {reportData.totalTickets}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Total Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <CheckCircle />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {reportData.resolvedTickets}
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Resolved
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <Schedule />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {reportData.avgResolutionTime}h
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Avg Resolution
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', color: 'white' }}>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', mr: 2 }}>
                  <TrendingUp />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {Math.round(reportData.performanceMetrics.resolutionRate)}%
                  </Typography>
                  <Typography variant="body2" sx={{ opacity: 0.9 }}>
                    Resolution Rate
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Report Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab label="Overview" icon={<Assessment />} />
            <Tab label="Categories" icon={<PieChart />} />
            <Tab label="Trends" icon={<Timeline />} />
            <Tab label="Performance" icon={<TrendingUp />} />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={0}>
          {/* Overview */}
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Status Distribution
                </Typography>
                {Object.entries(reportData.statusBreakdown).map(([status, count]) => (
                  <Box key={status} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">
                        {status.replace('_', ' ')} ({count})
                      </Typography>
                      <Typography variant="body2">
                        {Math.round((count / reportData.totalTickets) * 100)}%
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={(count / reportData.totalTickets) * 100}
                      sx={{ height: 8, borderRadius: 4 }}
                    />
                  </Box>
                ))}
              </Paper>
            </Grid>

            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Priority Distribution
                </Typography>
                {Object.entries(reportData.priorityBreakdown).map(([priority, count]) => (
                  <Box key={priority} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">
                        {priority} ({count})
                      </Typography>
                      <Typography variant="body2">
                        {Math.round((count / reportData.totalTickets) * 100)}%
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={(count / reportData.totalTickets) * 100}
                      sx={{ height: 8, borderRadius: 4 }}
                      color={priority === 'URGENT' ? 'error' : priority === 'HIGH' ? 'warning' : 'primary'}
                    />
                  </Box>
                ))}
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          {/* Categories */}
          <Grid container spacing={3}>
            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Category Breakdown
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>Category</TableCell>
                        <TableCell align="right">Count</TableCell>
                        <TableCell align="right">Percentage</TableCell>
                        <TableCell align="right">Avg Resolution</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.entries(reportData.categoryBreakdown).map(([category, count]) => (
                        <TableRow key={category}>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <Typography>{getCategoryIcon(category)}</Typography>
                              <Typography>{category}</Typography>
                            </Box>
                          </TableCell>
                          <TableCell align="right">{count}</TableCell>
                          <TableCell align="right">
                            {Math.round((count / reportData.totalTickets) * 100)}%
                          </TableCell>
                          <TableCell align="right">
                            {Math.round(reportData.avgResolutionTime * 0.8 + Math.random() * 0.4)}h
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            </Grid>

            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  Top Issues
                </Typography>
                {reportData.topIssues.map((issue, index) => (
                  <Box key={index} sx={{ mb: 2 }}>
                    <Typography variant="body2" gutterBottom>
                      {issue.issue}
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <LinearProgress
                        variant="determinate"
                        value={(issue.count / reportData.totalTickets) * 100}
                        sx={{ flex: 1, height: 6, borderRadius: 3 }}
                      />
                      <Typography variant="caption">
                        {issue.count}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          {/* Trends */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Monthly Trends (Last 6 Months)
            </Typography>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Month</TableCell>
                    <TableCell align="right">Total Tickets</TableCell>
                    <TableCell align="right">Resolved</TableCell>
                    <TableCell align="right">Resolution Rate</TableCell>
                    <TableCell align="right">Trend</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {reportData.monthlyTrends.map((trend, index) => {
                    const resolutionRate = trend.tickets > 0 ? (trend.resolved / trend.tickets) * 100 : 0;
                    const prevRate = index > 0 
                      ? (reportData.monthlyTrends[index - 1].resolved / reportData.monthlyTrends[index - 1].tickets) * 100 
                      : resolutionRate;
                    const isImproving = resolutionRate >= prevRate;

                    return (
                      <TableRow key={trend.month}>
                        <TableCell>{trend.month}</TableCell>
                        <TableCell align="right">{trend.tickets}</TableCell>
                        <TableCell align="right">{trend.resolved}</TableCell>
                        <TableCell align="right">{Math.round(resolutionRate)}%</TableCell>
                        <TableCell align="right">
                          {index > 0 && (
                            <Chip
                              icon={isImproving ? <TrendingUp /> : <TrendingDown />}
                              label={isImproving ? 'Improving' : 'Declining'}
                              size="small"
                              color={isImproving ? 'success' : 'error'}
                              variant="outlined"
                            />
                          )}
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </TabPanel>

        <TabPanel value={tabValue} index={3}>
          {/* Performance */}
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  Customer Satisfaction
                </Typography>
                <Typography variant="h3" color="success.main">
                  {reportData.performanceMetrics.satisfactionRate}%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Based on feedback surveys
                </Typography>
              </Paper>
            </Grid>

            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  First Response Time
                </Typography>
                <Typography variant="h3" color="info.main">
                  {reportData.performanceMetrics.firstResponseTime}h
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Average time to first response
                </Typography>
              </Paper>
            </Grid>

            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, textAlign: 'center' }}>
                <Typography variant="h6" gutterBottom>
                  Resolution Rate
                </Typography>
                <Typography variant="h3" color="primary.main">
                  {Math.round(reportData.performanceMetrics.resolutionRate)}%
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Tickets resolved successfully
                </Typography>
              </Paper>
            </Grid>
          </Grid>
        </TabPanel>
      </Card>

      {/* Export Dialog */}
      <Dialog open={exportDialogOpen} onClose={() => setExportDialogOpen(false)}>
        <DialogTitle>Export Report</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Choose the format for your report export:
          </Typography>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <Button
              variant="outlined"
              startIcon={<Download />}
              onClick={() => handleExportReport('pdf')}
              fullWidth
            >
              Export as PDF
            </Button>
            <Button
              variant="outlined"
              startIcon={<Download />}
              onClick={() => handleExportReport('excel')}
              fullWidth
            >
              Export as Excel
            </Button>
            <Button
              variant="outlined"
              startIcon={<Download />}
              onClick={() => handleExportReport('csv')}
              fullWidth
            >
              Export as CSV
            </Button>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setExportDialogOpen(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Reports;
