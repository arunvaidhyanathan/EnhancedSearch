package com.example.enhancedsearch.dto;

import java.util.List;

// Includes criteria list and tableName as per Prod.MD JDBC section
public record SearchRequest(
    List<SearchCriteria> criteria,
    String tableName
    // Add pagination/sorting fields later if needed
    // int page = 0;
    // int size = 20;
) {}
