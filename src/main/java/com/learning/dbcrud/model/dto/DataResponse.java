package com.learning.dbcrud.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponse {

    private String id;
    private String title;
    private String content;
    private String author;
    private List<String> tags;
    private Map<String, Object> metadata;
    private Map<String, Object> additionalFields;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String version;

    // Database specific fields
    private Long mysqlId;          // For MySQL
    private String mongoId;        // For MongoDB
    private String elasticId;      // For Elasticsearch
}