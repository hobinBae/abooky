package com.c203.autobiography.domain.communityBook.entity;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookLikeId;
import com.c203.autobiography.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_book_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CommunityBookLike {

    @EmbeddedId
    private CommunityBookLikeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("communityBookId")
    @JoinColumn(name = "community_book_id", nullable = false)
    private CommunityBook communityBook;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * 커뮤니티 책 좋아요 생성 팩토리 메서드
     */
    public static CommunityBookLike of(CommunityBook communityBook, Member member) {
        return CommunityBookLike.builder()
                .id(CommunityBookLikeId.of(communityBook.getCommunityBookId(), member.getMemberId()))
                .communityBook(communityBook)
                .member(member)
                .build();
    }
}