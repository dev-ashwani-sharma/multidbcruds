package com.learning.dbcrud.controller;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.DataResponse;
import com.learning.dbcrud.model.dto.PageResponse;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import com.learning.dbcrud.service.factory.DataServiceFactory;
import com.learning.dbcrud.service.interfaces.DataService;
import com.learning.dbcrud.validator.DataValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/data")
@RequiredArgsConstructor
public class DataController {

    private static final Logger log = LoggerFactory.getLogger(DataController.class);

    @Autowired
    private DataServiceFactory dataServiceFactory;

    @Autowired
    private DataValidator dataValidator;

    /**
     * Create a new record in the specified database
     */
    @PostMapping
    public ResponseEntity<DataResponse> create(
            @RequestHeader("X-Database-Type") String databaseType,
            @Valid @RequestBody CreateDataRequest request) {

        log.info("Create request received for database: {}", databaseType);

        // Validate database type
        DatabaseType dbType = validateDatabaseType(databaseType);

        // Validate request data
        dataValidator.validateCreateRequest(request);

        // Get appropriate service
        DataService service = dataServiceFactory.getService(dbType);

        // Perform create operation
        DataResponse response = service.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Database-Type", dbType.name())
                .body(response);
    }

    /**
     * Create with database type in path
     */
    @PostMapping("/{databaseType}")
    public ResponseEntity<DataResponse> createWithPath(
            @PathVariable String databaseType,
            @Valid @RequestBody CreateDataRequest request) {

        return create(databaseType, request);
    }

    /**
     * Get record by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataResponse> getById(
            @RequestHeader(value = "X-Database-Type", required = false) String databaseType,
            @PathVariable String id,
            @RequestParam(required = false) String dbType) {

        // Allow database type from header or query param
        String type = dbType != null ? dbType : databaseType;
        DatabaseType dbTypeEnum = validateDatabaseType(type);

        log.info("Get request for ID: {} from database: {}", id, dbTypeEnum);

        DataService service = dataServiceFactory.getService(dbTypeEnum);
        DataResponse response = service.getById(id);

        return ResponseEntity.ok()
                .header("X-Database-Type", dbTypeEnum.name())
                .body(response);
    }

    /**
     * Update record
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataResponse> update(
            @RequestHeader("X-Database-Type") String databaseType,
            @PathVariable String id,
            @Valid @RequestBody UpdateDataRequest request) {

        DatabaseType dbType = validateDatabaseType(databaseType);
        log.info("Update request for ID: {} in database: {}", id, dbType);

        dataValidator.validateUpdateRequest(request);

        DataService service = dataServiceFactory.getService(dbType);
        DataResponse response = service.update(id, request);

        return ResponseEntity.ok()
                .header("X-Database-Type", dbType.name())
                .body(response);
    }

    /**
     * Partial update record
     */
    @PatchMapping("/{id}")
    public ResponseEntity<DataResponse> partialUpdate(
            @RequestHeader("X-Database-Type") String databaseType,
            @PathVariable String id,
            @RequestBody Map<String, Object> updates) {

        DatabaseType dbType = validateDatabaseType(databaseType);
        log.info("Partial update request for ID: {} in database: {}", id, dbType);

        DataService service = dataServiceFactory.getService(dbType);
        DataResponse response = service.partialUpdate(id, updates);

        return ResponseEntity.ok()
                .header("X-Database-Type", dbType.name())
                .body(response);
    }

    /**
     * Delete record
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-Database-Type") String databaseType,
            @PathVariable String id) {

        DatabaseType dbType = validateDatabaseType(databaseType);
        log.info("Delete request for ID: {} from database: {}", id, dbType);

        DataService service = dataServiceFactory.getService(dbType);
        service.delete(id);

        return ResponseEntity.noContent()
                .header("X-Database-Type", dbType.name())
                .build();
    }

    /**
     * List records with pagination and filters
     */
    @GetMapping
    public ResponseEntity<PageResponse<DataResponse>> list(
            @RequestHeader(value = "X-Database-Type", required = false) String databaseType,
            @RequestParam(required = false) String dbType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam Map<String, String> filters) {

        // Allow database type from header or query param
        String type = dbType != null ? dbType : databaseType;
        DatabaseType dbTypeEnum = validateDatabaseType(type);

        log.info("List request for database: {} with page: {}, size: {}", dbTypeEnum, page, size);

        // Build sort
        Sort sort = buildSort(sortBy, sortDirection);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Remove pagination params from filters
        filters.remove("page");
        filters.remove("size");
        filters.remove("sortBy");
        filters.remove("sortDirection");
        filters.remove("dbType");

        DataService service = dataServiceFactory.getService(dbTypeEnum);
        PageResponse<DataResponse> response = service.list(filters, pageable);

        return ResponseEntity.ok()
                .header("X-Database-Type", dbTypeEnum.name())
                .body(response);
    }

    /**
     * Search records with complex queries
     */
    @PostMapping("/search")
    public ResponseEntity<PageResponse<DataResponse>> search(
            @RequestHeader(value = "X-Database-Type", required = false) String databaseType,
            @RequestParam(required = false) String dbType,
            @RequestBody Map<String, Object> searchCriteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String type = dbType != null ? dbType : databaseType;
        DatabaseType dbTypeEnum = validateDatabaseType(type);

        log.info("Search request for database: {}", dbTypeEnum);

        Pageable pageable = PageRequest.of(page, size);

        DataService service = dataServiceFactory.getService(dbTypeEnum);
        PageResponse<DataResponse> response = service.search(searchCriteria, pageable);

        return ResponseEntity.ok()
                .header("X-Database-Type", dbTypeEnum.name())
                .body(response);
    }

    /**
     * Batch create records
     */
    @PostMapping("/batch")
    public ResponseEntity<List<DataResponse>> batchCreate(
            @RequestHeader("X-Database-Type") String databaseType,
            @Valid @RequestBody List<CreateDataRequest> requests) {

        DatabaseType dbType = validateDatabaseType(databaseType);
        log.info("Batch create request for database: {} with {} items", dbType, requests.size());

        DataService service = dataServiceFactory.getService(dbType);
        List<DataResponse> responses = service.batchCreate(requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("X-Database-Type", dbType.name())
                .body(responses);
    }

    /**
     * Validate database type
     */
    private DatabaseType validateDatabaseType(String type) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type must be provided");
        }

        try {
            return DatabaseType.fromValue(type.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid database type: %s. Supported types: %s",
                            type, DatabaseType.getSupportedTypes())
            );
        }
    }

    /**
     * Build Sort object
     */
    private Sort buildSort(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return Sort.unsorted();
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(direction, sortBy);
    }
}