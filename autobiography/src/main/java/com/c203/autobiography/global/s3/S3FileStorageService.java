package com.c203.autobiography.global.s3;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Executors;

import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedUpload;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final S3TransferManager transferManager;
    private final S3Properties props;

    @Override
    public String store(MultipartFile file, String folder) {
        // 1) 파일명 유효성 검사
        String original = file.getOriginalFilename();
        if (!StringUtils.hasText(original) || !original.contains(".")) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }

//        // 2) 확장자 체크
        String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        if (!props.getAllowedExtensions().contains(ext)) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }

//
//        // 3) 크기 체크
        if (file.getSize() > props.getMaxFileSize()) {
            throw new ApiException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // 4) 키 생성
        String filename = UUID.randomUUID() + "." + ext;
        String key = folder.endsWith("/")
                ? folder + filename
                : folder + "/" + filename;

        // 5) 업로드
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(props.getBucket())
                .key(key)
                .contentType(file.getContentType())
                .build();

        try (InputStream is = file.getInputStream()) {
            if (file.getSize() > props.getMultipartThreshold()) {
                // 대용량 multipart 업로드: AsyncRequestBody.fromInputStream(is, length, executor)
                transferManager.upload(UploadRequest.builder()
                        .putObjectRequest(por)
                        .requestBody(AsyncRequestBody.fromInputStream(
                                is,
                                file.getSize(),
                                Executors.newCachedThreadPool()
                        ))
                        .build()
                ).completionFuture().join();
            } else {
                // 소용량 파일: 단일 PUT
                s3Client.putObject(por, RequestBody.fromInputStream(is, file.getSize()));
            }
        } catch (IOException | SdkException e) {
            log.error("S3 업로드 실패 (key={}): {}", key, e.getMessage());
            throw new ApiException(ErrorCode.S3_UPLOAD_ERROR);
        }

        // 6) URL 생성 및 반환
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder()
                        .bucket(props.getBucket())
                        .key(key)
                        .build())
                .toExternalForm();

    }

    @Override
    public void delete(String objectUrl) {
        // URL에서 키만 추출하도록 안전하게 처리
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/",
                props.getBucket(), props.getRegion());
        if (!objectUrl.startsWith(prefix)) {
            throw new ApiException(ErrorCode.INVALID_IMAGE_URL);
        }
        String key = objectUrl.substring(prefix.length());

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .build());
            log.info("S3 객체 삭제 성공: key={}", key);
        } catch (SdkException e) {
            log.error("S3 삭제 실패 (key={}): {}", key, e.getMessage());
              throw new ApiException(ErrorCode.S3_DELETE_ERROR);
        }
    }
}
