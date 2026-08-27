package com.learning.dbcrud.repository.mysql;

import com.learning.dbcrud.entity.mysql.MySQLEntity;
import com.learning.dbcrud.repository.base.BaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MySQLRepository extends BaseRepository<MySQLEntity, Long> {

    private static final Logger log = LoggerFactory.getLogger(MySQLRepository.class);

    private final MySQLDataRepository dataRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public MySQLRepository(MySQLDataRepository dataRepository,
                           JdbcTemplate jdbcTemplate,
                           ObjectMapper objectMapper) {
        this.dataRepository = dataRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected MySQLEntity doSave(MySQLEntity entity) {
        return dataRepository.save(entity);
    }

    @Override
    protected List<MySQLEntity> doSaveAll(List<MySQLEntity> entities) {
        return dataRepository.saveAll(entities);
    }

    @Override
    protected Optional<MySQLEntity> doFindById(Long id) {
        return dataRepository.findById(id);
    }

    @Override
    protected Page<MySQLEntity> doFindAll(Pageable pageable) {
        return dataRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    protected Page<MySQLEntity> doFindAll(Map<String, Object> filters, Pageable pageable) {
        // Extract filter values
        String title = filters.containsKey("title") ? (String) filters.get("title") : null;
        String author = filters.containsKey("author") ? (String) filters.get("author") : null;
        String content = filters.containsKey("content") ? (String) filters.get("content") : null;

        // Handle tags filter
        if (filters.containsKey("tags")) {
            Object tagsObj = filters.get("tags");
            if (tagsObj instanceof List) {
                List<String> tags = (List<String>) tagsObj;
                return dataRepository.findByTags(tags, pageable);
            }
        }

        return dataRepository.findByFilters(title, author, content, pageable);
    }

    @Override
    protected MySQLEntity doUpdate(MySQLEntity entity) {
        return dataRepository.save(entity);
    }

    @Override
    @Transactional
    protected MySQLEntity doPartialUpdate(Long id, Map<String, Object> updates) {
        MySQLEntity entity = dataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));

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
                    entity.setTags((List<String>) value);
                    break;
                case "metadata":
                    try {
                        entity.setMetadata(objectMapper.writeValueAsString(value));
                    } catch (Exception e) {
                        log.error("Error serializing metadata", e);
                    }
                    break;
                case "additionalFields":
                    try {
                        entity.setAdditionalFields(objectMapper.writeValueAsString(value));
                    } catch (Exception e) {
                        log.error("Error serializing additional fields", e);
                    }
                    break;
                default:
                    log.warn("Unknown field for partial update: {}", key);
                    break;
            }
        });

        return dataRepository.save(entity);
    }

    @Override
    protected void doDeleteById(Long id) {
        dataRepository.deleteById(id);
    }

    @Override
    protected void doDelete(MySQLEntity entity) {
        dataRepository.delete(entity);
    }

    @Override
    protected void doDeleteAll() {
        dataRepository.deleteAll();
    }

    @Override
    protected boolean doExistsById(Long id) {
        return dataRepository.existsById(id);
    }

    @Override
    protected long doCount() {
        return dataRepository.count();
    }

    @Override
    protected long doCount(Map<String, Object> filters) {
        String title = filters.containsKey("title") ? (String) filters.get("title") : null;
        String author = filters.containsKey("author") ? (String) filters.get("author") : null;

        return dataRepository.countByFilters(title, author);
    }

    @Override
    protected Page<MySQLEntity> doSearch(Map<String, Object> searchCriteria, Pageable pageable) {
        // Check if search text is present
        if (searchCriteria.containsKey("searchText")) {
            String searchText = (String) searchCriteria.get("searchText");
            return dataRepository.fullTextSearch(searchText, pageable);
        }

        // Otherwise use Criteria API for complex search
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<MySQLEntity> query = cb.createQuery(MySQLEntity.class);
        Root<MySQLEntity> root = query.from(MySQLEntity.class);

        List<Predicate> predicates = buildSearchPredicates(searchCriteria, cb, root);

        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }

        if (pageable.getSort().isSorted()) {
            List<Order> orders = pageable.getSort().stream()
                    .map(order -> order.isAscending()
                            ? cb.asc(root.get(order.getProperty()))
                            : cb.desc(root.get(order.getProperty())))
                    .collect(Collectors.toList());
            query.orderBy(orders);
        }

        List<MySQLEntity> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MySQLEntity> countRoot = countQuery.from(MySQLEntity.class);
        countQuery.select(cb.count(countRoot));

        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(new Predicate[0]));
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    protected boolean doIsHealthy() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            log.error("MySQL health check failed", e);
            return false;
        }
    }

    @Override
    public String getRepositoryType() {
        return "mysql";
    }

    private List<Predicate> buildSearchPredicates(Map<String, Object> searchCriteria,
                                                  CriteriaBuilder cb,
                                                  Root<MySQLEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        searchCriteria.forEach((key, value) -> {
            if (value == null) return;

            try {
                switch (key) {
                    case "title":
                        predicates.add(cb.like(cb.lower(root.get("title")),
                                "%" + value.toString().toLowerCase() + "%"));
                        break;

                    case "content":
                        predicates.add(cb.like(cb.lower(root.get("content")),
                                "%" + value.toString().toLowerCase() + "%"));
                        break;

                    case "author":
                        predicates.add(cb.equal(cb.lower(root.get("author")),
                                value.toString().toLowerCase()));
                        break;

                    case "tags":
                        if (value instanceof List) {
                            // Handle tags filtering with Criteria API
                            // This is more complex and would require a subquery
                            // Simplified for now
                        }
                        break;

                    case "createdAtFrom":
                        if (value instanceof LocalDateTime) {
                            predicates.add(cb.greaterThanOrEqualTo(
                                    root.get("createdAt"),
                                    (LocalDateTime) value
                            ));
                        }
                        break;

                    case "createdAtTo":
                        if (value instanceof LocalDateTime) {
                            predicates.add(cb.lessThanOrEqualTo(
                                    root.get("createdAt"),
                                    (LocalDateTime) value
                            ));
                        }
                        break;

                    default:
                        try {
                            if (root.getModel().getAttribute(key) != null) {
                                predicates.add(cb.equal(root.get(key), value));
                            }
                        } catch (IllegalArgumentException e) {
                            log.warn("Unknown field in search criteria: {}", key);
                        }
                        break;
                }
            } catch (Exception e) {
                log.warn("Error building predicate for key: {}, value: {}", key, value, e);
            }
        });

        // Always exclude deleted records
        predicates.add(cb.isFalse(root.get("isDeleted")));

        return predicates;
    }
}