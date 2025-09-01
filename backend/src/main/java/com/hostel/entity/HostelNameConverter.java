package com.hostel.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for HostelName enum to handle database string values
 */
@Converter(autoApply = true)
public class HostelNameConverter implements AttributeConverter<HostelName, String> {

    @Override
    public String convertToDatabaseColumn(HostelName hostelName) {
        if (hostelName == null) {
            return null;
        }
        return hostelName.getDisplayName();
    }

    @Override
    public HostelName convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        
        try {
            return HostelName.fromAnyName(dbData);
        } catch (IllegalArgumentException e) {
            // Log the error but don't fail - return null for unknown values
            System.err.println("Warning: Unknown hostel name in database: " + dbData);
            return null;
        }
    }
}
