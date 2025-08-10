package com.c203.autobiography.domain.communityBook.controller;

import com.c203.autobiography.domain.communityBook.service.CommunityBookService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name="커뮤니티 책 API", description = "커뮤니티 책 관련 API")
@RestController
@RequestMapping("/api/v1/community-book")
@RequiredArgsConstructor
public class CommunityBookController {

    private final CommunityBookService communityBookService;


    @Operation(summary = "커뮤니티 책 삭제", description = "커뮤니티 책을 삭제합니다")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteCommunityBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        communityBookService.deleteCommunityBook(memberId, bookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "커뮤니티 책 삭제 성공", null, httpRequest.getRequestURI()));
    }

}
