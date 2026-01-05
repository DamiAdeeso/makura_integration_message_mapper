package com.makura.runtime.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI runtimeServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ISO 20022 Runtime Service API")
                .description("Real-time translation and routing service for ISO 20022 messages. " +
                    "Translates between source system formats (JSON, SOAP, XML) and ISO 20022 XML messages. " +
                    "Supports bi-directional translation with configurable routing and encryption.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Makura Development Team")
                    .email("support@makura.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://makura.com/license")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.makura.com")
                    .description("Production Server")
            ))
            .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)
                    .name("X-API-Key")
                    .description("API key for authentication. Contact administrator to obtain an API key.")));
    }
}

