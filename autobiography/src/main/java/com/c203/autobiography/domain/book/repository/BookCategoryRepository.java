package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.Entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
}
