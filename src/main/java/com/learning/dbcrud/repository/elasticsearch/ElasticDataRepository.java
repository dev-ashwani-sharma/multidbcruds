package com.learning.dbcrud.repository.elasticsearch;

import com.learning.dbcrud.entity.elasticsearch.ElasticEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticDataRepository extends ElasticsearchRepository<ElasticEntity, String> {

    @Query("{\"match\": {\"author\": \"?0\"}}")
    Page<ElasticEntity> findByAuthor(String author, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"title\": \"?0\"}}]}}")
    Page<ElasticEntity> searchByTitle(String title, Pageable pageable);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"tags\": \"?0\"}}]}}")
    List<ElasticEntity> findByTags(String tag);

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"content\", \"author\"]}}")
    Page<ElasticEntity> fullTextSearch(String searchText, Pageable pageable);

    @Query("{\"range\": {\"createdAt\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}")
    Page<ElasticEntity> findByDateRange(String from, String to, Pageable pageable);
}