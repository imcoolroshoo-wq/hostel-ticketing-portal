import { createTheme } from '@mui/material/styles';

// IIM Trichy Brand Colors
const IIM_TRICHY_COLORS = {
  primary: {
    main: '#1565C0', // Deep Blue - Professional and trustworthy
    light: '#42A5F5',
    dark: '#0D47A1',
    contrastText: '#FFFFFF',
  },
  secondary: {
    main: '#FF6F00', // Orange - Energetic and warm
    light: '#FFB74D',
    dark: '#E65100',
    contrastText: '#FFFFFF',
  },
  accent: {
    main: '#2E7D32', // Green - Success and growth
    light: '#66BB6A',
    dark: '#1B5E20',
  },
  background: {
    default: '#FAFAFA',
    paper: '#FFFFFF',
    secondary: '#F5F5F5',
  },
  text: {
    primary: '#212121',
    secondary: '#757575',
    disabled: '#BDBDBD',
  },
  error: {
    main: '#D32F2F',
    light: '#EF5350',
    dark: '#C62828',
  },
  warning: {
    main: '#F57C00',
    light: '#FFB74D',
    dark: '#E65100',
  },
  info: {
    main: '#1976D2',
    light: '#64B5F6',
    dark: '#1565C0',
  },
  success: {
    main: '#388E3C',
    light: '#81C784',
    dark: '#2E7D32',
  },
};

// Custom typography for IIM Trichy
const typography = {
  fontFamily: [
    'Inter',
    'Roboto',
    '-apple-system',
    'BlinkMacSystemFont',
    '"Segoe UI"',
    'Arial',
    'sans-serif',
  ].join(','),
  h1: {
    fontSize: '2.5rem',
    fontWeight: 700,
    lineHeight: 1.2,
    letterSpacing: '-0.01562em',
  },
  h2: {
    fontSize: '2rem',
    fontWeight: 600,
    lineHeight: 1.3,
    letterSpacing: '-0.00833em',
  },
  h3: {
    fontSize: '1.75rem',
    fontWeight: 600,
    lineHeight: 1.4,
    letterSpacing: '0em',
  },
  h4: {
    fontSize: '1.5rem',
    fontWeight: 600,
    lineHeight: 1.4,
    letterSpacing: '0.00735em',
  },
  h5: {
    fontSize: '1.25rem',
    fontWeight: 600,
    lineHeight: 1.5,
    letterSpacing: '0em',
  },
  h6: {
    fontSize: '1.125rem',
    fontWeight: 600,
    lineHeight: 1.5,
    letterSpacing: '0.0075em',
  },
  subtitle1: {
    fontSize: '1rem',
    fontWeight: 500,
    lineHeight: 1.75,
    letterSpacing: '0.00938em',
  },
  subtitle2: {
    fontSize: '0.875rem',
    fontWeight: 500,
    lineHeight: 1.57,
    letterSpacing: '0.00714em',
  },
  body1: {
    fontSize: '1rem',
    fontWeight: 400,
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  body2: {
    fontSize: '0.875rem',
    fontWeight: 400,
    lineHeight: 1.43,
    letterSpacing: '0.01071em',
  },
  button: {
    fontSize: '0.875rem',
    fontWeight: 600,
    lineHeight: 1.75,
    letterSpacing: '0.02857em',
    textTransform: 'none' as const,
  },
  caption: {
    fontSize: '0.75rem',
    fontWeight: 400,
    lineHeight: 1.66,
    letterSpacing: '0.03333em',
  },
  overline: {
    fontSize: '0.75rem',
    fontWeight: 600,
    lineHeight: 2.66,
    letterSpacing: '0.08333em',
    textTransform: 'uppercase' as const,
  },
};

// Custom component styles
const components = {
  MuiButton: {
    styleOverrides: {
      root: {
        borderRadius: 8,
        textTransform: 'none' as const,
        fontWeight: 600,
        padding: '10px 24px',
        boxShadow: 'none',
        '&:hover': {
          boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
        },
      },
      contained: {
        '&:hover': {
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        },
      },
    },
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: 12,
        boxShadow: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)',
        '&:hover': {
          boxShadow: '0 3px 6px rgba(0,0,0,0.16), 0 3px 6px rgba(0,0,0,0.23)',
        },
        transition: 'box-shadow 0.3s ease-in-out',
      },
    },
  },
  MuiPaper: {
    styleOverrides: {
      root: {
        borderRadius: 8,
      },
      elevation1: {
        boxShadow: '0 1px 3px rgba(0,0,0,0.12), 0 1px 2px rgba(0,0,0,0.24)',
      },
    },
  },
  MuiAppBar: {
    styleOverrides: {
      root: {
        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
        backgroundColor: '#FFFFFF',
        color: '#212121',
      },
    },
  },
  MuiDrawer: {
    styleOverrides: {
      paper: {
        borderRight: '1px solid #E0E0E0',
        backgroundColor: '#FAFAFA',
      },
    },
  },
  MuiListItemButton: {
    styleOverrides: {
      root: {
        borderRadius: 8,
        margin: '2px 8px',
        '&.Mui-selected': {
          backgroundColor: IIM_TRICHY_COLORS.primary.main,
          color: '#FFFFFF',
          '&:hover': {
            backgroundColor: IIM_TRICHY_COLORS.primary.dark,
          },
          '& .MuiListItemIcon-root': {
            color: '#FFFFFF',
          },
        },
        '&:hover': {
          backgroundColor: 'rgba(21, 101, 192, 0.08)',
        },
      },
    },
  },
  MuiChip: {
    styleOverrides: {
      root: {
        borderRadius: 16,
        fontWeight: 500,
      },
    },
  },
  MuiTextField: {
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: 8,
        },
      },
    },
  },
  MuiAlert: {
    styleOverrides: {
      root: {
        borderRadius: 8,
      },
    },
  },
};

// Create the IIM Trichy theme
export const iimTrichyTheme = createTheme({
  palette: {
    mode: 'light',
    primary: IIM_TRICHY_COLORS.primary,
    secondary: IIM_TRICHY_COLORS.secondary,
    background: IIM_TRICHY_COLORS.background,
    text: IIM_TRICHY_COLORS.text,
    error: IIM_TRICHY_COLORS.error,
    warning: IIM_TRICHY_COLORS.warning,
    info: IIM_TRICHY_COLORS.info,
    success: IIM_TRICHY_COLORS.success,
  },
  typography,
  components,
  shape: {
    borderRadius: 8,
  },
  spacing: 8,
  breakpoints: {
    values: {
      xs: 0,
      sm: 600,
      md: 960,
      lg: 1280,
      xl: 1920,
    },
  },
});

// Priority colors for tickets
export const PRIORITY_COLORS = {
  LOW: {
    color: IIM_TRICHY_COLORS.success.main,
    backgroundColor: 'rgba(46, 125, 50, 0.1)',
  },
  MEDIUM: {
    color: IIM_TRICHY_COLORS.warning.main,
    backgroundColor: 'rgba(245, 124, 0, 0.1)',
  },
  HIGH: {
    color: IIM_TRICHY_COLORS.secondary.main,
    backgroundColor: 'rgba(255, 111, 0, 0.1)',
  },
  EMERGENCY: {
    color: IIM_TRICHY_COLORS.error.main,
    backgroundColor: 'rgba(211, 47, 47, 0.1)',
  },
};

// Status colors for tickets
export const STATUS_COLORS = {
  OPEN: {
    color: IIM_TRICHY_COLORS.info.main,
    backgroundColor: 'rgba(25, 118, 210, 0.1)',
  },
  ASSIGNED: {
    color: IIM_TRICHY_COLORS.warning.main,
    backgroundColor: 'rgba(245, 124, 0, 0.1)',
  },
  IN_PROGRESS: {
    color: IIM_TRICHY_COLORS.secondary.main,
    backgroundColor: 'rgba(255, 111, 0, 0.1)',
  },
  ON_HOLD: {
    color: '#757575',
    backgroundColor: 'rgba(117, 117, 117, 0.1)',
  },
  RESOLVED: {
    color: IIM_TRICHY_COLORS.success.main,
    backgroundColor: 'rgba(46, 125, 50, 0.1)',
  },
  CLOSED: {
    color: '#424242',
    backgroundColor: 'rgba(66, 66, 66, 0.1)',
  },
  CANCELLED: {
    color: IIM_TRICHY_COLORS.error.main,
    backgroundColor: 'rgba(211, 47, 47, 0.1)',
  },
  REOPENED: {
    color: IIM_TRICHY_COLORS.primary.main,
    backgroundColor: 'rgba(21, 101, 192, 0.1)',
  },
};

// Category colors
export const CATEGORY_COLORS = {
  ELECTRICAL_ISSUES: '#FF9800',
  PLUMBING_WATER: '#2196F3',
  HVAC: '#9C27B0',
  STRUCTURAL_CIVIL: '#795548',
  FURNITURE_FIXTURES: '#607D8B',
  NETWORK_INTERNET: '#3F51B5',
  COMPUTER_HARDWARE: '#009688',
  AUDIO_VISUAL_EQUIPMENT: '#E91E63',
  SECURITY_SYSTEMS: '#F44336',
  HOUSEKEEPING_CLEANLINESS: '#4CAF50',
  SAFETY_SECURITY: '#FF5722',
  LANDSCAPING_OUTDOOR: '#8BC34A',
  GENERAL: '#9E9E9E',
};

export default iimTrichyTheme;
