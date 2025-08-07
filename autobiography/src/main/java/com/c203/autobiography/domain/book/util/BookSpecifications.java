package com.c203.autobiography.domain.book.util;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookTag;
import com.c203.autobiography.domain.book.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class BookSpecifications {

    public static Specification<Book> titleContains(String title) {
        if (!StringUtils.hasText(title)) {
            return (root, query, cb) -> cb.conjunction(); // 조건 없음 (항상 true)
        }
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Book> categoryEquals(Long categoryId) {
        if (categoryId == null) {
            return (root, query, cb) -> cb.conjunction(); // 조건 없음 (항상 true)
        }
        return (root, query, cb) ->
                cb.equal(root.get("category").get("bookCategoryId"), categoryId);
    }

    public static Specification<Book> hasAnyTag(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return (root, query, cb) -> cb.conjunction(); // 조건 없음 (항상 true)
        }

        return (root, query, cb) -> {
            // DISTINCT 설정 (중복 제거)
            query.distinct(true);

            // Book → BookTag → Tag 조인 방식으로 변경
            Join<Book, BookTag> bookTagJoin = root.join("tags", JoinType.INNER);
            Join<BookTag, Tag> tagJoin = bookTagJoin.join("tag", JoinType.INNER);

            // 태그 이름 매칭 (대소문자 구분 없이)
            return tagJoin.get("tagName").in(tags);
        };
    }

    // 디버깅용: 삭제되지 않은 책만 조회
    public static Specification<Book> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}