import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Chip,
  IconButton,
  Tooltip,
  Button,
  Divider,
  Badge,
  Switch,
  FormControlLabel,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  Tab,
  Tabs,
  Paper
} from '@mui/material';
import {
  Notifications as NotificationsIcon,
  BugReport,
  CheckCircle,
  Warning,
  Info,
  Schedule,
  Delete,
  MarkAsUnread,
  MarkEmailRead,
  Settings,
  Clear,
  FilterList
} from '@mui/icons-material';
import { useAuth } from '../contexts/AuthContext';

interface Notification {
  id: string;
  type: 'ticket_created' | 'ticket_assigned' | 'ticket_updated' | 'ticket_resolved' | 'system' | 'reminder';
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  relatedTicketId?: string;
  priority: 'low' | 'medium' | 'high' | 'urgent';
  actionUrl?: string;
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
      id={`notifications-tabpanel-${index}`}
      aria-labelledby={`notifications-tab-${index}`}
      {...other}
    >
      {value === index && <Box>{children}</Box>}
    </div>
  );
}

const Notifications: React.FC = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [tabValue, setTabValue] = useState(0);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [notificationSettings, setNotificationSettings] = useState({
    emailNotifications: true,
    pushNotifications: true,
    ticketUpdates: true,
    assignments: true,
    reminders: true,
    systemAlerts: true
  });

  // Mock notifications data
  useEffect(() => {
    const mockNotifications: Notification[] = [
      {
        id: '1',
        type: 'ticket_assigned',
        title: 'New Ticket Assigned',
        message: 'You have been assigned ticket #TKT-2025-001 - Plumbing issue in Room 101',
        isRead: false,
        createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
        relatedTicketId: 'ticket-1',
        priority: 'high',
        actionUrl: '/tickets/ticket-1'
      },
      {
        id: '2',
        type: 'ticket_updated',
        title: 'Ticket Status Updated',
        message: 'Ticket #TKT-2025-002 has been marked as resolved',
        isRead: false,
        createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
        relatedTicketId: 'ticket-2',
        priority: 'medium',
        actionUrl: '/tickets/ticket-2'
      },
      {
        id: '3',
        type: 'ticket_created',
        title: 'New Ticket Created',
        message: 'A new maintenance ticket has been created for Building A',
        isRead: true,
        createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
        relatedTicketId: 'ticket-3',
        priority: 'medium',
        actionUrl: '/tickets/ticket-3'
      },
      {
        id: '4',
        type: 'reminder',
        title: 'Overdue Ticket Reminder',
        message: 'Ticket #TKT-2025-003 is overdue and requires attention',
        isRead: true,
        createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
        relatedTicketId: 'ticket-4',
        priority: 'urgent',
        actionUrl: '/tickets/ticket-4'
      },
      {
        id: '5',
        type: 'system',
        title: 'System Maintenance',
        message: 'Scheduled system maintenance will occur tonight from 2 AM to 4 AM',
        isRead: true,
        createdAt: new Date(Date.now() - 48 * 60 * 60 * 1000).toISOString(),
        priority: 'low'
      }
    ];

    // Filter notifications based on user role
    const filteredNotifications = mockNotifications.filter(notification => {
      if (user?.role === 'STUDENT') {
        return ['ticket_updated', 'ticket_resolved', 'system', 'reminder'].includes(notification.type);
      } else if (user?.role === 'STAFF') {
        return ['ticket_assigned', 'ticket_created', 'ticket_updated', 'system', 'reminder'].includes(notification.type);
      } else if (user?.role === 'ADMIN') {
        return true; // Admins see all notifications
      }
      return false;
    });

    setNotifications(filteredNotifications);
  }, [user]);

  const getNotificationIcon = (type: string) => {
    switch (type) {
      case 'ticket_created':
      case 'ticket_assigned':
        return <BugReport />;
      case 'ticket_updated':
        return <Info />;
      case 'ticket_resolved':
        return <CheckCircle />;
      case 'reminder':
        return <Schedule />;
      case 'system':
        return <Warning />;
      default:
        return <NotificationsIcon />;
    }
  };

  const getNotificationColor = (priority: string) => {
    switch (priority) {
      case 'urgent':
        return 'error';
      case 'high':
        return 'warning';
      case 'medium':
        return 'info';
      case 'low':
        return 'success';
      default:
        return 'default';
    }
  };

  const formatTimeAgo = (dateString: string) => {
    const now = new Date();
    const date = new Date(dateString);
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

    if (diffInMinutes < 60) {
      return `${diffInMinutes} minutes ago`;
    } else if (diffInMinutes < 1440) {
      return `${Math.floor(diffInMinutes / 60)} hours ago`;
    } else {
      return `${Math.floor(diffInMinutes / 1440)} days ago`;
    }
  };

  const handleMarkAsRead = (notificationId: string) => {
    setNotifications(prev =>
      prev.map(notification =>
        notification.id === notificationId
          ? { ...notification, isRead: true }
          : notification
      )
    );
  };

  const handleMarkAsUnread = (notificationId: string) => {
    setNotifications(prev =>
      prev.map(notification =>
        notification.id === notificationId
          ? { ...notification, isRead: false }
          : notification
      )
    );
  };

  const handleDeleteNotification = (notificationId: string) => {
    setNotifications(prev =>
      prev.filter(notification => notification.id !== notificationId)
    );
  };

  const handleMarkAllAsRead = () => {
    setNotifications(prev =>
      prev.map(notification => ({ ...notification, isRead: true }))
    );
  };

  const handleClearAll = () => {
    setNotifications([]);
  };

  const getFilteredNotifications = () => {
    switch (tabValue) {
      case 0: // All
        return notifications;
      case 1: // Unread
        return notifications.filter(n => !n.isRead);
      case 2: // Tickets
        return notifications.filter(n => n.type.startsWith('ticket_'));
      case 3: // System
        return notifications.filter(n => n.type === 'system' || n.type === 'reminder');
      default:
        return notifications;
    }
  };

  const filteredNotifications = getFilteredNotifications();
  const unreadCount = notifications.filter(n => !n.isRead).length;

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" component="h1" gutterBottom>
            Notifications 🔔
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Stay updated with your tickets and system alerts
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<Settings />}
            onClick={() => setSettingsOpen(true)}
          >
            Settings
          </Button>
          <Button
            variant="outlined"
            startIcon={<MarkEmailRead />}
            onClick={handleMarkAllAsRead}
            disabled={unreadCount === 0}
          >
            Mark All Read
          </Button>
          <Button
            variant="outlined"
            startIcon={<Clear />}
            onClick={handleClearAll}
            color="error"
            disabled={notifications.length === 0}
          >
            Clear All
          </Button>
        </Box>
      </Box>

      {/* Summary Cards */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4 }}>
        <Paper sx={{ p: 2, flex: 1, textAlign: 'center' }}>
          <Typography variant="h4" color="primary">
            {notifications.length}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Total Notifications
          </Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1, textAlign: 'center' }}>
          <Typography variant="h4" color="warning.main">
            {unreadCount}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Unread
          </Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1, textAlign: 'center' }}>
          <Typography variant="h4" color="error.main">
            {notifications.filter(n => n.priority === 'urgent').length}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Urgent
          </Typography>
        </Paper>
        <Paper sx={{ p: 2, flex: 1, textAlign: 'center' }}>
          <Typography variant="h4" color="success.main">
            {notifications.filter(n => n.type.includes('resolved')).length}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Resolved
          </Typography>
        </Paper>
      </Box>

      {/* Notification Tabs */}
      <Card>
        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
          <Tabs value={tabValue} onChange={(_, newValue) => setTabValue(newValue)}>
            <Tab 
              label={
                <Badge badgeContent={notifications.length} color="primary">
                  All
                </Badge>
              } 
            />
            <Tab 
              label={
                <Badge badgeContent={unreadCount} color="error">
                  Unread
                </Badge>
              } 
            />
            <Tab label="Tickets" />
            <Tab label="System" />
          </Tabs>
        </Box>

        <TabPanel value={tabValue} index={tabValue}>
          {filteredNotifications.length === 0 ? (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <NotificationsIcon sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
              <Typography variant="h6" color="text.secondary">
                No notifications found
              </Typography>
              <Typography variant="body2" color="text.secondary">
                {tabValue === 1 ? 'All caught up! No unread notifications.' : 'You\'ll see notifications here when they arrive.'}
              </Typography>
            </Box>
          ) : (
            <List>
              {filteredNotifications.map((notification, index) => (
                <React.Fragment key={notification.id}>
                  <ListItem
                    sx={{
                      bgcolor: notification.isRead ? 'transparent' : 'action.hover',
                      borderLeft: notification.isRead ? 'none' : `4px solid ${
                        notification.priority === 'urgent' ? 'error.main' :
                        notification.priority === 'high' ? 'warning.main' :
                        notification.priority === 'medium' ? 'info.main' : 'success.main'
                      }`
                    }}
                  >
                    <ListItemAvatar>
                      <Avatar 
                        sx={{ 
                          bgcolor: getNotificationColor(notification.priority) + '.main',
                          color: 'white'
                        }}
                      >
                        {getNotificationIcon(notification.type)}
                      </Avatar>
                    </ListItemAvatar>
                    <ListItemText
                      primary={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                          <Typography 
                            variant="subtitle1" 
                            sx={{ 
                              fontWeight: notification.isRead ? 'normal' : 'bold',
                              flex: 1
                            }}
                          >
                            {notification.title}
                          </Typography>
                          <Chip 
                            label={notification.priority.toUpperCase()} 
                            size="small" 
                            color={getNotificationColor(notification.priority) as any}
                            variant="outlined"
                          />
                          <Typography variant="caption" color="text.secondary">
                            {formatTimeAgo(notification.createdAt)}
                          </Typography>
                        </Box>
                      }
                      secondary={
                        <Box>
                          <Typography variant="body2" color="text.secondary">
                            {notification.message}
                          </Typography>
                          {notification.relatedTicketId && (
                            <Chip 
                              label={`Ticket: ${notification.relatedTicketId}`}
                              size="small"
                              variant="outlined"
                              sx={{ mt: 1 }}
                            />
                          )}
                        </Box>
                      }
                    />
                    <Box sx={{ display: 'flex', gap: 0.5 }}>
                      {!notification.isRead ? (
                        <Tooltip title="Mark as read">
                          <IconButton
                            size="small"
                            onClick={() => handleMarkAsRead(notification.id)}
                          >
                            <MarkEmailRead fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      ) : (
                        <Tooltip title="Mark as unread">
                          <IconButton
                            size="small"
                            onClick={() => handleMarkAsUnread(notification.id)}
                          >
                            <MarkAsUnread fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      )}
                      <Tooltip title="Delete">
                        <IconButton
                          size="small"
                          onClick={() => handleDeleteNotification(notification.id)}
                          color="error"
                        >
                          <Delete fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </ListItem>
                  {index < filteredNotifications.length - 1 && <Divider />}
                </React.Fragment>
              ))}
            </List>
          )}
        </TabPanel>
      </Card>

      {/* Settings Dialog */}
      <Dialog open={settingsOpen} onClose={() => setSettingsOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Notification Settings</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
            <Alert severity="info">
              Configure how you want to receive notifications
            </Alert>
            
            <Typography variant="h6">Delivery Methods</Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.emailNotifications}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    emailNotifications: e.target.checked 
                  }))}
                />
              }
              label="Email Notifications"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.pushNotifications}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    pushNotifications: e.target.checked 
                  }))}
                />
              }
              label="Push Notifications"
            />

            <Divider />

            <Typography variant="h6">Notification Types</Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.ticketUpdates}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    ticketUpdates: e.target.checked 
                  }))}
                />
              }
              label="Ticket Updates"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.assignments}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    assignments: e.target.checked 
                  }))}
                />
              }
              label="Ticket Assignments"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.reminders}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    reminders: e.target.checked 
                  }))}
                />
              }
              label="Reminders"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={notificationSettings.systemAlerts}
                  onChange={(e) => setNotificationSettings(prev => ({ 
                    ...prev, 
                    systemAlerts: e.target.checked 
                  }))}
                />
              }
              label="System Alerts"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSettingsOpen(false)}>Cancel</Button>
          <Button 
            onClick={() => {
              // Save settings logic would go here
              setSettingsOpen(false);
            }}
            variant="contained"
          >
            Save Settings
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Notifications;
