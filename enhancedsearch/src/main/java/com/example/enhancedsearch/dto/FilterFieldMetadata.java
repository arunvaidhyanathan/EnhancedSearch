package com.example.enhancedsearch.dto;

public class FilterFieldMetadata {
    private String field_name;
    private String field_group;
    private int display_order;
    private String field_type;

    public FilterFieldMetadata() {}
    public FilterFieldMetadata(String field_name, String field_group, int display_order, String field_type) {
        this.field_name = field_name;
        this.field_group = field_group;
        this.display_order = display_order;
        this.field_type = field_type;
    }
    public String getField_name() { return field_name; }
    public void setField_name(String field_name) { this.field_name = field_name; }
    public String getField_group() { return field_group; }
    public void setField_group(String field_group) { this.field_group = field_group; }
    public int getDisplay_order() { return display_order; }
    public void setDisplay_order(int display_order) { this.display_order = display_order; }
    public String getField_type() { return field_type; }
    public void setField_type(String field_type) { this.field_type = field_type; }
}
