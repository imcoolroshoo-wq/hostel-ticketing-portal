import React, { useState, useEffect } from 'react';
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
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Paper,
  Divider
} from '@mui/material';
import {
  BugReport,
  CheckCircle,
  Schedule,
  Warning,
  TrendingUp,
  Add,
  Refresh
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [refreshKey, setRefreshKey] = useState(0);

  // Mock data - in real app this would come from API
  const stats = {
    totalTickets: 24,
    openTickets: 8,
    inProgress: 6,
    resolved: 10,
    urgent: 2
  };

  const handleRefresh = () => {
    // In a real app, this would refetch data from APIs
    setRefreshKey(prev => prev + 1);
    console.log('Dashboard refreshed');
  };

  const recentTickets = [
    {
      id: 'TKT-001',
      title: 'Leaky faucet in bathroom',
      status: 'In Progress',
      priority: 'Medium',
      assignedTo: 'Mike Johnson',
      createdAt: '2 hours ago'
    },
    {
      id: 'TKT-002',
      title: 'Broken window lock',
      status: 'Open',
      priority: 'High',
      assignedTo: 'Unassigned',
      createdAt: '4 hours ago'
    },
    {
      id: 'TKT-003',
      title: 'Heating not working',
      status: 'Resolved',
      priority: 'Urgent',
      assignedTo: 'Sarah Wilson',
      createdAt: '1 day ago'
    }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Open': return 'default';
      case 'In Progress': return 'primary';
      case 'Resolved': return 'success';
      case 'Closed': return 'secondary';
      default: return 'default';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'Low': return 'success';
      case 'Medium': return 'warning';
      case 'High': return 'error';
      case 'Urgent': return 'error';
      default: return 'default';
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" component="h1">
          Dashboard
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate('/tickets/create')}
        >
          Create Ticket
        </Button>
      </Box>

      {/* Statistics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'primary.main', mr: 2 }}>
                  <BugReport />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.totalTickets}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'warning.main', mr: 2 }}>
                  <Schedule />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.openTickets}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Open Tickets
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'info.main', mr: 2 }}>
                  <TrendingUp />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.inProgress}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    In Progress
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <Avatar sx={{ bgcolor: 'success.main', mr: 2 }}>
                  <CheckCircle />
                </Avatar>
                <Box>
                  <Typography variant="h4" component="div">
                    {stats.resolved}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Resolved
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Progress Overview */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Ticket Status Overview
              </Typography>
              <Box sx={{ mt: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Open</Typography>
                  <Typography variant="body2">{stats.openTickets}/{stats.totalTickets}</Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={(stats.openTickets / stats.totalTickets) * 100} 
                  sx={{ mb: 2 }}
                />
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">In Progress</Typography>
                  <Typography variant="body2">{stats.inProgress}/{stats.totalTickets}</Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={(stats.inProgress / stats.totalTickets) * 100} 
                  sx={{ mb: 2 }}
                />
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body2">Resolved</Typography>
                  <Typography variant="body2">{stats.resolved}/{stats.totalTickets}</Typography>
                </Box>
                <LinearProgress 
                  variant="determinate" 
                  value={(stats.resolved / stats.totalTickets) * 100} 
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Quick Actions
              </Typography>
              <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Button
                  variant="outlined"
                  fullWidth
                  startIcon={<Add />}
                  onClick={() => navigate('/tickets/create')}
                >
                  Create New Ticket
                </Button>
                <Button
                  variant="outlined"
                  fullWidth
                  startIcon={<BugReport />}
                  onClick={() => navigate('/tickets')}
                >
                  View All Tickets
                </Button>
                <Button
                  variant="outlined"
                  fullWidth
                  startIcon={<Refresh />}
                  onClick={handleRefresh}
                >
                  Refresh Dashboard
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Recent Tickets */}
      <Card>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">
              Recent Tickets
            </Typography>
            <Button
              variant="text"
              onClick={() => navigate('/tickets')}
            >
              View All
            </Button>
          </Box>
          
          <List>
            {recentTickets.map((ticket, index) => (
              <React.Fragment key={ticket.id}>
                <ListItem>
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: 'primary.main' }}>
                      <BugReport />
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="subtitle1">
                          {ticket.title}
                        </Typography>
                        <Chip 
                          label={ticket.status} 
                          size="small" 
                          color={getStatusColor(ticket.status) as any}
                        />
                        <Chip 
                          label={ticket.priority} 
                          size="small" 
                          color={getPriorityColor(ticket.priority) as any}
                        />
                      </Box>
                    }
                    secondary={
                      <Box>
                        <Typography variant="body2" color="text.secondary">
                          {ticket.id} • {ticket.createdAt}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Assigned to: {ticket.assignedTo}
                        </Typography>
                      </Box>
                    }
                  />
                </ListItem>
                {index < recentTickets.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Dashboard; 