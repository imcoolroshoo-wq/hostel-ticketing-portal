package com.hostel.repository;

import com.hostel.entity.AttachmentType;
import com.hostel.entity.Ticket;
import com.hostel.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for TicketAttachment entity operations
 */
@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, UUID> {
    
    /**
     * Find attachments by ticket ID
     */
    List<TicketAttachment> findByTicketId(UUID ticketId);
    
    /**
     * Find attachments by ticket ID and attachment type
     */
    List<TicketAttachment> findByTicketIdAndAttachmentType(UUID ticketId, AttachmentType attachmentType);
    
    /**
     * Find attachments by ticket ID ordered by creation date
     */
    List<TicketAttachment> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
    
    /**
     * Find attachments by uploaded user
     */
    List<TicketAttachment> findByUploadedByIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find attachments by attachment type
     */
    List<TicketAttachment> findByAttachmentTypeOrderByCreatedAtDesc(AttachmentType attachmentType);
    
    /**
     * Check if attachment exists by ticket ID and type
     */
    boolean existsByTicketIdAndAttachmentType(UUID ticketId, AttachmentType attachmentType);
    
    /**
     * Find photo attachments (before/after work photos)
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.ticket.id = :ticketId AND " +
           "(ta.isBeforePhoto = true OR ta.isAfterPhoto = true)")
    List<TicketAttachment> findPhotoDocumentationByTicketId(@Param("ticketId") UUID ticketId);
    
    /**
     * Find before work photos by ticket
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.ticket.id = :ticketId AND ta.isBeforePhoto = true")
    List<TicketAttachment> findBeforePhotosByTicketId(@Param("ticketId") UUID ticketId);
    
    /**
     * Find after work photos by ticket
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.ticket.id = :ticketId AND ta.isAfterPhoto = true")
    List<TicketAttachment> findAfterPhotosByTicketId(@Param("ticketId") UUID ticketId);
    
    /**
     * Find required attachments by ticket
     */
    List<TicketAttachment> findByTicketIdAndIsRequiredTrue(UUID ticketId);
    
    /**
     * Find attachments by mime type
     */
    List<TicketAttachment> findByMimeTypeStartingWith(String mimeTypePrefix);
    
    /**
     * Find image attachments
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.mimeType LIKE 'image/%'")
    List<TicketAttachment> findImageAttachments();
    
    /**
     * Find attachments larger than specified size
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.fileSize > :sizeBytes")
    List<TicketAttachment> findAttachmentsLargerThan(@Param("sizeBytes") Long sizeBytes);
    
    /**
     * Find attachments by date range
     */
    List<TicketAttachment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find attachments by ticket ID and user
     */
    List<TicketAttachment> findByTicketIdAndUploadedByIdOrderByCreatedAtDesc(UUID ticketId, UUID userId);
    
    /**
     * Count attachments by ticket
     */
    long countByTicketId(UUID ticketId);
    
    /**
     * Count attachments by type
     */
    long countByAttachmentType(AttachmentType attachmentType);
    
    /**
     * Count attachments by user
     */
    long countByUploadedById(UUID userId);
    
    /**
     * Find attachments with metadata
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.photoMetadata IS NOT NULL AND ta.photoMetadata != ''")
    List<TicketAttachment> findAttachmentsWithMetadata();
    
    /**
     * Find orphaned attachments (tickets that no longer exist)
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE ta.ticket IS NULL")
    List<TicketAttachment> findOrphanedAttachments();
    
    /**
     * Find duplicate attachments (same filename and size)
     */
    @Query("SELECT ta FROM TicketAttachment ta WHERE EXISTS " +
           "(SELECT ta2 FROM TicketAttachment ta2 WHERE ta2.id != ta.id AND " +
           "ta2.originalFilename = ta.originalFilename AND ta2.fileSize = ta.fileSize)")
    List<TicketAttachment> findPotentialDuplicates();
    
    /**
     * Get attachment statistics
     */
    @Query("SELECT ta.attachmentType, COUNT(ta), AVG(ta.fileSize), SUM(ta.fileSize) " +
           "FROM TicketAttachment ta GROUP BY ta.attachmentType")
    List<Object[]> getAttachmentStatistics();
    
    /**
     * Find attachments by ticket and attachment type
     */
    List<TicketAttachment> findByTicketAndAttachmentType(Ticket ticket, AttachmentType attachmentType);
}
