package com.example.enhancedsearch.dto;

public class SearchFilter {
    private String filterCondition; // AND or OR
    private String fieldName;
    private String fieldType;
    private String fieldGroup;
    private String collectionName;
    private String fieldValue;
    private String operation;
    private String fieldValueTo;

    public String getFilterCondition() { return filterCondition; }
    public void setFilterCondition(String filterCondition) { this.filterCondition = filterCondition; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public String getFieldGroup() { return fieldGroup; }
    public void setFieldGroup(String fieldGroup) { this.fieldGroup = fieldGroup; }

    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }

    public String getFieldValue() { return fieldValue; }
    public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getFieldValueTo() { return fieldValueTo; }
    public void setFieldValueTo(String fieldValueTo) { this.fieldValueTo = fieldValueTo; }
}
