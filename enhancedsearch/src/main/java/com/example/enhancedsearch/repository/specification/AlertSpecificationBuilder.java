package com.example.enhancedsearch.repository.specification;

import com.example.enhancedsearch.dto.FilterCriteria;
import com.example.enhancedsearch.entity.*;

import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AlertSpecificationBuilder {

    private static final Logger log = LoggerFactory.getLogger(AlertSpecificationBuilder.class);

    // --- Field Name Mapping (UI Name -> Entity Path Element) ---
    // Consider loading this from cads.enhanced_search_fields at startup for dynamic behavior
    private static final Map<String, String> ALERT_MST_FIELD_MAP = Map.ofEntries(
            Map.entry("Application", "sourceSystem"),
            Map.entry("Receiver", "receiver"),
            Map.entry("Creation Date/Time", "createdDate"),
            Map.entry("Value Date", "valueDate"),
            Map.entry("Assigned To", "assignedTo"),
            Map.entry("Step", "step"),
            Map.entry("Alert ID", "alertBusinessId"), // Adjust if different
            Map.entry("Match Accuracy %", "matchAccuracy"),
            Map.entry("Business Unit", "businessUnit")
            // Add other direct alert_mst fields here
    );

    // Specific field names in related tables
    private static final String ALERT_DET_ALIAS_FIELD = "fieldNameAlias";
    private static final String ALERT_DET_VALUE_FIELD = "fieldValue";
    private static final String ALERT_CSTM_ATTR_FIELD = "ppMessage"; // Corresponds to "Transaction Message" UI field
    private static final String ALERT_CSTM_ATTR_UI_NAME = "Transaction Message";
    // Add mappings for hit_match_details if needed

    // Pre-compile date formatters for efficiency and consistency
    // Adjust formats based on the EXACT format in the input JSON 'fieldValue'
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME; // e.g., 2023-11-15T18:30:00.000
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // e.g., 2025-04-01
    // Example format for UI: "15-Nov-2023" - Requires a custom formatter
    private static final DateTimeFormatter UI_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


    public static Specification<AlertMst> build(List<FilterCriteria> filters) {
        if (filters == null || filters.isEmpty()) {
            return Specification.where(null); // No filters means return everything (or handle as error if needed)
        }

        Specification<AlertMst> finalSpec = null;

        for (FilterCriteria filter : filters) {
            if (filter == null || !StringUtils.hasText(filter.getFieldName()) || !StringUtils.hasText(filter.getOperation())) {
                 log.warn("Skipping invalid filter criteria: {}", filter);
                 continue;
            }
            Specification<AlertMst> currentSpec = createFilterSpecification(filter);

            if (finalSpec == null) {
                finalSpec = Specification.where(currentSpec);
            } else {
                // Use filterCondition for subsequent filters
                if ("OR".equalsIgnoreCase(filter.getFilterCondition())) {
                    finalSpec = finalSpec.or(currentSpec);
                } else { // Default to AND if null, empty, or "AND"
                    finalSpec = finalSpec.and(currentSpec);
                }
            }
        }
        return finalSpec;
    }

    private static Specification<AlertMst> createFilterSpecification(FilterCriteria filter) {
        return (root, query, cb) -> {
            try {
                switch (filter.getCollectionName().toLowerCase()) {
                    case "user_bu_map":
                        return buildUserBuMapPredicate(cb, query, root, filter);
                    case "alert_mst":
                        return buildAlertMstPredicate(cb, root, filter);
                    case "alert_det":
                        return buildAlertDetSubqueryPredicate(cb, query, root, filter);
                    case "alert_cstm_attribute":
                        return buildAlertCstmAttributeSubqueryPredicate(cb, query, root, filter);
                    case "hit_match_details":
                        // Implement if hit_match_details filtering is needed
                        // return buildHitMatchDetailsSubqueryPredicate(cb, query, root, filter);
                        log.warn("Filtering on collection 'hit_match_details' not yet implemented.");
                        return cb.conjunction(); // Returns TRUE, effectively skipping filter
                    default:
                        log.warn("Unsupported collection name: {}", filter.getCollectionName());
                        return cb.conjunction(); // Ignore unknown collections
                }
            } catch (Exception e) {
                log.error("Error building predicate for filter: {} - Error: {}", filter, e.getMessage(), e);
                // Return a predicate that evaluates to false to exclude potentially problematic data
                // Or throw a custom exception to be handled upstream
                return cb.disjunction(); // Represents FALSE
            }
        };
    }

    // --- Predicate Builders for Specific Collections ---

    private static Predicate buildAlertMstPredicate(CriteriaBuilder cb, Root<AlertMst> root, FilterCriteria filter) {
        String entityFieldName = ALERT_MST_FIELD_MAP.get(filter.getFieldName());
        if (entityFieldName == null) {
            log.warn("No mapping found for alert_mst field: {}", filter.getFieldName());
            return cb.conjunction(); // Skip filter if field mapping is missing
        }
        Path<?> path = root.get(entityFieldName);
        return buildPredicate(cb, path, filter);
    }

    private static Predicate buildUserBuMapPredicate(CriteriaBuilder cb, CriteriaQuery<?> query, Root<AlertMst> root, FilterCriteria filter) {
         Subquery<String> subquery = query.subquery(String.class);
        Root<UserBuMap> userBuMapRoot = subquery.from(UserBuMap.class);
        subquery.select(userBuMapRoot.get("buIdentifier")); // Select buIdentifier from UserBuMap

         // Join condition: alert_mst.business_unit = user_bu_map.bu_identifier
        Predicate joinPredicate = cb.equal(root.get("businessUnit"), userBuMapRoot.get("buIdentifier"));
        subquery.where(joinPredicate);
        String entityFieldName = ALERT_MST_FIELD_MAP.get(filter.getFieldName());
        if (entityFieldName == null) {
            log.warn("No mapping found for alert_mst field: {}", filter.getFieldName());
            return cb.conjunction(); // Skip filter if field mapping is missing
        }
        Path<?> path = root.get(entityFieldName);
        return buildPredicate(cb, path, filter);
    }

     private static Predicate buildAlertDetSubqueryPredicate(CriteriaBuilder cb, CriteriaQuery<?> query, Root<AlertMst> root, FilterCriteria filter) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AlertDet> detRoot = subquery.from(AlertDet.class);
        subquery.select(detRoot.get("id")); // Select any non-null column, id is fine

        // Predicate for matching field_name_alias
        Predicate aliasPredicate = cb.equal(detRoot.get(ALERT_DET_ALIAS_FIELD), filter.getFieldName());

        // Predicate for matching field_value based on operation/value
        // Note: Assuming field_value in alert_det is always treated as String for comparison here.
        // Adjust if alert_det.field_value needs dynamic type handling based on filter.fieldType
        Predicate valuePredicate = buildPredicate(cb, detRoot.get(ALERT_DET_VALUE_FIELD), filter);

        // Join condition: alert_det.alert_id = alert_mst.id
        Predicate joinPredicate = cb.equal(detRoot.get("alertMst").get("id"), root.get("id"));

        subquery.where(cb.and(joinPredicate, aliasPredicate, valuePredicate));

        return cb.exists(subquery);
    }

    private static Predicate buildAlertCstmAttributeSubqueryPredicate(CriteriaBuilder cb, CriteriaQuery<?> query, Root<AlertMst> root, FilterCriteria filter) {
        // Ensure this filter is specifically for the "Transaction Message" mapped field
        if (!ALERT_CSTM_ATTR_UI_NAME.equalsIgnoreCase(filter.getFieldName())) {
             log.warn("Filtering on alert_cstm_attribute is only supported for field name '{}', but got: {}", ALERT_CSTM_ATTR_UI_NAME, filter.getFieldName());
             return cb.conjunction(); // Skip if not the expected field
        }

        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AlertCstmAttribute> cstmRoot = subquery.from(AlertCstmAttribute.class);
        subquery.select(cstmRoot.get("id"));

        // Predicate for the value comparison on ppMessage
        Predicate valuePredicate = buildPredicate(cb, cstmRoot.get(ALERT_CSTM_ATTR_FIELD), filter);

        // Join condition
        Predicate joinPredicate = cb.equal(cstmRoot.get("alertMst").get("id"), root.get("id"));

        subquery.where(cb.and(joinPredicate, valuePredicate));

        return cb.exists(subquery);
    }


    // --- Generic Predicate Builder based on Operation & Type ---

    @SuppressWarnings({"unchecked", "rawtypes"}) // Needed for casting to Comparable
    private static Predicate buildPredicate(CriteriaBuilder cb, Path<?> path, FilterCriteria filter) {
        String operation = filter.getOperation().toUpperCase();
        String value = filter.getFieldValue();
        String valueTo = filter.getFieldValueTo();
        String fieldType = filter.getFieldType().toLowerCase();

        // Handle potential null value early
        if (value == null && !(operation.equals("IS NULL") || operation.equals("IS NOT NULL"))) {
             // Most operations require a non-null value. IS NULL/IS NOT NULL are exceptions.
             // Let specific operations handle null if they can, otherwise treat as invalid.
             // For now, returning TRUE to skip this filter part if value is null for common ops.
              log.warn("Null value provided for operation '{}' on field '{}', skipping predicate part.", operation, filter.getFieldName());
             return cb.conjunction(); // TRUE
        }


        switch (operation) {
            case "=":
                return cb.equal(path, parseValue(value, fieldType, path.getJavaType()));
            case "!=":
                return cb.notEqual(path, parseValue(value, fieldType, path.getJavaType()));

            case "LIKE":
                if (String.class.isAssignableFrom(path.getJavaType())) {
                    // Using lower for case-insensitive LIKE, requires DB index support or function-based index
                    return cb.like(cb.lower(path.as(String.class)), "%" + value.toLowerCase() + "%");
                } else {
                    log.warn("LIKE operation attempted on non-string field: {}", filter.getFieldName());
                    return cb.disjunction(); // FALSE
                }

            case "REGEX":
                 if (String.class.isAssignableFrom(path.getJavaType())) {
                    // Use native PostgreSQL regex function '~' (case-sensitive)
                    // For case-insensitive '~*', use "pg_catalog.texticregexeq"
                    Expression<Boolean> regexMatch = cb.function("pg_catalog.textregexeq", Boolean.class, path.as(String.class), cb.literal(value));
                    return cb.isTrue(regexMatch);
                 } else {
                    log.warn("RegEX operation attempted on non-string field: {}", filter.getFieldName());
                    return cb.disjunction(); // FALSE
                 }

            case ">=":
                 return cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) parseValue(value, fieldType, path.getJavaType()));
            case "<=":
                 return cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) parseValue(value, fieldType, path.getJavaType()));
            case ">":
                 return cb.greaterThan(path.as(Comparable.class), (Comparable) parseValue(value, fieldType, path.getJavaType()));
            case "<":
                 return cb.lessThan(path.as(Comparable.class), (Comparable) parseValue(value, fieldType, path.getJavaType()));

            case "BETWEEN":
                 if (!StringUtils.hasText(valueTo)) {
                     log.warn("BETWEEN operation requires 'fieldValueTo' for field: {}", filter.getFieldName());
                     return cb.disjunction(); // FALSE
                 }
                 Comparable from = (Comparable) parseValue(value, fieldType, path.getJavaType());
                 Comparable to = (Comparable) parseValue(valueTo, fieldType, path.getJavaType());
                 // Ensure 'from' is not after 'to' if necessary, depending on requirements
                 return cb.between(path.as(Comparable.class), from, to);

            // Add IS NULL / IS NOT NULL if needed
            // case "IS NULL":
            //     return cb.isNull(path);
            // case "IS NOT NULL":
            //     return cb.isNotNull(path);

            default:
                log.warn("Unsupported operation: {}", filter.getOperation());
                return cb.conjunction(); // TRUE - Skip unsupported operation
        }
    }

    // --- Value Parsing Helper ---
    private static Object parseValue(String value, String fieldType, Class<?> targetType) {
        if (value == null) return null;

        try {
            if (targetType.equals(String.class)) {
                return value;
            } else if (targetType.equals(Long.class)) {
                return Long.parseLong(value);
            } else if (targetType.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (targetType.equals(BigDecimal.class)) {
                return new BigDecimal(value);
            } else if (targetType.equals(Double.class)) {
                return Double.parseDouble(value);
            } else if (targetType.equals(Float.class)) {
                return Float.parseFloat(value);
            } else if (targetType.equals(LocalDateTime.class)) {
                 // Try multiple formats if needed
                try {
                    return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
                } catch (DateTimeParseException e) {
                    // Try the UI format "dd-MMM-yyyy" and assume start of day
                     LocalDate date = LocalDate.parse(value, UI_DATE_FORMATTER);
                     return date.atStartOfDay();
                }
            } else if (targetType.equals(LocalDate.class)) {
                 try {
                    return LocalDate.parse(value, DATE_FORMATTER);
                 } catch (DateTimeParseException e) {
                      // Try the UI format "dd-MMM-yyyy"
                     return LocalDate.parse(value, UI_DATE_FORMATTER);
                 }
            }
            // Add other types as needed (Boolean, etc.)
            log.warn("Unsupported target type for parsing: {}", targetType.getName());
            return value; // Fallback or throw error?
        } catch (NumberFormatException | DateTimeParseException e) {
            log.error("Failed to parse value '{}' for type '{}' or targetType '{}': {}", value, fieldType, targetType.getName(), e.getMessage());
            throw new IllegalArgumentException("Invalid value format for field type " + fieldType + ": " + value, e);
        }
    }
}
