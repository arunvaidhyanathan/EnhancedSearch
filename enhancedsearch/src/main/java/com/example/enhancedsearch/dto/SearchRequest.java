package com.example.enhancedsearch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data; // Add @Data or @Getter/@Setter

import java.util.List;
import java.util.Map;

@Data // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
public class SearchRequest {

    @Min(value = 1, message = "Page number must be at least 1")
    private int page = 1; // UI sends 1-based index

    @Min(value = 1, message = "Limit must be at least 1")
    private int limit = 50;

    // Assuming the structure is { "alertest": [ filters ] } was a typo in the question
    // and it should be directly "searchFilters": [ filters ]
    @NotEmpty(message = "Search filters cannot be empty")
    @Valid // Enable validation on FilterCriteria objects
    private List<FilterCriteria> searchFilters;

    // Map to 0-based index for Spring Data Pageable
    public int getZeroBasedPage() {
        return Math.max(0, page - 1);
    }
}