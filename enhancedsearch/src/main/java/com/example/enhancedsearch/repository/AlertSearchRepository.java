package com.example.enhancedsearch.repository;

import com.example.enhancedsearch.entity.AlertMst;
import com.example.enhancedsearch.model.AppFieldMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
@Repository
public interface AlertSearchRepository extends JpaRepository<AlertMst, Long>, JpaSpecificationExecutor<AlertMst> {
    // You can keep the custom methods here or move them to another interface/class if needed
    //public List<AppFieldMaster> loadAppFieldMaster(); //Done
    //List<AppFieldMaster> loadEnhancedSearchFieldMaster(); //Done
    //List<Map<String, Object>> getAlertsByCriteria(String userld, SearchCriteria criteria); //Done
    //List<CurrencyBuDto> getAvaliableCurrencyOfUserData(String userld);
    //List<EnhancedSearchField> loadEnhancedSearchFieldMasterData();
    //List<CurrencyBuDto> getBusOfUserData(String userld);
    //List<AppFieldMaster> loadEnhancedSearchFieldMasterForFilteredFields(); //Done
    //List<UserBuCriteria> getUserBuData(List<String> userlds);
    //List<Map<String, Object>> getSortableListData();
}
