package com.example.enhancedsearch.dto;

// Using the operators mentioned and adding REGEX based on Prod.MD
public enum SearchCondition {
    EQUALS, // =
    LIKE,   // LIKE / ILIKE (will use LIKE for now)
    LESS_THAN_EQUALS, // <=
    GREATER_THAN_EQUALS, // >=
    BETWEEN,
    REGEX   // ~ (PostgreSQL regex)
}
