package com.example.enhancedsearch.model;

import java.sql.Date;

import lombok.Data;

@Data
public class AppFieldMaster {
    private String fieldName;
    private String description;
    private String dataType;
    private String operators;
    private Long fieldSize;
    private String tableName;
    private String columnName;
    private String query;
    private String argument;
    private String createdBy;
    private Date createdDate;
    private Integer columnWidth;
    private String textAlign;
    private String guildentifier;   

    // Add explicit getters for all fields referenced in GridColumn
    public String getFieldName() { return fieldName; }
    public String getColumnName() { return columnName; }
    public Integer getColumnWidth() { return columnWidth; }
    public String getTextAlign() { return textAlign; }
    public String getDataType() { return dataType; }
    public String getGuildentifier() { return guildentifier; }
}
