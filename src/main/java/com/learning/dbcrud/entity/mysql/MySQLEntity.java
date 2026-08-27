package com.learning.dbcrud.entity.mysql;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "data_entries",
        indexes = {
                @Index(name = "idx_author", columnList = "author"),
                @Index(name = "idx_title", columnList = "title"),
                @Index(name = "idx_created_at", columnList = "created_at")
        })
public class MySQLEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    @ElementCollection
    @CollectionTable(name = "data_tags",
            joinColumns = @JoinColumn(name = "data_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "additional_fields", columnDefinition = "JSON")
    @JsonProperty("additional_fields")
    private String additionalFields;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "search_vector", columnDefinition = "TEXT")
    private String searchVector;

    // Default constructor
    public MySQLEntity() {}

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getAdditionalFields() {
        return additionalFields;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public String getSearchVector() {
        return searchVector;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public void setAdditionalFields(String additionalFields) {
        this.additionalFields = additionalFields;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void setSearchVector(String searchVector) {
        this.searchVector = searchVector;
    }
}