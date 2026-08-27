package com.learning.dbcrud.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DatabaseType {
    MYSQL("mysql", "MySQL"),
    MONGODB("mongodb", "MongoDB"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch");

    private final String value;
    private final String displayName;

    DatabaseType(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public String getValue() {
        return this.value;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Get DatabaseType from string value (case insensitive)
     */
    public static DatabaseType fromValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Database type cannot be null or empty");
        }

        for (DatabaseType type : DatabaseType.values()) {
            if (type.value.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException(
                String.format("Invalid database type: '%s'. Supported types: %s",
                        value, getSupportedTypes())
        );
    }

    /**
     * Get all supported database type values
     */
    public static List<String> getSupportedTypes() {
        return Arrays.stream(DatabaseType.values())
                .map(DatabaseType::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Get all display names
     */
    public static List<String> getDisplayNames() {
        return Arrays.stream(DatabaseType.values())
                .map(DatabaseType::getDisplayName)
                .collect(Collectors.toList());
    }

    /**
     * Check if a value is a valid database type
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        return Arrays.stream(DatabaseType.values())
                .anyMatch(type -> type.value.equalsIgnoreCase(value.trim()));
    }
}