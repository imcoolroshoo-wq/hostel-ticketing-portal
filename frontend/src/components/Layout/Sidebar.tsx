import React from 'react';
import {
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Typography,
  Box,
  Divider,
  Chip,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Dashboard,
  ConfirmationNumber,
  Add,
  People,
  Assessment,
  Notifications,
  Settings,
  AdminPanelSettings,
  Engineering,
  School,
  Home,
  Assignment,
  Category,
  Map,
  TrendingUp,
  SelectAll,
  Inventory,
} from '@mui/icons-material';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

interface SidebarProps {
  open: boolean;
  onClose: () => void;
  width: number;
}

interface MenuItem {
  text: string;
  icon: React.ReactNode;
  path: string;
  roles?: string[];
  badge?: number;
  divider?: boolean;
}

const Sidebar: React.FC<SidebarProps> = ({ open, onClose, width }) => {
  const theme = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const { user } = useAuth();

  // Define menu items based on roles
  const getMenuItems = (): MenuItem[] => {
    const baseItems: MenuItem[] = [
      {
        text: 'Dashboard',
        icon: <Dashboard />,
        path: '/dashboard',
      },
    ];

    // Student-specific items
    if (user?.role === 'STUDENT') {
      return [
        ...baseItems,
        {
          text: 'My Tickets',
          icon: <ConfirmationNumber />,
          path: '/tickets',
          badge: 2, // Example badge count
        },
        {
          text: 'Create Ticket',
          icon: <Add />,
          path: '/tickets/create',
        },
        {
          text: 'Notifications',
          icon: <Notifications />,
          path: '/notifications',
          badge: 3,
        },
        {
          text: 'Profile',
          icon: <School />,
          path: '/profile',
          divider: true,
        },
      ];
    }

    // Staff-specific items
    if (user?.role === 'STAFF') {
      return [
        ...baseItems,
        {
          text: 'Assigned Tickets',
          icon: <Assignment />,
          path: '/tickets',
          badge: 5,
        },
        {
          text: 'Reports',
          icon: <Assessment />,
          path: '/reports',
        },
        {
          text: 'Notifications',
          icon: <Notifications />,
          path: '/notifications',
          badge: 2,
        },
        {
          text: 'Profile',
          icon: <Engineering />,
          path: '/profile',
          divider: true,
        },
      ];
    }

    // Admin-specific items
    if (user?.role === 'ADMIN') {
      return [
        ...baseItems,
        {
          text: 'All Tickets',
          icon: <ConfirmationNumber />,
          path: '/tickets',
          badge: 12,
        },
        {
          text: 'User Management',
          icon: <People />,
          path: '/admin/users',
        },
        {
          text: 'Hostel Blocks',
          icon: <Home />,
          path: '/admin/hostel-blocks',
        },
        {
          text: 'Staff Mappings',
          icon: <Map />,
          path: '/admin/mappings',
        },
        {
          text: 'Categories',
          icon: <Category />,
          path: '/admin/categories',
        },
        {
          text: 'Reports & Analytics',
          icon: <Assessment />,
          path: '/reports',
        },
        {
          text: 'Escalation Management',
          icon: <TrendingUp />,
          path: '/admin/escalations',
        },
        {
          text: 'Bulk Operations',
          icon: <SelectAll />,
          path: '/admin/bulk-operations',
        },
        {
          text: 'Asset Management',
          icon: <Inventory />,
          path: '/admin/assets',
        },
        {
          text: 'System Settings',
          icon: <Settings />,
          path: '/admin/settings',
          divider: true,
        },
        {
          text: 'Notifications',
          icon: <Notifications />,
          path: '/notifications',
          badge: 8,
        },
      ];
    }

    return baseItems;
  };

  const menuItems = getMenuItems();

  const handleItemClick = (path: string) => {
    navigate(path);
    onClose();
  };

  const isSelected = (path: string) => {
    if (path === '/dashboard') {
      return location.pathname === '/' || location.pathname === '/dashboard';
    }
    return location.pathname.startsWith(path);
  };

  const getRoleInfo = () => {
    switch (user?.role) {
      case 'ADMIN':
        return {
          title: 'Administrator',
          subtitle: 'Full System Access',
          color: theme.palette.error.main,
          icon: <AdminPanelSettings />,
        };
      case 'STAFF':
        return {
          title: 'Staff Member',
          subtitle: user?.staffVertical || 'General Staff',
          color: theme.palette.warning.main,
          icon: <Engineering />,
        };
      case 'STUDENT':
        return {
          title: 'Student',
          subtitle: `Room ${user?.roomNumber || 'N/A'}, ${user?.hostelBlock || 'N/A'}`,
          color: theme.palette.primary.main,
          icon: <School />,
        };
      default:
        return {
          title: 'User',
          subtitle: 'System User',
          color: theme.palette.grey[500],
          icon: <School />,
        };
    }
  };

  const roleInfo = getRoleInfo();

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      {/* User Info Section */}
      <Box
        sx={{
          p: 2,
          backgroundColor: alpha(theme.palette.primary.main, 0.05),
          borderBottom: 1,
          borderColor: 'divider',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: '50%',
              backgroundColor: alpha(roleInfo.color, 0.1),
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: roleInfo.color,
            }}
          >
            {roleInfo.icon}
          </Box>
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography
              variant="subtitle2"
              sx={{
                fontWeight: 600,
                color: theme.palette.text.primary,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              }}
            >
              {user?.firstName} {user?.lastName}
            </Typography>
            <Typography
              variant="caption"
              sx={{
                color: theme.palette.text.secondary,
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                display: 'block',
              }}
            >
              {roleInfo.subtitle}
            </Typography>
          </Box>
        </Box>
        <Chip
          label={roleInfo.title}
          size="small"
          sx={{
            backgroundColor: alpha(roleInfo.color, 0.1),
            color: roleInfo.color,
            fontWeight: 600,
            fontSize: '0.75rem',
          }}
        />
      </Box>

      {/* Navigation Menu */}
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        <List sx={{ py: 1 }}>
          {menuItems.map((item, index) => (
            <React.Fragment key={item.path}>
              <ListItemButton
                selected={isSelected(item.path)}
                onClick={() => handleItemClick(item.path)}
                sx={{
                  mx: 1,
                  borderRadius: 2,
                  mb: 0.5,
                  '&.Mui-selected': {
                    backgroundColor: theme.palette.primary.main,
                    color: '#FFFFFF',
                    '&:hover': {
                      backgroundColor: theme.palette.primary.dark,
                    },
                    '& .MuiListItemIcon-root': {
                      color: '#FFFFFF',
                    },
                  },
                  '&:hover': {
                    backgroundColor: alpha(theme.palette.primary.main, 0.08),
                  },
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 40,
                    color: isSelected(item.path) ? '#FFFFFF' : theme.palette.text.secondary,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <ListItemText
                  primary={item.text}
                  primaryTypographyProps={{
                    fontSize: '0.875rem',
                    fontWeight: isSelected(item.path) ? 600 : 500,
                  }}
                />
                {item.badge && (
                  <Chip
                    label={item.badge}
                    size="small"
                    sx={{
                      height: 20,
                      fontSize: '0.75rem',
                      backgroundColor: isSelected(item.path)
                        ? alpha('#FFFFFF', 0.2)
                        : theme.palette.error.main,
                      color: isSelected(item.path) ? '#FFFFFF' : '#FFFFFF',
                      fontWeight: 600,
                    }}
                  />
                )}
              </ListItemButton>
              {item.divider && <Divider sx={{ my: 1, mx: 2 }} />}
            </React.Fragment>
          ))}
        </List>
      </Box>

      {/* Footer */}
      <Box
        sx={{
          p: 2,
          borderTop: 1,
          borderColor: 'divider',
          backgroundColor: alpha(theme.palette.grey[50], 0.5),
        }}
      >
        <Typography
          variant="caption"
          sx={{
            color: theme.palette.text.secondary,
            textAlign: 'center',
            display: 'block',
            fontWeight: 500,
          }}
        >
          IIM Trichy Hostel Management
        </Typography>
        <Typography
          variant="caption"
          sx={{
            color: theme.palette.text.disabled,
            textAlign: 'center',
            display: 'block',
            fontSize: '0.7rem',
          }}
        >
          Version 1.0.0
        </Typography>
      </Box>
    </Box>
  );

  return (
    <Drawer
      variant="temporary"
      open={open}
      onClose={onClose}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile.
      }}
      sx={{
        display: { xs: 'block', sm: 'none' },
        '& .MuiDrawer-paper': {
          boxSizing: 'border-box',
          width: width,
          backgroundColor: '#FAFAFA',
          borderRight: `1px solid ${theme.palette.divider}`,
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
};

export default Sidebar;