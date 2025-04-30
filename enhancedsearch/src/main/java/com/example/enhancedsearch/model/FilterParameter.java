package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FilterParameter {
    private String applicationNames;
    private String userld; 
    private String module;
    private String businessUnit;
    private String messageType;
}
