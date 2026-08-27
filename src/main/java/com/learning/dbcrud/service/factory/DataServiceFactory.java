package com.learning.dbcrud.service.factory;

import com.learning.dbcrud.enums.DatabaseType;
import com.learning.dbcrud.exception.DatabaseTypeException;
import com.learning.dbcrud.service.interfaces.DataService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataServiceFactory {

    private final List<DataService> dataServices;
    private final Map<DatabaseType, DataService> serviceMap = new EnumMap<>(DatabaseType.class);

    @PostConstruct
    public void init() {
        for (DataService service : dataServices) { 
            serviceMap.put(service.getDatabaseType(), service);
        }
    }

    public DataService getService(DatabaseType databaseType) {
        DataService service = serviceMap.get(databaseType);
        if (service == null) {
            throw new DatabaseTypeException(
                    String.format("No service found for database type: %s", databaseType)
            );
        }
        return service;
    }

    public DataService getService(String databaseType) {
        return getService(DatabaseType.fromValue(databaseType));
    }

    public boolean isSupported(DatabaseType databaseType) {
        return serviceMap.containsKey(databaseType);
    }
}
