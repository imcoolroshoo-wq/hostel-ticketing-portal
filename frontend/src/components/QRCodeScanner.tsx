import React, { useEffect, useRef, useState } from 'react';
import { Html5QrcodeScanner, Html5QrcodeScanType } from 'html5-qrcode';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Alert,
  IconButton,
  useTheme,
  useMediaQuery
} from '@mui/material';
import { Close as CloseIcon, QrCodeScanner as QrIcon } from '@mui/icons-material';

interface QRCodeScannerProps {
  open: boolean;
  onClose: () => void;
  onScan: (result: QRScanResult) => void;
  title?: string;
}

export interface QRScanResult {
  hostelBlock: string;
  roomNumber: string;
  location: string;
  assetId?: string;
  assetType?: string;
}

const QRCodeScanner: React.FC<QRCodeScannerProps> = ({
  open,
  onClose,
  onScan,
  title = "Scan QR Code"
}) => {
  const [scanner, setScanner] = useState<Html5QrcodeScanner | null>(null);
  const [error, setError] = useState<string>('');
  const [isScanning, setIsScanning] = useState(false);
  const scannerRef = useRef<HTMLDivElement>(null);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  useEffect(() => {
    if (open && scannerRef.current) {
      initializeScanner();
    }

    return () => {
      if (scanner) {
        scanner.clear().catch(console.error);
      }
    };
  }, [open]);

  const initializeScanner = () => {
    if (!scannerRef.current) return;

    const config = {
      fps: 10,
      qrbox: isMobile ? { width: 250, height: 250 } : { width: 300, height: 300 },
      aspectRatio: 1.0,
      disableFlip: false,
      supportedScanTypes: [Html5QrcodeScanType.SCAN_TYPE_CAMERA],
      showTorchButtonIfSupported: true,
      showZoomSliderIfSupported: true,
      defaultZoomValueIfSupported: 2,
    };

    const html5QrcodeScanner = new Html5QrcodeScanner(
      "qr-reader",
      config,
      false
    );

    html5QrcodeScanner.render(
      (decodedText) => {
        handleScanSuccess(decodedText);
      },
      (errorMessage) => {
        // Handle scan error - usually just no QR code found
        console.log('QR scan error:', errorMessage);
      }
    );

    setScanner(html5QrcodeScanner);
    setIsScanning(true);
    setError('');
  };

  const handleScanSuccess = (decodedText: string) => {
    try {
      // Parse QR code data
      const qrData = parseQRCode(decodedText);
      
      if (qrData) {
        onScan(qrData);
        handleClose();
      } else {
        setError('Invalid QR code format. Please scan a valid location QR code.');
      }
    } catch (err) {
      setError('Failed to parse QR code data.');
      console.error('QR parsing error:', err);
    }
  };

  const parseQRCode = (data: string): QRScanResult | null => {
    try {
      // Try parsing as JSON first (new format)
      const parsed = JSON.parse(data);
      if (parsed.hostelBlock && parsed.roomNumber) {
        return {
          hostelBlock: parsed.hostelBlock,
          roomNumber: parsed.roomNumber,
          location: parsed.location || `${parsed.hostelBlock} - Room ${parsed.roomNumber}`,
          assetId: parsed.assetId,
          assetType: parsed.assetType
        };
      }
    } catch {
      // Try parsing legacy format: "BUILDING|ROOM|LOCATION"
      const parts = data.split('|');
      if (parts.length >= 3) {
        return {
          hostelBlock: parts[0],
          roomNumber: parts[1],
          location: parts[2],
          assetId: parts[3],
          assetType: parts[4]
        };
      }

      // Try simple format: "Building Room"
      const match = data.match(/^([A-Za-z\s]+)\s+(\d+[A-Za-z]?)$/);
      if (match) {
        return {
          hostelBlock: match[1].trim(),
          roomNumber: match[2],
          location: `${match[1].trim()} - Room ${match[2]}`
        };
      }
    }

    return null;
  };

  const handleClose = () => {
    if (scanner) {
      scanner.clear().catch(console.error);
      setScanner(null);
    }
    setIsScanning(false);
    setError('');
    onClose();
  };

  return (
    <Dialog 
      open={open} 
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      fullScreen={isMobile}
    >
      <DialogTitle sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <QrIcon />
          <Typography variant="h6">{title}</Typography>
        </Box>
        <IconButton onClick={handleClose} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        <Box sx={{ textAlign: 'center', mb: 2 }}>
          <Typography variant="body2" color="text.secondary">
            Position the QR code within the frame to scan
          </Typography>
        </Box>

        <Box 
          id="qr-reader" 
          ref={scannerRef}
          sx={{ 
            width: '100%',
            '& > div': {
              border: 'none !important'
            },
            '& video': {
              borderRadius: 2,
              maxWidth: '100%'
            }
          }}
        />

        {!isScanning && (
          <Box sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="body2" color="text.secondary">
              Camera is starting...
            </Typography>
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={handleClose} variant="outlined">
          Cancel
        </Button>
        <Button 
          onClick={() => {
            setError('');
            if (scanner) {
              scanner.clear().then(() => {
                initializeScanner();
              }).catch(console.error);
            }
          }}
          variant="contained"
          disabled={!isScanning}
        >
          Retry
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default QRCodeScanner;
