package com.c203.autobiography.domain.stt.service;

import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SttServiceImpl implements SttService {
    private final SttClient sttClient;

    @Override
    public SttResponse recognize(SttRequest request) {
        return sttClient.recognize(
                request.getAudio(),
                request.getLang(),
                request.getAssessment(),
                request.getUtterance(),
                request.getBoostings(),
                request.getGraph()
        );
    }
}
