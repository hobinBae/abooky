package com.c203.autobiography.domain.book.service;

import com.c203.autobiography.domain.book.dto.BookCreateRequest;
import com.c203.autobiography.domain.book.dto.BookResponse;

public interface BookService {
    BookResponse createBook(Long memberId, BookCreateRequest request);
}
