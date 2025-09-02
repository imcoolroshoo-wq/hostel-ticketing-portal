package com.hostel.controller;

import com.hostel.entity.TicketTemplate;
import com.hostel.entity.TicketCategory;
import com.hostel.entity.HostelName;
import com.hostel.service.TicketTemplateService;
import com.hostel.service.TicketTemplateService.TemplateUsageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for managing ticket templates
 * Implements Template Management API as per PDD Section 4.1.4
 */
@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = {"http://localhost:3000", "https://hostel-ticketing-frontend.onrender.com"})
public class TicketTemplateController {
    
    @Autowired
    private TicketTemplateService ticketTemplateService;
    
    /**
     * Get all active templates
     */
    @GetMapping
    public ResponseEntity<List<TicketTemplate>> getAllTemplates() {
        try {
            List<TicketTemplate> templates = ticketTemplateService.getAllActiveTemplates();
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketTemplate> getTemplateById(@PathVariable UUID id) {
        try {
            return ticketTemplateService.getTemplateById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create a new template
     */
    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody TicketTemplate template, 
                                           @RequestParam UUID createdBy) {
        try {
            TicketTemplate createdTemplate = ticketTemplateService.createTemplate(template, createdBy);
            return ResponseEntity.ok(createdTemplate);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error creating template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Update an existing template
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable UUID id, 
                                           @RequestBody TicketTemplate template,
                                           @RequestParam UUID updatedBy) {
        try {
            TicketTemplate updatedTemplate = ticketTemplateService.updateTemplate(id, template, updatedBy);
            return ResponseEntity.ok(updatedTemplate);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error updating template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Deactivate a template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateTemplate(@PathVariable UUID id, 
                                               @RequestParam UUID deactivatedBy) {
        try {
            ticketTemplateService.deactivateTemplate(id, deactivatedBy);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Template deactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error deactivating template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get templates by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TicketTemplate>> getTemplatesByCategory(@PathVariable TicketCategory category) {
        try {
            List<TicketTemplate> templates = ticketTemplateService.getTemplatesByCategory(category);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get templates for a specific hostel
     */
    @GetMapping("/hostel/{hostelBlock}")
    public ResponseEntity<List<TicketTemplate>> getTemplatesForHostel(@PathVariable String hostelBlock) {
        try {
            HostelName hostelName = HostelName.fromAnyName(hostelBlock);
            List<TicketTemplate> templates = ticketTemplateService.getTemplatesForHostel(hostelName);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search templates
     */
    @GetMapping("/search")
    public ResponseEntity<List<TicketTemplate>> searchTemplates(@RequestParam String query) {
        try {
            List<TicketTemplate> templates = ticketTemplateService.searchTemplates(query);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get most used templates
     */
    @GetMapping("/most-used")
    public ResponseEntity<List<TicketTemplate>> getMostUsedTemplates(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<TicketTemplate> templates = ticketTemplateService.getMostUsedTemplates(limit);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get recently used templates
     */
    @GetMapping("/recent")
    public ResponseEntity<List<TicketTemplate>> getRecentlyUsedTemplates(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<TicketTemplate> templates = ticketTemplateService.getRecentlyUsedTemplates(limit);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Use a template to generate ticket data
     */
    @PostMapping("/{id}/use")
    public ResponseEntity<?> useTemplate(@PathVariable UUID id, 
                                        @RequestBody Map<String, String> variables) {
        try {
            TemplateUsageResult result = ticketTemplateService.useTemplate(id, variables);
            
            Map<String, Object> response = new HashMap<>();
            response.put("template", result.getTemplate());
            response.put("generatedTitle", result.getGeneratedTitle());
            response.put("generatedDescription", result.getGeneratedDescription());
            response.put("category", result.getTemplate().getCategory());
            response.put("defaultPriority", result.getTemplate().getDefaultPriority());
            response.put("estimatedResolutionHours", result.getTemplate().getEstimatedResolutionHours());
            response.put("roomNumberRequired", result.getTemplate().getRoomNumberRequired());
            response.put("locationDetailsRequired", result.getTemplate().getLocationDetailsRequired());
            response.put("photoRequired", result.getTemplate().getPhotoRequired());
            response.put("commonSolutions", result.getTemplate().getCommonSolutions());
            response.put("troubleshootingSteps", result.getTemplate().getTroubleshootingSteps());
            response.put("requiredMaterials", result.getTemplate().getRequiredMaterials());
            response.put("safetyNotes", result.getTemplate().getSafetyNotes());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error using template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get recommended templates
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<TicketTemplate>> getRecommendedTemplates(
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) String hostelBlock,
            @RequestParam(required = false) String keywords) {
        try {
            HostelName hostelName = hostelBlock != null ? HostelName.fromAnyName(hostelBlock) : null;
            List<TicketTemplate> templates = ticketTemplateService.getRecommendedTemplates(
                    category, hostelName, keywords);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Duplicate a template
     */
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<?> duplicateTemplate(@PathVariable UUID id, 
                                              @RequestParam String newName,
                                              @RequestParam UUID duplicatedBy) {
        try {
            TicketTemplate duplicatedTemplate = ticketTemplateService.duplicateTemplate(id, newName, duplicatedBy);
            return ResponseEntity.ok(duplicatedTemplate);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error duplicating template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Get template statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getTemplateStatistics() {
        try {
            Map<String, Object> stats = ticketTemplateService.getTemplateStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get templates by category and hostel
     */
    @GetMapping("/category/{category}/hostel/{hostelBlock}")
    public ResponseEntity<List<TicketTemplate>> getTemplatesByCategoryAndHostel(
            @PathVariable TicketCategory category,
            @PathVariable String hostelBlock) {
        try {
            HostelName hostelName = HostelName.fromAnyName(hostelBlock);
            List<TicketTemplate> templates = ticketTemplateService.getTemplatesByCategoryAndHostel(category, hostelName);
            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all ticket categories for template creation
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getTicketCategories() {
        try {
            List<Map<String, Object>> categories = new ArrayList<>();
            for (TicketCategory category : TicketCategory.values()) {
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("value", category);
                categoryData.put("displayName", category.getDisplayName());
                categoryData.put("description", category.getDescription());
                categoryData.put("icon", category.getIcon());
                categoryData.put("group", category.getCategoryGroup());
                categoryData.put("estimatedHours", category.getEstimatedResolutionHours());
                categoryData.put("requiresSpecializedStaff", category.requiresSpecializedStaff());
                categories.add(categoryData);
            }
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Preview template generation
     */
    @PostMapping("/{id}/preview")
    public ResponseEntity<?> previewTemplate(@PathVariable UUID id, 
                                            @RequestBody Map<String, String> variables) {
        try {
            Optional<TicketTemplate> templateOpt = ticketTemplateService.getTemplateById(id);
            if (templateOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Template not found");
                return ResponseEntity.notFound().build();
            }
            
            TicketTemplate template = templateOpt.get();
            
            // Extract variables
            String roomNumber = variables.get("roomNumber");
            String hostelName = variables.get("hostelName");
            String additionalInfo = variables.get("additionalInfo");
            String specificDetails = variables.get("specificDetails");
            
            // Generate preview without incrementing usage count
            String title = template.generateTitle(roomNumber, hostelName, additionalInfo);
            String description = template.generateDescription(roomNumber, hostelName, additionalInfo, specificDetails);
            
            Map<String, Object> preview = new HashMap<>();
            preview.put("title", title);
            preview.put("description", description);
            preview.put("category", template.getCategory());
            preview.put("defaultPriority", template.getDefaultPriority());
            preview.put("estimatedResolutionHours", template.getEstimatedResolutionHours());
            
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error previewing template: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
