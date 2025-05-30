package com.example.enhancedsearch.service;

import com.example.enhancedsearch.dto.AlertSummaryDTO;
import com.example.enhancedsearch.dto.SearchRequest;
import com.example.enhancedsearch.entity.AlertMst;
import com.example.enhancedsearch.repository.AlertRepository;
import com.example.enhancedsearch.repository.specification.AlertSpecificationBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Import Slf4j
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j // Add Slf4j annotation
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AlertSummaryDTO> searchAlerts(SearchRequest searchRequest) {
        //log.info("Received search request: {}", searchRequest);

        // Build the dynamic specification based on filters
        // Use getters generated by Lombok on SearchRequest
        Specification<AlertMst> spec = AlertSpecificationBuilder.build(searchRequest.getSearchFilters());

        // Create Pageable object for pagination (using 0-based index)
        // Use getters generated by Lombok on SearchRequest
        Pageable pageable = PageRequest.of(searchRequest.getZeroBasedPage(), searchRequest.getLimit());

        // Execute the query using the repository
        Page<AlertMst> alertPage = alertRepository.findAll(spec, pageable);
        //log.info("Found {} alerts on page {}/{}", alertPage.getNumberOfElements(), alertPage.getNumber() + 1, alertPage.getTotalPages());


        // Map the entity Page to a DTO Page
        return alertPage.map(AlertSummaryDTO::fromEntity);
    }
    // All log.info() calls will now work
}