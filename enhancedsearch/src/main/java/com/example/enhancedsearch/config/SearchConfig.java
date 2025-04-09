package com.example.enhancedsearch.config;

import com.example.enhancedsearch.dto.SearchCondition;

// Matches the structure in Prod.MD
public record SearchConfig(
    String tableName,
    String columnName,
    SearchCondition searchType
) {}
