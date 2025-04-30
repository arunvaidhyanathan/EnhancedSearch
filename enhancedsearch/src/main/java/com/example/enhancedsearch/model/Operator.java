package com.example.enhancedsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Operator {
    @JsonProperty("like")
    LIKE("like"),
    @JsonProperty("eq")
    EQ("="),
    @JsonProperty("gt")
    GT(">"),
    @JsonProperty("lt")
    LT("<"),
    @JsonProperty("IN")
    IN("in");

    private String operator;

    private Operator(String operator) {
        this.operator = operator;
    }

    public String toOperator() {
        return operator;
    }
}