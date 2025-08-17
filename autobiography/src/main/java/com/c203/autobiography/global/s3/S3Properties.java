package com.c203.autobiography.global.s3;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Properties {

    private String bucket;

    private String region;

    private List<String> allowedExtensions;

    private long maxFileSize;

    private long multipartThreshold;
}
