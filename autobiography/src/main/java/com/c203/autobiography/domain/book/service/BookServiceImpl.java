package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.*;
import com.c203.autobiography.domain.book.entity.*;
import com.c203.autobiography.domain.book.repository.*;
import com.c203.autobiography.domain.book.util.BookSpecifications;
import com.c203.autobiography.domain.communityBook.entity.CommunityBook;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookEpisode;
import com.c203.autobiography.domain.communityBook.entity.CommunityBookTag;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookEpisodeRepository;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookRepository;
import com.c203.autobiography.domain.communityBook.repository.CommunityBookTagRepository;
import com.c203.autobiography.domain.episode.dto.SessionStatus;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.episode.repository.EpisodeImageRepository;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.group.repository.GroupRepository;
import com.c203.autobiography.domain.groupbook.dto.GroupBookCreateResponse;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupType;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeStatus;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
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

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${aws.s3.base-url}")
    private String s3BaseUrl;

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
    private final CommunityBookTagRepository communityBookTagRepository;
    private final BookTagRepository bookTagRepository;
    private final GroupRepository groupRepository;
    private final GroupBookRepository groupBookRepository;
    private final EpisodeImageRepository episodeImageRepository;
    private final GroupEpisodeRepository groupEpisodeRepository;
    private final ConversationSessionRepository conversationSessionRepository;

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

        String currentImageUrl = book.getCoverImageUrl();
        String newImageUrl = currentImageUrl; // 기본값은 현재 이미지 URL
        if (file != null && !file.isEmpty()) {

            currentImageUrl = book.getCoverImageUrl();

            if (isDeletableS3File(currentImageUrl)) {
                fileStorageService.delete(currentImageUrl);
            }

            newImageUrl = fileStorageService.store(file, "book");

        } else if (request.getCoverImageUrl() != null && !request.getCoverImageUrl().equals(currentImageUrl)) {
            // [CASE 2] 새로운 파일 업로드는 없지만, 요청에 다른 이미지 URL(예: 다른 기본 이미지 선택)이 포함된 경우

            // 기존 이미지가 S3에 업로드된 파일인 경우에만 삭제
            if (isDeletableS3File(currentImageUrl)) {
                fileStorageService.delete(currentImageUrl);
            }
            // 요청으로 받은 URL을 새 URL로 설정
            newImageUrl = request.getCoverImageUrl();
        }

        book.updateBook(request.getTitle(), newImageUrl, request.getSummary(), category);

        List<String> tagNames = request.getTags();
        if (tagNames != null) {
            // 1. 요청으로 들어온 태그 이름들을 Tag 엔티티 Set으로 변환
            Set<Tag> newTags = tagNames.stream().distinct()
                    .map(name -> tagRepository.findByTagName(name)
                            .orElseGet(() -> tagRepository.save(Tag.builder().tagName(name).build()))
                    ).collect(Collectors.toSet());

            // 2. Book 엔티티의 동기화 메소드를 호출하여 한번에 업데이트
            book.updateTags(newTags);
        }

        return BookResponse.of(book);
    }

    private boolean isDeletableS3File(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        // ★★★ 핵심 수정 사항 ★★★
        // URL에 '/default_' 라는 문자열이 포함되어 있는지 확인하여 기본 이미지인지 판별합니다.
        boolean isDefaultImage = url.contains("/default_");

        // 삭제 가능한 파일의 조건: 우리 S3 URL로 시작하고, 기본 이미지는 아니어야 함.
        return url.startsWith(s3BaseUrl) && !isDefaultImage;
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

        // 2) ★★★ 태그 설정 로직 수정 ★★★
        if (tags != null) { // 비어있는 리스트도 처리하기 위해 !tags.isEmpty() 조건 제거
            // 2-1. 요청으로 들어온 태그 이름들을 Tag 엔티티 Set으로 변환
            Set<Tag> newTags = tags.stream().distinct()
                    .map(name -> tagRepository.findByTagName(name)
                            .orElseGet(() -> tagRepository.save(Tag.builder().tagName(name).build()))
                    ).collect(Collectors.toSet());

            // 2-2. Book 엔티티의 동기화 메소드를 호출하여 한번에 업데이트
            book.updateTags(newTags);
        }

        // 3) DTO 변환을 위한 데이터 조회 (기존과 동일)
        List<EpisodeResponse> episodes = episodeRepository
                .findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(bookId)
                .stream()
                .map(EpisodeResponse::of)
                .toList();

        List<String> tagNames = book.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

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
        // 에피소드 목록 조회
        List<Episode> episodes = episodeRepository
                .findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(bookId);


//        // 에피소드 목록 조회 + DTO 매핑
//        List<EpisodeResponse> episodes = episodeRepository
//                .findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(bookId)
//                .stream()
//                .map(EpisodeResponse::of)
//                .toList();

        // 태그 조회
        List<String> tagNames = book.getTags().stream()
                .map(bt -> bt.getTag().getTagName())
                .toList();

        List<EpisodeResponse> episodeResponses = episodes.stream()
                .map(episode -> {
                    // 각 에피소드에 대해 OPEN 상태인 세션이 있는지 조회
                    Optional<ConversationSession> activeSession = conversationSessionRepository
                                .findTopByEpisodeIdAndStatusOrderByCreatedAtDesc(episode.getEpisodeId(), SessionStatus.OPEN);
                    // 세션이 있다면 ID를, 없다면 null을 DTO에 담는다.
                    return EpisodeResponse.of(episode, activeSession.map(ConversationSession::getSessionId).orElse(null));
                })
                .collect(Collectors.toList());


        return BookResponse.of(book, episodeResponses, tagNames);
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

        if (!exists) {
            BookLike like = BookLike.of(book, member);
            bookLikeRepository.save(like);
            book.incrementLike();
            return new LikeResponse(true, bookLikeRepository.countByBook(book));

        } else {
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
                .map(r -> {
                    r.updateScore(request.getScore());
                    return r;
                })
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

        // 태그 복사
        Long communityBookId = savedCommunityBook.getCommunityBookId();
        copyTagsToCommunityBookTags(originalBook, savedCommunityBook);

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
        for (CommunityBookEpisode e : communityEpisodes) {
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
        // 에피소드의 이미지 URL 가져오기 (하나만 업로드 가능)
        String imageUrl = episodeImageRepository
                .findByEpisode_EpisodeIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(episode.getEpisodeId())
                .stream()
                .findFirst()
                .map(episodeImage -> episodeImage.getImageUrl())
                .orElse(null);

        return CommunityBookEpisode.builder()
                .communityBook(communityBook)
                .title(episode.getTitle())
                .episodeDate(episode.getEpisodeDate())
                .episodeOrder(episode.getEpisodeOrder())
                .content(episode.getContent())
                .audioUrl(episode.getAudioUrl())
                .imageUrl(imageUrl)
                .build();
    }

    private int copyTagsToCommunityBookTags(Book originalBook, CommunityBook communityBook) {
        try {
            // 1. book_tags 테이블에서 book_id로 BookTag 목록 조회
            List<BookTag> originalBookTags = bookTagRepository.findBookTagsByBookBookId(originalBook.getBookId());

            if (originalBookTags.isEmpty()) {
                log.debug("No tags found for book: {}", originalBook.getBookId());
                return 0;
            }

            // 2. BookTag에서 Tag 엔티티들을 추출하여 CommunityBookTag 생성
            List<CommunityBookTag> communityBookTags = originalBookTags.stream()
                    .map(bookTag -> CommunityBookTag.of(communityBook, bookTag.getTag()))
                    .collect(java.util.stream.Collectors.toList()); // Java 8 호환

            // 3. community_book_tags 테이블에 저장
            communityBookTagRepository.saveAll(communityBookTags);

            log.debug("Copied {} tags for community book: {}",
                    communityBookTags.size(), communityBook.getCommunityBookId());

            return communityBookTags.size();
        } catch (Exception e) {
            log.error("Failed to copy tags for community book: {}",
                    communityBook.getCommunityBookId(), e);
            return 0;
        }
    }

    @Transactional
    @Override
    public GroupBookCreateResponse exportBookToGroup(Long memberId, Long bookId, Long groupId) {
        // 1. 원본 책 조회 및 권한 검증
        Book originalBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 2. 작성자 권한 검증
        if (!originalBook.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 3. 완성된 책만 그룹책으로 변환 가능
        if (!originalBook.getCompleted()) {
            throw new ApiException(ErrorCode.BOOK_NOT_COMPLETED);
        }

        // 4. 그룹 존재 확인
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        // 5. GroupBook 생성
        GroupBook groupBook = createGroupBook(originalBook, group);
        GroupBook savedGroupBook = groupBookRepository.save(groupBook);

        // 6. Episode → GroupEpisode 복사
        List<Episode> episodes = episodeRepository.findByBookBookIdOrderByEpisodeOrderAsc(bookId);
        copyEpisodesToGroup(episodes, savedGroupBook);

        // 7. 태그 복사 (만약 GroupBookTag가 있다면)
        copyTagsToGroupBookTags(originalBook, savedGroupBook);

        // 8. 원본 책과 에피소드 소프트 삭제 -> 그룹북은 일단 삭제 되지 않도록 처리
//        originalBook.softDelete();
//        for (Episode episode : episodes) {
//            episode.softDelete();
//        }
        episodeRepository.saveAll(episodes);

        // 9. 응답 생성
        return GroupBookCreateResponse.from(savedGroupBook, bookId, originalBook.getTitle());
    }

    /**
     * Book 엔티티를 GroupBook으로 복사
     */
    private GroupBook createGroupBook(Book originalBook, Group group) {
        return GroupBook.builder()
                .member(originalBook.getMember())
                .group(group)
                .title(originalBook.getTitle())
                .coverImageUrl(originalBook.getCoverImageUrl())
                .summary(originalBook.getSummary())
                .bookType(originalBook.getBookType())
                .category(originalBook.getCategory())
                .completed(originalBook.getCompleted())
                .completedAt(originalBook.getCompletedAt())
                .groupType(GroupType.OTHER)
                .build();
    }

    /**
     * Episode들을 GroupEpisode로 복사
     */
    private int copyEpisodesToGroup(List<Episode> episodes, GroupBook groupBook) {
        if (episodes.isEmpty()) {
            return 0;
        }

        try {
            List<GroupEpisode> groupEpisodes = episodes.stream()
                    .map(episode -> GroupEpisode.builder()
                            .groupBook(groupBook)
                            .title(episode.getTitle())
                            .orderNo(episode.getEpisodeOrder())
                            .status(GroupEpisodeStatus.COMPLETE)
                            .rawNotes(episode.getContent())
                            .editedContent(episode.getContent())
                            .currentStep(0)
                            .template("IMPORTED")
                            .build())
                    .collect(Collectors.toList());

            groupEpisodeRepository.saveAll(groupEpisodes);
            return groupEpisodes.size();
        } catch (Exception e) {
            log.error("Failed to copy episodes to group book: {}", groupBook.getGroupBookId(), e);
            return 0;
        }
    }

    /**
     * Book의 태그들을 GroupBook으로 복사 (향후 GroupBookTag 구현 시 사용)
     */
    private int copyTagsToGroupBookTags(Book originalBook, GroupBook groupBook) {
        try {
            // GroupBookTag 엔티티가 구현되면 여기에 태그 복사 로직 추가
            // 현재는 GroupBookTag 엔티티가 없으므로 0 반환
            log.debug("GroupBookTag entity not implemented yet for group book: {}", groupBook.getGroupBookId());
            return 0;
        } catch (Exception e) {
            log.error("Failed to copy tags for group book: {}", groupBook.getGroupBookId(), e);
            return 0;
        }
    }
}
