package com.aerotech.ced_ops_backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CED OPS API")
                        .version("1.0.0")
                        .description(
                                "RESTful API for the CED (Continuous Electro-Deposition) Operations management system. " +
                                "This API provides endpoints for managing quality inspections, chemical consumption tracking, " +
                                "daily startup and shutdown procedures, pre-delivery inspections, and operator performance analytics. " +
                                "It supports the full inspection lifecycle including creation, " +
                                "submission, approval workflows, and role-based access control.")
                        .contact(new Contact()
                                .name("AeroTech Support")
                                .email("support@aerotech.com")
                                .url("https://aerotech.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://aerotech.com/license"))
                        .termsOfService("https://aerotech.com/terms"))
                .addServersItem(new Server()
                        .url("/")
                        .description("Default Server URL"))
                .tags(List.of(
                        new Tag().name("Daily Inspection").description("Daily inspection report management"),
                        new Tag().name("Chemical Consumption").description("Chemical consumption tracking and reporting"),
                        new Tag().name("Daily Startup").description("Daily startup and shutdown procedure management"),
                        new Tag().name("Pre-Delivery Inspection").description("Pre-delivery inspection management"),
                        new Tag().name("Analytics").description("KPI dashboards and analytical reports"),
                        new Tag().name("Audit Logs").description("System audit trail and activity logging"),
                        new Tag().name("Notifications").description("User notification management"),
                        new Tag().name("Settings").description("System configuration and settings"),
                        new Tag().name("Global Search").description("Cross-module search functionality"),
                        new Tag().name("Authentication").description("User authentication and authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token")));
    }
}
