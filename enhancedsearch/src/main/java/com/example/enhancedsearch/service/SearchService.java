package com.example.enhancedsearch.service;

import com.example.enhancedsearch.dto.SearchCriteria;
import com.example.enhancedsearch.dto.SearchRequest;
import com.example.enhancedsearch.model.AppFieldMaster;
import com.example.enhancedsearch.model.DynamicList;
import com.example.enhancedsearch.model.GridColumn;
import com.example.enhancedsearch.model.GridFields;
import com.example.enhancedsearch.model.GridMetaData;
import com.example.enhancedsearch.model.Response;
import com.example.enhancedsearch.model.SolSearchCriteria;
import com.example.enhancedsearch.dto.DataSearchRequest;
import com.example.enhancedsearch.dto.SearchFilter;
import com.example.enhancedsearch.repository.AlertSearchRepositoryimpl;
import com.example.enhancedsearch.repository.FieldMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.expression.spel.ast.Operator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
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
    private FieldMetadataRepository fieldMetadataRepository;
    
    private AlertSearchRepositoryimpl alertSearchRepositoryImpl;

    @Autowired
    public SearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DynamicList search(SolSearchCriteria solrSearchCriteria) throws IOException ,ParseException{
        Response response = new Response();
        DynamicList data = new DynamicList();
        GridMetaData metaData = new GridMetaData();
        List<GridColumn> columns;
        List<GridFields> fields;
        LinkedHashSet<String> userSelectedFields = new LinkedHashSet<>();
        LinkedHashSet<String> userSelectedAlertDetColumns = new LinkedHashSet<>();
        buildDBQuery(solrSearchCriteria, userSelectedFields, userSelectedAlertDetColumns);
        
        try {
            LinkedHashSet<String> totalFields = new LinkedHashSet<>();
            columns = getColumnList(solrSearchCriteria, userSelectedFields,totalFields, userSelectedAlertDetColumns);
            if(!columns.isEmpty()) {
                fields = GridColumn.getGridFields(columns);
            }
            StringBuilder selectedField = new StringBuilder();
            totalFields.forEach(field -> {
                if(selectedField.length()>0) {
                    selectedField.append(",");
                }
                if(field.equalsIgnoreCase("ID")) {
                    selectedField.append("ID:ALERT_ID");
                } else if(field.equalsIgnoreCase("PP_MESSAGE")) {
                    selectedField.append("PP_MESSAGE:PP_MESSAGE");
                } else {
                    selectedField.append(field);
                }
            });
            selectedField.append("*");
        } catch (Exception e) {
            log.error("Error during search", e);
        }
        return data;
    }

    private List<GridColumn> getColumnList(SolSearchCriteria solrSearchCriteria,
            LinkedHashSet<String> userSelectedFields,
            LinkedHashSet<String> totalFields, LinkedHashSet<String> alertDetColumns) {
                List<GridColumn> columns;

                if (solrSearchCriteria.getColumns() != null && !solrSearchCriteria.getColumns().isEmpty()) {
                    solrSearchCriteria.getColumns().forEach(f -> totalFields.add(f.getFieldName()));
                
                for (String field : userSelectedFields) {
                    if (!totalFields.contains(field)) {
                        totalFields.add(field);
                    }
                }
                totalFields.addAll(alertDetColumns);
                columns = getHeaderInfoForFilteredFields(totalFields);
                totalFields.removeAll(alertDetColumns);
            } else {
                Map<String, AppFieldMaster> appFieldMasterMap = fieldMetadataRepository.getFieldMetadataMap();
                totalFields.addAll(appFieldMasterMap.keySet());
                columns = getHeaderInfo(totalFields);
            }
            return columns;
    }

    private List<GridColumn> getHeaderInfoForFilteredFields(LinkedHashSet<String> totalFields) {
        Map<String, AppFieldMaster> appFieldMasterMap = (Map<String, AppFieldMaster>) alertSearchRepositoryImpl.loadEnhancedSearchFieldMasterForFilteredFields();
        List<GridColumn> gridColumnsList = new ArrayList<>();
        int count = 0;
        for (String element: totalFields) {
            if (appFieldMasterMap.get(element) == null)
                continue;
            gridColumnsList.add(new GridColumn().mapGridColumnFields(appFieldMasterMap.get(element), ++count));
        }
        return gridColumnsList;
    }

    private List<GridColumn> getHeaderInfo(LinkedHashSet<String> totalFields) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderInfo'");
    }

    private void buildDBQuery(SolSearchCriteria solrSearchCriteria, LinkedHashSet<String> userSelectedFields, LinkedHashSet<String> userSelectedAlertDetColumns) {
        //Perform the database query
    
    }

    private String getTableName(String collectionName) {
        if ("alert_mst".equals(collectionName)) return "alert_mst";
        if ("alert_cstm_attribute".equals(collectionName)) return "alert_cstm_attribute";
        if ("hit_match_details".equals(collectionName)) return "hit_match_details";
        if ("alert_det".equals(collectionName)) return "alert_det";
        return collectionName;
    }

    private String getSqlOperator(String operation, String fieldType) {
        switch (operation.toUpperCase()) {
            case "=": return "=";
            case "!=": return "!=";
            case "<=": return "<=";
            case ">=": return ">=";
            case "LIKE": return "ILIKE";
            case "BETWEEN": return "BETWEEN";
            case "REGEX": return "~*";
            default: return "=";
        }
    }

    private String buildClause(SearchFilter f, Map<String, Object> meta, String op) {
        String fieldExpr;
        if ("alert_det".equals(f.getCollectionName())) {
            // Special: search field_value where field_name_alias = ?
            fieldExpr = "field_value";
            return "field_name_alias = ? AND " + fieldExpr + " " + op + " ?";
        } else {
            fieldExpr = f.getFieldName();
            if ("REGEX".equalsIgnoreCase(f.getOperation())) {
                return fieldExpr + " ~* ?";
            } else if ("BETWEEN".equalsIgnoreCase(f.getOperation())) {
                return fieldExpr + " BETWEEN ? AND ?";
            } else {
                return fieldExpr + " " + op + " ?";
            }
        }
    }

    private void addSqlParameters(List<Object> params, SearchFilter f, String op) {
        if ("alert_det".equals(f.getCollectionName())) {
            params.add(f.getFieldName()); // alias
            params.add(f.getFieldValue());
        } else if ("BETWEEN".equalsIgnoreCase(f.getOperation())) {
            params.add(f.getFieldValue());
            params.add(f.getFieldValueTo());
        } else {
            if ("LIKE".equalsIgnoreCase(f.getOperation())) {
                params.add("%" + f.getFieldValue() + "%");
            } else {
                params.add(f.getFieldValue());
            }
        }
    }

    private String buildConditionClause(Operator operator, String columnName) {
        /*switch (operator) {
            case EQUALS:
                return columnName + (operator.isDate() ? " = ?::date" : " = ?");
            case LIKE:
                // Using standard LIKE. For pg_trgm similarity, you'd use % or similarity() function.
                 // Ensure value is wrapped with %
                return columnName + " LIKE ?";
            case LESS_THAN_EQUALS:
                return columnName + (operator.isDate() ? " <= ?::date" : " <= ?");
            case GREATER_THAN_EQUALS:
                return columnName + (operator.isDate() ? " >= ?::date" : " >= ?");
            case BETWEEN:
                if (operator.isDate()) {
                    return columnName + " BETWEEN ?::date AND ?::date";
                } else {
                    return columnName + " BETWEEN ? AND ?";
                }
            case REGEX:
                // Using PostgreSQL specific regex operator ~
                // Add ~* for case-insensitive if needed
                return columnName + " ~ ?";
            default:
                log.warn("Unsupported search condition: {}", criteria.condition());*/
                return null;
        
    }

    private void addParameters(SearchCriteria criteria, List<Object> params) throws IllegalArgumentException {
        /*try {
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
        }*/
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
