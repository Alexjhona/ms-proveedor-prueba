package com.example.ms_proveedor.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @LoadBalanced
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("OPEN API MICROSERVICIO PROVEEDORES")
                .version("0.0.1")
                .description("Documentación de endpoints para CRUD de Proveedor")
                .termsOfService("http://swagger.io/terms")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"))
        );
    }
}