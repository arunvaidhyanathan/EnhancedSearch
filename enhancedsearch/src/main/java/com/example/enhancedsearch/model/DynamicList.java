package com.example.enhancedsearch.model;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicList {
    private GridMetaData metaData;
    private Long total = 0L;
    private List<Map<String, Object>> items;
    }