package com.c203.autobiography.domain.member.controller;

import com.c203.autobiography.domain.member.service.MemberService;
import com.c203.autobiography.domain.member.dto.MemberCreateRequest;
import com.c203.autobiography.domain.member.dto.MemberResponse;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.s3.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "회원 API", description = "회원가입 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final FileStorageService fileStorageService;
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원가입 + 프로필 사진 등록")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<MemberResponse>> register(
            @ModelAttribute @Valid MemberCreateRequest request,
            @RequestPart(value = "file", required = false)
            MultipartFile file, HttpServletRequest httpRequest
    ) {
        if (file != null && !file.isEmpty()) {
            String imageUrl = fileStorageService.store(file, "profiles");
            request.setProfileImageUrl(imageUrl);
        }
        MemberResponse response = memberService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "회원가입 성공", response, httpRequest.getRequestURI()));
    }


}
