package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.entity.BookCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookCategoryRepository extends JpaRepository<BookCategory, Long> {
    Optional<BookCategory> findByCategoryName(String name);
    Boolean existsByCategoryName(String name);
}
