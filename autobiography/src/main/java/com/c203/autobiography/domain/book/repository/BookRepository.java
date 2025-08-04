package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.Entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByBookIdAndDeletedAtIsNull(Long bookId);
}
