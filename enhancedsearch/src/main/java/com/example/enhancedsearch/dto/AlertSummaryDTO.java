package com.example.enhancedsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.example.enhancedsearch.entity.AlertMst;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertSummaryDTO {
    // Fields needed in the UI response
    private Long id; // From AlertMst
    private String alertId;
    private String sourceSystem;
    private String receiver;
    private java.time.LocalDateTime createdDate; // Or Instant/ZonedDateTime
    // Add other fields from AlertMst or derived fields as needed
    // e.g., private String beneficiaryNamePreview; (Could be fetched via join or separate query if complex)

    public static AlertSummaryDTO fromEntity(AlertMst entity) {
        AlertSummaryDTO dto = new AlertSummaryDTO();
        dto.setId(entity.getId());
        dto.setAlertId(entity.getAlertBusinessId());
        dto.setSourceSystem(entity.getSourceSystem());
        dto.setReceiver(entity.getReceiver());
        dto.setCreatedDate(entity.getCreatedDate());
        return dto;
    }
}
