package com.c203.autobiography.domain.book.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookCategoryRepository bookCategoryRepository;

    @Override
    @Transactional
    public BookResponse createBook(Long memberId, BookCreateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        BookCategory category = null;
        // 오류 추가해야함
        if (request.getCategoryId() != null) {
            category = bookCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_CATEGORY_NOT_FOUND));
        }
        Book book = Book.builder()
                .member(member)
                .title(request.getTitle())
                .summary(request.getSummary())
                .bookType(request.getBookType())
                .category(category)
                .build();

        Book saved = bookRepository.save(book);
        return BookResponse.of(saved);
    }
}
