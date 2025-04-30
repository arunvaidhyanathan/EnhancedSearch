package com.example.enhancedsearch.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.service.AlertSearchServiceCacheImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Repository
public class FieldMetadataRepository {
    private final JdbcTemplate jdbcTemplate;

    private AlertSearchServiceCacheImpl alertSearchServiceCacheImpl;


    @Autowired
    public FieldMetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> fetchFieldMetadata() {
        String sql = "select collection_name as table_name, field_name as column_name, field_type as column_type, regex_enabled from cads.enhanced_search_fields esf, cads.gui_label gl where esf.gui_identifier = gl.gui_identifier and esf.is_active = 'Y'";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, AppFieldMaster> getFieldMetadataMap() {
        List<Map<String, AppFieldMaster>> metaList = (List<Map<String, AppFieldMaster>>) alertSearchServiceCacheImpl.loadAppFieldMasterData();
        Map<String, AppFieldMaster> result = new HashMap<>();
        for (Map<String, AppFieldMaster> row : metaList) {
            String key = row.get("table_name") + "." + row.get("column_name");
            result.put(key, row.get("column_name"));
        }
        return result;
    }
}
