package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookTagRepository extends JpaRepository<BookTag, Long > {
    List<BookTag> findBookTagsByBookBookId(Long bookId);
}
