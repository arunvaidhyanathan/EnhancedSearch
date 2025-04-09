package com.example.enhancedsearch.controller;

import com.example.enhancedsearch.dto.SearchRequest;
import com.example.enhancedsearch.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
// Enable CORS for Angular app (adjust origin as needed for production)
@CrossOrigin(origins = "*") // Allow all origins for simplicity, restrict in production
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> search(@RequestBody SearchRequest request) {
        log.info("Received search request: {}", request);
        try {
            List<Map<String, Object>> results = searchService.performSearch(request);
            return ResponseEntity.ok(results);
        } catch (IllegalArgumentException e) {
            // Catch specific exceptions thrown by the service for bad input
            log.warn("Bad search request: {}", e.getMessage());
            // Returning a generic error message, could return e.getMessage() if safe
            return ResponseEntity.badRequest().body(Collections.singletonList(Map.of("error", "Invalid search criteria: " + e.getMessage())));
        } catch (Exception e) {
            // Catch broader exceptions for internal errors
            log.error("An unexpected error occurred during search: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Collections.singletonList(Map.of("error", "An internal server error occurred.")));
        }
    }
}
