package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames=true)
public class DynamicCriteria {
    private String name; 
    private String value;
    private Operator operator;
}
