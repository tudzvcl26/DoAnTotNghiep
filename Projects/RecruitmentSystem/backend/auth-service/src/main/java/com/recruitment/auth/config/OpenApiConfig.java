package com.recruitment.auth.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI recruitmentOpenAPI() {

        return new OpenAPI()

                .info(new Info()

                        .title("Recruitment System API")

                        .description("Authentication Service API")

                        .version("v1.0.0")

                        .contact(new Contact()

                                .name("Recruitment Team")

                                .email("admin@recruitment.local"))

                        .license(new License()

                                .name("MIT")))

                .externalDocs(new ExternalDocumentation()

                        .description("Project Documentation")

                        .url("https://localhost"));

    }

}