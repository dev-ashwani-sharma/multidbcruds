package com.learning.dbcrud.repository.mysql;

import com.learning.dbcrud.entity.mysql.MySQLEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MySQLDataRepository extends JpaRepository<MySQLEntity, Long> {

    // Basic find methods - Spring Data JPA derives these from method names
    Optional<MySQLEntity> findByTitleAndAuthor(String title, String author);

    List<MySQLEntity> findByAuthor(String author);

    Page<MySQLEntity> findByAuthor(String author, Pageable pageable);

    Page<MySQLEntity> findByIsDeletedFalse(Pageable pageable);

    // Custom query with filters using @Query
    @Query("SELECT e FROM MySQLEntity e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(e.author) = LOWER(:author)) AND " +
            "(:content IS NULL OR LOWER(e.content) LIKE LOWER(CONCAT('%', :content, '%'))) AND " +
            "e.isDeleted = false")
    Page<MySQLEntity> findByFilters(@Param("title") String title,
                                    @Param("author") String author,
                                    @Param("content") String content,
                                    Pageable pageable);

    // Count with filters
    @Query("SELECT COUNT(e) FROM MySQLEntity e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(e.author) = LOWER(:author)) AND " +
            "e.isDeleted = false")
    long countByFilters(@Param("title") String title,
                        @Param("author") String author);

    // Full-text search using native query
    @Query(value = "SELECT * FROM data_entries e WHERE " +
            "MATCH(e.title, e.content, e.search_vector) AGAINST(:searchText IN NATURAL LANGUAGE MODE) " +
            "AND e.is_deleted = false",
            nativeQuery = true)
    Page<MySQLEntity> fullTextSearch(@Param("searchText") String searchText, Pageable pageable);

    // Find by tags (using JOIN)
    @Query("SELECT DISTINCT e FROM MySQLEntity e JOIN e.tags t WHERE t IN :tags AND e.isDeleted = false")
    Page<MySQLEntity> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    // Find by author and tags
    @Query("SELECT DISTINCT e FROM MySQLEntity e JOIN e.tags t WHERE e.author = :author AND t IN :tags AND e.isDeleted = false")
    Page<MySQLEntity> findByAuthorAndTags(@Param("author") String author,
                                          @Param("tags") List<String> tags,
                                          Pageable pageable);

    // Date range query
    @Query("SELECT e FROM MySQLEntity e WHERE " +
            "(:fromDate IS NULL OR e.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR e.createdAt <= :toDate) AND " +
            "e.isDeleted = false")
    Page<MySQLEntity> findByDateRange(@Param("fromDate") java.time.LocalDateTime fromDate,
                                      @Param("toDate") java.time.LocalDateTime toDate,
                                      Pageable pageable);
}