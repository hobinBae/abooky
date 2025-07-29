package com.c203.autobiography.domain.stt.client;

import com.c203.autobiography.domain.stt.dto.SttResponse;
import org.springframework.web.multipart.MultipartFile;

public interface  SttClient {
    /**
     * Recognize speech from audio and return raw transcript
     * @param audio multipart audio file
     * @return recognized text
     */
    SttResponse recognize(
            MultipartFile audio,
            String lang,
            boolean assessment,
            String utterance,
            String boostings,
            boolean graph
    );
}
