package com.learning.dbcrud.repository.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.learning.dbcrud.entity.elasticsearch.ElasticEntity;
import com.learning.dbcrud.repository.base.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ElasticsearchRepository extends BaseRepository<ElasticEntity, String> {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRepository.class);

    private final ElasticsearchClient esClient;
    private final ElasticDataRepository dataRepository;
    private static final String INDEX_NAME = "data_entries";

    public ElasticsearchRepository(ElasticsearchClient esClient,
                                   ElasticDataRepository dataRepository) {
        this.esClient = esClient;
        this.dataRepository = dataRepository;
    }

    @Override
    protected ElasticEntity doSave(ElasticEntity entity) {
        log.debug("Saving entity to Elasticsearch: {}", entity.getId());
        try {
            IndexResponse response = esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(entity.getId())
                    .document(entity)
                    .refresh(Refresh.True)
            );

            log.debug("Document indexed with ID: {}", response.id());
            return entity;

        } catch (IOException | ElasticsearchException e) {
            log.error("Error saving entity to Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entity to Elasticsearch", e);
        }
    }

    @Override
    protected List<ElasticEntity> doSaveAll(List<ElasticEntity> entities) {
        log.debug("Saving {} entities to Elasticsearch", entities.size());
        try {
            BulkResponse response = esClient.bulk(b -> {
                b.refresh(Refresh.True);
                for (ElasticEntity entity : entities) {
                    b.operations(op -> op
                            .index(idx -> idx
                                    .index(INDEX_NAME)
                                    .id(entity.getId())
                                    .document(entity)
                            )
                    );
                }
                return b;
            });

            if (response.errors()) {
                log.warn("Some documents failed to index. Check individual responses.");
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("Error indexing document: {}", item.error().reason());
                    }
                });
            }

            return entities;

        } catch (IOException | ElasticsearchException e) {
            log.error("Error saving entities to Elasticsearch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save entities to Elasticsearch", e);
        }
    }

    @Override
    protected Optional<ElasticEntity> doFindById(String id) {
        log.debug("Finding entity by ID: {} in Elasticsearch", id);
        try {
            GetResponse<ElasticEntity> response = esClient.get(g -> g
                            .index(INDEX_NAME)
                            .id(id),
                    ElasticEntity.class
            );

            if (response.found()) {
                return Optional.of(response.source());
            }
            return Optional.empty();

        } catch (IOException | ElasticsearchException e) {
            log.error("Error finding entity by ID: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    protected Page<ElasticEntity> doFindAll(Pageable pageable) {
        log.debug("Finding all entities from Elasticsearch with pagination: {}", pageable);
        try {
            SearchResponse<ElasticEntity> response = esClient.search(s -> {
                        s.index(INDEX_NAME)
                                .from((int) pageable.getOffset())
                                .size(pageable.getPageSize());

                        if (pageable.getSort().isSorted()) {
                            for (Sort.Order order : pageable.getSort()) {
                                s.sort(so -> so
                                        .field(f -> f
                                                .field(order.getProperty())
                                                .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                                        )
                                );
                            }
                        }
                        return s;
                    },
                    ElasticEntity.class
            );

            List<ElasticEntity> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ?
                    response.hits().total().value() : 0;

            return new PageImpl<>(results, pageable, total);

        } catch (IOException | ElasticsearchException e) {
            log.error("Error finding all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find all entities", e);
        }
    }

    @Override
    protected Page<ElasticEntity> doFindAll(Map<String, Object> filters, Pageable pageable) {
        log.debug("Finding entities with filters: {}, pageable: {}", filters, pageable);
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // Always exclude deleted records
            boolBuilder.filter(f -> f.term(t -> t
                    .field("isDeleted")
                    .value(false)
            ));

            // Apply filters
            filters.forEach((key, value) -> {
                if (value == null) return;

                switch (key) {
                    case "title":
                        boolBuilder.must(m -> m.match(mt -> mt
                                .field("title")
                                .query(value.toString())
                        ));
                        break;
                    case "author":
                        boolBuilder.must(m -> m.term(t -> t
                                .field("author.keyword")
                                .value(value.toString())
                        ));
                        break;
                    case "content":
                        boolBuilder.must(m -> m.match(mt -> mt
                                .field("content")
                                .query(value.toString())
                        ));
                        break;
                    case "tags":
                        if (value instanceof List) {
                            for (String tag : (List<String>) value) {
                                boolBuilder.must(m -> m.term(t -> t
                                        .field("tags.keyword")
                                        .value(tag)
                                ));
                            }
                        }
                        break;
                    default:
                        boolBuilder.must(m -> m.match(mt -> mt
                                .field(key)
                                .query(value.toString())
                        ));
                        break;
                }
            });

            SearchResponse<ElasticEntity> response = esClient.search(s -> {
                        s.index(INDEX_NAME)
                                .query(q -> q.bool(boolBuilder.build()))
                                .from((int) pageable.getOffset())
                                .size(pageable.getPageSize());

                        if (pageable.getSort().isSorted()) {
                            for (Sort.Order order : pageable.getSort()) {
                                s.sort(so -> so
                                        .field(f -> f
                                                .field(order.getProperty())
                                                .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                                        )
                                );
                            }
                        }
                        return s;
                    },
                    ElasticEntity.class
            );

            List<ElasticEntity> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ?
                    response.hits().total().value() : 0;

            return new PageImpl<>(results, pageable, total);

        } catch (IOException | ElasticsearchException e) {
            log.error("Error finding entities with filters: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to find entities with filters", e);
        }
    }

    @Override
    protected ElasticEntity doUpdate(ElasticEntity entity) {
        log.debug("Updating entity in Elasticsearch: {}", entity.getId());
        try {
            esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(entity.getId())
                    .document(entity)
                    .refresh(Refresh.True)
            );
            return entity;

        } catch (IOException | ElasticsearchException e) {
            log.error("Error updating entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update entity", e);
        }
    }

    @Override
    protected ElasticEntity doPartialUpdate(String id, Map<String, Object> updates) {
        log.debug("Partial update for ID: {}, updates: {}", id, updates);
        try {
            Optional<ElasticEntity> existing = doFindById(id);
            if (existing.isEmpty()) {
                throw new RuntimeException("Entity not found with id: " + id);
            }

            ElasticEntity entity = existing.get();

            updates.forEach((key, value) -> {
                switch (key) {
                    case "title":
                        entity.setTitle((String) value);
                        break;
                    case "content":
                        entity.setContent((String) value);
                        break;
                    case "author":
                        entity.setAuthor((String) value);
                        break;
                    case "tags":
                        if (value instanceof List) {
                            entity.setTags((List<String>) value);
                        }
                        break;
                    case "metadata":
                        if (value instanceof Map) {
                            entity.setMetadata((Map<String, Object>) value);
                        }
                        break;
                    case "additionalFields":
                        if (value instanceof Map) {
                            entity.setAdditionalFields((Map<String, Object>) value);
                        }
                        break;
                    default:
                        log.warn("Unknown field for partial update: {}", key);
                        break;
                }
            });

            return doSave(entity);

        } catch (Exception e) {
            log.error("Error performing partial update: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to perform partial update", e);
        }
    }

    @Override
    protected void doDeleteById(String id) {
        log.debug("Deleting entity by ID: {} from Elasticsearch", id);
        try {
            esClient.delete(d -> d
                    .index(INDEX_NAME)
                    .id(id)
                    .refresh(Refresh.True)
            );

        } catch (IOException | ElasticsearchException e) {
            log.error("Error deleting entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete entity", e);
        }
    }

    @Override
    protected void doDelete(ElasticEntity entity) {
        doDeleteById(entity.getId());
    }

    @Override
    protected void doDeleteAll() {
        log.debug("Deleting all entities from Elasticsearch");
        try {
            esClient.deleteByQuery(d -> d
                    .index(INDEX_NAME)
                    .query(q -> q.matchAll(m -> m))
                    .refresh(true)
            );

        } catch (IOException | ElasticsearchException e) {
            log.error("Error deleting all entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all entities", e);
        }
    }

    @Override
    protected boolean doExistsById(String id) {
        log.debug("Checking existence of ID: {} in Elasticsearch", id);
        try {
            GetResponse<ElasticEntity> response = esClient.get(g -> g
                            .index(INDEX_NAME)
                            .id(id),
                    ElasticEntity.class
            );
            return response.found();

        } catch (Exception e) {
            log.error("Error checking existence: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    protected long doCount() {
        log.debug("Counting total entities in Elasticsearch");
        try {
            CountResponse response = esClient.count(c -> c
                    .index(INDEX_NAME)
                    .query(q -> q.matchAll(m -> m))
            );
            return response.count();

        } catch (IOException | ElasticsearchException e) {
            log.error("Error counting entities: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    protected long doCount(Map<String, Object> filters) {
        log.debug("Counting entities with filters: {}", filters);
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // Always exclude deleted records
            boolBuilder.filter(f -> f.term(t -> t
                    .field("isDeleted")
                    .value(false)
            ));

            filters.forEach((key, value) -> {
                if (value == null) return;
                boolBuilder.must(m -> m.match(mt -> mt
                        .field(key)
                        .query(value.toString())
                ));
            });

            CountResponse response = esClient.count(c -> c
                    .index(INDEX_NAME)
                    .query(q -> q.bool(boolBuilder.build()))
            );
            return response.count();

        } catch (IOException | ElasticsearchException e) {
            log.error("Error counting entities with filters: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    protected Page<ElasticEntity> doSearch(Map<String, Object> searchCriteria, Pageable pageable) {
        log.debug("Searching with criteria: {}, pageable: {}", searchCriteria, pageable);
        try {
            BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

            // Always exclude deleted records
            boolBuilder.filter(f -> f.term(t -> t
                    .field("isDeleted")
                    .value(false)
            ));

            // Handle search text
            if (searchCriteria.containsKey("searchText")) {
                String searchText = (String) searchCriteria.get("searchText");
                boolBuilder.must(m -> m.multiMatch(mm -> mm
                        .query(searchText)
                        .fields("title^3", "content^2", "author^1", "searchVector")
                ));
                searchCriteria.remove("searchText");
            }

            // Handle other criteria
            searchCriteria.forEach((key, value) -> {
                if (value == null || key.equals("range")) return;

                if (key.equals("tags") && value instanceof List) {
                    for (String tag : (List<String>) value) {
                        boolBuilder.filter(f -> f.term(t -> t
                                .field("tags.keyword")
                                .value(tag)
                        ));
                    }
                } else {
                    boolBuilder.must(m -> m.match(mt -> mt
                            .field(key)
                            .query(value.toString())
                    ));
                }
            });

            // Handle range queries
            if (searchCriteria.containsKey("range")) {
                Map<String, Map<String, Object>> ranges =
                        (Map<String, Map<String, Object>>) searchCriteria.get("range");

                ranges.forEach((field, rangeValues) -> {
                    if (rangeValues.containsKey("gte")) {
                        boolBuilder.filter(f -> f.range(r -> r
                                .field(field)
                                .gte(JsonData.of(rangeValues.get("gte")))
                        ));
                    }
                    if (rangeValues.containsKey("lte")) {
                        boolBuilder.filter(f -> f.range(r -> r
                                .field(field)
                                .lte(JsonData.of(rangeValues.get("lte")))
                        ));
                    }
                });
            }

            SearchResponse<ElasticEntity> response = esClient.search(s -> {
                        s.index(INDEX_NAME)
                                .query(q -> q.bool(boolBuilder.build()))
                                .from((int) pageable.getOffset())
                                .size(pageable.getPageSize());

                        if (pageable.getSort().isSorted()) {
                            for (Sort.Order order : pageable.getSort()) {
                                s.sort(so -> so
                                        .field(f -> f
                                                .field(order.getProperty())
                                                .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                                        )
                                );
                            }
                        }
                        return s;
                    },
                    ElasticEntity.class
            );

            List<ElasticEntity> results = response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ?
                    response.hits().total().value() : 0;

            return new PageImpl<>(results, pageable, total);

        } catch (IOException | ElasticsearchException e) {
            log.error("Error searching entities: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to search entities", e);
        }
    }

    @Override
    protected boolean doIsHealthy() {
        log.debug("Checking Elasticsearch health");
        try {
            // Check if cluster is healthy
            var healthResponse = esClient.cluster().health();
            return healthResponse != null;

        } catch (IOException | ElasticsearchException e) {
            log.error("Elasticsearch health check failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getRepositoryType() {
        return "elasticsearch";
    }
}