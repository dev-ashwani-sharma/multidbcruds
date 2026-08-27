package com.learning.dbcrud.service.factory;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.entity.mysql.MySQLEntity;
import com.learning.dbcrud.exception.ResourceNotFoundException;
import com.learning.dbcrud.mapper.EntityMapper;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.PageResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import com.learning.dbcrud.repository.interfaces.DataRepository;
import com.learning.dbcrud.repository.mysql.MySQLRepository;
import com.learning.dbcrud.service.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class MySQLDataService implements DataService {
    
    private static final Logger log = LoggerFactory.getLogger(MySQLDataService.class);
    
    private final MySQLRepository repository;
    private final EntityMapper entityMapper;
    
    public MySQLDataService(MySQLRepository repository, EntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        log.info("MySQLDataService initialized");
    }
    
    @Override
    public DataResponse create(CreateDataRequest request) {
        log.info("Creating record in MySQL");
        try {
            MySQLEntity entity = entityMapper.toMySQLEntity(request);
            MySQLEntity saved = repository.save(entity);
            log.info("Record created in MySQL with ID: {}", saved.getId());
            return entityMapper.toDataResponse(saved);
        } catch (Exception e) {
            log.error("Error creating record in MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create record in MySQL", e);
        }
    }
    
    @Override
    public DataResponse getById(String id) {
        log.info("Getting record by ID: {} from MySQL", id);
        try {
            Long longId = Long.parseLong(id);
            MySQLEntity entity = repository.findById(longId)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            return entityMapper.toDataResponse(entity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + id);
        } catch (Exception e) {
            log.error("Error getting record from MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get record from MySQL", e);
        }
    }
    
    @Override
    public DataResponse update(String id, UpdateDataRequest request) {
        log.info("Updating record in MySQL with ID: {}", id);
        try {
            Long longId = Long.parseLong(id);
            MySQLEntity existing = repository.findById(longId)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            
            entityMapper.updateMySQLEntity(existing, request);
            MySQLEntity updated = repository.update(existing);
            log.info("Record updated in MySQL with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + id);
        } catch (Exception e) {
            log.error("Error updating record in MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update record in MySQL", e);
        }
    }
    
    @Override
    public DataResponse partialUpdate(String id, Map<String, Object> updates) {
        log.info("Partially updating record in MySQL with ID: {}", id);
        try {
            Long longId = Long.parseLong(id);
            MySQLEntity updated = repository.partialUpdate(longId, updates);
            log.info("Record partially updated in MySQL with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + id);
        } catch (Exception e) {
            log.error("Error partially updating record in MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to partially update record in MySQL", e);
        }
    }
    
    @Override
    public void delete(String id) {
        log.info("Deleting record from MySQL with ID: {}", id);
        try {
            Long longId = Long.parseLong(id);
            repository.deleteById(longId);
            log.info("Record deleted from MySQL with ID: {}", id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ID format: " + id);
        } catch (Exception e) {
            log.error("Error deleting record from MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete record from MySQL", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> list(Map<String, String> filters, Pageable pageable) {
        log.info("Listing records from MySQL with filters: {}", filters);
        try {
            // Convert Map<String, String> to Map<String, Object>
            Map<String, Object> filterMap = filters.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            Page<MySQLEntity> page = repository.findAll(filterMap, pageable);
            List<DataResponse> responses = page.getContent().stream()
                    .map(entityMapper::toDataResponse)
                    .collect(Collectors.toList());
            
            return new PageResponse<>(
                responses,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
            );
        } catch (Exception e) {
            log.error("Error listing records from MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list records from MySQL", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> search(Map<String, Object> searchCriteria, Pageable pageable) {
        log.info("Searching records in MySQL with criteria: {}", searchCriteria);
        try {
            Page<MySQLEntity> page = repository.search(searchCriteria, pageable);
            List<DataResponse> responses = page.getContent().stream()
                    .map(entityMapper::toDataResponse)
                    .collect(Collectors.toList());
            
            return new PageResponse<>(
                responses,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
            );
        } catch (Exception e) {
            log.error("Error searching records in MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search records in MySQL", e);
        }
    }
    
    @Override
    public List<DataResponse> batchCreate(List<CreateDataRequest> requests) {
        log.info("Batch creating {} records in MySQL", requests.size());
        try {
            List<MySQLEntity> entities = requests.stream()
                    .map(entityMapper::toMySQLEntity)
                    .collect(Collectors.toList());
            
            List<MySQLEntity> saved = repository.saveAll(entities);
            log.info("Batch created {} records in MySQL", saved.size());
            
            return saved.stream()
                    .map(entityMapper::toDataResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error batch creating records in MySQL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch create records in MySQL", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        log.debug("Checking MySQL health");
        return repository.isHealthy();
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}