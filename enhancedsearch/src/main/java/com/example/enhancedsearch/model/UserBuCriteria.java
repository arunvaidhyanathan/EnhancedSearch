package com.example.enhancedsearch.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString(includeFieldNames = true)
public class UserBuCriteria {
    private String userId;
    private String businessUnit;
    private String createdDate;
    private String createdBy;

    public UserBuCriteria(String userId, String businessUnit) {
        this.userId = userId;
        this.businessUnit = businessUnit;
    }

    public UserBuCriteria() {
    }

}
