package com.example.enhancedsearch.service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.model.CurrencyBuDto;
import com.example.enhancedsearch.repository.AlertSearchRepository;

public class AlertSearchServiceCacheImpl implements AlertSearchServiceCache{

    @Autowired
    private AlertSearchRepository alertSearchRepository;

    @Override   
    @Cacheable(cacheNames = {"appFieldMasterCache"})
    public Map<String, AppFieldMaster> loadAppFieldMasterData()
    {
        
        List< AppFieldMaster> appFieldMasterList = alertSearchRepository.loadAppFieldMaster();
        Map<String, AppFieldMaster> appFieldMasterMap = new LinkedHashMap<>();
        if (appFieldMasterList != null && !appFieldMasterList.isEmpty()){
            appFieldMasterList.stream().forEach(obj -> appFieldMasterMap.put(obj.getColumnName(), obj));
            return appFieldMasterMap;
        }
        return appFieldMasterMap;
    }

    @Override 
    @Cacheable(cacheNames = {"solrFieldMasterCache"})
    public Map<String, AppFieldMaster> loadSolrFieldMasterData() {
        List<AppFieldMaster> appFieldMasterList = alertSearchRepository.loadEnhancedSearchFieldMaster();
        Map<String, AppFieldMaster> appFieldMasterMap = new LinkedHashMap<>();
        if (appFieldMasterList != null && !appFieldMasterList.isEmpty()) {
            appFieldMasterList.stream().forEach(obj -> appFieldMasterMap.put(obj.getColumnName(), obj));
            return appFieldMasterMap;
        }
        return appFieldMasterMap;
    }

    @Override
    public Map<String, AppFieldMaster> loadSolrFieldMasterDataForFilteredFields() {
        List<AppFieldMaster> appFieldMasterList = alertSearchRepository.loadEnhancedSearchFieldMasterForFilteredFields();
        Map<String, AppFieldMaster> appFieldMasterMap = new LinkedHashMap<>();
        if (appFieldMasterList != null && !appFieldMasterList.isEmpty()) {
            appFieldMasterList.stream().forEach(obj -> appFieldMasterMap.put(obj.getColumnName(), obj));
            return appFieldMasterMap;
        }
        return appFieldMasterMap;
    }

    @Override
    @Cacheable(cacheNames = {"currencyOfUserCache"})
    public List<CurrencyBuDto> getAvaliableCurrencyOfUser(String userld) {
        return alertSearchRepository.getAvaliableCurrencyOfUserData(userld);
    }
    @Override
    @Cacheable(cacheNames = {"busOfUserCache"})
    public List<CurrencyBuDto> getBusOfUser(String userld) {
        return alertSearchRepository.getBusOfUserData(userld);
    }

    @Override
    public List<Map<String, Object>> getSortableList() {
        return alertSearchRepository.getSortableListData();
    }
}
