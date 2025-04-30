package com.example.enhancedsearch.model;

import lombok.Data;

@Data
public class GridFields {
    private String name; 
    private String type;

    // Add explicit getters and setters for compatibility
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
