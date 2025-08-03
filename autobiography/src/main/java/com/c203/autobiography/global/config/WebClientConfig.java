package com.c203.autobiography.global.config;

import com.c203.autobiography.domain.ai.client.OpenAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class WebClientConfig {
    private final OpenAiProperties openAiProps;

    public WebClientConfig(OpenAiProperties openAiProps){
        this.openAiProps = openAiProps;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        return builder
                .baseUrl(openAiProps.getApiUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProps.getApiKey())
                .build();
    }

}
