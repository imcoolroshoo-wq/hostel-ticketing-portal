package com.hostel.controller;

import com.hostel.entity.Asset;
import com.hostel.entity.AssetMovement;
import com.hostel.entity.AssetStatus;
import com.hostel.entity.AssetType;
import com.hostel.entity.MaintenanceSchedule;
import com.hostel.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Asset Management Controller implementing Asset Management functionality
 * as per IIM Trichy Hostel Ticket Management System Product Design Document Section 4.3.4
 */
@RestController
@RequestMapping("/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    /**
     * Get all assets with pagination and filtering
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Map<String, Object>> getAllAssets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        try {
            Page<Asset> assetsPage = assetService.getAllAssets(
                pageable, building, type, status, search);
            
            Map<String, Object> response = new HashMap<>();
            response.put("assets", assetsPage.getContent());
            response.put("currentPage", assetsPage.getNumber());
            response.put("totalItems", assetsPage.getTotalElements());
            response.put("totalPages", assetsPage.getTotalPages());
            response.put("hasNext", assetsPage.hasNext());
            response.put("hasPrevious", assetsPage.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Error fetching assets: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Get asset by ID
     */
    @GetMapping("/{assetId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> getAssetById(@PathVariable UUID assetId) {
        try {
            Optional<Asset> assetOpt = assetService.getAssetById(assetId);
            if (assetOpt.isPresent()) {
                return ResponseEntity.ok(assetOpt.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Asset not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Create new asset
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAsset(@RequestBody Asset asset) {
        try {
            Asset createdAsset = assetService.createAsset(asset);
            return ResponseEntity.ok(createdAsset);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update asset
     */
    @PutMapping("/{assetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAsset(@PathVariable UUID assetId, @RequestBody Asset assetDetails) {
        try {
            Asset updatedAsset = assetService.updateAsset(assetId, assetDetails);
            return ResponseEntity.ok(updatedAsset);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete asset (soft delete)
     */
    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAsset(@PathVariable UUID assetId) {
        try {
            assetService.deleteAsset(assetId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Asset deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get asset by tag
     */
    @GetMapping("/tag/{assetTag}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> getAssetByTag(@PathVariable String assetTag) {
        try {
            Optional<Asset> assetOpt = assetService.getAssetByTag(assetTag);
            if (assetOpt.isPresent()) {
                return ResponseEntity.ok(assetOpt.get());
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Asset not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get assets by building
     */
    @GetMapping("/building/{building}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> getAssetsByBuilding(@PathVariable String building) {
        try {
            List<Asset> assets = assetService.getAssetsByBuilding(building);
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get assets by type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> getAssetsByType(@PathVariable AssetType type) {
        try {
            List<Asset> assets = assetService.getAssetsByType(type);
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get assets by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> getAssetsByStatus(@PathVariable AssetStatus status) {
        try {
            List<Asset> assets = assetService.getAssetsByStatus(status);
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Assign asset to user
     */
    @PostMapping("/{assetId}/assign/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignAsset(@PathVariable UUID assetId, @PathVariable UUID userId) {
        try {
            Asset asset = assetService.assignAsset(assetId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Asset assigned successfully");
            response.put("asset", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Unassign asset
     */
    @PostMapping("/{assetId}/unassign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unassignAsset(@PathVariable UUID assetId) {
        try {
            Asset asset = assetService.unassignAsset(assetId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Asset unassigned successfully");
            response.put("asset", asset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get asset movements/history
     */
    @GetMapping("/{assetId}/movements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<AssetMovement>> getAssetMovements(@PathVariable UUID assetId) {
        try {
            List<AssetMovement> movements = assetService.getAssetMovements(assetId);
            return ResponseEntity.ok(movements);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add asset movement
     */
    @PostMapping("/{assetId}/movements")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<?> addAssetMovement(@PathVariable UUID assetId, @RequestBody AssetMovement movement) {
        try {
            AssetMovement createdMovement = assetService.addAssetMovement(assetId, movement);
            return ResponseEntity.ok(createdMovement);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get asset maintenance schedules
     */
    @GetMapping("/{assetId}/maintenance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<MaintenanceSchedule>> getAssetMaintenanceSchedules(@PathVariable UUID assetId) {
        try {
            List<MaintenanceSchedule> schedules = assetService.getMaintenanceSchedules(assetId);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create maintenance schedule
     */
    @PostMapping("/{assetId}/maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMaintenanceSchedule(@PathVariable UUID assetId, @RequestBody MaintenanceSchedule schedule) {
        try {
            MaintenanceSchedule createdSchedule = assetService.createMaintenanceSchedule(assetId, schedule);
            return ResponseEntity.ok(createdSchedule);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get asset statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<Map<String, Object>> getAssetStatistics() {
        try {
            Map<String, Object> stats = assetService.getAssetStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search assets
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> searchAssets(@RequestParam String query) {
        try {
            List<Asset> assets = assetService.searchAssets(query);
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get assets requiring maintenance
     */
    @GetMapping("/maintenance-required")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> getAssetsRequiringMaintenance() {
        try {
            List<Asset> assets = assetService.getAssetsRequiringMaintenance();
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get assets with expired warranty
     */
    @GetMapping("/warranty-expired")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<List<Asset>> getAssetsWithExpiredWarranty() {
        try {
            List<Asset> assets = assetService.getAssetsWithExpiredWarranty();
            return ResponseEntity.ok(assets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
