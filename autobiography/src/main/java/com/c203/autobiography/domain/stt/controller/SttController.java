package com.c203.autobiography.domain.stt.controller;

import com.c203.autobiography.domain.stt.client.SttClient;
import com.c203.autobiography.domain.stt.dto.SttResponse;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SttResponse recognize(@RequestPart("audio") MultipartFile audio,
                                 @RequestParam String lang,
                                 @RequestParam(defaultValue = "false") boolean assessment,
                                 @RequestParam(required = false) String utterance,
                                 @RequestParam(required = false) String boostings,
                                 @RequestParam(defaultValue = "false") boolean graph) {
        return sttClient.recognize(audio, lang, assessment, utterance, boostings, graph);

    }
}
