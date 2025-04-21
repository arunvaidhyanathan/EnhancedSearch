package com.example.enhancedsearch.dto;

import java.util.List;
import java.util.Map;

public class DataSearchRequest {
    private int page;
    private int currentPageNo;
    private int start;
    private int limit;
    private List<Map<String, List<SearchFilter>>> searchFilters;

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getCurrentPageNo() { return currentPageNo; }
    public void setCurrentPageNo(int currentPageNo) { this.currentPageNo = currentPageNo; }

    public int getStart() { return start; }
    public void setStart(int start) { this.start = start; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public List<Map<String, List<SearchFilter>>> getSearchFilters() { return searchFilters; }
    public void setSearchFilters(List<Map<String, List<SearchFilter>>> searchFilters) { this.searchFilters = searchFilters; }
}
