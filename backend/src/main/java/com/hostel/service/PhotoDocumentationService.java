package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import com.hostel.repository.TicketAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Photo Documentation Service implementing photo documentation requirements
 * as per IIM Trichy Hostel Ticket Management System Product Design Document Section 4.2.2
 */
@Service
public class PhotoDocumentationService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketAttachmentRepository ticketAttachmentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Get required photo types for a ticket category
     */
    public List<AttachmentType> getRequiredPhotoTypes(TicketCategory category) {
        List<AttachmentType> requiredTypes = new ArrayList<>();
        
        // Problem documentation is always recommended
        requiredTypes.add(AttachmentType.PROBLEM_PHOTO);
        
        // Before/after photos required for physical work
        if (requiresBeforeAfterPhotos(category)) {
            requiredTypes.add(AttachmentType.BEFORE_WORK_PHOTO);
            requiredTypes.add(AttachmentType.AFTER_WORK_PHOTO);
        }
        
        // Evidence photos for certain categories
        if (requiresEvidencePhotos(category)) {
            requiredTypes.add(AttachmentType.EVIDENCE_PHOTO);
        }
        
        return requiredTypes;
    }

    /**
     * Check if a ticket has all required photos
     */
    public PhotoComplianceResult checkPhotoCompliance(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        List<AttachmentType> requiredTypes = getRequiredPhotoTypes(ticket.getCategory());
        List<TicketAttachment> attachments = ticketAttachmentRepository.findByTicketId(ticketId);
        
        PhotoComplianceResult result = new PhotoComplianceResult();
        result.setTicketId(ticketId);
        result.setTicketStatus(ticket.getStatus());
        result.setCompliant(true);
        
        Map<AttachmentType, List<TicketAttachment>> attachmentsByType = attachments.stream()
                .filter(att -> att.getAttachmentType() != null)
                .collect(Collectors.groupingBy(TicketAttachment::getAttachmentType));
        
        List<String> missingPhotos = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        for (AttachmentType requiredType : requiredTypes) {
            List<TicketAttachment> typeAttachments = attachmentsByType.get(requiredType);
            
            if (typeAttachments == null || typeAttachments.isEmpty()) {
                missingPhotos.add(requiredType.getDisplayName());
                result.setCompliant(false);
            } else {
                // Validate photo quality and metadata
                for (TicketAttachment attachment : typeAttachments) {
                    if (!isPhotoQualityAcceptable(attachment)) {
                        recommendations.add("Improve quality of " + requiredType.getDisplayName());
                    }
                }
            }
        }
        
        // Check for before/after photo pairs
        if (requiresBeforeAfterPhotos(ticket.getCategory())) {
            boolean hasBeforePhoto = attachmentsByType.containsKey(AttachmentType.BEFORE_WORK_PHOTO);
            boolean hasAfterPhoto = attachmentsByType.containsKey(AttachmentType.AFTER_WORK_PHOTO);
            
            if (hasBeforePhoto && !hasAfterPhoto && ticket.getStatus() == TicketStatus.RESOLVED) {
                missingPhotos.add("After work photo is required before closure");
                result.setCompliant(false);
            }
        }
        
        result.setMissingPhotos(missingPhotos);
        result.setRecommendations(recommendations);
        
        return result;
    }

    /**
     * Upload and process photo documentation
     */
    public TicketAttachment uploadPhotoDocumentation(UUID ticketId, MultipartFile file, 
                                                   AttachmentType attachmentType, String description, 
                                                   User uploadedBy) throws IOException {
        
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        // Validate file type and size
        validatePhotoFile(file, attachmentType);
        
        // Store file
        String fileName = fileStorageService.storeFile(file, "tickets/" + ticketId + "/photos");
        
        // Extract metadata
        String metadata = extractPhotoMetadata(file);
        
        // Create attachment record
        TicketAttachment attachment = new TicketAttachment();
        attachment.setTicket(ticket);
        attachment.setFilename(fileName);
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setFilePath(fileName);
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());
        attachment.setUploadedBy(uploadedBy);
        attachment.setAttachmentType(attachmentType);
        attachment.setDescription(description);
        attachment.setPhotoMetadata(metadata);
        
        // Set photo type flags
        if (attachmentType == AttachmentType.BEFORE_WORK_PHOTO) {
            attachment.setIsBeforePhoto(true);
        } else if (attachmentType == AttachmentType.AFTER_WORK_PHOTO) {
            attachment.setIsAfterPhoto(true);
        }
        
        // Mark as required if it's a mandatory photo type
        attachment.setIsRequired(attachmentType.isRequiredForWorkType(ticket.getCategory()));
        
        attachment = ticketAttachmentRepository.save(attachment);
        
        // Update ticket history
        addPhotoUploadToHistory(ticket, attachment, uploadedBy);
        
        return attachment;
    }

    /**
     * Validate before/after photo pairs
     */
    public PhotoPairValidationResult validateBeforeAfterPhotos(UUID ticketId) {
        List<TicketAttachment> beforePhotos = ticketAttachmentRepository
                .findByTicketIdAndAttachmentType(ticketId, AttachmentType.BEFORE_WORK_PHOTO);
        List<TicketAttachment> afterPhotos = ticketAttachmentRepository
                .findByTicketIdAndAttachmentType(ticketId, AttachmentType.AFTER_WORK_PHOTO);
        
        PhotoPairValidationResult result = new PhotoPairValidationResult();
        result.setTicketId(ticketId);
        result.setHasBeforePhotos(!beforePhotos.isEmpty());
        result.setHasAfterPhotos(!afterPhotos.isEmpty());
        result.setBeforePhotoCount(beforePhotos.size());
        result.setAfterPhotoCount(afterPhotos.size());
        
        List<String> issues = new ArrayList<>();
        
        // Check timestamp consistency
        if (!beforePhotos.isEmpty() && !afterPhotos.isEmpty()) {
            LocalDateTime latestBefore = beforePhotos.stream()
                    .map(TicketAttachment::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            
            LocalDateTime earliestAfter = afterPhotos.stream()
                    .map(TicketAttachment::getCreatedAt)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            
            if (latestBefore != null && earliestAfter != null && 
                latestBefore.isAfter(earliestAfter)) {
                issues.add("After photos were taken before some before photos");
            }
        }
        
        // Check for missing pairs
        if (beforePhotos.isEmpty() && !afterPhotos.isEmpty()) {
            issues.add("After photos exist without corresponding before photos");
        }
        
        if (!beforePhotos.isEmpty() && afterPhotos.isEmpty()) {
            issues.add("Before photos exist without corresponding after photos");
        }
        
        result.setIssues(issues);
        result.setValid(issues.isEmpty());
        
        return result;
    }

    /**
     * Generate photo documentation report
     */
    public PhotoDocumentationReport generateReport(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        List<TicketAttachment> allAttachments = ticketAttachmentRepository.findByTicketId(ticketId);
        List<TicketAttachment> photoAttachments = allAttachments.stream()
                .filter(TicketAttachment::isDocumentationPhoto)
                .collect(Collectors.toList());
        
        PhotoDocumentationReport report = new PhotoDocumentationReport();
        report.setTicketId(ticketId);
        report.setTicketNumber(ticket.getTicketNumber());
        report.setTicketTitle(ticket.getTitle());
        report.setCategory(ticket.getCategory());
        report.setStatus(ticket.getStatus());
        report.setTotalPhotos(photoAttachments.size());
        
        // Group photos by type
        Map<AttachmentType, List<TicketAttachment>> photosByType = photoAttachments.stream()
                .collect(Collectors.groupingBy(TicketAttachment::getAttachmentType));
        
        report.setPhotosByType(photosByType);
        
        // Check compliance
        PhotoComplianceResult compliance = checkPhotoCompliance(ticketId);
        report.setCompliant(compliance.isCompliant());
        report.setMissingPhotos(compliance.getMissingPhotos());
        
        // Validate before/after pairs
        PhotoPairValidationResult pairValidation = validateBeforeAfterPhotos(ticketId);
        report.setPairValidation(pairValidation);
        
        // Calculate quality metrics
        double averageFileSize = photoAttachments.stream()
                .mapToLong(TicketAttachment::getFileSize)
                .average()
                .orElse(0.0);
        report.setAverageFileSizeKB(averageFileSize / 1024);
        
        // Photos with metadata
        long photosWithMetadata = photoAttachments.stream()
                .filter(p -> p.getPhotoMetadata() != null && !p.getPhotoMetadata().isEmpty())
                .count();
        report.setPhotosWithMetadata((int) photosWithMetadata);
        
        return report;
    }

    /**
     * Get photos required for work completion
     */
    public List<AttachmentType> getPhotosRequiredForCompletion(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        
        List<AttachmentType> required = new ArrayList<>();
        
        if (requiresBeforeAfterPhotos(ticket.getCategory())) {
            // Check if we have before photos
            boolean hasBeforePhoto = ticketAttachmentRepository
                    .existsByTicketIdAndAttachmentType(ticketId, AttachmentType.BEFORE_WORK_PHOTO);
            
            if (hasBeforePhoto) {
                // If work is being marked as resolved, after photo is required
                required.add(AttachmentType.AFTER_WORK_PHOTO);
            }
        }
        
        return required;
    }

    // Private helper methods
    
    private boolean requiresBeforeAfterPhotos(TicketCategory category) {
        return category == TicketCategory.ELECTRICAL_ISSUES ||
               category == TicketCategory.PLUMBING_WATER ||
               category == TicketCategory.HVAC ||
               category == TicketCategory.STRUCTURAL_CIVIL ||
               category == TicketCategory.FURNITURE_FIXTURES;
    }
    
    private boolean requiresEvidencePhotos(TicketCategory category) {
        return category == TicketCategory.SAFETY_SECURITY ||
               category == TicketCategory.STRUCTURAL_CIVIL;
    }
    
    private void validatePhotoFile(MultipartFile file, AttachmentType attachmentType) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        // Check file size
        if (file.getSize() > attachmentType.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        // Check file type
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Invalid filename");
        }
        
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        String[] allowedExtensions = attachmentType.getAllowedExtensions();
        
        boolean validExtension = false;
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                validExtension = true;
                break;
            }
        }
        
        if (!validExtension) {
            throw new IllegalArgumentException("File type not allowed for " + attachmentType.getDisplayName());
        }
    }
    
    private String extractPhotoMetadata(MultipartFile file) {
        // In a real implementation, this would extract EXIF data, GPS coordinates, etc.
        // For now, return basic metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("originalSize", file.getSize());
        metadata.put("contentType", file.getContentType());
        metadata.put("uploadTimestamp", LocalDateTime.now().toString());
        
        // TODO: Implement actual metadata extraction using libraries like metadata-extractor
        // metadata.put("gpsCoordinates", extractGPSCoordinates(file));
        // metadata.put("cameraInfo", extractCameraInfo(file));
        // metadata.put("imageResolution", extractImageResolution(file));
        
        return metadata.toString();
    }
    
    private boolean isPhotoQualityAcceptable(TicketAttachment attachment) {
        // Basic quality checks
        if (attachment.getFileSize() < 50 * 1024) { // Less than 50KB
            return false;
        }
        
        // TODO: Implement more sophisticated quality checks
        // - Image resolution analysis
        // - Blur detection
        // - Brightness/contrast analysis
        
        return true;
    }
    
    private void addPhotoUploadToHistory(Ticket ticket, TicketAttachment attachment, User uploadedBy) {
        // TODO: Add to ticket history
        // This would create a TicketHistory entry for the photo upload
    }

    // Inner classes for return types
    
    public static class PhotoComplianceResult {
        private UUID ticketId;
        private TicketStatus ticketStatus;
        private boolean compliant;
        private List<String> missingPhotos;
        private List<String> recommendations;
        
        // Getters and setters
        public UUID getTicketId() { return ticketId; }
        public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
        public TicketStatus getTicketStatus() { return ticketStatus; }
        public void setTicketStatus(TicketStatus ticketStatus) { this.ticketStatus = ticketStatus; }
        public boolean isCompliant() { return compliant; }
        public void setCompliant(boolean compliant) { this.compliant = compliant; }
        public List<String> getMissingPhotos() { return missingPhotos; }
        public void setMissingPhotos(List<String> missingPhotos) { this.missingPhotos = missingPhotos; }
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }
    
    public static class PhotoPairValidationResult {
        private UUID ticketId;
        private boolean hasBeforePhotos;
        private boolean hasAfterPhotos;
        private int beforePhotoCount;
        private int afterPhotoCount;
        private boolean valid;
        private List<String> issues;
        
        // Getters and setters
        public UUID getTicketId() { return ticketId; }
        public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
        public boolean isHasBeforePhotos() { return hasBeforePhotos; }
        public void setHasBeforePhotos(boolean hasBeforePhotos) { this.hasBeforePhotos = hasBeforePhotos; }
        public boolean isHasAfterPhotos() { return hasAfterPhotos; }
        public void setHasAfterPhotos(boolean hasAfterPhotos) { this.hasAfterPhotos = hasAfterPhotos; }
        public int getBeforePhotoCount() { return beforePhotoCount; }
        public void setBeforePhotoCount(int beforePhotoCount) { this.beforePhotoCount = beforePhotoCount; }
        public int getAfterPhotoCount() { return afterPhotoCount; }
        public void setAfterPhotoCount(int afterPhotoCount) { this.afterPhotoCount = afterPhotoCount; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<String> getIssues() { return issues; }
        public void setIssues(List<String> issues) { this.issues = issues; }
    }
    
    public static class PhotoDocumentationReport {
        private UUID ticketId;
        private String ticketNumber;
        private String ticketTitle;
        private TicketCategory category;
        private TicketStatus status;
        private int totalPhotos;
        private Map<AttachmentType, List<TicketAttachment>> photosByType;
        private boolean compliant;
        private List<String> missingPhotos;
        private PhotoPairValidationResult pairValidation;
        private double averageFileSizeKB;
        private int photosWithMetadata;
        
        // Getters and setters
        public UUID getTicketId() { return ticketId; }
        public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
        public String getTicketNumber() { return ticketNumber; }
        public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
        public String getTicketTitle() { return ticketTitle; }
        public void setTicketTitle(String ticketTitle) { this.ticketTitle = ticketTitle; }
        public TicketCategory getCategory() { return category; }
        public void setCategory(TicketCategory category) { this.category = category; }
        public TicketStatus getStatus() { return status; }
        public void setStatus(TicketStatus status) { this.status = status; }
        public int getTotalPhotos() { return totalPhotos; }
        public void setTotalPhotos(int totalPhotos) { this.totalPhotos = totalPhotos; }
        public Map<AttachmentType, List<TicketAttachment>> getPhotosByType() { return photosByType; }
        public void setPhotosByType(Map<AttachmentType, List<TicketAttachment>> photosByType) { this.photosByType = photosByType; }
        public boolean isCompliant() { return compliant; }
        public void setCompliant(boolean compliant) { this.compliant = compliant; }
        public List<String> getMissingPhotos() { return missingPhotos; }
        public void setMissingPhotos(List<String> missingPhotos) { this.missingPhotos = missingPhotos; }
        public PhotoPairValidationResult getPairValidation() { return pairValidation; }
        public void setPairValidation(PhotoPairValidationResult pairValidation) { this.pairValidation = pairValidation; }
        public double getAverageFileSizeKB() { return averageFileSizeKB; }
        public void setAverageFileSizeKB(double averageFileSizeKB) { this.averageFileSizeKB = averageFileSizeKB; }
        public int getPhotosWithMetadata() { return photosWithMetadata; }
        public void setPhotosWithMetadata(int photosWithMetadata) { this.photosWithMetadata = photosWithMetadata; }
    }
}
