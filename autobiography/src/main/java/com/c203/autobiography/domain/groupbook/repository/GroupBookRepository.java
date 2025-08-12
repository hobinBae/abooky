package com.c203.autobiography.domain.groupbook.repository;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;


public interface GroupBookRepository extends JpaRepository<GroupBook, Long>, JpaSpecificationExecutor<GroupBook> {
    Optional<GroupBook> findByGroupBookIdAndDeletedAtIsNull(Long bookId);

    List<GroupBook> findAllByGroupGroupIdAndDeletedAtIsNull(Long groupId);
}
