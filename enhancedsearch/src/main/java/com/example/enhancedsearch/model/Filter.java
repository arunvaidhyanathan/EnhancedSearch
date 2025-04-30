package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = true)
public class Filter {
    String fieldName;
    String operation;
    String fieldValue;
    String fieldValueTo;
    String fieldType;
    String filterCondition;
    String collectionName;
}
