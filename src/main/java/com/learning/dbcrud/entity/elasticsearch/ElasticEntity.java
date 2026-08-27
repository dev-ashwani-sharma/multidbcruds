package com.learning.dbcrud.entity.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "data_entries")
@Setting(settingPath = "/elasticsearch/settings.json")
@Mapping(mappingPath = "/elasticsearch/mappings.json")
public class ElasticEntity {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard",
            searchAnalyzer = "standard", fielddata = true)
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard",
            searchAnalyzer = "standard")
    private String content;

    @Field(type = FieldType.Keyword)
    private String author;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Object)
    private Map<String, Object> metadata;

    @Field(type = FieldType.Object)
    private Map<String, Object> additionalFields;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Long)
    private Long version;

    @Field(type = FieldType.Boolean)
    private Boolean isDeleted = false;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String searchVector;
}