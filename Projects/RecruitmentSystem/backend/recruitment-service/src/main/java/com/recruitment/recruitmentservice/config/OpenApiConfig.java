package com.recruitment.recruitmentservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI recruitmentOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title("Recruitment System - Recruitment Service API")
                                .version("1.0.0")
                                .description("""
                                        Recruitment Service API của hệ thống tuyển dụng.

                                        Chức năng:
                                        - Quản lý tin tuyển dụng
                                        - Quản lý danh mục nghề nghiệp
                                        - Quản lý kỹ năng
                                        - Quản lý phúc lợi
                                        - JWT Authentication
                                        """)
                                .contact(
                                        new Contact()
                                                .name("Recruitment System")
                                                .email("support@recruitment.com")
                                )
                                .license(
                                        new License()
                                                .name("MIT")
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );

    }

}