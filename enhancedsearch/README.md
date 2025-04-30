
I want to create a java 17 springboot 3.2, data base is Postgresql 13 program should be with Controller which take the input coming from the UI as per the picture and build a springboot controller with endpoint /data,  it should follow the architecture of controller, service, repository and good design pattern consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE
input JSON: 
{
"page": 1,
"currentPageNo": 1,
"start": 0,
"limit": 50,
"searchFilters": [
{
“alertest”:[
{
"filterCondition": "AND",
"fieldName": "SOURCE_SYSTEM",
"fieldType": "string",
"fieldGroup": "Alert Details",
"collectionName": "alert_mst",
"fieldValue": "GLOBAL",
"operation": "Like",
"fieldValueTo": null
},
{
"filterCondition": "AND",
"fieldName": "PP_MESSAGE",
"fieldType": "string",
"fieldGroup": "Alert Details",
"collectionName": "alert_cstm_attribute",
"fieldValue": "AQUA",
"operation": "RegEX",
"fieldValueTo": null
},
{
"filterCondition": "AND",
"fieldName": "RECEIVER",
"fieldType": "string",
"fieldGroup": "Transaction Details",
"collectionName": "alert_mst",
"fieldValue": "ORIGC",
"operation": "Like",
"fieldValueTo": null
},
{
"filterCondition": "OR",
"fieldName": "CREATED_DATE",
"fieldType": "date",
"fieldGroup": "Alert Details",
"collectionName": "alert_mst",
"fieldValue": "2023-11-15T18:30:00.000",
"operation": "Between",
"fieldValueTo": "2025-04-15T18:30:00.000"
},
{
"filterCondition": "AND",
"fieldName": "BENEFICIARY_NAME",
"fieldType": "string",
"fieldGroup": "BENEFICIARY",
"collectionName": "alert_det",
"fieldValue": "aqua",
"operation": "Like",
"fieldValueTo": null
},
{
"filterCondition": "OR",
"fieldName": "VALUE_DATE",
"fieldType": "date",
"fieldGroup": "Transaction Details",
"collectionName": "alert_mst",
"fieldValue": "2025-04-01T15:30:00.000",
"operation": "Like",
"fieldValueTo": null
}
]
}
]
I need to build search using Spring Specification JPA, If the collectionName is alert_mst, I need to search from table cads.alert_mst, if the collectionName is alert_cstm_attribute I need to search from table cads.alert_cstm_attribute the filterCondition can be "and" or "or" on the JSON field with column name "fieldName". if the collection name is "hit_match_details" then it should and should be on the table cads.hit_match_details.There is a special condition when the collectionName = alert_det, the the columns should be from field_name_alias and the should compare against field_value as this collection has to search for the field_value column with the Field_name_alias as the condition.

fieldType = Int Then the search operations can be
= , !=，<=，>=, between
fieldType = string Then the search operations can be
= , !=， Like
fieldType = date Then the search operations can be
= , !=，<=，>=, between
if the fieldType = Regex Then the search operations can be
=, !=, like, regex
Where I get the table name, column name, field type and regex_ebaled from the below query

select collection_name as table_name, field_name as column_name, field_type as column_type, regex_enabled from cads.enhanced_search_fields esf, cads.gui_label gl
where esf.gui_identifier = gl.gui_identifier and esf.is_active = 'Y';

Goal: Build a flexible search API for alerts based on dynamic criteria spanning multiple tables.
Input: JSON payload with pagination and a list of filter conditions (AND/OR, field, operator, value).
Core Tables: alert_mst, alert_det, alert_cstm_attribute, hit_match_details. All seem linked by alert_id.
Metadata: Searchable fields, their types, target tables (collectionName), and regex support are defined in enhanced_search_fields and gui_label.
Search Operations: Specific operators required for Int, String, Date, Regex types.
Technology: Java 17, Spring Boot 3.2, PostgreSQL 13, JPA (implied by Spring Boot context), pg_trgm.
Special Logic: alert_det search uses field_name_alias for the column and field_value for the value. alert_cstm_attribute searches specifically on PP_MESSAGE for the alias "transaction_message".
Performance: Leverage pg_trgm, implement appropriate indexing, consider query efficiency.
Indexing: Automate index creation (GIN for strings/pg_trgm, B-Tree for int/date) using a PostgreSQL function based on metadata. Address performance and maintenance.
Output: Paginated JSON results.
Architecture: Controller -> Service -> Repository.

Dynamic Query Strategy:
Solution use : Spring Data JPA Specification: Offers a flexible way to create specifications for dynamic queries.
Decision:
Given the complexity of the search logic, and the need for flexibility, I will use the Spring Data JPA Specification. It provides a clean and type-safe way to define dynamic specifications for queries.
Implementation Plan
Data Model:
Define entities for alert_mst, alert_det, alert_cstm_attribute, hit_match_details.
Use @Entity, @Table, @Column annotations to map entities to database tables and columns.
Use @Id, @GeneratedValue for primary keys.
Use @OneToMany, @ManyToOne, @JoinColumn for relationships.
Search Criteria Model:
Define a SearchCriteria class to represent individual filter conditions.
Use @Data annotation for getters, setters, toString, equals, hashCode.
Define enum for fieldType (Int, String, Date, Regex).
Define enum for operation (Equals, NotEquals, LessThan, GreaterThan, Between, Like, Regex).
Search Specification:
Create a SearchSpecification class that extends Specification.
Implement toPredicate method to build dynamic predicates based on SearchCriteria.
Use CriteriaBuilder to create Predicates based on fieldType and operation.
Use CriteriaQuery to define the root and criteria query.
Use CriteriaBuilder to create Predicates based on fieldType and operation.

Handling Special alert_det Logic:
When a filter has collectionName = "alert_det", the Criteria query needs to ensure an alert_det record exists for the alert_mst record where alert_det.alert_id = alert_mst.id AND alert_det.field_name_alias = filter.fieldName AND alert_det.field_value matches filter.fieldValue based on filter.operation.
This is a perfect case for a Subquery or EXISTS clause within the main Specification.
Handling alert_cstm_attribute:
Similar to alert_det, but simpler. If collectionName = "alert_cstm_attribute" and fieldName is "transaction_message" (or mapped from PP_MESSAGE), check for existence of a related record where alert_cstm_attribute.pp_message matches the criteria. Use EXISTS subquery.

Specification Builder: Create a helper class/method (AlertSpecifications.buildSpecification(List<FilterCriteria> filters)) that iterates through the filters and constructs the final Specification<AlertMst>. This keeps the service layer cleaner.
Entities: JPA entities (@Entity) for AlertMst, AlertDet, AlertCstmAttribute, HitMatchDetails with appropriate relationships (@OneToMany, @ManyToOne).
DTOs: SearchRequest, FilterCriteria, AlertSummaryDTO (containing fields needed in the UI response).

Specification Builder (com.example.search.repository.specification):
AlertSpecificationBuilder:
Static method build(List<FilterCriteria> filters): Returns Specification<AlertMst>.
Iterates through filters.
Uses Specification.where() for the first filter and spec.and() or spec.or() for subsequent filters based on filterCondition.
Inside the loop, create a Specification<AlertMst> for the current filter:
toPredicate(Root<AlertMst> root, CriteriaQuery<?> query, CriteriaBuilder cb, FilterCriteria filter) helper method.
Inside toPredicate:
Handle collectionName:
alert_mst: Get path directly from root.get(filter.fieldName).
alert_det: Use Subquery/EXISTS. Create a Subquery<Long> subquery = query.subquery(Long.class). Root<AlertDet> detRoot = subquery.from(AlertDet.class). Add subquery.select(detRoot.get("id")) (or any non-null field). Add WHERE detRoot.get("alertMst").get("id").equal(root.get("id")) AND detRoot.get("fieldNameAlias").equal(filter.fieldName) AND predicate for detRoot.get("fieldValue") based on operation/value. Return cb.exists(subquery).
alert_cstm_attribute: Similar EXISTS subquery on AlertCstmAttribute, filtering on ppMessage and linking back to root.get("id").
hit_match_details: Similar EXISTS subquery.
Handle operation and fieldType: Build the correct Predicate (e.g., cb.like, cb.equal, cb.between, cb.greaterThanOrEqualTo, cb.function for regex). Parse dates/numbers carefully. Handle case-insensitivity for strings if needed (cb.lower). Handle null values.
Return the final combined Specification.

Define DTOs (src/main/java/com/example/search/dto)

package com.example.search.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data // Or use @Getter, @Setter, @ToString, @EqualsAndHashCode
public class SearchRequest {

    @Min(value = 1, message = "Page number must be at least 1")
    private int page = 1; // UI sends 1-based index

    @Min(value = 1, message = "Limit must be at least 1")
    private int limit = 50;

    // Assuming the structure is { "alertest": [ filters ] } is a typo in the question
    // and it should be directly "searchFilters": [ filters ]
    @NotEmpty(message = "Search filters cannot be empty")
    @Valid // Enable validation on FilterCriteria objects
    private List<FilterCriteria> searchFilters;

    // Map to 0-based index for Spring Data Pageable
    public int getZeroBasedPage() {
        return Math.max(0, page - 1);
    }
}

@Data
public class FilterCriteria {

    @NotBlank(message = "Filter condition (AND/OR) is required")
    private String filterCondition; // "AND" or "OR" (Only relevant after the first filter)

    @NotBlank(message = "Field name is required")
    private String fieldName; // UI Field Name (e.g., "Application", "Transaction Message")

    @NotBlank(message = "Field type is required")
    private String fieldType; // "string", "date", "int", "regex" (regex might imply string type for value)

    // Not directly used in spec builder if we map fieldName to table/column,
    // but kept for context or potential future use. Can be derived from fieldName mapping.
    private String fieldGroup;

    @NotBlank(message = "Collection name is required")
    private String collectionName; // "alert_mst", "alert_det", "alert_cstm_attribute", "hit_match_details"

    // Value can be null for certain operations (e.g., IS NULL, IS NOT NULL - not shown in requirements but good practice)
    private String fieldValue; // The value to search for

    @NotBlank(message = "Operation is required")
    private String operation; // "Like", "RegEX", "=", "!=", "Between", ">=", "<=", ">", "<"

    // Used only when operation is "Between"
    private String fieldValueTo;
}


@Data
public class AlertSummaryDTO {
    // Fields needed in the UI response
    private Long id; // From AlertMst
    private String sourceSystem;
    private String receiver;
    private java.time.LocalDateTime createdDate; // Or Instant/ZonedDateTime
    // Add other fields from AlertMst or derived fields as needed
    // e.g., private String beneficiaryNamePreview; (Could be fetched via join or separate query if complex)

    public static AlertSummaryDTO fromEntity(AlertMst entity) {
        AlertSummaryDTO dto = new AlertSummaryDTO();
        dto.setId(entity.getId());
        dto.setSourceSystem(entity.getSourceSystem());
        dto.setReceiver(entity.getReceiver());
        dto.setCreatedDate(entity.getCreatedDate());
        // map other fields
        return dto;
    }
}

Define Entities (src/main/java/com/example/search/entity)

package com.example.search.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "alert_mst", schema = "cads")
@Getter
@Setter
public class AlertMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Or SEQUENCE if preferred
    private Long id;

    @Column(name = "SOURCE_SYSTEM") // Match DB column name if different from field name
    private String sourceSystem; // Maps to "Application" in UI?

    @Column(name = "RECEIVER")
    private String receiver;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate; // Maps to "Creation Date/Time"

    @Column(name = "VALUE_DATE")
    private LocalDate valueDate; // Maps to "Value Date"

    @Column(name = "ASSIGNED_TO")
    private String assignedTo;

    @Column(name = "STEP")
    private String step;

    // Assuming "Alert ID" in UI maps to a business identifier, not the PK
    @Column(name = "ALERT_BUSINESS_ID") // Example name, adjust to your actual column
    private String alertBusinessId; // Maps to "Alert ID" (use appropriate type, maybe Long/Integer)

    @Column(name = "MATCH_ACCURACY") // Example for Match Accuracy %
    private BigDecimal matchAccuracy;

    // --- Relationships --- FetchType.LAZY is important! ---
    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlertDet> alertDets = new HashSet<>();

    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlertCstmAttribute> alertCstmAttributes = new HashSet<>();

    @OneToMany(mappedBy = "alertMst", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<HitMatchDetails> hitMatchDetails = new HashSet<>();

    // Add other fields from alert_mst as needed
}

@Entity
@Table(name = "alert_det", schema = "cads")
@Getter
@Setter
public class AlertDet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false) // FK column name
    private AlertMst alertMst;

    @Column(name = "FIELD_NAME_ALIAS", nullable = false)
    private String fieldNameAlias; // e.g., "BENEFICIARY_NAME"

    @Column(name = "FIELD_VALUE")
    private String fieldValue; // The value associated with the alias
}

@Entity
@Table(name = "alert_cstm_attribute", schema = "cads")
@Getter
@Setter
public class AlertCstmAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertMst alertMst;

    // Maps to "Transaction Message" in UI
    @Column(name = "PP_MESSAGE")
    private String ppMessage;
}

@Entity
@Table(name = "hit_match_details", schema = "cads")
@Getter
@Setter
public class HitMatchDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private AlertMst alertMst;

    // Add other fields relevant to hit_match_details searching if needed
    // @Column(name = "SOME_FIELD")
    // private String someField;
}

Define Repository (src/main/java/com/example/search/repository)
package com.example.search.repository;

import com.example.search.entity.AlertMst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<AlertMst, Long>, JpaSpecificationExecutor<AlertMst> {
    // JpaSpecificationExecutor provides findAll(Specification, Pageable)
}

Implement Specification Builder (src/main/java/com/example/search/repository/specification)

package com.example.search.repository.specification;

import com.example.search.dto.FilterCriteria;
import com.example.search.entity.*;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public class AlertSpecificationBuilder {

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
            Map.entry("Match Accuracy %", "matchAccuracy")
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
