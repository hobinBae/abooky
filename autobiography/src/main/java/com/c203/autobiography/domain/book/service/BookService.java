package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;
import com.c203.autobiography.domain.book.dto.BookUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookResponse createBook(Long memberId, BookCreateRequest request, MultipartFile file);

    BookResponse updateBook(Long memberId, Long bookId, BookUpdateRequest request, MultipartFile file);

    Void deleteBook(Long memberId, Long bookId);

    BookResponse completeBook(Long memberId, Long bookId);

    BookResponse getBookDetail(Long memberId, Long bookId);

    List<BookResponse> getMyBooks(Long memberId);
}
