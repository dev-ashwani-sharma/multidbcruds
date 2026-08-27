package com.learning.dbcrud.repository.mongodb;

import com.learning.dbcrud.entity.mongodb.MongoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoDataRepository extends MongoRepository<MongoEntity, String> {

    Optional<MongoEntity> findByTitleAndAuthor(String title, String author);

    List<MongoEntity> findByAuthor(String author);

    Page<MongoEntity> findByAuthor(String author, Pageable pageable);

    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'is_deleted': false }")
    Page<MongoEntity> searchByTitle(String title, Pageable pageable);

    @Query("{ 'author': ?0, 'is_deleted': false }")
    Page<MongoEntity> findByAuthorWithPagination(String author, Pageable pageable);

    @Query(value = "{ 'tags': { $in: ?0 }, 'is_deleted': false }",
            fields = "{ 'title': 1, 'author': 1, 'createdAt': 1 }")
    List<MongoEntity> findProjectsByTags(List<String> tags);
}