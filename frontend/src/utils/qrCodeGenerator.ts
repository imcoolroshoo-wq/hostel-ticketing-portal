import QRCode from 'qrcode';

export interface LocationData {
  hostelBlock: string;
  roomNumber: string;
  location?: string;
  assetId?: string;
  assetType?: string;
  floor?: string;
  zone?: string;
}

export class QRCodeGenerator {
  /**
   * Generate QR code data URL for a location
   */
  static async generateLocationQR(locationData: LocationData): Promise<string> {
    const qrData = JSON.stringify({
      hostelBlock: locationData.hostelBlock,
      roomNumber: locationData.roomNumber,
      location: locationData.location || `${locationData.hostelBlock} - Room ${locationData.roomNumber}`,
      assetId: locationData.assetId,
      assetType: locationData.assetType,
      floor: locationData.floor,
      zone: locationData.zone,
      type: 'HOSTEL_LOCATION',
      generated: new Date().toISOString()
    });

    try {
      const qrCodeDataURL = await QRCode.toDataURL(qrData, {
        errorCorrectionLevel: 'M' as const,
        margin: 1,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        },
        width: 256
      });

      return qrCodeDataURL;
    } catch (error) {
      console.error('Error generating QR code:', error);
      throw new Error('Failed to generate QR code');
    }
  }

  /**
   * Generate QR code for asset tracking
   */
  static async generateAssetQR(assetData: {
    assetId: string;
    assetType: string;
    hostelBlock: string;
    roomNumber?: string;
    description?: string;
  }): Promise<string> {
    const qrData = JSON.stringify({
      assetId: assetData.assetId,
      assetType: assetData.assetType,
      hostelBlock: assetData.hostelBlock,
      roomNumber: assetData.roomNumber,
      description: assetData.description,
      type: 'HOSTEL_ASSET',
      generated: new Date().toISOString()
    });

    try {
      const qrCodeDataURL = await QRCode.toDataURL(qrData, {
        errorCorrectionLevel: 'H' as const,
        margin: 1,
        color: {
          dark: '#1976d2',
          light: '#FFFFFF'
        },
        width: 256
      });

      return qrCodeDataURL;
    } catch (error) {
      console.error('Error generating asset QR code:', error);
      throw new Error('Failed to generate asset QR code');
    }
  }

  /**
   * Generate batch QR codes for multiple locations
   */
  static async generateBatchLocationQRs(locations: LocationData[]): Promise<Array<{
    location: LocationData;
    qrCode: string;
  }>> {
    const results = [];

    for (const location of locations) {
      try {
        const qrCode = await this.generateLocationQR(location);
        results.push({ location, qrCode });
      } catch (error) {
        console.error(`Failed to generate QR for ${location.hostelBlock} ${location.roomNumber}:`, error);
      }
    }

    return results;
  }

  /**
   * Download QR code as image file
   */
  static downloadQRCode(dataURL: string, filename: string): void {
    const link = document.createElement('a');
    link.download = filename;
    link.href = dataURL;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  /**
   * Generate QR codes for all rooms in a building
   */
  static async generateBuildingQRs(
    hostelBlock: string,
    floors: number,
    roomsPerFloor: number,
    startingRoom: number = 1
  ): Promise<Array<{ location: LocationData; qrCode: string }>> {
    const locations: LocationData[] = [];

    for (let floor = 1; floor <= floors; floor++) {
      for (let room = 0; room < roomsPerFloor; room++) {
        const roomNumber = `${floor}${String(startingRoom + room).padStart(2, '0')}`;
        locations.push({
          hostelBlock,
          roomNumber,
          floor: floor.toString(),
          location: `${hostelBlock} - Floor ${floor} - Room ${roomNumber}`
        });
      }
    }

    return this.generateBatchLocationQRs(locations);
  }

  /**
   * Print QR codes in a grid layout
   */
  static printQRCodes(qrCodes: Array<{ location: LocationData; qrCode: string }>): void {
    const printWindow = window.open('', '_blank');
    if (!printWindow) return;

    const html = `
      <!DOCTYPE html>
      <html>
      <head>
        <title>Location QR Codes</title>
        <style>
          body { 
            font-family: Arial, sans-serif; 
            margin: 20px;
            print-color-adjust: exact;
            -webkit-print-color-adjust: exact;
          }
          .qr-grid { 
            display: grid; 
            grid-template-columns: repeat(3, 1fr); 
            gap: 20px; 
            page-break-inside: avoid;
          }
          .qr-item { 
            text-align: center; 
            border: 1px solid #ddd; 
            padding: 15px; 
            border-radius: 8px;
            page-break-inside: avoid;
          }
          .qr-item img { 
            width: 150px; 
            height: 150px; 
            margin-bottom: 10px;
          }
          .qr-item h3 { 
            margin: 0 0 5px 0; 
            font-size: 14px;
            color: #333;
          }
          .qr-item p { 
            margin: 0; 
            font-size: 12px; 
            color: #666;
          }
          @media print {
            .qr-grid { grid-template-columns: repeat(2, 1fr); }
            .qr-item { margin-bottom: 20px; }
          }
        </style>
      </head>
      <body>
        <h1>Hostel Location QR Codes</h1>
        <div class="qr-grid">
          ${qrCodes.map(({ location, qrCode }) => `
            <div class="qr-item">
              <img src="${qrCode}" alt="QR Code for ${location.hostelBlock} ${location.roomNumber}" />
              <h3>${location.hostelBlock} - Room ${location.roomNumber}</h3>
              <p>${location.location}</p>
            </div>
          `).join('')}
        </div>
      </body>
      </html>
    `;

    printWindow.document.write(html);
    printWindow.document.close();
    printWindow.focus();
    
    // Auto-print after a short delay
    setTimeout(() => {
      printWindow.print();
    }, 1000);
  }
}

export default QRCodeGenerator;
