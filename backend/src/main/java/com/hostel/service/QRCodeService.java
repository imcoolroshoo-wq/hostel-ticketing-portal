package com.hostel.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hostel.entity.Ticket;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes for tickets as per IIM Trichy Hostel Ticket Management System
 * Product Design Document Section 4.3.1
 */
@Service
public class QRCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final int QR_CODE_MARGIN = 2;

    /**
     * Generate QR code for a ticket containing ticket information
     */
    public byte[] generateTicketQRCode(Ticket ticket) throws WriterException, IOException {
        // Create QR code content with ticket information
        String qrContent = buildQRContent(ticket);
        
        // Generate QR code
        return generateQRCodeImage(qrContent, QR_CODE_SIZE, QR_CODE_SIZE);
    }

    /**
     * Generate QR code with custom content
     */
    public byte[] generateQRCode(String content) throws WriterException, IOException {
        return generateQRCodeImage(content, QR_CODE_SIZE, QR_CODE_SIZE);
    }

    /**
     * Generate QR code with custom size
     */
    public byte[] generateQRCode(String content, int width, int height) throws WriterException, IOException {
        return generateQRCodeImage(content, width, height);
    }

    /**
     * Build QR code content from ticket information
     */
    private String buildQRContent(Ticket ticket) {
        // Create a JSON-like structure for ticket information
        StringBuilder content = new StringBuilder();
        content.append("TICKET_ID:").append(ticket.getId()).append("|");
        content.append("TICKET_NUMBER:").append(ticket.getTicketNumber()).append("|");
        content.append("TITLE:").append(ticket.getTitle()).append("|");
        content.append("STATUS:").append(ticket.getStatus()).append("|");
        content.append("PRIORITY:").append(ticket.getPriority()).append("|");
        content.append("CATEGORY:").append(ticket.getEffectiveCategory()).append("|");
        content.append("LOCATION:").append(ticket.getHostelBlock()).append("-").append(ticket.getRoomNumber()).append("|");
        content.append("CREATED:").append(ticket.getCreatedAt()).append("|");
        
        if (ticket.getAssignedTo() != null) {
            content.append("ASSIGNED_TO:").append(ticket.getAssignedTo().getFullName()).append("|");
        }
        
        // Add URL for direct access
        content.append("URL:").append("/tickets/").append(ticket.getId());
        
        return content.toString();
    }

    /**
     * Generate QR code image as byte array
     */
    private byte[] generateQRCodeImage(String content, int width, int height) throws WriterException, IOException {
        // Set QR code generation hints
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, QR_CODE_MARGIN);

        // Generate QR code matrix
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        // Create buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        // Create graphics and fill background
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        // Draw QR code
        for (int i = 0; i < bitMatrix.getWidth(); i++) {
            for (int j = 0; j < bitMatrix.getHeight(); j++) {
                if (bitMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    /**
     * Parse QR code content to extract ticket information
     */
    public Map<String, String> parseQRContent(String qrContent) {
        Map<String, String> ticketInfo = new HashMap<>();
        
        if (qrContent != null && !qrContent.isEmpty()) {
            String[] parts = qrContent.split("\\|");
            for (String part : parts) {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length == 2) {
                    ticketInfo.put(keyValue[0], keyValue[1]);
                }
            }
        }
        
        return ticketInfo;
    }

    /**
     * Extract ticket ID from QR content
     */
    public String extractTicketId(String qrContent) {
        Map<String, String> ticketInfo = parseQRContent(qrContent);
        return ticketInfo.get("TICKET_ID");
    }

    /**
     * Extract ticket number from QR content
     */
    public String extractTicketNumber(String qrContent) {
        Map<String, String> ticketInfo = parseQRContent(qrContent);
        return ticketInfo.get("TICKET_NUMBER");
    }

    /**
     * Validate QR content format
     */
    public boolean isValidTicketQR(String qrContent) {
        try {
            Map<String, String> ticketInfo = parseQRContent(qrContent);
            return ticketInfo.containsKey("TICKET_ID") && 
                   ticketInfo.containsKey("TICKET_NUMBER") &&
                   ticketInfo.containsKey("TITLE");
        } catch (Exception e) {
            return false;
        }
    }
}
