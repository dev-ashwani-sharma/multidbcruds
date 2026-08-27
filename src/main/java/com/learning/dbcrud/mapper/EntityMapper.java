package com.learning.dbcrud.mapper;

import com.learning.dbcrud.entity.elasticsearch.ElasticEntity;
import com.learning.dbcrud.entity.mongodb.MongoEntity;
import com.learning.dbcrud.entity.mysql.MySQLEntity;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class EntityMapper {
    
    private static final Logger log = LoggerFactory.getLogger(EntityMapper.class);
    
    private final ObjectMapper objectMapper;
    
    public EntityMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    // ============ TO ENTITY METHODS (CREATE) ============
    
    public MySQLEntity toMySQLEntity(CreateDataRequest request) {
        if (request == null) return null;
        
        MySQLEntity entity = new MySQLEntity();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setAuthor(request.getAuthor());
        entity.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        entity.setIsDeleted(false);
        
        try {
            if (request.getMetadata() != null) {
                entity.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            }
            if (request.getAdditionalFields() != null) {
                entity.setAdditionalFields(objectMapper.writeValueAsString(request.getAdditionalFields()));
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializing metadata", e);
        }
        
        entity.setSearchVector(buildSearchVector(request));
        return entity;
    }
    
    public MongoEntity toMongoEntity(CreateDataRequest request) {
        if (request == null) return null;
        
        MongoEntity entity = new MongoEntity();
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setAuthor(request.getAuthor());
        entity.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        entity.setMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>());
        entity.setAdditionalFields(request.getAdditionalFields() != null ? request.getAdditionalFields() : new HashMap<>());
        entity.setIsDeleted(false);
        entity.setSearchText(buildSearchVector(request));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVersion(null);
        
        return entity;
    }
    
    public ElasticEntity toElasticEntity(CreateDataRequest request) {
        if (request == null) return null;
        
        ElasticEntity entity = new ElasticEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setAuthor(request.getAuthor());
        entity.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        entity.setMetadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>());
        entity.setAdditionalFields(request.getAdditionalFields() != null ? request.getAdditionalFields() : new HashMap<>());
        entity.setIsDeleted(false);
        entity.setSearchVector(buildSearchVector(request));
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVersion(0L);
        
        return entity;
    }
    
    // ============ UPDATE ENTITY METHODS ============
    
    /**
     * Update MySQL entity with data from UpdateDataRequest
     * Only updates fields that are not null in the request
     */
    public void updateMySQLEntity(MySQLEntity entity, UpdateDataRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        // Only update if the field is present in the request
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            entity.setContent(request.getContent());
        }
        
        if (request.getAuthor() != null) {
            entity.setAuthor(request.getAuthor());
        }
        
        if (request.getTags() != null) {
            entity.setTags(request.getTags());
        }
        
        if (request.getMetadata() != null) {
            try {
                entity.setMetadata(objectMapper.writeValueAsString(request.getMetadata()));
            } catch (JsonProcessingException e) {
                log.error("Error serializing metadata", e);
            }
        }
        
        if (request.getAdditionalFields() != null) {
            try {
                entity.setAdditionalFields(objectMapper.writeValueAsString(request.getAdditionalFields()));
            } catch (JsonProcessingException e) {
                log.error("Error serializing additional fields", e);
            }
        }
        
        // Update search vector if title, content, or author changed
        if (request.getTitle() != null || request.getContent() != null || request.getAuthor() != null) {
            entity.setSearchVector(buildSearchVectorFromEntity(entity));
        }
    }
    
    /**
     * Update MongoDB entity with data from UpdateDataRequest
     * Only updates fields that are not null in the request
     */
    public void updateMongoEntity(MongoEntity entity, UpdateDataRequest request) {
        if (entity == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }

        if (request.getContent() != null) {
            entity.setContent(request.getContent());
        }

        if (request.getAuthor() != null) {
            entity.setAuthor(request.getAuthor());
        }

        if (request.getTags() != null) {
            entity.setTags(request.getTags());
        }

        if (request.getMetadata() != null) {
            entity.setMetadata(request.getMetadata());
        }

        if (request.getAdditionalFields() != null) {
            entity.setAdditionalFields(request.getAdditionalFields());
        }

        if (request.getTitle() != null || request.getContent() != null || request.getAuthor() != null) {
            entity.setSearchText(buildSearchVectorFromMongoEntity(entity));
        }

        entity.setUpdatedAt(LocalDateTime.now());
        // Version will be automatically incremented by MongoDB
    }
    
    /**
     * Update Elasticsearch entity with data from UpdateDataRequest
     * Only updates fields that are not null in the request
     */
    public void updateElasticEntity(ElasticEntity entity, UpdateDataRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        // Only update if the field is present in the request
        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            entity.setContent(request.getContent());
        }
        
        if (request.getAuthor() != null) {
            entity.setAuthor(request.getAuthor());
        }
        
        if (request.getTags() != null) {
            entity.setTags(request.getTags());
        }
        
        if (request.getMetadata() != null) {
            entity.setMetadata(request.getMetadata());
        }
        
        if (request.getAdditionalFields() != null) {
            entity.setAdditionalFields(request.getAdditionalFields());
        }
        
        // Update search vector if title, content, or author changed
        if (request.getTitle() != null || request.getContent() != null || request.getAuthor() != null) {
            entity.setSearchVector(buildSearchVectorFromElasticEntity(entity));
        }
        
        // Always update the updatedAt timestamp
        entity.setUpdatedAt(LocalDateTime.now());
    }
    
    // ============ TO RESPONSE METHODS ============
    
    public DataResponse toDataResponse(MySQLEntity entity) {
        if (entity == null) return null;
        
        DataResponse response = new DataResponse();
        response.setId(entity.getId() != null ? entity.getId().toString() : null);
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setAuthor(entity.getAuthor());
        response.setTags(entity.getTags());
        response.setMetadata(parseJsonToMap(entity.getMetadata()));
        response.setAdditionalFields(parseJsonToMap(entity.getAdditionalFields()));
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setMysqlId(entity.getId());
        response.setVersion(entity.getVersion() != null ? entity.getVersion().toString() : null);
        
        return response;
    }
    
    public DataResponse toDataResponse(MongoEntity entity) {
        if (entity == null) return null;
        
        DataResponse response = new DataResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setAuthor(entity.getAuthor());
        response.setTags(entity.getTags());
        response.setMetadata(entity.getMetadata());
        response.setAdditionalFields(entity.getAdditionalFields());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setMongoId(entity.getId());
        response.setVersion(entity.getVersion() != null ? entity.getVersion().toString() : null);
        
        return response;
    }
    
    public DataResponse toDataResponse(ElasticEntity entity) {
        if (entity == null) return null;
        
        DataResponse response = new DataResponse();
        response.setId(entity.getId());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setAuthor(entity.getAuthor());
        response.setTags(entity.getTags());
        response.setMetadata(entity.getMetadata());
        response.setAdditionalFields(entity.getAdditionalFields());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        response.setElasticId(entity.getId());
        response.setVersion(entity.getVersion() != null ? entity.getVersion().toString() : null);
        
        return response;
    }
    
    // ============ HELPER METHODS ============
    
    private Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isEmpty() || "{}".equals(json)) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON to Map", e);
            return new HashMap<>();
        }
    }

    private String buildSearchVector(CreateDataRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getTitle() != null) {
            sb.append(request.getTitle()).append(" ");
        }
        if (request.getContent() != null) {
            sb.append(request.getContent()).append(" ");
        }
        if (request.getAuthor() != null) {
            sb.append(request.getAuthor()).append(" ");
        }
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            sb.append(String.join(" ", request.getTags()));
        }
        return sb.toString().trim();
    }

    private String buildSearchVectorFromEntity(MySQLEntity entity) {
        StringBuilder sb = new StringBuilder();
        if (entity.getTitle() != null) {
            sb.append(entity.getTitle()).append(" ");
        }
        if (entity.getContent() != null) {
            sb.append(entity.getContent()).append(" ");
        }
        if (entity.getAuthor() != null) {
            sb.append(entity.getAuthor()).append(" ");
        }
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            sb.append(String.join(" ", entity.getTags()));
        }
        return sb.toString().trim();
    }
    
    private String buildSearchVectorFromMongoEntity(MongoEntity entity) {
        StringBuilder sb = new StringBuilder();
        if (entity.getTitle() != null) {
            sb.append(entity.getTitle()).append(" ");
        }
        if (entity.getContent() != null) {
            sb.append(entity.getContent()).append(" ");
        }
        if (entity.getAuthor() != null) {
            sb.append(entity.getAuthor()).append(" ");
        }
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            sb.append(String.join(" ", entity.getTags()));
        }
        return sb.toString().trim();
    }
    
    private String buildSearchVectorFromElasticEntity(ElasticEntity entity) {
        StringBuilder sb = new StringBuilder();
        if (entity.getTitle() != null) {
            sb.append(entity.getTitle()).append(" ");
        }
        if (entity.getContent() != null) {
            sb.append(entity.getContent()).append(" ");
        }
        if (entity.getAuthor() != null) {
            sb.append(entity.getAuthor()).append(" ");
        }
        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            sb.append(String.join(" ", entity.getTags()));
        }
        return sb.toString().trim();
    }
}