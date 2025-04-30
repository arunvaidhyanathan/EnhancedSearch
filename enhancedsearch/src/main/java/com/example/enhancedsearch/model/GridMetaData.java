package com.example.enhancedsearch.model;

import java.util.List;

import lombok.Data;
@Data
public class GridMetaData {
private List<GridColumn> columns;
private List<GridFields> fields;
}
