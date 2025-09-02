package com.hostel.service;

import com.hostel.entity.*;
import com.hostel.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for detecting duplicate and similar tickets
 * Implements Smart Duplicate Detection as per PDD Section 4.1.1
 */
@Service
public class DuplicateDetectionService {
    
    @Autowired
    private TicketRepository ticketRepository;
    
    // Configuration for duplicate detection
    private static final double OVERALL_SIMILARITY_THRESHOLD = 0.75;
    private static final int RECENT_TICKETS_DAYS = 30;
    
    /**
     * Detect potential duplicate tickets for a new ticket
     */
    public DuplicateDetectionResult detectDuplicates(Ticket newTicket) {
        List<Ticket> recentTickets = getRecentTickets(newTicket);
        List<SimilarTicket> similarTickets = new ArrayList<>();
        
        for (Ticket existingTicket : recentTickets) {
            double similarity = calculateSimilarity(newTicket, existingTicket);
            
            if (similarity >= OVERALL_SIMILARITY_THRESHOLD) {
                SimilarTicket similar = new SimilarTicket(
                    existingTicket,
                    similarity,
                    getDuplicateType(similarity),
                    generateSimilarityExplanation(newTicket, existingTicket, similarity)
                );
                similarTickets.add(similar);
            }
        }
        
        // Sort by similarity score (highest first)
        similarTickets.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        boolean hasPotentialDuplicates = !similarTickets.isEmpty();
        String recommendation = generateRecommendation(similarTickets);
        
        return new DuplicateDetectionResult(
            hasPotentialDuplicates,
            similarTickets,
            recommendation,
            OVERALL_SIMILARITY_THRESHOLD
        );
    }
    
    /**
     * Find existing tickets that might be related to a new ticket
     */
    public List<SimilarTicket> findRelatedTickets(Ticket ticket, int maxResults) {
        List<Ticket> candidateTickets = getCandidateTickets(ticket);
        List<SimilarTicket> relatedTickets = new ArrayList<>();
        
        for (Ticket candidate : candidateTickets) {
            if (candidate.getId().equals(ticket.getId())) continue;
            
            double similarity = calculateSimilarity(ticket, candidate);
            
            if (similarity >= 0.3) { // Lower threshold for related tickets
                relatedTickets.add(new SimilarTicket(
                    candidate,
                    similarity,
                    getRelationType(similarity),
                    generateSimilarityExplanation(ticket, candidate, similarity)
                ));
            }
        }
        
        return relatedTickets.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate similarity between two tickets
     */
    private double calculateSimilarity(Ticket ticket1, Ticket ticket2) {
        // Location similarity (30% weight)
        double locationSimilarity = calculateLocationSimilarity(ticket1, ticket2);
        
        // Category similarity (25% weight)
        double categorySimilarity = calculateCategorySimilarity(ticket1, ticket2);
        
        // Title similarity (25% weight)
        double titleSimilarity = calculateTextSimilarity(ticket1.getTitle(), ticket2.getTitle());
        
        // Description similarity (20% weight)
        double descriptionSimilarity = calculateTextSimilarity(ticket1.getDescription(), ticket2.getDescription());
        
        return (locationSimilarity * 0.3) + 
               (categorySimilarity * 0.25) + 
               (titleSimilarity * 0.25) + 
               (descriptionSimilarity * 0.2);
    }
    
    /**
     * Calculate location similarity between tickets
     */
    private double calculateLocationSimilarity(Ticket ticket1, Ticket ticket2) {
        double similarity = 0.0;
        
        // Exact room match
        if (Objects.equals(ticket1.getRoomNumber(), ticket2.getRoomNumber()) &&
            Objects.equals(ticket1.getHostelBlock(), ticket2.getHostelBlock())) {
            similarity = 1.0;
        }
        // Same building, different room
        else if (Objects.equals(ticket1.getHostelBlock(), ticket2.getHostelBlock())) {
            similarity = 0.7;
            
            // Same floor bonus
            if (Objects.equals(ticket1.getFloorNumber(), ticket2.getFloorNumber())) {
                similarity = 0.85;
            }
        }
        // Different buildings
        else {
            similarity = 0.0;
        }
        
        return similarity;
    }
    
    /**
     * Calculate category similarity between tickets
     */
    private double calculateCategorySimilarity(Ticket ticket1, Ticket ticket2) {
        if (ticket1.getCategory() == ticket2.getCategory()) {
            return 1.0;
        }
        
        // Check if categories are in the same group
        if (ticket1.getCategory() != null && ticket2.getCategory() != null) {
            String group1 = ticket1.getCategory().getCategoryGroup();
            String group2 = ticket2.getCategory().getCategoryGroup();
            
            if (Objects.equals(group1, group2)) {
                return 0.6;
            }
        }
        
        return 0.0;
    }
    
    /**
     * Calculate text similarity using various algorithms
     */
    private double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) return 0.0;
        if (text1.isEmpty() || text2.isEmpty()) return 0.0;
        
        // Normalize texts
        String normalized1 = normalizeText(text1);
        String normalized2 = normalizeText(text2);
        
        // Exact match
        if (normalized1.equals(normalized2)) {
            return 1.0;
        }
        
        // Jaccard similarity for word-based comparison
        double jaccardSimilarity = calculateJaccardSimilarity(normalized1, normalized2);
        
        // Levenshtein distance similarity
        double levenshteinSimilarity = calculateLevenshteinSimilarity(normalized1, normalized2);
        
        // Substring containment
        double containmentSimilarity = calculateContainmentSimilarity(normalized1, normalized2);
        
        // Weighted combination
        return (jaccardSimilarity * 0.5) + (levenshteinSimilarity * 0.3) + (containmentSimilarity * 0.2);
    }
    
    /**
     * Normalize text for comparison
     */
    private String normalizeText(String text) {
        return text.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", " ")  // Remove punctuation
                   .replaceAll("\\s+", " ")         // Normalize whitespace
                   .trim();
    }
    
    /**
     * Calculate Jaccard similarity between two texts
     */
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate Levenshtein-based similarity
     */
    private double calculateLevenshteinSimilarity(String text1, String text2) {
        int distance = calculateLevenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        if (maxLength == 0) return 1.0;
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Calculate Levenshtein distance
     */
    private int calculateLevenshteinDistance(String text1, String text2) {
        int[][] dp = new int[text1.length() + 1][text2.length() + 1];
        
        for (int i = 0; i <= text1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= text2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= text1.length(); i++) {
            for (int j = 1; j <= text2.length(); j++) {
                int cost = text1.charAt(i - 1) == text2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }
        
        return dp[text1.length()][text2.length()];
    }
    
    /**
     * Calculate containment similarity
     */
    private double calculateContainmentSimilarity(String text1, String text2) {
        if (text1.contains(text2) || text2.contains(text1)) {
            return 0.8;
        }
        
        // Check for substantial overlap
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");
        
        for (String word1 : words1) {
            if (word1.length() > 3) { // Only consider significant words
                for (String word2 : words2) {
                    if (word2.length() > 3 && word1.equals(word2)) {
                        return 0.4;
                    }
                }
            }
        }
        
        return 0.0;
    }
    
    /**
     * Get recent tickets for comparison
     */
    private List<Ticket> getRecentTickets(Ticket newTicket) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RECENT_TICKETS_DAYS);
        
        // Get recent tickets excluding tickets by the same user
        return ticketRepository.findByCreatedAtAfterOrderByCreatedAtDesc(cutoffDate)
                .stream()
                .filter(ticket -> !ticket.getCreatedBy().equals(newTicket.getCreatedBy()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get candidate tickets for similarity comparison
     */
    private List<Ticket> getCandidateTickets(Ticket ticket) {
        List<Ticket> candidates = new ArrayList<>();
        List<TicketStatus> activeStatuses = Arrays.asList(
            TicketStatus.OPEN, TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED
        );
        
        // Same location tickets
        if (ticket.getHostelBlock() != null) {
            candidates.addAll(ticketRepository.findByHostelBlockAndStatusIn(
                ticket.getHostelBlock(), activeStatuses
            ));
        }
        
        // Same category tickets
        if (ticket.getCategory() != null) {
            candidates.addAll(ticketRepository.findByCategoryAndStatusIn(
                ticket.getCategory(), activeStatuses
            ));
        }
        
        // Remove duplicates and limit for performance
        return candidates.stream()
                .distinct()
                .limit(50)
                .collect(Collectors.toList());
    }
    
    /**
     * Determine duplicate type based on similarity score
     */
    private DuplicateType getDuplicateType(double similarity) {
        if (similarity >= 0.95) {
            return DuplicateType.EXACT_DUPLICATE;
        } else if (similarity >= 0.85) {
            return DuplicateType.LIKELY_DUPLICATE;
        } else if (similarity >= 0.75) {
            return DuplicateType.POSSIBLE_DUPLICATE;
        } else {
            return DuplicateType.RELATED;
        }
    }
    
    /**
     * Determine relation type based on similarity score
     */
    private DuplicateType getRelationType(double similarity) {
        if (similarity >= 0.7) {
            return DuplicateType.HIGHLY_RELATED;
        } else if (similarity >= 0.5) {
            return DuplicateType.RELATED;
        } else {
            return DuplicateType.LOOSELY_RELATED;
        }
    }
    
    /**
     * Generate explanation for similarity
     */
    private String generateSimilarityExplanation(Ticket ticket1, Ticket ticket2, double similarity) {
        List<String> factors = new ArrayList<>();
        
        // Location factors
        if (Objects.equals(ticket1.getRoomNumber(), ticket2.getRoomNumber()) &&
            Objects.equals(ticket1.getHostelBlock(), ticket2.getHostelBlock())) {
            factors.add("same location");
        } else if (Objects.equals(ticket1.getHostelBlock(), ticket2.getHostelBlock())) {
            factors.add("same building");
        }
        
        // Category factors
        if (ticket1.getCategory() == ticket2.getCategory()) {
            factors.add("same category");
        }
        
        // Title similarity
        double titleSim = calculateTextSimilarity(ticket1.getTitle(), ticket2.getTitle());
        if (titleSim > 0.7) {
            factors.add("very similar titles");
        } else if (titleSim > 0.5) {
            factors.add("similar titles");
        }
        
        // Time proximity
        if (ticket2.getCreatedAt() != null) {
            long hoursDiff = Math.abs(java.time.Duration.between(
                ticket1.getCreatedAt() != null ? ticket1.getCreatedAt() : LocalDateTime.now(), 
                ticket2.getCreatedAt()
            ).toHours());
            
            if (hoursDiff < 24) {
                factors.add("created within 24 hours");
            } else if (hoursDiff < 168) { // 1 week
                factors.add("created within a week");
            }
        }
        
        String explanation = "Similar due to: " + String.join(", ", factors);
        return explanation + String.format(" (%.1f%% similarity)", similarity * 100);
    }
    
    /**
     * Generate recommendation based on similar tickets
     */
    private String generateRecommendation(List<SimilarTicket> similarTickets) {
        if (similarTickets.isEmpty()) {
            return "No similar tickets found. Proceed with ticket creation.";
        }
        
        SimilarTicket mostSimilar = similarTickets.get(0);
        double highestSimilarity = mostSimilar.getSimilarityScore();
        
        if (highestSimilarity >= 0.95) {
            return "Very high similarity detected! Please review existing ticket " + 
                   mostSimilar.getTicket().getTicketNumber() + " before creating a new one.";
        } else if (highestSimilarity >= 0.85) {
            return "High similarity detected. Consider checking ticket " + 
                   mostSimilar.getTicket().getTicketNumber() + " or adding details to differentiate this issue.";
        } else if (highestSimilarity >= 0.75) {
            return "Possible duplicate found. Review ticket " + 
                   mostSimilar.getTicket().getTicketNumber() + " to ensure this is a separate issue.";
        } else {
            return "Similar tickets found. You may want to reference related tickets for context.";
        }
    }
    
    // Enums and Data Classes
    
    public enum DuplicateType {
        EXACT_DUPLICATE("Exact Duplicate", "🔴"),
        LIKELY_DUPLICATE("Likely Duplicate", "🟠"),
        POSSIBLE_DUPLICATE("Possible Duplicate", "🟡"),
        HIGHLY_RELATED("Highly Related", "🔵"),
        RELATED("Related", "🟢"),
        LOOSELY_RELATED("Loosely Related", "⚪");
        
        private final String displayName;
        private final String icon;
        
        DuplicateType(String displayName, String icon) {
            this.displayName = displayName;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
    }
    
    public static class DuplicateDetectionResult {
        private final boolean hasPotentialDuplicates;
        private final List<SimilarTicket> similarTickets;
        private final String recommendation;
        private final double threshold;
        
        public DuplicateDetectionResult(boolean hasPotentialDuplicates, List<SimilarTicket> similarTickets, 
                                      String recommendation, double threshold) {
            this.hasPotentialDuplicates = hasPotentialDuplicates;
            this.similarTickets = similarTickets;
            this.recommendation = recommendation;
            this.threshold = threshold;
        }
        
        public boolean hasPotentialDuplicates() { return hasPotentialDuplicates; }
        public List<SimilarTicket> getSimilarTickets() { return similarTickets; }
        public String getRecommendation() { return recommendation; }
        public double getThreshold() { return threshold; }
        public int getSimilarCount() { return similarTickets.size(); }
    }
    
    public static class SimilarTicket {
        private final Ticket ticket;
        private final double similarityScore;
        private final DuplicateType type;
        private final String explanation;
        
        public SimilarTicket(Ticket ticket, double similarityScore, DuplicateType type, String explanation) {
            this.ticket = ticket;
            this.similarityScore = similarityScore;
            this.type = type;
            this.explanation = explanation;
        }
        
        public Ticket getTicket() { return ticket; }
        public double getSimilarityScore() { return similarityScore; }
        public DuplicateType getType() { return type; }
        public String getExplanation() { return explanation; }
    }
}
