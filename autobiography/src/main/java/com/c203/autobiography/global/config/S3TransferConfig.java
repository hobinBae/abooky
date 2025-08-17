package com.c203.autobiography.global.config;

import com.c203.autobiography.global.s3.S3Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Configuration
public class S3TransferConfig {
    @Bean
    public S3AsyncClient s3AsyncClient(S3Properties props){
        return S3AsyncClient.builder()
                .region(Region.of(props.getRegion()))
                .build();
    }

    @Bean
    public S3TransferManager transferManager(S3AsyncClient asyncClient) {
        return S3TransferManager.builder()
                .s3Client(asyncClient)
                .build();
    }
}
