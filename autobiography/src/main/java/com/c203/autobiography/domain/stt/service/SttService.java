package com.c203.autobiography.domain.stt.service;

import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SttService {
    SttResponse recognize(MultipartFile audio);
    SttResponse recognize(MultipartFile audio, String customProperNouns);
}
