package com.example.enhancedsearch.controller;

import com.example.enhancedsearch.dto.DataSearchRequest;
import com.example.enhancedsearch.dto.FilterFieldMetadata;
import com.example.enhancedsearch.dto.SearchRequest;
import com.example.enhancedsearch.model.DynamicList;
import com.example.enhancedsearch.model.SolSearchCriteria;
import com.example.enhancedsearch.service.SearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/data")
// Enable CORS for Angular app (adjust origin as needed for production)
@CrossOrigin(origins = "*") // Allow all origins for simplicity, restrict in production
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DynamicList> search(@RequestBody SolSearchCriteria solrSearchCriteria) {
        log.info("Received /data search request: {}", solrSearchCriteria);
        try {
            return ResponseEntity.ok(searchService.search(solrSearchCriteria));
        } catch (Exception e) {
            log.error("Error during search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
