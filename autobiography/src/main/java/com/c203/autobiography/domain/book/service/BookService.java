package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookResponse createBook(Long memberId, BookCreateRequest request, MultipartFile file);

    BookResponse updateBook(Long memberId, Long bookId, BookUpdateRequest request, MultipartFile file);

    Void deleteBook(Long memberId, Long bookId);

    BookResponse completeBook(Long memberId, Long bookId, List<String> tags);

    BookResponse getBookDetail(Long memberId, Long bookId);

    List<BookResponse> getMyBooks(Long memberId);

    BookCopyResponse copyBook(Long memberId, Long bookId, BookCopyRequest request);

    Page<BookResponse> searchBooks(String title, Long categoryId,List<String> tags, Pageable pageable);

    LikeResponse toggleLike(Long bookId, Long memberId);

    BookRatingResponse rateBook(Long memberId, Long bookId, BookRatingRequest request);

    BookRatingResponse getBookRating(Long memberId, Long bookId);
}
