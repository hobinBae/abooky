package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.BookCopyRequest;
import com.c203.autobiography.domain.book.dto.BookCopyResponse;
import com.c203.autobiography.domain.book.dto.BookUpdateRequest;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;
import com.c203.autobiography.domain.book.entity.Tag;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.book.repository.TagRepository;
import com.c203.autobiography.domain.book.util.BookSpecifications;
import com.c203.autobiography.domain.episode.dto.EpisodeCopyRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;

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

}
