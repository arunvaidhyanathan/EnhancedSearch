package com.example.enhancedsearch.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FieldMetadataRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FieldMetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> fetchFieldMetadata() {
        String sql = "select collection_name as table_name, field_name as column_name, field_type as column_type, regex_enabled from cads.enhanced_search_fields esf, cads.gui_label gl where esf.gui_identifier = gl.gui_identifier and esf.is_active = 'Y'";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Map<String, Object>> getFieldMetadataMap() {
        List<Map<String, Object>> metaList = fetchFieldMetadata();
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (Map<String, Object> row : metaList) {
            String key = row.get("table_name") + "." + row.get("column_name");
            result.put(key, row);
        }
        return result;
    }
}
