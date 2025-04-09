package com.example.enhancedsearch.service;

import com.example.enhancedsearch.config.SearchConfig;
import com.example.enhancedsearch.dto.SearchCondition;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class IndexManager {

    private static final Logger log = LoggerFactory.getLogger(IndexManager.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IndexManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        log.info("Initializing database indexes...");
        List<SearchConfig> searchConfigs = loadSearchConfigs();
        if (searchConfigs.isEmpty()) {
            log.warn("No index configurations found in search_config table. No indexes will be created automatically.");
            return;
        }
        log.info("Found {} index configurations.", searchConfigs.size());
        for (SearchConfig config : searchConfigs) {
            createIndex(config);
        }
        log.info("Finished initializing indexes.");
    }

    private void createIndex(SearchConfig config) {
        // Basic validation
        if (config.tableName() == null || config.tableName().isBlank() ||
            config.columnName() == null || config.columnName().isBlank() ||
            config.searchType() == null) {
            log.warn("Skipping invalid search config: {}", config);
            return;
        }

        // Sanitize table and column names (basic protection against SQL injection in DDL)
        String tableName = sanitizeIdentifier(config.tableName());
        String columnName = sanitizeIdentifier(config.columnName());
        String indexName = "idx_" + tableName + "_" + columnName; // Simple naming convention

        String indexType;
        String columnSpec;

        switch (config.searchType()) {
            case LIKE:
            case REGEX:
                // Use GIN with pg_trgm for LIKE and REGEX as per Prod.MD
                indexType = "GIN";
                columnSpec = columnName + " gin_trgm_ops";
                log.info("Creating GIN index {} on {}({}) for LIKE/REGEX", indexName, tableName, columnName);
                break;
            case BETWEEN:
            case EQUALS:
            case LESS_THAN_EQUALS:
            case GREATER_THAN_EQUALS:
                // Use standard B-tree index for other operators
                indexType = "BTREE"; // Default index type, often implicit
                columnSpec = columnName;
                log.info("Creating standard BTREE index {} on {}({}) for comparison/equality", indexName, tableName, columnName);
                break;
            default:
                log.warn("Unsupported search type {} for index creation on {}.{}. Skipping.",
                         config.searchType(), tableName, columnName);
                return;
        }

        // Note: Using schema 'cads' as mentioned in Prod.MD
        // If the schema is different or configurable, adjust this.
        String schema = "cads";
        String qualifiedTableName = sanitizeIdentifier(schema) + "." + tableName;

        String sql = String.format("CREATE INDEX IF NOT EXISTS %s ON %s USING %s (%s)",
                                   sanitizeIdentifier(indexName), qualifiedTableName, indexType, columnSpec);

        try {
            jdbcTemplate.execute(sql);
            log.debug("Executed: {}", sql);
        } catch (DataAccessException e) {
            log.error("Failed to create index {} on {}: {}", indexName, qualifiedTableName, e.getMessage());
            // Decide if this should be a fatal error or just a warning
        }
    }

    private List<SearchConfig> loadSearchConfigs() {
        // Assuming search_config table is in the 'cads' schema
        String sql = "SELECT table_name, column_name, search_type FROM cads.search_config";
        try {
            return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    try {
                        // Handle potential invalid enum values gracefully
                        SearchCondition condition = SearchCondition.valueOf(rs.getString("search_type").toUpperCase());
                        return new SearchConfig(
                            rs.getString("table_name"),
                            rs.getString("column_name"),
                            condition
                        );
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid search_type '{}' found in cads.search_config for table '{}', column '{}'. Skipping config.",
                                 rs.getString("search_type"), rs.getString("table_name"), rs.getString("column_name"));
                        return null; // Skip this invalid configuration
                    }
                }
            ).stream().filter(java.util.Objects::nonNull).toList(); // Filter out nulls from invalid configs
        } catch (DataAccessException e) {
            log.error("Failed to load search configurations from cads.search_config: {}. Ensure the table exists and is accessible.", e.getMessage());
            return Collections.emptyList(); // Return empty list on error
        }
    }

    // Basic identifier sanitization to prevent SQL injection in DDL
    private String sanitizeIdentifier(String identifier) {
        if (identifier == null) return null;
        // Remove potentially harmful characters. Allows letters, numbers, underscores.
        // Adjust regex as needed for your specific naming conventions.
        return identifier.replaceAll("[^a-zA-Z0-9_]", "");
    }
}
