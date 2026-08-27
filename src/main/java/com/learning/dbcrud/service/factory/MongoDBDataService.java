package com.learning.dbcrud.service.factory;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.entity.mongodb.MongoEntity;
import com.learning.dbcrud.exception.ResourceNotFoundException;
import com.learning.dbcrud.mapper.EntityMapper;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.PageResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import com.learning.dbcrud.repository.mongodb.MongoDBRepository;
import com.learning.dbcrud.service.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MongoDBDataService implements DataService {
    
    private static final Logger log = LoggerFactory.getLogger(MongoDBDataService.class);
    
    private final MongoDBRepository repository;
    private final EntityMapper entityMapper;
    
    public MongoDBDataService(MongoDBRepository repository, EntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        log.info("MongoDBDataService initialized");
    }
    
    @Override
    public DataResponse create(CreateDataRequest request) {
        log.info("Creating record in MongoDB");
        try {
            MongoEntity entity = entityMapper.toMongoEntity(request);
            MongoEntity saved = repository.save(entity);
            log.info("Record created in MongoDB with ID: {}", saved.getId());
            return entityMapper.toDataResponse(saved);
        } catch (Exception e) {
            log.error("Error creating record in MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create record in MongoDB", e);
        }
    }
    
    @Override
    public DataResponse getById(String id) {
        log.info("Getting record by ID: {} from MongoDB", id);
        try {
            MongoEntity entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            return entityMapper.toDataResponse(entity);
        } catch (Exception e) {
            log.error("Error getting record from MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get record from MongoDB", e);
        }
    }
    
    @Override
    public DataResponse update(String id, UpdateDataRequest request) {
        log.info("Updating record in MongoDB with ID: {}", id);
        try {
            MongoEntity existing = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            
            entityMapper.updateMongoEntity(existing, request);
            MongoEntity updated = repository.update(existing);
            log.info("Record updated in MongoDB with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (Exception e) {
            log.error("Error updating record in MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update record in MongoDB", e);
        }
    }
    
    @Override
    public DataResponse partialUpdate(String id, Map<String, Object> updates) {
        log.info("Partially updating record in MongoDB with ID: {}", id);
        try {
            MongoEntity updated = repository.partialUpdate(id, updates);
            log.info("Record partially updated in MongoDB with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (Exception e) {
            log.error("Error partially updating record in MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to partially update record in MongoDB", e);
        }
    }
    
    @Override
    public void delete(String id) {
        log.info("Deleting record from MongoDB with ID: {}", id);
        try {
            repository.deleteById(id);
            log.info("Record deleted from MongoDB with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting record from MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete record from MongoDB", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> list(Map<String, String> filters, Pageable pageable) {
        log.info("Listing records from MongoDB with filters: {}", filters);
        try {
            Map<String, Object> filterMap = filters.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            Page<MongoEntity> page = repository.findAll(filterMap, pageable);
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
            log.error("Error listing records from MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list records from MongoDB", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> search(Map<String, Object> searchCriteria, Pageable pageable) {
        log.info("Searching records in MongoDB with criteria: {}", searchCriteria);
        try {
            Page<MongoEntity> page = repository.search(searchCriteria, pageable);
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
            log.error("Error searching records in MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search records in MongoDB", e);
        }
    }
    
    @Override
    public List<DataResponse> batchCreate(List<CreateDataRequest> requests) {
        log.info("Batch creating {} records in MongoDB", requests.size());
        try {
            List<MongoEntity> entities = requests.stream()
                    .map(entityMapper::toMongoEntity)
                    .collect(Collectors.toList());
            
            List<MongoEntity> saved = repository.saveAll(entities);
            log.info("Batch created {} records in MongoDB", saved.size());
            
            return saved.stream()
                    .map(entityMapper::toDataResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error batch creating records in MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch create records in MongoDB", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        log.debug("Checking MongoDB health");
        return repository.isHealthy();
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MONGODB;
    }
}