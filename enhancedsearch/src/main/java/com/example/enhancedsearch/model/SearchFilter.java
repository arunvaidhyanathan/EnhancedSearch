package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@ToString(includeFieldNames = true)
public class SearchFilter {
    @JsonProperty ("alertDet")
    public ArrayList<Filter> alertDet;
    @JsonProperty("hitMatchDetail")
    public ArrayList<Filter> hitMatchDetail;
    @JsonProperty("alertMst")
    public ArrayList<Filter> alertMst;
}
