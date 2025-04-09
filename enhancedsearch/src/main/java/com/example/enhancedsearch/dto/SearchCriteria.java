package com.example.enhancedsearch.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Renamed from FilterCondition in Prod.MD (JPA section) to match JDBC section
// Added isDate flag as mentioned in Prod.MD JDBC section
public record SearchCriteria(
    String columnName,
    SearchCondition condition,
    String value,
    String value2, // For BETWEEN
    @JsonProperty("isDate") // Explicit mapping for boolean getter
    boolean isDate // Flag to indicate if the value(s) should be treated as dates
) {}
