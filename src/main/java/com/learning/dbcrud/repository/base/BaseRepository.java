package com.learning.dbcrud.repository.base;

import com.learning.dbcrud.repository.interfaces.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base repository implementing common functionality
 * Following Template Method Pattern
 *
 * @param <T> The entity type
 * @param <ID> The ID type
 */
public abstract class BaseRepository<T, ID> implements DataRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseRepository.class);

    protected abstract T doSave(T entity);
    protected abstract List<T> doSaveAll(List<T> entities);
    protected abstract Optional<T> doFindById(ID id);
    protected abstract Page<T> doFindAll(Pageable pageable);
    protected abstract Page<T> doFindAll(Map<String, Object> filters, Pageable pageable);
    protected abstract T doUpdate(T entity);
    protected abstract T doPartialUpdate(ID id, Map<String, Object> updates);
    protected abstract void doDeleteById(ID id);
    protected abstract void doDelete(T entity);
    protected abstract void doDeleteAll();
    protected abstract boolean doExistsById(ID id);
    protected abstract long doCount();
    protected abstract long doCount(Map<String, Object> filters);
    protected abstract Page<T> doSearch(Map<String, Object> searchCriteria, Pageable pageable);
    protected abstract boolean doIsHealthy();

    @Override
    public T save(T entity) {
        log.debug("Saving entity: {}", entity);
        try {
            return doSave(entity);
        } catch (Exception e) {
            log.error("Error saving entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entity", e);
        }
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        log.debug("Saving {} entities", entities.size());
        try {
            return doSaveAll(entities);
        } catch (Exception e) {
            log.error("Error saving entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entities", e);
        }
    }

    @Override
    public Optional<T> findById(ID id) {
        log.debug("Finding entity by ID: {}", id);
        try {
            return doFindById(id);
        } catch (Exception e) {
            log.error("Error finding entity: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        log.debug("Finding all entities with pagination: {}", pageable);
        try {
            return doFindAll(pageable);
        } catch (Exception e) {
            log.error("Error finding entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find entities", e);
        }
    }

    @Override
    public Page<T> findAll(Map<String, Object> filters, Pageable pageable) {
        log.debug("Finding entities with filters: {}, pageable: {}", filters, pageable);
        try {
            return doFindAll(filters, pageable);
        } catch (Exception e) {
            log.error("Error finding entities with filters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find entities with filters", e);
        }
    }

    @Override
    public T update(T entity) {
        log.debug("Updating entity: {}", entity);
        try {
            return doUpdate(entity);
        } catch (Exception e) {
            log.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Override
    public T partialUpdate(ID id, Map<String, Object> updates) {
        log.debug("Partial update for ID: {}, updates: {}", id, updates);
        try {
            return doPartialUpdate(id, updates);
        } catch (Exception e) {
            log.error("Error performing partial update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform partial update", e);
        }
    }

    @Override
    public void deleteById(ID id) {
        log.debug("Deleting entity by ID: {}", id);
        try {
            doDeleteById(id);
        } catch (Exception e) {
            log.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    @Override
    public void delete(T entity) {
        log.debug("Deleting entity: {}", entity);
        try {
            doDelete(entity);
        } catch (Exception e) {
            log.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    @Override
    public void deleteAll() {
        log.debug("Deleting all entities");
        try {
            doDeleteAll();
        } catch (Exception e) {
            log.error("Error deleting all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all entities", e);
        }
    }

    @Override
    public boolean existsById(ID id) {
        log.debug("Checking existence of ID: {}", id);
        try {
            return doExistsById(id);
        } catch (Exception e) {
            log.error("Error checking existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public long count() {
        log.debug("Counting total entities");
        try {
            return doCount();
        } catch (Exception e) {
            log.error("Error counting entities: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long count(Map<String, Object> filters) {
        log.debug("Counting entities with filters: {}", filters);
        try {
            return doCount(filters);
        } catch (Exception e) {
            log.error("Error counting entities with filters: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public Page<T> search(Map<String, Object> searchCriteria, Pageable pageable) {
        log.debug("Searching with criteria: {}, pageable: {}", searchCriteria, pageable);
        try {
            return doSearch(searchCriteria, pageable);
        } catch (Exception e) {
            log.error("Error searching entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search entities", e);
        }
    }

    @Override
    public boolean isHealthy() {
        log.debug("Checking repository health");
        try {
            return doIsHealthy();
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}