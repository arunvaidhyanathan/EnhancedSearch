package com.example.enhancedsearch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data; // Add @Data or @Getter/@Setter

@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class FilterCriteria {

    private String filterCondition = "AND"; // Default to AND

    @NotBlank(message = "Field name cannot be blank")
    private String fieldName;

    @NotBlank(message = "Field type cannot be blank")
    private String fieldType; // e.g., "string", "date", "int", "decimal"

    private String fieldGroup; // e.g., "Alert Details", "Transaction Details"

    @NotBlank(message = "Collection name cannot be blank")
    private String collectionName; // e.g., "alert_mst", "alert_det"

    // fieldValue can be null for operations like IS NULL/IS NOT NULL
    private String fieldValue;

    @NotBlank(message = "Operation cannot be blank")
    private String operation; // e.g., "Like", "RegEX", "Between", "=", ">="

    // Used for operations like "Between"
    private String fieldValueTo;
}