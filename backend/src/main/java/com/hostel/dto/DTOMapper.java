package com.hostel.dto;

import com.hostel.entity.Ticket;
import com.hostel.entity.User;

public class DTOMapper {

    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDTO(
            user.getId().toString(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getStudentId(),
            user.getRoomNumber(),
            user.getHostelBlock() != null ? user.getHostelBlock().getDisplayName() : null,
            user.getPhone(),
            user.getIsActive() != null ? user.getIsActive() : true,
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    public static TicketDTO toTicketDTO(Ticket ticket) {
        if (ticket == null) {
            return null;
        }
        
        return new TicketDTO(
            ticket.getId().toString(),
            ticket.getTicketNumber(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getHostelBlock(),
            ticket.getRoomNumber(),
            ticket.getLocationDetails(),
            toUserDTO(ticket.getCreatedBy()),
            toUserDTO(ticket.getAssignedTo()),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getResolvedAt()
        );
    }
}
