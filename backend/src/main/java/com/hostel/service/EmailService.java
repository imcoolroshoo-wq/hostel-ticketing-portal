package com.hostel.service;

import com.hostel.entity.NotificationType;
import com.hostel.entity.Ticket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

/**
 * Email Service for sending notification emails
 * Implementation of email notifications as per Product Design Document
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    /**
     * Send notification email
     */
    @Async
    public CompletableFuture<Void> sendNotificationEmail(String to, String title, String message, 
                                                        NotificationType type, Ticket relatedTicket) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(String.format("[IIM Trichy Hostel] %s", title));
            helper.setFrom("noreply@iimtrichy.ac.in");
            
            String htmlContent = buildEmailContent(title, message, type, relatedTicket);
            helper.setText(htmlContent, true);
            
            emailSender.send(mimeMessage);
            
        } catch (MessagingException e) {
            // Log error and possibly retry
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send simple text email
     */
    @Async
    public CompletableFuture<Void> sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("noreply@iimtrichy.ac.in");
            
            emailSender.send(message);
            
        } catch (Exception e) {
            System.err.println("Failed to send simple email to " + to + ": " + e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Build HTML email content
     */
    private String buildEmailContent(String title, String message, NotificationType type, Ticket relatedTicket) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<title>").append(title).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; }");
        html.append(".ticket-info { background-color: #f8f9fa; padding: 15px; border-left: 4px solid #007bff; margin: 15px 0; }");
        html.append(".priority-emergency { border-left-color: #dc3545; }");
        html.append(".priority-high { border-left-color: #fd7e14; }");
        html.append(".priority-medium { border-left-color: #ffc107; }");
        html.append(".priority-low { border-left-color: #28a745; }");
        html.append(".footer { background-color: #f8f9fa; padding: 15px; text-align: center; font-size: 12px; color: #6c757d; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>").append(type.getIcon()).append(" ").append(title).append("</h1>");
        html.append("</div>");
        
        // Content
        html.append("<div class='content'>");
        
        // Main message
        html.append("<p>").append(message.replaceAll("\n", "<br>")).append("</p>");
        
        // Ticket information (if available)
        if (relatedTicket != null) {
            String priorityClass = "priority-" + relatedTicket.getPriority().name().toLowerCase();
            html.append("<div class='ticket-info ").append(priorityClass).append("'>");
            html.append("<h3>Ticket Details</h3>");
            html.append("<p><strong>Ticket Number:</strong> ").append(relatedTicket.getTicketNumber()).append("</p>");
            html.append("<p><strong>Title:</strong> ").append(relatedTicket.getTitle()).append("</p>");
            html.append("<p><strong>Status:</strong> ").append(relatedTicket.getStatus().getDisplayName()).append("</p>");
            html.append("<p><strong>Priority:</strong> ").append(relatedTicket.getPriority().getDisplayName()).append("</p>");
            html.append("<p><strong>Category:</strong> ").append(relatedTicket.getEffectiveCategory()).append("</p>");
            html.append("<p><strong>Location:</strong> ").append(relatedTicket.getHostelBlock()).append(" - ").append(relatedTicket.getRoomNumber()).append("</p>");
            html.append("</div>");
        }
        
        // Call to action
        if (type == NotificationType.TICKET_ASSIGNMENT || type == NotificationType.ESCALATION) {
            html.append("<p style='background-color: #d4edda; padding: 10px; border-radius: 5px;'>");
            html.append("<strong>Action Required:</strong> Please log into the hostel management system to review and respond to this ticket.");
            html.append("</p>");
        }
        
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated notification from the IIM Trichy Hostel Management System.</p>");
        html.append("<p>Please do not reply to this email. For support, contact the hostel administration.</p>");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }
}
