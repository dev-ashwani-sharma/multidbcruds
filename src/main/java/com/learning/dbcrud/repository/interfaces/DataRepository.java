package com.learning.dbcrud.repository.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base repository interface defining common CRUD operations
 * Following Interface Segregation Principle (ISP)
 */
public interface DataRepository<T, ID> {

    /**
     * Save a single entity
     */
    T save(T entity);

    /**
     * Save multiple entities
     */
    List<T> saveAll(List<T> entities);

    /**
     * Find by ID
     */
    Optional<T> findById(ID id);

    /**
     * Find all with pagination
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Find all with filters
     */
    Page<T> findAll(Map<String, Object> filters, Pageable pageable);

    /**
     * Update an entity
     */
    T update(T entity);

    /**
     * Partial update
     */
    T partialUpdate(ID id, Map<String, Object> updates);

    /**
     * Delete by ID
     */
    void deleteById(ID id);

    /**
     * Delete entity
     */
    void delete(T entity);

    /**
     * Delete all
     */
    void deleteAll();

    /**
     * Check if exists
     */
    boolean existsById(ID id);

    /**
     * Count total records
     */
    long count();

    /**
     * Count with filters
     */
    long count(Map<String, Object> filters);

    /**
     * Search with complex criteria
     */
    Page<T> search(Map<String, Object> searchCriteria, Pageable pageable);

    /**
     * Check if repository is healthy
     */
    boolean isHealthy();

    /**
     * Get repository type
     */
    String getRepositoryType();
}