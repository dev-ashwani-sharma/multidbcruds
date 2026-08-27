package com.learning.dbcrud.validator;

import com.learning.dbcrud.model.dto.CreateDataRequest;
import com.learning.dbcrud.model.dto.UpdateDataRequest;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataValidator {
    
    public void validateCreateRequest(CreateDataRequest request) {
        List<String> errors = new ArrayList<>();
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            errors.add("Title is required");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            errors.add("Content is required");
        }
        
        if (request.getAuthor() == null || request.getAuthor().trim().isEmpty()) {
            errors.add("Author is required");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString(), new Exception());
        }
    }
    
    public void validateUpdateRequest(UpdateDataRequest request) {
        List<String> errors = new ArrayList<>();
        
        // At least one field should be present
        if (request.getTitle() == null && 
            request.getContent() == null && 
            request.getAuthor() == null && 
            request.getTags() == null && 
            request.getMetadata() == null && 
            request.getAdditionalFields() == null) {
            errors.add("At least one field must be provided for update");
        }
        
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            errors.add("Title cannot be empty");
        }
        
        if (request.getContent() != null && request.getContent().trim().isEmpty()) {
            errors.add("Content cannot be empty");
        }
        
        if (request.getAuthor() != null && request.getAuthor().trim().isEmpty()) {
            errors.add("Author cannot be empty");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString(), new Exception());
        }
    }
}