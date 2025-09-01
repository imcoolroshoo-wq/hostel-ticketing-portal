import React from 'react';
import {
  Box,
  Container,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  CardActions,
  useTheme,
  alpha,
  Stack,
  Chip,
} from '@mui/material';
import {
  School,
  ConfirmationNumber,
  Speed,
  Security,
  Support,
  Analytics,
  ArrowForward,
  CheckCircle,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const LandingPage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();

  const features = [
    {
      icon: <ConfirmationNumber sx={{ fontSize: 40 }} />,
      title: 'Smart Ticket Management',
      description: 'Intelligent assignment system that routes tickets to the right staff based on expertise and workload.',
      color: theme.palette.primary.main,
    },
    {
      icon: <Speed sx={{ fontSize: 40 }} />,
      title: 'Quick Resolution',
      description: 'Average resolution time reduced by 60% with automated workflows and priority-based assignment.',
      color: theme.palette.secondary.main,
    },
    {
      icon: <Security sx={{ fontSize: 40 }} />,
      title: 'Role-Based Access',
      description: 'Secure system with strict role-based access control ensuring data privacy and operational security.',
      color: theme.palette.success.main,
    },
    {
      icon: <Support sx={{ fontSize: 40 }} />,
      title: '24/7 Support',
      description: 'Round-the-clock support for emergency issues with immediate notification and escalation.',
      color: theme.palette.warning.main,
    },
    {
      icon: <Analytics sx={{ fontSize: 40 }} />,
      title: 'Real-time Analytics',
      description: 'Comprehensive dashboards and reports for tracking performance and identifying improvement areas.',
      color: theme.palette.info.main,
    },
    {
      icon: <CheckCircle sx={{ fontSize: 40 }} />,
      title: 'Quality Assurance',
      description: 'Built-in quality checks and satisfaction tracking to ensure excellent service delivery.',
      color: theme.palette.error.main,
    },
  ];

  const stats = [
    { value: '90%+', label: 'Student Satisfaction' },
    { value: '60%', label: 'Faster Resolution' },
    { value: '40%', label: 'Staff Efficiency Gain' },
    { value: '95%', label: 'System Adoption' },
  ];

  const categories = [
    'Electrical Issues',
    'Plumbing & Water',
    'HVAC',
    'IT Support',
    'Housekeeping',
    'Security',
    'Furniture',
    'General',
  ];

  return (
    <Box sx={{ minHeight: '100vh', backgroundColor: theme.palette.background.default }}>
      {/* Hero Section */}
      <Box
        sx={{
          background: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
          color: 'white',
          py: 12,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        {/* Background Pattern */}
        <Box
          sx={{
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundImage: `radial-gradient(circle at 25% 25%, ${alpha('#FFFFFF', 0.1)} 0%, transparent 50%)`,
          }}
        />
        
        <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 1 }}>
          <Grid container spacing={6} alignItems="center">
            <Grid item xs={12} md={6}>
              <Box sx={{ mb: 3 }}>
                <Chip
                  label="IIM Trichy Official"
                  sx={{
                    backgroundColor: alpha('#FFFFFF', 0.2),
                    color: 'white',
                    fontWeight: 600,
                    mb: 2,
                  }}
                />
              </Box>
              
              <Typography
                variant="h2"
                sx={{
                  fontWeight: 700,
                  mb: 3,
                  fontSize: { xs: '2.5rem', md: '3.5rem' },
                  lineHeight: 1.1,
                }}
              >
                IIM Trichy
                <br />
                <Typography
                  component="span"
                  variant="h2"
                  sx={{
                    fontWeight: 700,
                    fontSize: { xs: '2.5rem', md: '3.5rem' },
                    color: theme.palette.secondary.main,
                  }}
                >
                  Hostel Management
                </Typography>
              </Typography>
              
              <Typography
                variant="h5"
                sx={{
                  mb: 4,
                  opacity: 0.9,
                  fontWeight: 400,
                  lineHeight: 1.4,
                }}
              >
                Streamlined ticket management system for efficient hostel operations and enhanced student experience.
              </Typography>
              
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <Button
                  variant="contained"
                  size="large"
                  onClick={() => navigate('/login')}
                  endIcon={<ArrowForward />}
                  sx={{
                    backgroundColor: theme.palette.secondary.main,
                    color: 'white',
                    py: 1.5,
                    px: 4,
                    fontSize: '1.1rem',
                    fontWeight: 600,
                    '&:hover': {
                      backgroundColor: theme.palette.secondary.dark,
                    },
                  }}
                >
                  Access Portal
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  onClick={() => {
                    // Scroll to features section
                    const featuresSection = document.querySelector('[data-section="features"]');
                    if (featuresSection) {
                      featuresSection.scrollIntoView({ behavior: 'smooth' });
                    }
                  }}
                  sx={{
                    borderColor: 'white',
                    color: 'white',
                    py: 1.5,
                    px: 4,
                    fontSize: '1.1rem',
                    fontWeight: 600,
                    '&:hover': {
                      borderColor: 'white',
                      backgroundColor: alpha('#FFFFFF', 0.1),
                    },
                  }}
                >
                  Learn More
                </Button>
              </Stack>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <Box
                sx={{
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  height: 400,
                }}
              >
                <School
                  sx={{
                    fontSize: 300,
                    color: alpha('#FFFFFF', 0.2),
                  }}
                />
              </Box>
            </Grid>
          </Grid>
        </Container>
      </Box>

      {/* Stats Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Grid container spacing={4}>
          {stats.map((stat, index) => (
            <Grid item xs={6} md={3} key={index}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography
                  variant="h3"
                  sx={{
                    fontWeight: 700,
                    color: theme.palette.primary.main,
                    mb: 1,
                  }}
                >
                  {stat.value}
                </Typography>
                <Typography
                  variant="body1"
                  sx={{
                    color: theme.palette.text.secondary,
                    fontWeight: 500,
                  }}
                >
                  {stat.label}
                </Typography>
              </Box>
            </Grid>
          ))}
        </Grid>
      </Container>

      {/* Features Section */}
      <Box data-section="features" sx={{ backgroundColor: alpha(theme.palette.primary.main, 0.02), py: 8 }}>
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center', mb: 6 }}>
            <Typography
              variant="h3"
              sx={{
                fontWeight: 700,
                mb: 2,
                color: theme.palette.text.primary,
              }}
            >
              Comprehensive Solution
            </Typography>
            <Typography
              variant="h6"
              sx={{
                color: theme.palette.text.secondary,
                maxWidth: 600,
                mx: 'auto',
                lineHeight: 1.6,
              }}
            >
              Built specifically for IIM Trichy's hostel infrastructure with intelligent automation and user-centric design.
            </Typography>
          </Box>
          
          <Grid container spacing={4}>
            {features.map((feature, index) => (
              <Grid item xs={12} md={6} lg={4} key={index}>
                <Card
                  sx={{
                    height: '100%',
                    transition: 'all 0.3s ease-in-out',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: theme.shadows[8],
                    },
                  }}
                >
                  <CardContent sx={{ p: 3 }}>
                    <Box
                      sx={{
                        width: 64,
                        height: 64,
                        borderRadius: 2,
                        backgroundColor: alpha(feature.color, 0.1),
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        mb: 2,
                        color: feature.color,
                      }}
                    >
                      {feature.icon}
                    </Box>
                    <Typography
                      variant="h6"
                      sx={{
                        fontWeight: 600,
                        mb: 1,
                        color: theme.palette.text.primary,
                      }}
                    >
                      {feature.title}
                    </Typography>
                    <Typography
                      variant="body2"
                      sx={{
                        color: theme.palette.text.secondary,
                        lineHeight: 1.6,
                      }}
                    >
                      {feature.description}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>

      {/* Categories Section */}
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Box sx={{ textAlign: 'center', mb: 6 }}>
          <Typography
            variant="h3"
            sx={{
              fontWeight: 700,
              mb: 2,
              color: theme.palette.text.primary,
            }}
          >
            Service Categories
          </Typography>
          <Typography
            variant="h6"
            sx={{
              color: theme.palette.text.secondary,
              maxWidth: 600,
              mx: 'auto',
              lineHeight: 1.6,
            }}
          >
            Comprehensive coverage of all hostel maintenance and service requirements.
          </Typography>
        </Box>
        
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, justifyContent: 'center' }}>
          {categories.map((category, index) => (
            <Chip
              key={index}
              label={category}
              sx={{
                py: 2,
                px: 3,
                fontSize: '1rem',
                fontWeight: 500,
                backgroundColor: alpha(theme.palette.primary.main, 0.1),
                color: theme.palette.primary.main,
                '&:hover': {
                  backgroundColor: alpha(theme.palette.primary.main, 0.2),
                },
              }}
            />
          ))}
        </Box>
      </Container>

      {/* CTA Section */}
      <Box
        sx={{
          backgroundColor: theme.palette.primary.main,
          color: 'white',
          py: 8,
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center' }}>
            <Typography
              variant="h3"
              sx={{
                fontWeight: 700,
                mb: 2,
              }}
            >
              Ready to Get Started?
            </Typography>
            <Typography
              variant="h6"
              sx={{
                mb: 4,
                opacity: 0.9,
                maxWidth: 600,
                mx: 'auto',
              }}
            >
              Access the IIM Trichy Hostel Management System and experience streamlined operations.
            </Typography>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate('/login')}
              endIcon={<ArrowForward />}
              sx={{
                backgroundColor: theme.palette.secondary.main,
                color: 'white',
                py: 1.5,
                px: 4,
                fontSize: '1.1rem',
                fontWeight: 600,
                '&:hover': {
                  backgroundColor: theme.palette.secondary.dark,
                },
              }}
            >
              Access Portal
            </Button>
          </Box>
        </Container>
      </Box>

      {/* Footer */}
      <Box
        sx={{
          backgroundColor: theme.palette.grey[900],
          color: 'white',
          py: 4,
        }}
      >
        <Container maxWidth="lg">
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="body2" sx={{ mb: 1 }}>
              © 2024 Indian Institute of Management Tiruchirappalli
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.7 }}>
              Hostel Management System - Version 1.0.0
            </Typography>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};

export default LandingPage;