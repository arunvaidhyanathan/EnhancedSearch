package com.example.enhancedsearch.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.enhancedsearch.dto.AlertSummaryDTO;
import com.example.enhancedsearch.dto.SearchRequest;

/**
 * Service interface for searching alerts.
 */
public interface AlertService {

    /**
     * Searches for alerts based on the provided criteria and pagination.
     *
     * @param searchRequest The search request containing filters and pagination info.
     * @return A Page of AlertSummaryDTO matching the criteria.
     */
    Page<AlertSummaryDTO> searchAlerts(SearchRequest searchRequest);
}