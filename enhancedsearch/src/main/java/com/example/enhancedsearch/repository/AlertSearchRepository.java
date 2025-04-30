package com.example.enhancedsearch.repository;

import java.util.List;
import java.util.Map;

import com.example.enhancedsearch.dto.SearchCriteria;
import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.model.CurrencyBuDto;
import com.example.enhancedsearch.model.EnhancedSearchField;
import com.example.enhancedsearch.model.UserBuCriteria;

public interface AlertSearchRepository {


    public List<AppFieldMaster> loadAppFieldMaster(); //Done

    List<AppFieldMaster> loadEnhancedSearchFieldMaster(); //Done

    List<Map<String, Object>> getAlertsByCriteria(String userld, SearchCriteria criteria); //Done

    List<CurrencyBuDto> getAvaliableCurrencyOfUserData(String userld); 

    List<EnhancedSearchField> loadEnhancedSearchFieldMasterData();

    List<CurrencyBuDto> getBusOfUserData(String userld);

    List<AppFieldMaster> loadEnhancedSearchFieldMasterForFilteredFields(); //Done

    List<UserBuCriteria> getUserBuData(List<String> userlds);

    List<Map<String, Object>> getSortableListData();

    }
