package com.example.enhancedsearch.dto;

import lombok.Data;
import lombok.ToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.enhancedsearch.model.FilterCriteria;
import com.example.enhancedsearch.model.SortCriteria;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.TypeFactory;

@Data
@ToString(includeFieldNames = true)
@JsonIgnoreProperties(ignoreUnknown = true)

public class SearchCriteria {

    private String alertld;
    private String txnId;
    private String status;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date fromDate;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private Date toDate;

    @JsonProperty(value="sourceSystem")
    private String application;
    private String businessUnit;
    @JsonProperty(value="assign")
    private String assignTo;

    private String userld;
    private String page;

    private String start;
    private String limit;

    private String requestType;
    private String sort;
    private String role;

    @JsonProperty(value= "filter")
    private List < FilterCriteria> filterList;

    public void convertSortToSortList() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<SortCriteria> sortList = new ArrayList<>(Arrays.asList(mapper.readValue(sort, 
        TypeFactory.defaultInstance().constructArrayType(SortCriteria.class))));
        sort = sortList.get(0).getProperty() +""+  sortList.get(0).getDirection();
    }

    public String columnName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'columnName'");
    }

    // Add explicit getter for filterList to resolve missing symbol errors
    public List<FilterCriteria> getFilterList() {
        return filterList;
    }
}