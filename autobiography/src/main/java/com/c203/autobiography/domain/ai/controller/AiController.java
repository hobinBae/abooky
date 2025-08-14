package com.c203.autobiography.domain.ai.controller;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.ai.dto.ProofreadRequest;
import com.c203.autobiography.domain.ai.dto.ProofreadResponse;
import com.c203.autobiography.domain.ai.service.OpenAiService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

   private final AiClient aiClient;

    @PostMapping("/proofread")
    public ResponseEntity<ApiResponse<ProofreadResponse>> proofreadText(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProofreadRequest request,
            HttpServletRequest httpRequest
    ){
        String correctedText = aiClient.proofread(request.getTextToCorrect(), request.getBookCategory());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "교정 성공", new ProofreadResponse(correctedText), httpRequest.getRequestURI()));
    }
}
