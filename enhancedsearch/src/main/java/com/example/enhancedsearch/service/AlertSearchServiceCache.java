package com.example.enhancedsearch.service;

import java.util.List;
import java.util.Map;

import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.model.CurrencyBuDto;

public interface AlertSearchServiceCache {
    Map<String, AppFieldMaster> loadAppFieldMasterData();
    Map<String, AppFieldMaster> loadSolrFieldMasterData();
    Map<String, AppFieldMaster> loadSolrFieldMasterDataForFilteredFields();
    List<CurrencyBuDto> getAvaliableCurrencyOfUser(String userld);
    List<CurrencyBuDto> getBusOfUser(String userld);
    List<Map<String, Object>> getSortableList();
}
