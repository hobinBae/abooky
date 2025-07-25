package com.c203.autobiography.global.s3;

import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MimeTypeUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3FileStorageServiceTest {

    @InjectMocks
    private S3FileStorageService s3FileStorageService;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3TransferManager transferManager;

    @Mock
    private S3Properties props;

    private MockMultipartFile validFile;

    @BeforeEach
    void setup() {
        validFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MimeTypeUtils.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        // 수정된 부분: List 사용
        when(props.getBucket()).thenReturn("test-bucket");
        when(props.getRegion()).thenReturn("ap-northeast-2");
        when(props.getAllowedExtensions()).thenReturn(Arrays.asList("jpg", "jpeg", "png", "mp3", "wav", "m4a"));
        when(props.getMaxFileSize()).thenReturn(10 * 1024 * 1024L); // 10MB
        when(props.getMultipartThreshold()).thenReturn(50 * 1024 * 1024L); // 50MB
    }

    @Test
    void store_성공_단일파일업로드() throws IOException {
        // given
        when(s3Client.utilities().getUrl(any(GetUrlRequest.class)))
                .thenReturn(new URL("https://test-bucket.s3.ap-northeast-2.amazonaws.com/folder/test.jpg"));

        // when
        String url = s3FileStorageService.store(validFile, "folder");

        // then
        assertThat(url).startsWith("https://test-bucket.s3.ap-northeast-2.amazonaws.com/");
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void store_확장자_실패() {
        MockMultipartFile badFile = new MockMultipartFile("file", "test.exe", "application/octet-stream", "virus".getBytes());

        assertThatThrownBy(() -> s3FileStorageService.store(badFile, "folder"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_FORMAT.getMessage());
    }

    @Test
    void store_파일명없음_실패() {
        MockMultipartFile badFile = new MockMultipartFile("file", "", MimeTypeUtils.IMAGE_JPEG_VALUE, "data".getBytes());

        assertThatThrownBy(() -> s3FileStorageService.store(badFile, "folder"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.INVALID_FILE_FORMAT.getMessage());
    }

    @Test
    void store_파일용량초과_실패() {
        byte[] bigData = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile bigFile = new MockMultipartFile("file", "big.png", MimeTypeUtils.IMAGE_PNG_VALUE, bigData);

        assertThatThrownBy(() -> s3FileStorageService.store(bigFile, "folder"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
    }

    @Test
    void delete_성공() {
        String url = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/folder/test.jpg";

        s3FileStorageService.delete(url);

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void delete_잘못된URL_실패() {
        String wrongUrl = "https://malicious-bucket.fake.com/folder/test.jpg";

        assertThatThrownBy(() -> s3FileStorageService.delete(wrongUrl))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining(ErrorCode.INVALID_IMAGE_URL.getMessage());
    }
}