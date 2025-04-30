package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = true)
public class SortCriteria {
    private String property;
    private String direction;
    
    // Add explicit getters for compatibility
    public String getProperty() { return property; }
    public String getDirection() { return direction; }
}
