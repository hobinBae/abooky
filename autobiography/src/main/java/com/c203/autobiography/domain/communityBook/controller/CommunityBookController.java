package com.c203.autobiography.domain.communityBook.controller;

import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentRequest;
import com.c203.autobiography.domain.communityBook.dto.CommunityBookCommentResponse;
import com.c203.autobiography.domain.communityBook.service.CommunityBookService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="커뮤니티 책 API", description = "커뮤니티 책 관련 API")
@RestController
@RequestMapping("/api/v1/communities/community-book")
@RequiredArgsConstructor
public class CommunityBookController {

    private final CommunityBookService communityBookService;


    @Operation(summary = "커뮤니티 책 삭제", description = "커뮤니티 책을 삭제합니다")
    @DeleteMapping("/{communityBookId}")
    public ResponseEntity<ApiResponse<Void>> deleteCommunityBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long communityBookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        communityBookService.deleteCommunityBook(memberId, communityBookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 삭제 성공", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 댓글 생성", description = "커뮤니티 책에 대한 댓글을 생성합니다")
    @PostMapping("/{communityBookId}/comments")
    public ResponseEntity<ApiResponse<CommunityBookCommentResponse>> createCommunityBookComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommunityBookCommentRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        CommunityBookCommentResponse response= communityBookService.createCommunityBookComment(memberId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 댓글 생성 성공", response, httpRequest.getRequestURI()));
    }

}
