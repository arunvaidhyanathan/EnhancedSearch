# EnhancedSearch Backend

This is the Spring Boot backend application for the Enhanced Search functionality. It provides a REST API for performing dynamic searches against a PostgreSQL database.

## Technology Stack

* Java 17
* Spring Boot 3.2.4
* Maven
* Spring Data JDBC
* PostgreSQL 16 (with pg_trgm extension)
* Lombok

## Key Files

* `pom.xml`: Defines Maven project dependencies and build settings.
* `src/main/resources/application.yml`: Configures the application, including database connection details and server port (currently set to 8081).
* `src/main/java/com/example/enhancedsearch/EnhancedSearchApplication.java`: The main entry point for the Spring Boot application.
* `src/main/java/com/example/enhancedsearch/controller/SearchController.java`: Defines the REST API endpoint (`POST /api/search`) for receiving search requests.
* `src/main/java/com/example/enhancedsearch/service/SearchService.java`: Contains the core logic for dynamically building and executing SQL queries based on the incoming search criteria.
* `src/main/java/com/example/enhancedsearch/service/IndexManager.java`: Manages the creation of database indexes based on configurations stored in the `cads.search_config` table. Runs on application startup.
* `src/main/java/com/example/enhancedsearch/service/FilterMetadataService.java`: Provides metadata for the frontend filter UI (currently hardcoded, intended to be loaded from a configuration or database).
* `src/main/java/com/example/enhancedsearch/dto/`: Contains Data Transfer Objects (DTOs) used for API requests and responses (`SearchRequest`, `SearchCriteria`, `SearchCondition`).
* `src/main/java/com/example/enhancedsearch/config/SearchConfig.java`: (Potentially used for application-level configurations - review its specific purpose if modified).

## Database Setup

1. **PostgreSQL Instance**: Ensure you have a PostgreSQL 16 instance running.

2. **Connection Details**: Update the database URL, username, and password in `src/main/resources/application.yml` to match your environment.

3. **Schema**: Create the required schema:

   ```sql
   CREATE SCHEMA IF NOT EXISTS cads;
   ```

4. **pg_trgm Extension**: Enable the trigram extension required for efficient `LIKE`/`ILIKE` searches using GIN indexes:

   ```sql
   CREATE EXTENSION IF NOT EXISTS pg_trgm SCHEMA pg_catalog;
   -- Or specify another schema if preferred, ensure it's accessible
   ```

5. **Configuration Table**: Create the table used by `IndexManager`:

   ```sql
   CREATE TABLE IF NOT EXISTS cads.search_config (
       table_name VARCHAR(255) NOT NULL,
       column_name VARCHAR(255) NOT NULL,
       search_type VARCHAR(50) NOT NULL, -- e.g., 'exact', 'partial', 'range', 'date_range'
       PRIMARY KEY (table_name, column_name)
   );
   ```

   * Populate this table with entries for the tables and columns you want the `IndexManager` to automatically create indexes for (e.g., `INSERT INTO cads.search_config (table_name, column_name, search_type) VALUES ('your_table', 'your_column', 'partial');`).

6. **Data Tables**: Ensure the actual data tables (e.g., `cads.products`) that you intend to search exist within the `cads` schema.

## Running the Application

1. Navigate to the project's root directory (`/Users/arunvaidhyanathan/Developer/EnhancedSearch/enhancedsearch`).

2. Run the application using Maven:

   ```bash
   mvn spring-boot:run
   ```

3. The application will start, connect to the database, attempt to create indexes based on `cads.search_config`, and listen on the configured port (default: 8081).

## API Usage

* **Endpoint**: `POST /api/search`

* **Request Body**: A JSON object matching the `SearchRequest` DTO structure:

  ```json
  {
    "tableName": "your_table_name",
    "criteria": [
      {
        "field": "column_name",
        "condition": "EQUALS", // e.g., EQUALS, LIKE, BETWEEN, REGEX, etc. (See SearchCondition enum)
        "value": "search_value",
        "value2": "optional_second_value_for_BETWEEN"
      },
      // ... more criteria
    ]
  }
  ```

* **Response**: An array of maps, where each map represents a row matching the search criteria.

  ```json
  [
      { "column1": "value1", "column2": "value2", ... },
      { "column1": "value3", "column2": "value4", ... }
  ]
  ```
