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
                                "This API is built on a configuration-driven report engine: master data (module types, " +
                                "modules, template versions, processes, process parameters, global parameters), report " +
                                "sessions and completed reports, plus dashboard, unified search, and analytics over the " +
                                "engine. It supports the full inspection lifecycle and role-based access control.")
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
                        new Tag().name("Module Type Master").description("Module type configuration"),
                        new Tag().name("Module Master").description("Module and template version configuration"),
                        new Tag().name("Process Master").description("Module process configuration"),
                        new Tag().name("Process Parameter Master").description("Process parameter binding configuration"),
                        new Tag().name("Global Parameter Master").description("Global reusable parameter configuration"),
                        new Tag().name("Report Engine (Module-driven)").description("Configuration-driven report sessions and completed reports"),
                        new Tag().name("Dashboard").description("Report dashboard KPIs and overviews"),
                        new Tag().name("Unified Search").description("Cross-module search over reports, users and parameters"),
                        new Tag().name("Analytics & KPIs").description("KPI dashboards and analytical reports"),
                        new Tag().name("Line Master").description("Production line configuration"),
                        new Tag().name("Shift Master").description("Shift configuration"),
                        new Tag().name("User Management").description("User administration"),
                        new Tag().name("Authentication").description("User authentication and authorization"),
                        new Tag().name("Attachment Management").description("File attachments"),
                        new Tag().name("Audit Trail").description("System audit trail and activity logging"),
                        new Tag().name("Notifications").description("User notification management"),
                        new Tag().name("System Settings").description("System configuration and settings"),
                        new Tag().name("Integration Center").description("External system connectors")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token")));
    }
}
