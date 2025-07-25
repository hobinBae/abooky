package com.c203.autobiography.global.s3;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file, String folder);

    void delete(String objectKey);
}
