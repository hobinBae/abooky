package com.c203.autobiography.global.config;

import io.livekit.server.RoomServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LivekitConfig {

    @Value("${livekit.serverUrl}")
    private String serverUrl;

    @Value("${livekit.apiKey}")
    private String apiKey;

    @Value("${livekit.apiSecret}")
    private String apiSecret;

    @Bean
    public RoomServiceClient roomServiceClient() {
        return RoomServiceClient.createClient(serverUrl, apiKey, apiSecret);
    }
}
