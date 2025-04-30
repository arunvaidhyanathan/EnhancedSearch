package com.example.enhancedsearch.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils; // Import StringUtils

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GridColumn {
    private String header; 
    private String datalndex; 
    private String stateld;
    private int width; 
    private String icon; 
    private String align;
    private String fieldType; 
    private String fieldName;
    private String guildentifier;
    private boolean locked;
    private boolean hidden;

    private GridFields getGridField() {
        GridFields field = new GridFields();
        field.setName(this.datalndex);
        field.setType(this.fieldType);
        return field;
    }

    public static List<GridFields> getGridFields(List<GridColumn> gridColumns) {
        List<GridFields> gridFields = null;
        gridFields = gridColumns.stream().map(p -> p.getGridField()).collect(Collectors.toList());
        return gridFields;
    }

    public GridColumn mapGridColumnFields(AppFieldMaster appFieldMaster, int count) {
        GridColumn gridColumn = new GridColumn();   
                gridColumn.setHeader(appFieldMaster.getFieldName());
                gridColumn.setDatalndex(appFieldMaster.getColumnName());
                gridColumn.setStateld(appFieldMaster.getColumnName());
                gridColumn.setWidth(appFieldMaster.getColumnWidth()==null?150:appFieldMaster.getColumnWidth().intValue());
                gridColumn.setAlign(appFieldMaster.getTextAlign()==null?"start":appFieldMaster.getTextAlign().toLowerCase());
                gridColumn.setFieldType(appFieldMaster.getDataType()==null?"string":appFieldMaster.getDataType().toLowerCase());
                gridColumn.setFieldName(appFieldMaster.getColumnName());
                gridColumn.setGuildentifier(appFieldMaster.getGuildentifier()); 
                
                if(StringUtils.equalsIgnoreCase(appFieldMaster.getColumnName(),"FIRST_NAME") || StringUtils.equalsIgnoreCase(appFieldMaster.getColumnName(), "LAST_NAME"))
                gridColumn.setHidden(true);
                if(count <= 1) {
                    gridColumn.setLocked(false);
                }
                return gridColumn;
            }
        public GridColumn (String header, String datalndex, String stateld, int width, String icon, String align,
            String fieldType, String fieldName, String guildentifier, boolean locked, boolean hidden) {
            this.header = header;
            this.datalndex = datalndex;
            this.stateld = stateld;
            this.width = width;
            this.icon = icon;
            this.align = align;
            this.fieldType = fieldType;
            this.fieldName = fieldName;
            this.guildentifier = guildentifier;
            this.locked = locked;
            this.hidden = hidden;
        }
        
        public GridColumn() {
        }

    // Add explicit setters for all fields referenced in mapGridColumnFields
    public void setHeader(String header) { this.header = header; }
    public void setDatalndex(String datalndex) { this.datalndex = datalndex; }
    public void setStateld(String stateld) { this.stateld = stateld; }
    public void setWidth(int width) { this.width = width; }
    public void setIcon(String icon) { this.icon = icon; }
    public void setAlign(String align) { this.align = align; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    /**
     * Explicit getter for fieldName to ensure compilation.
     */
    public String getFieldName() { return this.fieldName; }
    public void setGuildentifier(String guildentifier) { this.guildentifier = guildentifier; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

}
