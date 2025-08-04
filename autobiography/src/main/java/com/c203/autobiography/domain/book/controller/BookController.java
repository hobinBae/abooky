package com.c203.autobiography.domain.book.controller;

import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;
import com.c203.autobiography.domain.book.service.BookService;
import com.c203.autobiography.domain.episode.dto.EpisodeRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.service.EpisodeService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final EpisodeService episodeService;

    @Operation(summary = "책 생성", description = "책 정보 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @AuthenticationPrincipal CustomUserDetails userDetail, @Valid @RequestBody BookCreateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetail.getMemberId();
        BookResponse response = bookService.createBook(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "책 생성 성공", response, httpRequest.getRequestURI()));

    }

    //여기부터 에피소드 관련 api
    @Operation(summary = "에피소드 생성", description = "대화 세션을 마무리하고 에피소드를 생성합니다.")
    @PostMapping("{bookId}/episodes")
    public ResponseEntity<ApiResponse<EpisodeResponse>> createEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @RequestParam String sessionId, HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        EpisodeResponse response = episodeService.createEpisode(memberId, bookId, sessionId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "에피소드 생성 성공", response, httpRequest.getRequestURI()));
    }
}
