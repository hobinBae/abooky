package com.c203.autobiography.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("아북이 API 문서")
                        .description("AI와 대화를 통해 자서전을 만드는 웹사이트 아북이의 API 사용 방법을 설명합니다.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("01-Auth")
                .pathsToMatch("/api/v1/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi memberApi() {
        return GroupedOpenApi.builder()
                .group("02-Member")
                .pathsToMatch("/api/v1/members/**")
                .build();
    }
    @Bean
    public GroupedOpenApi bookApi() {
        return GroupedOpenApi.builder()
                .group("03-Book")
                .pathsToMatch("/api/v1/books/**")
                .build();
    }

    @Bean
    public GroupedOpenApi groupApi() {
        return GroupedOpenApi.builder()
                .group("04-Group")
                .pathsToMatch("/api/v1/groups/**")
                .build();
    }

    @Bean
    public GroupedOpenApi rtcApi() {
        return GroupedOpenApi.builder()
                .group("05-RTC")
                .pathsToMatch("/api/v1/groups/*/rtc/**")
                .build();
    }

    @Bean
    public GroupedOpenApi communityApi() {
        return GroupedOpenApi.builder()
                .group("06-Community")
                .pathsToMatch("/api/v1/communities/**")
                .build();
    }

}
