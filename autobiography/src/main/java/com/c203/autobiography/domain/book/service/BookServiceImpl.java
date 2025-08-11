package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.*;
import com.c203.autobiography.domain.book.entity.*;
import com.c203.autobiography.domain.book.repository.*;
import com.c203.autobiography.domain.book.util.BookSpecifications;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookEpisodeRepository;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookRepository;
import com.c203.autobiography.domain.episode.dto.EpisodeCopyRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.c203.autobiography.domain.book.dto.BookType.FREE_FORM;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final FileStorageService fileStorageService;
    private final EpisodeRepository episodeRepository;
    private final TagRepository tagRepository;
    private final BookLikeRepository bookLikeRepository;
    private final RatingRepository ratingRepository;
    private final MemberRatingSummaryRespository memberRatingSummaryRespository;
    private final CommunityBookRepository communityBookRepository;
    private final CommunityBookEpisodeRepository communityBookEpisodeRepository;

    @Override
    @Transactional
    public BookResponse createBook(Long memberId, BookCreateRequest request, MultipartFile file) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        BookCategory category = switch (request.getBookType()) {
            case AUTO -> bookCategoryRepository
                    .findByCategoryName("자서전")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case DIARY -> bookCategoryRepository
                    .findByCategoryName("일기")
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            case FREE_FORM -> {
                if (request.getCategoryId() == null) {
                    throw new ApiException(ErrorCode.VALIDATION_FAILED);
                }
                yield bookCategoryRepository
                        .findById(request.getCategoryId())
                        .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
            }
            default -> throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        };

        String coverImageUrl = null;
        if (file != null && !file.isEmpty()) {
            coverImageUrl = fileStorageService.store(file, "book");
        }
        Book book = request.toEntity(member, category, coverImageUrl);

        Book saved = bookRepository.save(book);
        return BookResponse.of(saved);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long memberId, Long bookId, BookUpdateRequest request, MultipartFile file) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        BookCategory category = null;

        if (request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }

        String newImageUrl = book.getCoverImageUrl();
        if (file != null && !file.isEmpty()) {
            String currentImageUrl = book.getCoverImageUrl();
            if (currentImageUrl != null && !currentImageUrl.isBlank()) {
                fileStorageService.delete(currentImageUrl);
            }

            newImageUrl = fileStorageService.store(file, "book");

        }
        book.updateBook(request.getTitle(), newImageUrl, request.getSummary(), category);
        return BookResponse.of(book);
    }

    @Override
    @Transactional
    public Void deleteBook(Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        book.softDelete();

        return null;
    }

    @Override
    @Transactional
    public BookResponse completeBook(Long memberId, Long bookId, List<String> tags) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 3) 완료 처리
        book.markCompleted();

        // 4) 태그 설정 (null/empty 스킵)
        if (tags != null && !tags.isEmpty()) {
            book.getTags().clear();
            tags.stream().distinct().forEach(name -> {
                Tag tag = tagRepository.findByTagName(name)
                        .orElseGet(() -> tagRepository.save(
                                Tag.builder().tagName(name).build()
                        ));
                book.addTag(tag);
            });
        }

        List<EpisodeResponse> episodes = episodeRepository
                .findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(bookId)
                .stream()
                .map(EpisodeResponse::of)
                .toList();

        List<String> tagNames = book.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        // 6) DTO 변환
        return BookResponse.of(book, episodes, tagNames);

    }

    @Override
    public BookResponse getBookDetail(Long memberId, Long bookId) {

        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 에피소드 목록 조회 + DTO 매핑
        List<EpisodeResponse> episodes = episodeRepository
                .findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(bookId)
                .stream()
                .map(EpisodeResponse::of)
                .toList();

        // 태그 조회
        List<String> tagNames = book.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        return BookResponse.of(book, episodes, tagNames);
    }

    @Override
    public List<BookResponse> getMyBooks(Long memberId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return bookRepository
                .findAllByMemberMemberIdAndDeletedAtIsNull(memberId)
                .stream()
                .map(book -> {
                    // 에피소드 목록이 필요 없으면 빈 리스트
                    List<EpisodeResponse> episodes = List.of();

                    // 실제로 붙어있는 태그 이름들만 추출
                    List<String> tagNames = book.getTags().stream()
                            .map(bt -> bt.getTag().getTagName())
                            .toList();

                    return BookResponse.of(book, episodes, tagNames);
                })
                .toList();
    }

    @Override
    @Transactional
    public BookCopyResponse copyBook(Long memberId, Long bookId, BookCopyRequest request) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book original = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        if (!original.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 3) 카테고리 검증
        BookCategory category = null;

        if (original.getBookType() == FREE_FORM) {
            if (request.getCategoryId() == null) {
                throw new ApiException(
                        ErrorCode.VALIDATION_FAILED
                );
            }
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }

        Book copy = Book.builder()
                .member(member)
                .title(request.getTitle())
                .summary(request.getSummary())
                .coverImageUrl(original.getCoverImageUrl())
                .bookType(original.getBookType())
                .category(category)
                .build();
        bookRepository.save(copy);

        for (EpisodeCopyRequest dto : request.getEpisodes()) {
            if (dto.isDelete()) {
                continue;
            }
            Episode origEp = episodeRepository.findByEpisodeIdAndDeletedAtIsNull(dto.getEpisodeId())
                    .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));

            Episode newEp = Episode.builder()
                    .book(copy)
                    .title(Optional.ofNullable(dto.getTitle()).orElse(origEp.getTitle()))
                    .content(Optional.ofNullable(dto.getContent()).orElse(origEp.getContent()))
                    .episodeDate(Optional.ofNullable(dto.getEpisodeDate()).orElse(origEp.getEpisodeDate()))
                    .episodeOrder(Optional.ofNullable(dto.getEpisodeOrder()).orElse(origEp.getEpisodeOrder()))
                    .audioUrl(Optional.ofNullable(dto.getAudioUrl()).orElse(origEp.getAudioUrl()))
                    .build();
            episodeRepository.save(newEp);

            // (선택) 태그/이미지도 복제하려면 이곳에서 처리
        }
        // 6) 응답 반환
        return BookCopyResponse.builder()
                .bookId(copy.getBookId())
                .title(copy.getTitle())
                .summary(copy.getSummary())
                .categoryId(copy.getCategory() != null ? copy.getCategory().getBookCategoryId() : null)
                .createdAt(copy.getCreatedAt())
                .updatedAt(copy.getUpdatedAt())
                .build();

    }

    @Override
    public Page<BookResponse> searchBooks(String title, Long categoryId, List<String> tags, Pageable pageable) {

        Specification<Book> spec = BookSpecifications.titleContains(title)
                .and(BookSpecifications.categoryEquals(categoryId))
                .and(BookSpecifications.hasAnyTag(tags));

        Page<Book> page = bookRepository.findAll(spec, pageable);


        return page.map(book -> {
            List<String> tagNames = book.getTags().stream()
                    .map(bt -> bt.getTag().getTagName())
                    .toList();
            return BookResponse.of(book, List.of(), tagNames);
        });
        }

    @Override
    @Transactional
    public LikeResponse toggleLike(Long bookId, Long memberId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        boolean exists = bookLikeRepository.existsByBookAndMember(book, member);

        if(!exists){
            BookLike like = BookLike.of(book, member);
            bookLikeRepository.save(like);
            book.incrementLike();
            return new LikeResponse(true, bookLikeRepository.countByBook(book));

        }else{
            bookLikeRepository.deleteByBookAndMember(book, member);
            book.decrementLike();
            return new LikeResponse(false, bookLikeRepository.countByBook(book));

        }
    }

    @Override
    @Transactional
    public BookRatingResponse rateBook(Long memberId, Long bookId, BookRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // upsert
        RatingId ratingId = new RatingId(bookId, memberId);
        Rating rating = ratingRepository.findById(ratingId)
                .map(r -> { r.updateScore(request.getScore()); return r; })
                .orElseGet(() -> Rating.create(book, member, request.getScore()));
        ratingRepository.save(rating);

        // 책 평균/카운트 계산
        Double avg = ratingRepository.findAverageScoreByBookId(bookId);
        long count = ratingRepository.countByBook_BookId(bookId);
        BigDecimal avgOneDecimal = BigDecimal.valueOf(avg == null ? 0.0 : avg)
                .setScale(1, RoundingMode.HALF_UP);

        // Book Entity 평균 갱신
        book.updateAverageRating(avgOneDecimal);

        // 멤버 요약 테이블 갱신
        Double mAvg = ratingRepository.findAverageScoreByBookId(bookId);
        long mCount = ratingRepository.countByBook_BookId(bookId);
        BigDecimal mAvgOneDecimal = BigDecimal.valueOf(avg == null ? 0.0 : avg)
                .setScale(1, RoundingMode.HALF_UP);
        MemberRatingSummary summary = memberRatingSummaryRespository.findById(memberId)
                .orElseGet(() -> MemberRatingSummary.init(memberId));
        summary.update(mAvgOneDecimal, (int) mCount);
        memberRatingSummaryRespository.save(summary);

        return BookRatingResponse.builder()
                .bookId(bookId)
                .myScore(rating.getScore())
                .averageRating(avgOneDecimal)
                .ratingCount(count)
                .build();
    }

    @Override
    public BookRatingResponse getBookRating(Long memberId, Long bookId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 내 평점
        Integer myScore = ratingRepository.findById(new RatingId(bookId, memberId))
                .map(Rating::getScore)
                .orElse(null);

        // 책 평균/카운트
        Double avg = ratingRepository.findAverageScoreByBookId(bookId);
        long count = ratingRepository.countByBook_BookId(bookId);
        BigDecimal avgOneDecimal = BigDecimal.valueOf(avg == null ? 0.0 : avg)
                .setScale(1, RoundingMode.HALF_UP);

        return BookRatingResponse.builder()
                .bookId(bookId)
                .myScore(myScore)
                .averageRating(avgOneDecimal)
                .ratingCount(count)
                .build();
    }

    @Transactional
    @Override
    public CommunityBookCreateResponse exportBookToCommunity(Long memberId, Long bookId) {
        // 1. 원본 책 조회 및 권한 검증
        Book originalBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 2. 작성자 권한 검증
        if (!originalBook.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        if (!originalBook.getCompleted()) {
            throw new ApiException(ErrorCode.BOOK_NOT_COMPLETED);
        }

        // 원본 책 조회
        CommunityBook communityBook = createCommunityBook(originalBook);

        // Book → CommunityBook 복사
        CommunityBook savedCommunityBook = communityBookRepository.save(communityBook);

        // Episode → CommunityBookEpisode 복사
        List<Episode> episodes = episodeRepository.findByBookBookIdOrderByEpisodeOrderAsc(bookId);
        int copiedEpisodeCount = copyEpisodesToCommunity(episodes, savedCommunityBook);

        // 원본 책, 에피소드 삭제
        originalBook.softDelete();

        for (Episode episode : episodes) {
            episode.softDelete();
        }

        // 배치로 저장하여 성능 최적화
        episodeRepository.saveAll(episodes);

        // 응답 생성
        return CommunityBookCreateResponse.of(bookId, savedCommunityBook, copiedEpisodeCount);
    }

    /**
     * Book 엔티티를 CommunityBook으로 복사
     */
    private CommunityBook createCommunityBook(Book originalBook) {
        return CommunityBook.builder()
                // 기본 정보 복사
                .title(originalBook.getTitle())
                .coverImageUrl(originalBook.getCoverImageUrl())
                .summary(originalBook.getSummary())

                // 관계 정보 복사
                .member(originalBook.getMember())
                .bookType(originalBook.getBookType())
                .category(originalBook.getCategory())

                // 상태 정보 복사
                .completed(originalBook.getCompleted())

                // 통계 정보 초기화
                .likeCount(0)
                .viewCount(0)
                .averageRating(BigDecimal.ZERO)

                .build();
    }

    /**
     * Episode 리스트를 CommunityBookEpisode로 복사
     */
    private int copyEpisodesToCommunity(List<Episode> episodes, CommunityBook communityBook) {
        // 책에 에피소드가 하나도 없는 경우
        if (episodes.isEmpty()) {
            throw new ApiException(ErrorCode.BOOK_HAS_NO_EPISODES);
        }

        List<CommunityBookEpisode> communityEpisodes = episodes.stream()
                .map(episode -> createCommunityBookEpisode(episode, communityBook))
                .toList();

        System.out.println("community Episodes");
        for(CommunityBookEpisode e : communityEpisodes){
            System.out.println(e.toString());
        }

        // 배치로 저장 (성능 최적화)
        List<CommunityBookEpisode> savedEpisodes = communityBookEpisodeRepository.saveAll(communityEpisodes);

        return savedEpisodes.size();
    }

    /**
     * Episode 엔티티를 CommunityBookEpisode로 복사
     */
    private CommunityBookEpisode createCommunityBookEpisode(Episode episode, CommunityBook communityBook) {
        return CommunityBookEpisode.builder()
                .communityBook(communityBook)
                .title(episode.getTitle())
                .episodeDate(episode.getEpisodeDate())
                .episodeOrder(episode.getEpisodeOrder())
                .content(episode.getContent())
                .audioUrl(episode.getAudioUrl())
                .build();
    }
}
