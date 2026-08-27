package com.learning.dbcrud.service.factory;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.entity.elasticsearch.ElasticEntity;
import com.learning.dbcrud.exception.ResourceNotFoundException;
import com.learning.dbcrud.mapper.EntityMapper;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.PageResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import com.learning.dbcrud.repository.elasticsearch.ElasticsearchRepository;
import com.learning.dbcrud.service.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ElasticsearchDataService implements DataService {
    
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchDataService.class);
    
    private final ElasticsearchRepository repository;
    private final EntityMapper entityMapper;
    
    public ElasticsearchDataService(ElasticsearchRepository repository, EntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        log.info("ElasticsearchDataService initialized");
    }
    
    @Override
    public DataResponse create(CreateDataRequest request) {
        log.info("Creating record in Elasticsearch");
        try {
            // Generate ID if not present
            if (request.getAdditionalFields() != null && 
                request.getAdditionalFields().containsKey("id")) {
                // Use provided ID
            }
            ElasticEntity entity = entityMapper.toElasticEntity(request);
            ElasticEntity saved = repository.save(entity);
            log.info("Record created in Elasticsearch with ID: {}", saved.getId());
            return entityMapper.toDataResponse(saved);
        } catch (Exception e) {
            log.error("Error creating record in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create record in Elasticsearch", e);
        }
    }
    
    @Override
    public DataResponse getById(String id) {
        log.info("Getting record by ID: {} from Elasticsearch", id);
        try {
            ElasticEntity entity = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            return entityMapper.toDataResponse(entity);
        } catch (Exception e) {
            log.error("Error getting record from Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get record from Elasticsearch", e);
        }
    }
    
    @Override
    public DataResponse update(String id, UpdateDataRequest request) {
        log.info("Updating record in Elasticsearch with ID: {}", id);
        try {
            ElasticEntity existing = repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Record not found with ID: " + id));
            
            entityMapper.updateElasticEntity(existing, request);
            ElasticEntity updated = repository.update(existing);
            log.info("Record updated in Elasticsearch with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (Exception e) {
            log.error("Error updating record in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update record in Elasticsearch", e);
        }
    }
    
    @Override
    public DataResponse partialUpdate(String id, Map<String, Object> updates) {
        log.info("Partially updating record in Elasticsearch with ID: {}", id);
        try {
            ElasticEntity updated = repository.partialUpdate(id, updates);
            log.info("Record partially updated in Elasticsearch with ID: {}", updated.getId());
            return entityMapper.toDataResponse(updated);
        } catch (Exception e) {
            log.error("Error partially updating record in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to partially update record in Elasticsearch", e);
        }
    }
    
    @Override
    public void delete(String id) {
        log.info("Deleting record from Elasticsearch with ID: {}", id);
        try {
            repository.deleteById(id);
            log.info("Record deleted from Elasticsearch with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting record from Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete record from Elasticsearch", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> list(Map<String, String> filters, Pageable pageable) {
        log.info("Listing records from Elasticsearch with filters: {}", filters);
        try {
            Map<String, Object> filterMap = filters.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            Page<ElasticEntity> page = repository.findAll(filterMap, pageable);
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
            log.error("Error listing records from Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list records from Elasticsearch", e);
        }
    }
    
    @Override
    public PageResponse<DataResponse> search(Map<String, Object> searchCriteria, Pageable pageable) {
        log.info("Searching records in Elasticsearch with criteria: {}", searchCriteria);
        try {
            // Add searchText if not present
            if (!searchCriteria.containsKey("searchText")) {
                // You might want to handle this differently
            }
            
            Page<ElasticEntity> page = repository.search(searchCriteria, pageable);
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
            log.error("Error searching records in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search records in Elasticsearch", e);
        }
    }
    
    @Override
    public List<DataResponse> batchCreate(List<CreateDataRequest> requests) {
        log.info("Batch creating {} records in Elasticsearch", requests.size());
        try {
            List<ElasticEntity> entities = requests.stream()
                    .map(entityMapper::toElasticEntity)
                    .collect(Collectors.toList());
            
            List<ElasticEntity> saved = repository.saveAll(entities);
            log.info("Batch created {} records in Elasticsearch", saved.size());
            
            return saved.stream()
                    .map(entityMapper::toDataResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error batch creating records in Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch create records in Elasticsearch", e);
        }
    }
    
    @Override
    public boolean isHealthy() {
        log.debug("Checking Elasticsearch health");
        return repository.isHealthy();
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.ELASTICSEARCH;
    }
}