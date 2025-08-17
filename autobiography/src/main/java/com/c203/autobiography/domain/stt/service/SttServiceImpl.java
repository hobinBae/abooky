package com.c203.autobiography.domain.stt.service;

import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class SttServiceImpl implements SttService {
    private final SttClient sttClient;

    @Override
    public SttResponse recognize(MultipartFile audio) {
        return sttClient.recognize(audio);
    }

    @Override
    public SttResponse recognize(MultipartFile audio, String customProperNouns) {
        return sttClient.recognize(audio, customProperNouns);
    }
}
