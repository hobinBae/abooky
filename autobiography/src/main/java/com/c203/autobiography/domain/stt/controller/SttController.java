package com.c203.autobiography.domain.stt.controller;

import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttRequest;
import com.c203.autobiography.domain.stt.dto.SttResponse;
import com.c203.autobiography.domain.stt.service.SttService;
import com.c203.autobiography.domain.stt.service.SttServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stt")
@CrossOrigin(origins="*")
public class SttController {
    private final SttClient sttClient;
    private final SttService sttService;

    @PostMapping(value = "/whisper", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SttResponse recognizeWithWhisper(@RequestPart("audio") MultipartFile audio,
                                            @RequestParam(required = false) String customProperNouns) {
        SttRequest req = SttRequest.builder()
                .audio(audio)
                // customProperNouns는 optional, null 허용
                .build();
        // 서비스 메서드 호출: customProperNouns 전달
        return sttService.recognize(audio, customProperNouns);
    }
}
