package com.learning.dbcrud.entity.mongodb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "data_entries")
@CompoundIndex(name = "idx_author_title", def = "{'author': 1, 'title': 1}")
public class MongoEntity {

    @Id
    private String id;

    @Indexed(unique = false)
    @Field("title")
    private String title;

    @Field("content")
    private String content;

    @Indexed
    @Field("author")
    private String author;

    @Field("tags")
    private List<String> tags;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Field("additional_fields")
    private Map<String, Object> additionalFields;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Field("version")
    private Long version;

    @Field("is_deleted")
    private Boolean isDeleted = false;

    @Field("search_text")
    private String searchText;
}