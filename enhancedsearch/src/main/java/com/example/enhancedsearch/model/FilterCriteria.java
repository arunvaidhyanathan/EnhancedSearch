package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames=true)
public class FilterCriteria {
    private Operator operator;
    private String value;
    private String property;

    // Add explicit getters for compatibility
    public String getValue() { return value; }
    public String getProperty() { return property; }
    public Operator getOperator() { return operator; }
}
