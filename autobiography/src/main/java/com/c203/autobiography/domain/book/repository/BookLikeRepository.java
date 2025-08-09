package com.c203.autobiography.domain.book.repository;


import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookLike;
import com.c203.autobiography.domain.book.entity.BookLikeId;
import com.c203.autobiography.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookLikeRepository extends JpaRepository<BookLike, BookLikeId> {
    /**
     * 책 좋아요 이력 조회
     * @param book
     * @param member
     * @return
     */
    boolean existsByBookAndMember(Book book, Member member);

    long countByBook(Book book);

    void deleteByBookAndMember(Book book, Member member);


}
