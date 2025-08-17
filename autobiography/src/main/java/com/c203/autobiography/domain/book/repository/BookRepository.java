package com.c203.autobiography.domain.book.repository;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    Optional<Book> findByBookIdAndDeletedAtIsNull(Long bookId);

    List<Book> findAllByMemberMemberIdAndDeletedAtIsNull(Long memberId);
    // BookRepository.java
    boolean existsByBookIdAndMember_MemberIdAndDeletedAtIsNull(Long bookId, Long memberId);


}
