package com.example.ms_proveedor.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public abstract class MySqlIntegrationTest {

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                MySQLTestContainer::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                MySQLTestContainer::getUsername
        );
        registry.add(
                "spring.datasource.password",
                MySQLTestContainer::getPassword
        );
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "com.mysql.cj.jdbc.Driver"
        );
    }
}
