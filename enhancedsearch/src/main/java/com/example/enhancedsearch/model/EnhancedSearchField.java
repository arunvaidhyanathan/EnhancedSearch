package com.example.enhancedsearch.model;

import lombok.Data;

@Data
public class EnhancedSearchField {
    String fieldGroup;
    String fieldName;
    String guildentifier;
    String fieldType; int displayOrder;
    String isSortable;
    String collectionName;
    String regexEnabled;
}
