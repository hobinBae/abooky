package com.c203.autobiography.domain.episode.controller;

import com.c203.autobiography.domain.episode.dto.EpisodeImageResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeImageUploadRequest;
import com.c203.autobiography.domain.episode.service.EpisodeService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Episode")
@RestController
@RequestMapping("/api/v1/books/{bookId}/episodes")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;

    // ========== 이미지 관리 API ==========

    @Operation(summary = "에피소드 이미지 업로드", description = "일반책 에피소드에 이미지를 업로드합니다.")
    @PostMapping(value = "/{episodeId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EpisodeImageResponse>> uploadImage(
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "orderNo", required = false) Integer orderNo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        // Request 객체 생성
        EpisodeImageUploadRequest request = EpisodeImageUploadRequest.builder()
                .description(description)
                .orderNo(orderNo)
                .build();
        
        EpisodeImageResponse response = episodeService.uploadImage(
                bookId, episodeId, file, request, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.of(HttpStatus.CREATED, "이미지 업로드 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 이미지 목록 조회", description = "일반책 에피소드의 모든 이미지를 조회합니다.")
    @GetMapping("/{episodeId}/images")
    public ResponseEntity<ApiResponse<List<EpisodeImageResponse>>> getImages(
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        List<EpisodeImageResponse> response = episodeService.getImages(
                bookId, episodeId, userDetails.getMemberId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.of(HttpStatus.OK, "이미지 목록 조회 성공", response, httpRequest.getRequestURI())
        );
    }

    @Operation(summary = "에피소드 이미지 삭제", description = "일반책 에피소드의 특정 이미지를 삭제합니다.")
    @DeleteMapping("/{episodeId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        episodeService.deleteImage(bookId, episodeId, imageId, userDetails.getMemberId());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.of(HttpStatus.NO_CONTENT, "이미지 삭제 성공", null, httpRequest.getRequestURI())
        );
    }
}