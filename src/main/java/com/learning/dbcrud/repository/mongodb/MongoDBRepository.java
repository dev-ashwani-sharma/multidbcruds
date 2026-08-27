package com.learning.dbcrud.repository.mongodb;

import com.learning.dbcrud.entity.mongodb.MongoEntity;
import com.learning.dbcrud.repository.base.BaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoDBRepository extends BaseRepository<MongoEntity, String> {

    private final MongoDataRepository dataRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    protected MongoEntity doSave(MongoEntity entity) {
        return dataRepository.save(entity);
    }

    @Override
    protected List<MongoEntity> doSaveAll(List<MongoEntity> entities) {
        return dataRepository.saveAll(entities);
    }

    @Override
    protected Optional<MongoEntity> doFindById(String id) {
        return dataRepository.findById(id);
    }

    @Override
    protected Page<MongoEntity> doFindAll(Pageable pageable) {
        return dataRepository.findAll(pageable);
    }

    @Override
    protected Page<MongoEntity> doFindAll(Map<String, Object> filters, Pageable pageable) {
        Query query = buildQueryFromFilters(filters);
        query.with(pageable);

        List<MongoEntity> results = mongoTemplate.find(query, MongoEntity.class);
        long total = mongoTemplate.count(query, MongoEntity.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    protected MongoEntity doUpdate(MongoEntity entity) {
        return dataRepository.save(entity);
    }

    @Override
    protected MongoEntity doPartialUpdate(String id, Map<String, Object> updates) {
        Query query = new Query(Criteria.where("id").is(id));
        Update update = new Update();

        updates.forEach((key, value) -> {
            switch (key) {
                case "title":
                    update.set("title", value);
                    break;
                case "content":
                    update.set("content", value);
                    break;
                case "author":
                    update.set("author", value);
                    break;
                case "tags":
                    update.set("tags", value);
                    break;
                case "metadata":
                    update.set("metadata", value);
                    break;
                case "additionalFields":
                    update.set("additional_fields", value);
                    break;
                default:
                    update.set(key, value);
                    break;
            }
        });

        update.set("updated_at", java.time.LocalDateTime.now());

        MongoEntity updated = mongoTemplate.findAndModify(query, update, MongoEntity.class);
        if (updated == null) {
            throw new RuntimeException("Entity not found with id: " + id);
        }
        return updated;
    }

    @Override
    protected void doDeleteById(String id) {
        dataRepository.deleteById(id);
    }

    @Override
    protected void doDelete(MongoEntity entity) {
        dataRepository.delete(entity);
    }

    @Override
    protected void doDeleteAll() {
        dataRepository.deleteAll();
    }

    @Override
    protected boolean doExistsById(String id) {
        return dataRepository.existsById(id);
    }

    @Override
    protected long doCount() {
        return dataRepository.count();
    }

    @Override
    protected long doCount(Map<String, Object> filters) {
        Query query = buildQueryFromFilters(filters);
        return mongoTemplate.count(query, MongoEntity.class);
    }

    @Override
    protected Page<MongoEntity> doSearch(Map<String, Object> searchCriteria, Pageable pageable) {
        Query query = buildSearchQuery(searchCriteria);
        query.with(pageable);

        // Add text search if available
        if (searchCriteria.containsKey("searchText")) {
            String searchText = (String) searchCriteria.get("searchText");
            query.addCriteria(Criteria.where("search_text").regex(searchText, "i"));
        }

        List<MongoEntity> results = mongoTemplate.find(query, MongoEntity.class);
        long total = mongoTemplate.count(query, MongoEntity.class);

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    protected boolean doIsHealthy() {
        try {
            mongoTemplate.executeCommand("{ ping: 1 }");
            return true;
        } catch (Exception e) {
            log.error("MongoDB health check failed", e);
            return false;
        }
    }

    @Override
    public String getRepositoryType() {
        return "mongodb";
    }

    private Query buildQueryFromFilters(Map<String, Object> filters) {
        Query query = new Query();

        filters.forEach((key, value) -> {
            if (value == null) return;

            switch (key) {
                case "title":
                    query.addCriteria(Criteria.where("title").regex(value.toString(), "i"));
                    break;
                case "content":
                    query.addCriteria(Criteria.where("content").regex(value.toString(), "i"));
                    break;
                case "author":
                    query.addCriteria(Criteria.where("author").is(value));
                    break;
                case "tags":
                    query.addCriteria(Criteria.where("tags").in(value));
                    break;
                case "createdAtFrom":
                    query.addCriteria(Criteria.where("created_at").gte(value));
                    break;
                case "createdAtTo":
                    query.addCriteria(Criteria.where("created_at").lte(value));
                    break;
                default:
                    query.addCriteria(Criteria.where(key).is(value));
                    break;
            }
        });

        // Exclude deleted records by default
        query.addCriteria(Criteria.where("is_deleted").ne(true));

        return query;
    }

    private Query buildSearchQuery(Map<String, Object> searchCriteria) {
        Query query = new Query();

        searchCriteria.forEach((key, value) -> {
            if (value == null) return;

            switch (key) {
                case "title":
                    query.addCriteria(Criteria.where("title").regex(value.toString(), "i"));
                    break;
                case "author":
                    query.addCriteria(Criteria.where("author").is(value));
                    break;
                case "tags":
                    query.addCriteria(Criteria.where("tags").in(value));
                    break;
                case "metadata":
                    query.addCriteria(Criteria.where("metadata").is(value));
                    break;
                case "range":
                    Map<String, Object> range = (Map<String, Object>) value;
                    range.forEach((field, rangeValue) -> {
                        Map<String, Object> rangeValues = (Map<String, Object>) rangeValue;
                        if (rangeValues.containsKey("gte")) {
                            query.addCriteria(Criteria.where(field).gte(rangeValues.get("gte")));
                        }
                        if (rangeValues.containsKey("lte")) {
                            query.addCriteria(Criteria.where(field).lte(rangeValues.get("lte")));
                        }
                    });
                    break;
                default:
                    query.addCriteria(Criteria.where(key).is(value));
                    break;
            }
        });

        query.addCriteria(Criteria.where("is_deleted").ne(true));
        return query;
    }
}