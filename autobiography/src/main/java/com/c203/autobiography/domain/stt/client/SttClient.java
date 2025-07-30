package com.c203.autobiography.domain.stt.client;

import com.c203.autobiography.domain.stt.dto.SttResponse;
import org.springframework.web.multipart.MultipartFile;

public interface  SttClient {
    SttResponse recognize(MultipartFile audio, String customProperNouns );
    SttResponse recognize(MultipartFile audio);

}
