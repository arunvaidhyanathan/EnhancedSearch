## I am want to implement the following search scenarios in Postgresql using PG_TRGM extension in postgreSQL data base schema cads for below scenarios.
Scenario 1: Search using =, <=, >=, BETWEEN, LIKE, ILIKE
Scenario 2: Search using regex on various columns Regex Search (/.*/)
Scenario 3: search between dates
The request comes as API request from swagger/postman making call to API endpoints in JSON format. The search condition has to be dynamic based on the filter fields taking the conditions as fields and search for the above scenarios.
Give the step by step implementation to make the Postgresql data base calls based on the scenarios, get the response and respond back. Give me the steps by step instructions to implement the call in java spring boot application. set up the postgresql with proper indexing on tables with multiple columns and the implications in creating multiple index on the table columns and give the optimized implementation for performance. The data is partitioned in the table and the partitions are weekly or monthly partitions based on the table column called as patition_date.

create the project and implement the code using springboot 3.4.4 and java 17 with postgresql 16 as maven project.

## Create an Entity
import jakarta.persistence.*;
import lombok.Data; // Or add getters/setters manually
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "search_data")
@Data // Lombok annotation
public class SearchData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming id is auto-generated; adjust if not
    private Long id; // If using composite key (id, partition_date), need @EmbeddedId

    @Column(name = "text_col1")
    private String textCol1;

    @Column(name = "text_col2")
    private String textCol2;

    @Column(name = "numeric_col")
    private BigDecimal numericCol;

    @Column(name = "date_col")
    private LocalDate dateCol;

    @Column(name = "partition_date", nullable = false)
    private LocalDate partitionDate; // Important for queries

    // Add other columns and constructors, getters, setters if not using Lombok
}

## Request DTOs
import lombok.Data;
import java.util.List;

@Data
public class SearchRequest {
    private List<FilterCondition> filters;
    // Optional: Add pagination info (page, size)
    private int page = 0;
    private int size = 20;
    // Optional: Add sorting info
    // private String sortBy;
    // private String sortDirection = "ASC";
}

@Data
public class FilterCondition {
    private String field;      // e.g., "textCol1", "numericCol", "dateCol", "partitionDate"
    private String operator;   // e.g., "EQ", "LTE", "GTE", "BETWEEN", "LIKE", "ILIKE", "REGEX", "DATE_BETWEEN"
    private Object value;      // Value for most operators
    private Object value2;     // Second value for BETWEEN operators
}

## Repository DTOs
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface SearchDataRepository extends JpaRepository<SearchData, Long>, // Use Long if id is the primary key type
                                             JpaSpecificationExecutor<SearchData> {
    // JpaSpecificationExecutor provides methods like findAll(Specification<T> spec)
    // You can add custom query methods if needed, but Specifications handle dynamics
}

## Specification Builder Service
import org.springfraimport jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // For checking empty strings

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Import BigDecimal

@Service
public class SearchSpecificationBuilder {

    public Specification<SearchData> build(List<FilterCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return Specification.where(null); // No filters, return all (or handle as needed)
        }

        Specification<SearchData> spec = Specification.where(null); // Start with empty spec

        // *** Crucial for Partition Pruning ***
        // Attempt to derive partition_date range from other date filters or require it explicitly
        LocalDate minPartitionDate = null;
        LocalDate maxPartitionDate = null;

        // Example: If filtering on date_col, infer a possible partition_date range
        // You might need more sophisticated logic based on your partitioning strategy (weekly/monthly)
        // and how date_col relates to partition_date
        for (FilterCondition condition : conditions) {
             if ("dateCol".equalsIgnoreCase(condition.getField())) {
                 try {
                     LocalDate filterDate = parseDate(condition.getValue());
                     if (filterDate != null) {
                        // Basic example: assume partition covers the month of date_col
                        LocalDate monthStart = filterDate.withDayOfMonth(1);
                        LocalDate nextMonthStart = monthStart.plusMonths(1);

                        if ("GTE".equalsIgnoreCase(condition.getOperator()) || "EQ".equalsIgnoreCase(condition.getOperator())) {
                            if (minPartitionDate == null || monthStart.isAfter(minPartitionDate)) {
                                minPartitionDate = monthStart;
                            }
                        }
                        if ("LTE".equalsIgnoreCase(condition.getOperator()) || "EQ".equalsIgnoreCase(condition.getOperator())) {
                            if (maxPartitionDate == null || nextMonthStart.isBefore(maxPartitionDate)) {
                                 // Search up to the start of the *next* partition boundary
                                maxPartitionDate = nextMonthStart;
                            }
                        }
                        if ("DATE_BETWEEN".equalsIgnoreCase(condition.getOperator())) {
                           LocalDate date1 = parseDate(condition.getValue());
                           LocalDate date2 = parseDate(condition.getValue2());
                           if (date1 != null && date2 != null) {
                               LocalDate startPartition = date1.withDayOfMonth(1);
                               LocalDate endPartition = date2.withDayOfMonth(1).plusMonths(1); // up to next month start
                                if (minPartitionDate == null || startPartition.isAfter(minPartitionDate)) {
                                    minPartitionDate = startPartition;
                                }
                                 if (maxPartitionDate == null || endPartition.isBefore(maxPartitionDate)) {
                                    maxPartitionDate = endPartition;
                                }
                           }
                        }
                     }
                 } catch (Exception e) {
                    // Handle parsing error - maybe log it
                 }
             }
             // Explicit partitionDate filter takes precedence
             if ("partitionDate".equalsIgnoreCase(condition.getField())) {
                try {
                    LocalDate partitionFilterDate = parseDate(condition.getValue());
                     if ("GTE".equalsIgnoreCase(condition.getOperator()) || "EQ".equalsIgnoreCase(condition.getOperator())) {
                         minPartitionDate = partitionFilterDate; // Override derived
                     }
                     if ("LTE".equalsIgnoreCase(condition.getOperator())) {
                         // LTE on partition_date needs careful thought depending on partition range definition
                         // If partitions are [start, end), then LTE '2024-01-15' should search partition ending '2024-02-01'
                         // This logic might need adjustment based on precise partition boundaries.
                         // For simplicity, let's assume we include the partition containing the LTE date.
                         maxPartitionDate = partitionFilterDate.plusDays(1); // Search up to the day after
                     }
                     // Handle other operators for partitionDate if needed
                 } catch (Exception e) {
                     // Handle error
                 }
             }
        }

        // *** Add derived or explicit partition_date filter to the Specification ***
        if (minPartitionDate != null) {
            spec = spec.and(partitionDateGte(minPartitionDate));
        }
         if (maxPartitionDate != null) {
            spec = spec.and(partitionDateLt(maxPartitionDate)); // Use Less Than for range [start, end)
        }
        // Consider adding a default wide range or throwing error if no date filter provides partition info


        // --- Build predicates for other filters ---
        for (FilterCondition condition : conditions) {
            // Skip partitionDate here as we handled it above
            if ("partitionDate".equalsIgnoreCase(condition.getField())) {
                continue;
            }
            spec = spec.and(createSpecification(condition));
        }

        return spec;
    }

    private Specification<SearchData> createSpecification(FilterCondition condition) {
        return (root, query, criteriaBuilder) -> {
            String field = condition.getField();
            String operator = condition.getOperator().toUpperCase();
            Object value = condition.getValue();
            Object value2 = condition.getValue2();

            // Handle potential type mismatches
            Path<?> path = root.get(field); // Get path (can check path.getJavaType())

            try {
                switch (operator) {
                    // Scenario 1: =, <=, >=, BETWEEN, LIKE, ILIKE
                    case "EQ":
                        if (value == null) return criteriaBuilder.isNull(path);
                        return criteriaBuilder.equal(path, convertToType(value, path.getJavaType()));
                    case "NEQ":
                        if (value == null) return criteriaBuilder.isNotNull(path);
                        return criteriaBuilder.notEqual(path, convertToType(value, path.getJavaType()));
                    case "GT":
                        return criteriaBuilder.greaterThan(path.as(Comparable.class), (Comparable) convertToType(value, path.getJavaType()));
                    case "GTE":
                        return criteriaBuilder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) convertToType(value, path.getJavaType()));
                    case "LT":
                        return criteriaBuilder.lessThan(path.as(Comparable.class), (Comparable) convertToType(value, path.getJavaType()));
                    case "LTE":
                        return criteriaBuilder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) convertToType(value, path.getJavaType()));
                    case "BETWEEN": // For numeric or potentially non-date types
                         return criteriaBuilder.between(path.as(Comparable.class),
                                (Comparable) convertToType(value, path.getJavaType()),
                                (Comparable) convertToType(value2, path.getJavaType()));
                    case "LIKE":
                        if (value instanceof String && StringUtils.hasText((String)value)) {
                            return criteriaBuilder.like(path.as(String.class), "%" + value + "%"); // Example: contains search
                            // Adjust wildcard placement as needed: value + "%" (starts with), "%" + value (ends with)
                        } else {
                            return criteriaBuilder.disjunction(); // Or handle error
                        }
                    case "ILIKE": // Case-insensitive LIKE
                        if (value instanceof String && StringUtils.hasText((String)value)) {
                            // Standard JPA doesn't have a direct ILIKE. Use lower() workaround or native query/function.
                            // This lower() approach might prevent index usage on case-sensitive collations,
                            // but pg_trgm GIN/GiST indexes often work with lower() or handle case-insensitivity inherently depending on collation.
                            // PostgreSQL's ILIKE operator is generally preferred if possible.
                            return criteriaBuilder.like(criteriaBuilder.lower(path.as(String.class)), "%" + ((String) value).toLowerCase() + "%");
                            // **Alternative for true ILIKE (might require native query or specific Hibernate features)**
                            // See notes below about native queries for better ILIKE/REGEX handling
                        } else {
                            return criteriaBuilder.disjunction(); // Or handle error
                        }

                    // Scenario 2: Regex (Requires Native Function Call - Less Portable)
                    case "REGEX":
                        if (value instanceof String && StringUtils.hasText((String) value)) {
                            // Using criteriaBuilder.function is one way, but can be complex.
                            // It's often easier to use @Query with nativeQuery=true for regex.
                            // Placeholder predicate - **This will likely NOT work correctly without native SQL**
                            // return criteriaBuilder.equal(criteriaBuilder.function("regexp_like", Boolean.class, path, criteriaBuilder.literal((String)value)), Boolean.TRUE); // Example syntax, function name depends on DB
                            // **Recommendation:** Handle REGEX via native queries in the repository if needed.
                            System.err.println("WARN: REGEX operator using Specifications is complex/non-standard. Consider native query.");
                            return criteriaBuilder.disjunction(); // Return no results for this filter via Spec
                        } else {
                            return criteriaBuilder.disjunction();
                        }

                    // Scenario 3: Date Between
                    case "DATE_BETWEEN":
                         LocalDate date1 = parseDate(value);
                         LocalDate date2 = parseDate(value2);
                         if (date1 != null && date2 != null && path.getJavaType().equals(LocalDate.class)) {
                             // Ensure date1 is before or equal to date2
                             if (date1.isAfter(date2)) {
                                 LocalDate temp = date1;
                                 date1 = date2;
                                 date2 = temp;
                             }
                             // Use GTE for start date and LTE for end date for inclusive range
                             Predicate p1 = criteriaBuilder.greaterThanOrEqualTo(path.as(LocalDate.class), date1);
                             Predicate p2 = criteriaBuilder.lessThanOrEqualTo(path.as(LocalDate.class), date2);
                             return criteriaBuilder.and(p1, p2);
                         } else {
                            return criteriaBuilder.disjunction(); // Invalid date input
                         }


                    default:
                        throw new IllegalArgumentException("Unsupported operator: " + operator);
                }
            } catch (Exception e) {
                // Log error, maybe return a predicate that yields no results
                 System.err.println("Error processing filter for field '" + field + "': " + e.getMessage());
                return criteriaBuilder.disjunction(); // Effectively ignores this filter on error
            }
        };
    }

    // --- Partition Date Helper Specifications ---
    private Specification<SearchData> partitionDateGte(LocalDate date) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.greaterThanOrEqualTo(root.get("partitionDate"), date);
    }

     private Specification<SearchData> partitionDateLt(LocalDate date) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.lessThan(root.get("partitionDate"), date);
    }


    // --- Type Conversion Helpers ---
    private Object convertToType(Object value, Class<?> targetType) {
         if (value == null) return null;
         String stringValue = value.toString(); // Assume input might be String from JSON

        if (targetType.equals(String.class)) {
            return stringValue;
        } else if (targetType.equals(Integer.class) || targetType.equals(int.class)) {
            return Integer.parseInt(stringValue);
        } else if (targetType.equals(Long.class) || targetType.equals(long.class)) {
            return Long.parseLong(stringValue);
        } else if (targetType.equals(BigDecimal.class)) {
            return new BigDecimal(stringValue);
        } else if (targetType.equals(Double.class) || targetType.equals(double.class)) {
            return Double.parseDouble(stringValue);
        } else if (targetType.equals(Float.class) || targetType.equals(float.class)) {
             return Float.parseFloat(stringValue);
        } else if (targetType.equals(LocalDate.class)) {
            return parseDate(value); // Use existing date parsing logic
        } else if (targetType.equals(Boolean.class) || targetType.equals(boolean.class)) {
            return Boolean.parseBoolean(stringValue);
        }
        // Add other type conversions as needed
        throw new IllegalArgumentException("Unsupported target type for conversion: " + targetType.getName());
    }

    private LocalDate parseDate(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        try {
            // Adjust date format parsing as needed (e.g., yyyy-MM-dd)
            return LocalDate.parse(value.toString());
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + value + " - " + e.getMessage());
            return null; // Or throw exception
        }
    }
}

Operator Enum: It's often better to use an Enum for the operator field for type safety and clarity instead of raw strings.

##Controller
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchDataRepository searchDataRepository;

    @Autowired
    private SearchSpecificationBuilder specificationBuilder;

    @PostMapping
    public ResponseEntity<Page<SearchData>> searchData(@RequestBody SearchRequest searchRequest) {
        try {
            Specification<SearchData> spec = specificationBuilder.build(searchRequest.getFilters());

            // Add pagination (ensure size > 0)
            int pageSize = Math.max(searchRequest.getSize(), 1);
            Pageable pageable = PageRequest.of(searchRequest.getPage(), pageSize);
            // TODO: Add sorting if needed in PageRequest.of()

            Page<SearchData> results = searchDataRepository.findAll(spec, pageable);

            return ResponseEntity.ok(results);

        } catch (IllegalArgumentException e) {
             // Handle invalid input like bad operator, bad date format etc.
             System.err.println("Search failed: " + e.getMessage());
             return ResponseEntity.badRequest().build(); // Or return specific error message
        } catch (Exception e) {
            // Handle other potential errors (e.g., database connection issues)
            System.err.println("An unexpected error occurred during search: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.internalServerError().build();
        }
    }
}

##Repository
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchDataRepository extends JpaRepository<SearchData, Long> {
    // Add custom query methods if needed
}   

##Application
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

Scenario 1: Standard Operators (=, <=, >=, BETWEEN, LIKE, ILIKE)
Covered by the Specification builder using CriteriaBuilder methods.
= , range operators, BETWEEN will use B-Tree indexes if available on the filtered column.
LIKE, ILIKE will benefit significantly from the pg_trgm GIN/GiST indexes you created. The lower() workaround for ILIKE in Specifications might sometimes prevent index usage depending on DB/collation, but often works with pg_trgm. For optimal ILIKE index usage, a native query might be slightly better.
Scenario 2: Regex Search (~, ~*)
Standard JPA Specifications don't have clean support for database-specific regex operators.
Best Approach: Use native queries via Spring Data JPA's @Query annotation if regex is essential.

// In SearchDataRepository
@Query(value = "SELECT * FROM search_data WHERE text_col1 ~ :pattern AND partition_date >= :startDate AND partition_date < :endDate", nativeQuery = true)
Page<SearchData> findByTextCol1Regex(@Param("pattern") String pattern, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
// Add similar methods for other columns or case-insensitive regex (~*)
// Remember to ALWAYS include the partition_date filter!

Call these specific repository methods from your service layer when a "REGEX" operator is detected in the request, instead of trying to build it into the Specification.
Performance: pg_trgm indexes offer limited help for regex. Performance depends heavily on the pattern. Avoid leading wildcards (.*pattern) if possible. Complex regex might lead to full scans within the selected partitions.
Scenario 3: Date Range Search (BETWEEN, >=, <=)
Covered by the Specification builder.
The DATE_BETWEEN custom operator simplifies handling date ranges.
Uses B-Tree indexes on date_col.
Crucially, ensure the partition_date filter is also applied correctly based on the date_col range to enable partition pruning. The logic in SearchSpecificationBuilder provides a basic example; refine it based on your exact partitioning scheme (weekly/monthly) and data distribution. If date_col always falls within the same partition period as partition_date, the logic is simpler.

connect to postgresql database using application.yml entries as below
    postgresql:
      jdbc-url: jdbc:postgresql://db.nrhsoabqeskybrznxfyi.supabase.co:5432/postgres
      username: postgres
      password: Sreeja1608!@
      driver-class-name: org.postgresql.Driver
      hirkai:
        connectionInitSql: SET search_path TO cads
        minimumIdle: 1
        maximumPoolSize: 5
        auto-commit: false

application structure

EnhancedSearch/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── db/
│   │   │           └── release/
│   │   │               ├── config/
│   │   │               │   └── SearchConfig.java
│   │   │               ├── controller/
│   │   │               │   └── SearchController.java
│   │   │               ├── exception/
│   │   │               │   ├── SearchException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── service/
│   │   │               │   └── SearchSpecificationBuilder.java
│   │   │               └── repository/
│   │   │                   └── SearchDataRepository.java
│   │   │               └── EnhancedSearchApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── java/
│           └── com/
└── pom.xml

Implement the application using factory pattern for the search service. The factory pattern will be used to create the search service based on the arguments passed to the factory to connect to the database.

##Factory
Use factory cache to store the search service instances based on the arguments passed to the factory.

USe the below data as reference and below query to fetch the filter data from table.

##FilterFields
use the FilterFields table to fetch the filter data from table.
use below query to fetch the filter data from table.

select e.field_group, e.field_name, e.field_type, e.display_order, e.gui_identifier,e.is_sortable,e.collection_name, g.label, e.regex_enabled from cads.enhanced_search_fields e , cads.gui_label g 
where e.gui_identifier = g.gui_identifier and e.is_active='Y' group by e.field_group, e.field_name, e.field_type, e.display_order, e.gui_identifier, e.is_sortable,
e.collection_name, g.label, e.regex_enabled order by 1,4;

##Filter Conditions
as per the filter fields table, the filter conditions are as below:
Search using =, <=, >=, BETWEEN, LIKE, REGEX

// SearchCondition.java (Enum for search types)
public enum SearchCondition {
    LIKE, EQUALS, LESS_THAN_EQUALS, GREATER_THAN_EQUALS, BETWEEN, REGEX
}
// SearchCriteria.java (Updated to include date-specific fields)
public record SearchCriteria(
    String columnName,
    SearchCondition condition,
    String value,
    String value2,        // For BETWEEN condition
    boolean isDate        // New flag to indicate date type
) {}

// SearchRequest.java (Unchanged)
public record SearchRequest(
    List<SearchCriteria> criteria,
    String tableName
) {}

// SearchService.java (Updated with date handling)
@Service
public class SearchService {
    private final JdbcTemplate jdbcTemplate;
    private final IndexManager indexManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Autowired
    public SearchService(JdbcTemplate jdbcTemplate, IndexManager indexManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.indexManager = indexManager;
    }

    public List<Map<String, Object>> search(SearchRequest request) {
        if (request == null || request.criteria() == null || request.criteria().isEmpty() || request.tableName() == null) {
            throw new IllegalArgumentException("Invalid search request");
        }

        StringBuilder query = new StringBuilder("SELECT * FROM ")
            .append(sanitizeTableName(request.tableName()))
            .append(" WHERE ");

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        for (SearchCriteria criteria : request.criteria()) {
            String condition = buildCondition(criteria);
            conditions.add(condition);
            addParameters(criteria, params);
        }

        query.append(String.join(" AND ", conditions));
        
        return jdbcTemplate.queryForList(query.toString(), params.toArray());
    }

    private String buildCondition(SearchCriteria criteria) {
        if (criteria.isDate() && criteria.condition() == SearchCondition.BETWEEN) {
            return criteria.columnName() + " BETWEEN ?::date AND ?::date";
        }

        return switch (criteria.condition()) {
            case LIKE -> 
                "%" + criteria.columnName() + "% <% '" + criteria.value() + "' AND similarity(" + 
                criteria.columnName() + ", '" + criteria.value() + "') > 0.3";
            case EQUALS -> 
                criteria.isDate() ? 
                    criteria.columnName() + " = ?::date" : 
                    criteria.columnName() + " = ?";
            case LESS_THAN_EQUALS -> 
                criteria.isDate() ? 
                    criteria.columnName() + " <= ?::date" : 
                    criteria.columnName() + " <= ?";
            case GREATER_THAN_EQUALS -> 
                criteria.isDate() ? 
                    criteria.columnName() + " >= ?::date" : 
                    criteria.columnName() + " >= ?";
            case BETWEEN -> 
                criteria.columnName() + " BETWEEN ? AND ?";
            case REGEX -> 
                criteria.columnName() + " ~ ?";
        };
    }

    private void addParameters(SearchCriteria criteria, List<Object> params) {
        try {
            switch (criteria.condition()) {
                case LIKE:
                    // No parameters needed as we're using similarity directly
                    break;
                case BETWEEN:
                    if (criteria.isDate()) {
                        params.add(LocalDate.parse(criteria.value(), DATE_FORMATTER));
                        params.add(LocalDate.parse(criteria.value2(), DATE_FORMATTER));
                    } else {
                        params.add(criteria.value());
                        params.add(criteria.value2());
                    }
                    break;
                default:
                    if (criteria.isDate()) {
                        params.add(LocalDate.parse(criteria.value(), DATE_FORMATTER));
                    } else {
                        params.add(criteria.value());
                    }
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for " + criteria.columnName() + 
                ". Expected format: YYYY-MM-DD", e);
        }
    }

    private String sanitizeTableName(String tableName) {
        return tableName.replaceAll("[^a-zA-Z0-9_]", "");
    }
}

// IndexManager.java (Updated to handle date indexes)
@Service
public class IndexManager {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public IndexManager(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeIndexes() {
        List<SearchConfig> searchConfigs = loadSearchConfigs();
        for (SearchConfig config : searchConfigs) {
            createIndex(config);
        }
    }

    private void createIndex(SearchConfig config) {
        String indexName = "idx_" + config.tableName() + "_" + config.columnName();
        String sql = switch (config.searchType()) {
            case LIKE, REGEX -> 
                "CREATE INDEX IF NOT EXISTS " + indexName + 
                " ON " + config.tableName() + 
                " USING GIN (" + config.columnName() + " gin_trgm_ops)";
            case BETWEEN, EQUALS, LESS_THAN_EQUALS, GREATER_THAN_EQUALS -> 
                "CREATE INDEX IF NOT EXISTS " + indexName + 
                " ON " + config.tableName() + 
                " (" + config.columnName() + ")";
        };
        
        jdbcTemplate.execute(sql);
    }

    private List<SearchConfig> loadSearchConfigs() {
        return jdbcTemplate.query(
            "SELECT table_name, column_name, search_type FROM search_config",
            (rs, rowNum) -> new SearchConfig(
                rs.getString("table_name"),
                rs.getString("column_name"),
                SearchCondition.valueOf(rs.getString("search_type"))
            )
        );
    }
}

// SearchConfig.java (Unchanged)
public record SearchConfig(
    String tableName,
    String columnName,
    SearchCondition searchType
) {}

// SearchController.java (Unchanged)
@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> search(
            @RequestBody SearchRequest request) {
        List<Map<String, Object>> results = searchService.searchProducts(request);
        return ResponseEntity.ok(results);
    }
}

Collection name from the filter fields table is the collection name to be used in the search service as the table name for search.

Create an single page angular application to display the filter data.

##Angular Application

