package com.learning.dbcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.learning.dbcrud.repository.mysql")
@EnableMongoRepositories(basePackages = "com.learning.dbcrud.repository.mongodb")
@EnableElasticsearchRepositories(basePackages = "com.learning.dbcrud.repository.elasticsearch")
public class CrudApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CrudApplication.class, args);
    }
}