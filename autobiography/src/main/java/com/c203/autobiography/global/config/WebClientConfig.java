package com.c203.autobiography.global.config;

import com.c203.autobiography.domain.ai.client.OpenAiProperties;
import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class WebClientConfig {
    private final OpenAiProperties openAiProps;

    public WebClientConfig(OpenAiProperties openAiProps){
        this.openAiProps = openAiProps;
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder){
        HttpClient http = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(openAiProps.getRequestTimeoutSec()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
                .compress(true)          // gzip
                .keepAlive(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(http))
                .baseUrl(openAiProps.getApiUrl())        // https://…/v1
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiProps.getApiKey())
                /* 3회 exponential backoff retry */
                .filter((req, next) ->
                        next.exchange(req)
                                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))))
                .build();
    }

}
