package com.c203.autobiography.domain.book.controller;

import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;
import com.c203.autobiography.domain.book.service.BookService;
import com.c203.autobiography.domain.episode.dto.EpisodeCreateRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.service.EpisodeService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

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
    @PostMapping("/{bookId}/episodes")
    public ResponseEntity<ApiResponse<EpisodeResponse>> createEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @RequestParam String sessionId, HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        EpisodeResponse response = episodeService.createEpisode(memberId, bookId, sessionId);
        //episodeCreateRequest 추가
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "에피소드 생성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "에피소드 수정", description = "에피소드를 수정합니다.")
    @PatchMapping("/{bookId}/episodes/{episodeId}")
    public ResponseEntity<ApiResponse<EpisodeResponse>> updateEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            @Valid @RequestBody EpisodeUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        EpisodeResponse response = episodeService.updateEpisode(memberId, bookId, episodeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "에피소드 수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "에피소드 삭제", description = "에피소드를 삭제합니다.")
    @DeleteMapping("/{bookId}/episodes/{episodeId}")
    public ResponseEntity<ApiResponse<Void>> deleteEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @PathVariable Long episodeId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        episodeService.deleteEpisode(memberId, bookId, episodeId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "에피소드 삭제 성공", null, httpRequest.getRequestURI()));
    }

}
