package com.example.enhancedsearch.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@JsonInclude(content =  Include.NON_NULL)
public class Response {
    int numFound;
    int start;

    long total;
    @JsonProperty("items")
    List<Map<String, Object>> items;
}
