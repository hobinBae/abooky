package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.BookUpdateRequest;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;
import com.c203.autobiography.domain.book.repository.BookCategoryRepository;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookCategoryRepository bookCategoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public BookResponse createBook(Long memberId, BookCreateRequest request, MultipartFile file) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        BookCategory category = null;
        // 오류 추가해야함
        if (request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }
        if (file != null && !file.isEmpty()) {
            String imageUrl = fileStorageService.store(file, "book");
            request.setCoverImageUrl(imageUrl);
        }else{
            request.setCoverImageUrl(null);
        }
        Book book = request.toEntity(member, category);

        Book saved = bookRepository.save(book);
        return BookResponse.of(saved);
    }

    @Override
    public BookResponse updateBook(Long memberId,Long bookId, BookUpdateRequest request, MultipartFile file) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        BookCategory category = null;
        // 오류 추가해야함
        if (request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }

        if(file != null && !file.isEmpty()){
            String currentImageUrl = book.getCoverImageUrl();
            if(currentImageUrl != null && !currentImageUrl.isBlank()){
                fileStorageService.delete(currentImageUrl);
            }

            String newImageUrl = fileStorageService.store(file, "book");

            request.setCoverImageUrl(newImageUrl);
        }
        book.updateBook(request.getTitle(), request.getCoverImageUrl(), request.getSummary(), category);
        return BookResponse.of(book);
    }

    @Override
    public Void deleteBook(Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        book.softDelete();

        return null;
    }

    @Override
    public BookResponse completeBook(Long memberId, Long bookId) {
        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 3) 완료 처리
        book.markCompleted();

        // 4) BookResponse 변환
        return BookResponse.of(book);
    }

    @Override
    public BookResponse readBook(Long memberId, Long bookId) {

        memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        return null;
    }

    @Override
    public List<BookResponse> getMyBooks(Long memberId) {
        return List.of();
    }

}
