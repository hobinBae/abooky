package com.c203.autobiography.domain.book.controller;

import com.c203.autobiography.domain.book.dto.*;
import com.c203.autobiography.domain.book.service.BookService;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.service.EpisodeService;
import com.c203.autobiography.global.dto.ApiResponse;
import com.c203.autobiography.global.dto.PageResponse;
import com.c203.autobiography.global.security.jwt.CustomUserDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.Path;

import java.util.List;

@Tag(name = "책 API", description = "책, 에피소드 관련 API")
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final EpisodeService episodeService;

    public void test(){
        System.out.println("merge test");
        return;
    }

    @Operation(summary = "책 생성", description = "책 정보 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @AuthenticationPrincipal CustomUserDetails userDetail, @Valid @ModelAttribute BookCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetail.getMemberId();
        BookResponse response = bookService.createBook(memberId, request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "책 생성 성공", response, httpRequest.getRequestURI()));

    }

    @Operation(summary = "책 완성", description = "책을 완성한다.")
    @PatchMapping("/{bookId}/complete")
    public ResponseEntity<ApiResponse<BookResponse>> completeBook(
            @AuthenticationPrincipal CustomUserDetails userDetails
            , @PathVariable Long bookId
            , @Valid @RequestBody BookCompleteRequest request
            , HttpServletRequest httpRequest
    ){
        Long memberId = userDetails.getMemberId();
        BookResponse response = bookService.completeBook(memberId, bookId, request.getTags());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "책 완성 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 조회", description = "책을 조회한다.")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        BookResponse response = bookService.getBookDetail(memberId, bookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "책 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "나의 쓴 책 목록 조회", description = "내가 쓴 책들을 조회한다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchMyBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        List<BookResponse> response = bookService.getMyBooks(memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "나의 보유 책 목록 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 검색", description = "description = 제목, 카테고리, 태그로 책을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> searchBooks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<String> tags,
            @Parameter(
                    description = "정렬 기준. 예: sort=likeCount,desc;viewCount,asc",
                    example = "likeCount,desc"
            )
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            HttpServletRequest httpRequest

    ) {
        Page<BookResponse> page = bookService.searchBooks(title, categoryId, tags, pageable);
        PageResponse<BookResponse> response = PageResponse.from(page);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "책 검색 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 수정", description = "책 정보 수정")
    @PatchMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @ModelAttribute BookUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file, HttpServletRequest httpRequest) {
        Long memberId = userDetails.getMemberId();
        BookResponse response = bookService.updateBook(memberId, bookId,  request, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "책 수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 삭제", description = "책 삭제를 합니다.")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> deleteBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
       bookService.deleteBook(memberId, bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.of(HttpStatus.NO_CONTENT, "책 삭제 성공", null, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 다른 이름으로 저장", description = "원본을 그대로 두고, 수정/삭제한 에피소드 반영하여 새 책을 만듭니다.")
    @PostMapping("/{bookId}/copy")
    public ResponseEntity<ApiResponse<BookCopyResponse>> copyBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @RequestBody BookCopyRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = userDetails.getMemberId();
        BookCopyResponse response = bookService.copyBook(memberId, bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "다른 이름으로 저장 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 좋아요/좋아요 취소", description = "다른 책에 좋아요를 누릅니다.")
    @PostMapping("/{bookId}/likes")
    public ResponseEntity<ApiResponse<LikeResponse>> likeBook(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            HttpServletRequest httpRequest

    ) {
        Long memberId = userDetails.getMemberId();

        LikeResponse response = bookService.toggleLike(bookId, memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.CREATED, "좋아요/취소 성공", response, httpRequest.getRequestURI()));


    }

    //여기부터 에피소드 관련 api
    @Operation(summary = "에피소드 생성", description = "대화 세션을 마무리하고 에피소드를 생성합니다.")
    @PostMapping("/{bookId}/episodes")
    public ResponseEntity<ApiResponse<EpisodeResponse>> createEpisode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @RequestParam String sessionId, HttpServletRequest httpRequest
    ) throws JsonProcessingException {

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
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "에피소드 수정 성공", response, httpRequest.getRequestURI()));
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

    @Operation(summary = "책 평점 등록/수정", description = "한 유저당 한 책의 평점 한 개만 가질 수 있습니다. (재요청 시 갱신)")
    @PostMapping("/{bookId}/ratings")
    public ResponseEntity<ApiResponse<BookRatingResponse>> rateBook(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long bookId,
            @RequestBody @Valid BookRatingRequest request,
            HttpServletRequest httpRequest
    ) {
        Long memberId = user.getMemberId();
        BookRatingResponse response = bookService.rateBook(memberId, bookId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "평점 등록/수정 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "책 평점 조회", description = "내 평점, 평균 평점(소수점 한자리), 총 평점 수를 조회합니다.")
    @GetMapping("/{bookId}/ratings")
    public ResponseEntity<ApiResponse<BookRatingResponse>> getBookRating(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = user.getMemberId();
        BookRatingResponse response = bookService.getBookRating(memberId, bookId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.of(HttpStatus.OK, "평점 조회 성공", response, httpRequest.getRequestURI()));
    }

    @Operation(summary = "커뮤니티 책 생성", description = "개인 책을 커뮤니티로 내보냅니다 (Book → CommunityBook, Episode → CommunityBookEpisode)")
    @PostMapping("/{bookId}/export/community")
    public ResponseEntity<ApiResponse<CommunityBookCreateResponse>> exportBookToCommunity(
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "책 ID") @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        Long memberId = user.getMemberId();
        CommunityBookCreateResponse response = bookService.exportBookToCommunity(memberId, bookId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        HttpStatus.CREATED,
                        "책 커뮤니티 내보내기 성공",
                        response,
                        httpRequest.getRequestURI()
                ));
    }
}
