package com.hostel.controller;

import com.hostel.entity.Ticket;
import com.hostel.entity.User;
import com.hostel.service.QualityAssuranceService;
import com.hostel.service.TicketService;
import com.hostel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Quality Assurance Controller for ticket quality management
 * Implements QA endpoints as per Product Design Document
 */
@RestController
@RequestMapping("/api/quality")
@CrossOrigin(origins = {"http://localhost:3000", "https://hostel-ticketing-frontend.onrender.com"})
public class QualityAssuranceController {

    @Autowired
    private QualityAssuranceService qualityAssuranceService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    /**
     * Process student feedback on resolved ticket
     */
    @PostMapping("/feedback/{ticketId}")
    public ResponseEntity<?> processStudentFeedback(
            @PathVariable UUID ticketId,
            @RequestBody FeedbackRequest request) {
        try {
            Ticket ticket = ticketService.getTicketByIdDirect(ticketId);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            boolean isResolved = qualityAssuranceService.processStudentFeedback(
                ticket, 
                request.getSatisfactionRating(), 
                request.getFeedback(), 
                request.getIsResolved()
            );

            return ResponseEntity.ok(new FeedbackResponse(
                "Feedback processed successfully", 
                isResolved ? "Ticket closed" : "Ticket reopened for further work"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing feedback: " + e.getMessage());
        }
    }

    /**
     * Get quality metrics for a staff member
     */
    @GetMapping("/metrics/staff/{staffId}")
    public ResponseEntity<?> getStaffQualityMetrics(@PathVariable UUID staffId) {
        try {
            User staff = userService.getUserByIdDirect(staffId);
            if (staff == null) {
                return ResponseEntity.notFound().build();
            }

            QualityAssuranceService.QualityMetrics metrics = 
                qualityAssuranceService.calculateStaffQualityMetrics(staff);

            return ResponseEntity.ok(metrics);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating quality metrics: " + e.getMessage());
        }
    }

    /**
     * Validate photo documentation for a ticket
     */
    @PostMapping("/validate-photos/{ticketId}")
    public ResponseEntity<?> validatePhotoDocumentation(@PathVariable UUID ticketId) {
        try {
            Ticket ticket = ticketService.getTicketByIdDirect(ticketId);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            boolean isValid = qualityAssuranceService.validatePhotoDocumentation(ticket);

            return ResponseEntity.ok(new ValidationResponse(
                isValid,
                isValid ? "Photo documentation is complete" : "Missing required photo documentation"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error validating photos: " + e.getMessage());
        }
    }

    /**
     * Validate resolution time for a ticket
     */
    @PostMapping("/validate-time/{ticketId}")
    public ResponseEntity<?> validateResolutionTime(@PathVariable UUID ticketId) {
        try {
            Ticket ticket = ticketService.getTicketByIdDirect(ticketId);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            qualityAssuranceService.validateResolutionTime(ticket);

            return ResponseEntity.ok(new ValidationResponse(
                true,
                "Resolution time validation completed"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error validating resolution time: " + e.getMessage());
        }
    }

    /**
     * Trigger recurring issue detection
     */
    @PostMapping("/detect-recurring-issues")
    public ResponseEntity<?> detectRecurringIssues() {
        try {
            qualityAssuranceService.detectRecurringIssues();
            return ResponseEntity.ok("Recurring issue detection completed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error detecting recurring issues: " + e.getMessage());
        }
    }

    /**
     * Process automatic closure of verified tickets
     */
    @PostMapping("/auto-close-verified")
    public ResponseEntity<?> autoCloseVerifiedTickets() {
        try {
            qualityAssuranceService.autoCloseVerifiedTickets();
            return ResponseEntity.ok("Auto-closure process completed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error in auto-closure process: " + e.getMessage());
        }
    }

    // Data classes

    public static class FeedbackRequest {
        private Integer satisfactionRating;
        private String feedback;
        private Boolean isResolved;

        public Integer getSatisfactionRating() { return satisfactionRating; }
        public void setSatisfactionRating(Integer satisfactionRating) { this.satisfactionRating = satisfactionRating; }
        public String getFeedback() { return feedback; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public Boolean getIsResolved() { return isResolved; }
        public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }
    }

    public static class FeedbackResponse {
        private final String message;
        private final String status;

        public FeedbackResponse(String message, String status) {
            this.message = message;
            this.status = status;
        }

        public String getMessage() { return message; }
        public String getStatus() { return status; }
    }

    public static class ValidationResponse {
        private final boolean isValid;
        private final String message;

        public ValidationResponse(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        public boolean isValid() { return isValid; }
        public String getMessage() { return message; }
    }
}
