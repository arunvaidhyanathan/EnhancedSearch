package com.example.enhancedsearch.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

import com.example.enhancedsearch.model.GridColumn;

@Data
@ToString(includeFieldNames = true)
public class SolSearchCriteria {
    @JsonProperty("limit")
    int docPerPage;
    @JsonProperty("page")
    int currentPage;
    int startRow;
    public int getStartRow() { return (currentPage-1)*docPerPage; }
    String sortField;
    String sort;
    String userld;
    int buCount;
    ArrayList<SearchFilter> searchFilters;
    ArrayList<GridColumn> columns;
    /**
     * Getter for columns: resolves missing Lombok getter.
     */
    public List<GridColumn> getColumns() {
        return this.columns;
    }
    boolean fromExcel;
    int total;

    public void convertSortToSortList() throws IOException {
    }
}
