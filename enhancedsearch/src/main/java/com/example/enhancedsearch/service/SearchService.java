package com.example.enhancedsearch.service;

import com.example.enhancedsearch.dto.SearchCriteria;
import com.example.enhancedsearch.dto.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // YYYY-MM-DD
    // Basic protection against non-alphanumeric column names
    private static final String COLUMN_NAME_PATTERN = "^[a-zA-Z0-9_]+$";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> performSearch(SearchRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.criteria()) || request.tableName() == null || request.tableName().isBlank()) {
            log.warn("Invalid search request received: Empty criteria or missing table name.");
            return Collections.emptyList();
        }

        // Sanitize table name
        String tableName = sanitizeIdentifier(request.tableName());
        if (tableName == null) {
             log.error("Invalid table name provided: {}", request.tableName());
             // Or throw specific exception
             return Collections.emptyList();
        }
        // Use 'cads' schema as specified
        String qualifiedTableName = "cads." + tableName;

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ");
        queryBuilder.append(qualifiedTableName);
        queryBuilder.append(" WHERE ");

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (SearchCriteria criteria : request.criteria()) {
            String columnName = sanitizeIdentifier(criteria.columnName());
            if (columnName == null) {
                log.warn("Invalid column name '{}' in criteria, skipping.", criteria.columnName());
                continue; // Skip this criterion
            }

            try {
                String conditionClause = buildConditionClause(criteria, columnName);
                if (conditionClause != null) {
                    conditions.add(conditionClause);
                    addParameters(criteria, params);
                }
            } catch (IllegalArgumentException e) {
                log.warn("Skipping criteria due to invalid argument: {} - {}", criteria, e.getMessage());
                // Potentially collect errors and return a bad request status later
            }
        }

        if (conditions.isEmpty()) {
            log.warn("No valid search conditions could be built from the request.");
            return Collections.emptyList(); // Or maybe return all results? Depends on requirements.
        }

        queryBuilder.append(conditions.stream().collect(Collectors.joining(" AND ")));

        // Add partition filtering if applicable (as mentioned in Prod.MD overview)
        // This is a simple example assuming a 'partition_date' column exists and is relevant.
        // More complex partitioning logic might be needed.
        // Example: Filter partitions based on date ranges in the criteria
        addPartitionFilter(request.criteria(), queryBuilder, params);

        // Add ORDER BY, LIMIT, OFFSET for pagination/sorting if needed
        // queryBuilder.append(" ORDER BY some_column LIMIT ? OFFSET ?");
        // params.add(request.size());
        // params.add(request.page() * request.size());

        String finalQuery = queryBuilder.toString();
        log.info("Executing search query: {}", finalQuery);
        log.debug("With parameters: {}", params);

        try {
            return jdbcTemplate.queryForList(finalQuery, params.toArray());
        } catch (DataAccessException e) {
            log.error("Error executing search query [{}]: {}", finalQuery, e.getMessage(), e);
            // Re-throw a custom exception or return an empty list/error indicator
            return Collections.emptyList();
        }
    }

    private String buildConditionClause(SearchCriteria criteria, String columnName) {
        switch (criteria.condition()) {
            case EQUALS:
                return columnName + (criteria.isDate() ? " = ?::date" : " = ?");
            case LIKE:
                // Using standard LIKE. For pg_trgm similarity, you'd use % or similarity() function.
                 // Ensure value is wrapped with %
                return columnName + " LIKE ?";
            case LESS_THAN_EQUALS:
                return columnName + (criteria.isDate() ? " <= ?::date" : " <= ?");
            case GREATER_THAN_EQUALS:
                return columnName + (criteria.isDate() ? " >= ?::date" : " >= ?");
            case BETWEEN:
                if (criteria.isDate()) {
                    return columnName + " BETWEEN ?::date AND ?::date";
                } else {
                    return columnName + " BETWEEN ? AND ?";
                }
            case REGEX:
                // Using PostgreSQL specific regex operator ~
                // Add ~* for case-insensitive if needed
                return columnName + " ~ ?";
            default:
                log.warn("Unsupported search condition: {}", criteria.condition());
                return null;
        }
    }

    private void addParameters(SearchCriteria criteria, List<Object> params) throws IllegalArgumentException {
        try {
            switch (criteria.condition()) {
                case BETWEEN:
                    if (criteria.isDate()) {
                        params.add(parseDate(criteria.value(), criteria.columnName()));
                        params.add(parseDate(criteria.value2(), criteria.columnName()));
                    } else {
                        // Assuming numeric or text for non-date BETWEEN
                        params.add(criteria.value());
                        params.add(criteria.value2());
                    }
                    break;
                case LIKE:
                    // Add wildcards for LIKE search
                     params.add("%" + criteria.value() + "%");
                     break;
                case EQUALS:
                case LESS_THAN_EQUALS:
                case GREATER_THAN_EQUALS:
                case REGEX:
                    if (criteria.isDate()) {
                        params.add(parseDate(criteria.value(), criteria.columnName()));
                    } else {
                        params.add(criteria.value());
                    }
                    break;
                 default:
                     // Should not happen if buildConditionClause handles all cases
                     break;
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for column '" + criteria.columnName() +
                                               "'. Expected format: YYYY-MM-DD", e);
        }
    }

    private LocalDate parseDate(String dateValue, String columnName) {
         if (dateValue == null || dateValue.isBlank()) {
             throw new IllegalArgumentException("Date value cannot be empty for column '" + columnName + "'");
         }
         return LocalDate.parse(dateValue, DATE_FORMATTER);
    }

    // Example partition filter - adjust based on actual partitioning strategy
    private void addPartitionFilter(List<SearchCriteria> criteria, StringBuilder queryBuilder, List<Object> params) {
        // Look for criteria related to the partition key (e.g., 'partition_date')
        List<SearchCriteria> partitionCriteria = criteria.stream()
                .filter(c -> "partition_date".equalsIgnoreCase(c.columnName())) // Assuming partition key is 'partition_date'
                .toList();

        if (!partitionCriteria.isEmpty()) {
            // If explicit partition filters are already added, we might not need this.
            // This is a simple example: if *any* partition_date criteria exists,
            // we assume the main WHERE clause handles it. More sophisticated logic
            // might be needed to *ensure* partition pruning happens if no relevant
            // criteria were provided, e.g., adding a default date range.
            log.debug("Partition key criteria found, assuming WHERE clause handles partitioning.");
        } else {
            // Optional: Add a default filter if no partition criteria are specified
            // to prevent scanning all partitions unnecessarily.
            // E.g., queryBuilder.append(" AND partition_date >= ?"); params.add(LocalDate.now().minusMonths(3));
            log.debug("No partition key criteria found. Consider adding default filters for performance.");
        }
    }

    private String sanitizeIdentifier(String identifier) {
         if (identifier == null || identifier.isBlank()) {
             return null;
         }
         String trimmed = identifier.trim();
         // Basic check against common injection patterns and ensure alphanumeric/underscore
         if (!trimmed.matches(COLUMN_NAME_PATTERN) || trimmed.contains("--") || trimmed.contains(";") || trimmed.contains("/*")) {
             log.warn("Potentially unsafe identifier detected and rejected: {}", identifier);
             return null;
         }
         // Return the validated identifier (consider quoting if needed, e.g., using `"`) - requires DB specific handling
         return trimmed; // Keep it simple for now
    }

}
