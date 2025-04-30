package com.example.enhancedsearch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.enhancedsearch.entity.AlertMst;

@Repository
public interface AlertRepository extends JpaRepository<AlertMst, Long>, JpaSpecificationExecutor<AlertMst> {
    // JpaSpecificationExecutor provides:
    // Optional<T> findOne(@Nullable Specification<T> spec);
    // List<T> findAll(@Nullable Specification<T> spec);
    // Page<T> findAll(@Nullable Specification<T> spec, Pageable pageable);
    // List<T> findAll(@Nullable Specification<T> spec, Sort sort);
    // long count(@Nullable Specification<T> spec);
}