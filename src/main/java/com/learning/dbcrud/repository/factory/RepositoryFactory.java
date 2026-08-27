package com.learning.dbcrud.repository.factory;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.repository.elasticsearch.ElasticsearchRepository;
import com.learning.dbcrud.repository.interfaces.DataRepository;
import com.learning.dbcrud.repository.mongodb.MongoDBRepository;
import com.learning.dbcrud.repository.mysql.MySQLRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RepositoryFactory {

    private final MySQLRepository mySQLRepository;
    private final MongoDBRepository mongoDBRepository;
    private final ElasticsearchRepository elasticsearchRepository;

    private final Map<DatabaseType, DataRepository> repositoryMap = new EnumMap<>(DatabaseType.class);

    @PostConstruct
    public void init() {
        repositoryMap.put(DatabaseType.MYSQL, mySQLRepository);
        repositoryMap.put(DatabaseType.MONGODB, mongoDBRepository);
        repositoryMap.put(DatabaseType.ELASTICSEARCH, elasticsearchRepository);
    }

    public DataRepository getRepository(DatabaseType databaseType) {
        DataRepository repository = repositoryMap.get(databaseType);
        if (repository == null) {
            throw new IllegalArgumentException("No repository found for type: " + databaseType);
        }
        return repository;
    }
}