package com.learning.dbcrud.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDataRequest {

    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Size(min = 1, message = "Content cannot be empty")
    private String content;

    @Size(min = 1, max = 100, message = "Author must be between 1 and 100 characters")
    private String author;

    private List<String> tags;

    private Map<String, Object> metadata;

    private Map<String, Object> additionalFields;
}