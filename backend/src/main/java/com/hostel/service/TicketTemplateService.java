package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketTemplateRepository;
import com.hostel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service for managing ticket templates
 * Implements Template System as per PDD Section 4.1.4
 */
@Service
@Transactional
public class TicketTemplateService {
    
    @Autowired
    private TicketTemplateRepository ticketTemplateRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Create a new ticket template
     */
    public TicketTemplate createTemplate(TicketTemplate template, UUID createdById) {
        User creator = userRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        // Only admins and staff can create templates
        if (!creator.canManageTickets()) {
            throw new RuntimeException("Insufficient permissions to create templates");
        }
        
        template.setCreatedBy(creator);
        template.setIsActive(true);
        template.setUsageCount(0);
        
        TicketTemplate savedTemplate = ticketTemplateRepository.save(template);
        
        // Notify admins about new template
        notificationService.sendAdminNotification(
            "New Ticket Template Created",
            String.format("Template '%s' has been created by %s for category %s",
                template.getName(), creator.getFullName(), template.getCategory().getDisplayName()),
            NotificationType.SYSTEM,
            null
        );
        
        return savedTemplate;
    }
    
    /**
     * Update an existing template
     */
    public TicketTemplate updateTemplate(UUID templateId, TicketTemplate updatedTemplate, UUID updatedById) {
        User updater = userRepository.findById(updatedById)
                .orElseThrow(() -> new RuntimeException("Updater not found"));
        
        // Only admins and staff can update templates
        if (!updater.canManageTickets()) {
            throw new RuntimeException("Insufficient permissions to update templates");
        }
        
        TicketTemplate existingTemplate = ticketTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        // Update fields
        existingTemplate.setName(updatedTemplate.getName());
        existingTemplate.setDescription(updatedTemplate.getDescription());
        existingTemplate.setTitleTemplate(updatedTemplate.getTitleTemplate());
        existingTemplate.setDescriptionTemplate(updatedTemplate.getDescriptionTemplate());
        existingTemplate.setCategory(updatedTemplate.getCategory());
        existingTemplate.setDefaultPriority(updatedTemplate.getDefaultPriority());
        existingTemplate.setHostelBlock(updatedTemplate.getHostelBlock());
        existingTemplate.setRoomNumberRequired(updatedTemplate.getRoomNumberRequired());
        existingTemplate.setLocationDetailsRequired(updatedTemplate.getLocationDetailsRequired());
        existingTemplate.setPhotoRequired(updatedTemplate.getPhotoRequired());
        existingTemplate.setEstimatedResolutionHours(updatedTemplate.getEstimatedResolutionHours());
        existingTemplate.setCommonSolutions(updatedTemplate.getCommonSolutions());
        existingTemplate.setTroubleshootingSteps(updatedTemplate.getTroubleshootingSteps());
        existingTemplate.setRequiredMaterials(updatedTemplate.getRequiredMaterials());
        existingTemplate.setSafetyNotes(updatedTemplate.getSafetyNotes());
        
        return ticketTemplateRepository.save(existingTemplate);
    }
    
    /**
     * Get all active templates
     */
    public List<TicketTemplate> getAllActiveTemplates() {
        return ticketTemplateRepository.findByIsActiveTrueOrderByUsageCountDesc();
    }
    
    /**
     * Get templates by category
     */
    public List<TicketTemplate> getTemplatesByCategory(TicketCategory category) {
        return ticketTemplateRepository.findByCategoryAndIsActiveTrueOrderByUsageCountDesc(category);
    }
    
    /**
     * Get templates applicable to a hostel block
     */
    public List<TicketTemplate> getTemplatesForHostel(HostelName hostelBlock) {
        return ticketTemplateRepository.findByHostelBlockOrNullOrderByUsageCountDesc(hostelBlock);
    }
    
    /**
     * Get templates by category and hostel block
     */
    public List<TicketTemplate> getTemplatesByCategoryAndHostel(TicketCategory category, HostelName hostelBlock) {
        return ticketTemplateRepository.findByCategoryAndHostelBlockOrNullOrderByUsageCountDesc(category, hostelBlock);
    }
    
    /**
     * Search templates by name or description
     */
    public List<TicketTemplate> searchTemplates(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveTemplates();
        }
        return ticketTemplateRepository.searchTemplates(searchTerm.trim());
    }
    
    /**
     * Get template by ID
     */
    public Optional<TicketTemplate> getTemplateById(UUID templateId) {
        return ticketTemplateRepository.findById(templateId);
    }
    
    /**
     * Get most used templates
     */
    public List<TicketTemplate> getMostUsedTemplates(int limit) {
        List<TicketTemplate> templates = ticketTemplateRepository.findMostUsedTemplates();
        return templates.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Get recently used templates
     */
    public List<TicketTemplate> getRecentlyUsedTemplates(int limit) {
        List<TicketTemplate> templates = ticketTemplateRepository.findRecentlyUsedTemplates();
        return templates.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * Use a template to create ticket data
     */
    public TemplateUsageResult useTemplate(UUID templateId, Map<String, String> variables) {
        TicketTemplate template = ticketTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        if (!template.getIsActive()) {
            throw new RuntimeException("Template is not active");
        }
        
        // Extract variables
        String roomNumber = variables.get("roomNumber");
        String hostelName = variables.get("hostelName");
        String additionalInfo = variables.get("additionalInfo");
        String specificDetails = variables.get("specificDetails");
        
        // Generate title and description
        String title = template.generateTitle(roomNumber, hostelName, additionalInfo);
        String description = template.generateDescription(roomNumber, hostelName, additionalInfo, specificDetails);
        
        // Increment usage count
        template.incrementUsageCount();
        ticketTemplateRepository.save(template);
        
        return new TemplateUsageResult(template, title, description);
    }
    
    /**
     * Deactivate a template
     */
    public void deactivateTemplate(UUID templateId, UUID deactivatedById) {
        User deactivator = userRepository.findById(deactivatedById)
                .orElseThrow(() -> new RuntimeException("Deactivator not found"));
        
        // Only admins can deactivate templates
        if (!deactivator.isAdmin()) {
            throw new RuntimeException("Only administrators can deactivate templates");
        }
        
        TicketTemplate template = ticketTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        template.setIsActive(false);
        ticketTemplateRepository.save(template);
        
        // Notify about deactivation
        notificationService.sendAdminNotification(
            "Template Deactivated",
            String.format("Template '%s' has been deactivated by %s", 
                template.getName(), deactivator.getFullName()),
            NotificationType.SYSTEM,
            null
        );
    }
    
    /**
     * Get template statistics
     */
    public Map<String, Object> getTemplateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<TicketTemplate> allTemplates = ticketTemplateRepository.findByIsActiveTrueOrderByUsageCountDesc();
        
        stats.put("totalTemplates", allTemplates.size());
        stats.put("totalUsage", allTemplates.stream().mapToInt(TicketTemplate::getUsageCount).sum());
        
        // Group by category
        Map<String, Long> categoryStats = allTemplates.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getCategory().getDisplayName(),
                    Collectors.counting()
                ));
        stats.put("categoryDistribution", categoryStats);
        
        // Most used templates
        List<TicketTemplate> mostUsed = getMostUsedTemplates(10);
        stats.put("mostUsedTemplates", mostUsed.stream()
                .map(t -> Map.of(
                    "id", t.getId(),
                    "name", t.getName(),
                    "usageCount", t.getUsageCount(),
                    "category", t.getCategory().getDisplayName()
                ))
                .collect(Collectors.toList()));
        
        // Unused templates
        List<TicketTemplate> unused = ticketTemplateRepository.findUnusedTemplates();
        stats.put("unusedTemplatesCount", unused.size());
        
        return stats;
    }
    
    /**
     * Get recommended templates based on ticket data
     */
    public List<TicketTemplate> getRecommendedTemplates(TicketCategory category, 
                                                        HostelName hostelBlock, 
                                                        String keywords) {
        List<TicketTemplate> templates;
        
        if (category != null && hostelBlock != null) {
            templates = getTemplatesByCategoryAndHostel(category, hostelBlock);
        } else if (category != null) {
            templates = getTemplatesByCategory(category);
        } else if (hostelBlock != null) {
            templates = getTemplatesForHostel(hostelBlock);
        } else {
            templates = getAllActiveTemplates();
        }
        
        // If keywords provided, filter by relevance
        if (keywords != null && !keywords.trim().isEmpty()) {
            String lowerKeywords = keywords.toLowerCase();
            templates = templates.stream()
                    .filter(t -> 
                        t.getName().toLowerCase().contains(lowerKeywords) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(lowerKeywords)) ||
                        t.getTitleTemplate().toLowerCase().contains(lowerKeywords)
                    )
                    .collect(Collectors.toList());
        }
        
        // Limit to top 10 recommendations
        return templates.stream().limit(10).collect(Collectors.toList());
    }
    
    /**
     * Duplicate a template
     */
    public TicketTemplate duplicateTemplate(UUID templateId, String newName, UUID duplicatedById) {
        TicketTemplate originalTemplate = ticketTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        
        User duplicator = userRepository.findById(duplicatedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!duplicator.canManageTickets()) {
            throw new RuntimeException("Insufficient permissions to duplicate templates");
        }
        
        TicketTemplate duplicatedTemplate = new TicketTemplate();
        duplicatedTemplate.setName(newName);
        duplicatedTemplate.setDescription(originalTemplate.getDescription());
        duplicatedTemplate.setTitleTemplate(originalTemplate.getTitleTemplate());
        duplicatedTemplate.setDescriptionTemplate(originalTemplate.getDescriptionTemplate());
        duplicatedTemplate.setCategory(originalTemplate.getCategory());
        duplicatedTemplate.setDefaultPriority(originalTemplate.getDefaultPriority());
        duplicatedTemplate.setHostelBlock(originalTemplate.getHostelBlock());
        duplicatedTemplate.setRoomNumberRequired(originalTemplate.getRoomNumberRequired());
        duplicatedTemplate.setLocationDetailsRequired(originalTemplate.getLocationDetailsRequired());
        duplicatedTemplate.setPhotoRequired(originalTemplate.getPhotoRequired());
        duplicatedTemplate.setEstimatedResolutionHours(originalTemplate.getEstimatedResolutionHours());
        duplicatedTemplate.setCommonSolutions(originalTemplate.getCommonSolutions());
        duplicatedTemplate.setTroubleshootingSteps(originalTemplate.getTroubleshootingSteps());
        duplicatedTemplate.setRequiredMaterials(originalTemplate.getRequiredMaterials());
        duplicatedTemplate.setSafetyNotes(originalTemplate.getSafetyNotes());
        duplicatedTemplate.setCreatedBy(duplicator);
        duplicatedTemplate.setIsActive(true);
        duplicatedTemplate.setUsageCount(0);
        
        return ticketTemplateRepository.save(duplicatedTemplate);
    }
    
    // Helper class for template usage results
    public static class TemplateUsageResult {
        private final TicketTemplate template;
        private final String generatedTitle;
        private final String generatedDescription;
        
        public TemplateUsageResult(TicketTemplate template, String generatedTitle, String generatedDescription) {
            this.template = template;
            this.generatedTitle = generatedTitle;
            this.generatedDescription = generatedDescription;
        }
        
        public TicketTemplate getTemplate() { return template; }
        public String getGeneratedTitle() { return generatedTitle; }
        public String getGeneratedDescription() { return generatedDescription; }
    }
}
