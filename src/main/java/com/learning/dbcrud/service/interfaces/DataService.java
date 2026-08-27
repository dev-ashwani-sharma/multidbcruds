package com.learning.dbcrud.service.interfaces;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.PageResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface DataService {
    
    DataResponse create(CreateDataRequest request);
    
    DataResponse getById(String id);
    
    DataResponse update(String id, UpdateDataRequest request);
    
    DataResponse partialUpdate(String id, Map<String, Object> updates);
    
    void delete(String id);
    
    PageResponse<DataResponse> list(Map<String, String> filters, Pageable pageable);
    
    PageResponse<DataResponse> search(Map<String, Object> searchCriteria, Pageable pageable);
    
    List<DataResponse> batchCreate(List<CreateDataRequest> requests);
    
    boolean isHealthy();
    
    DatabaseType getDatabaseType();
}