package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.Episode;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {
    Optional<Episode> findByEpisodeIdAndDeletedAtIsNull(Long episodeId);

    Optional<Episode> findByEpisodeIdAndBookBookIdAndDeletedAtIsNull(Long episodeId, Long bookId);
    List<Episode> findAllByBookBookIdAndDeletedAtIsNullOrderByEpisodeOrder(Long bookId);

    Long countByBookBookIdAndDeletedAtIsNull(Long bookId);
    /**
     * 특정 책(bookId)에 속하면서, content가 null이 아니고 빈 문자열("")이 아닌
     * 에피소드의 개수를 계산합니다. (완료된 챕터 수를 세는 데 사용)
     *
     * @param bookId 책 ID
     * @param content 제외할 내용 (보통 빈 문자열 "")
     * @return 조건에 맞는 에피소드의 개수
     */
    long countByBookBookIdAndContentIsNotNullAndContentIsNot(Long bookId, String content);


    /**
     * 특정 책(bookId)에 속하면서, content가 채워져 있는(null이나 빈 문자열이 아닌)
     * 에피소드의 개수를 계산합니다. (완료된 챕터 수를 세는 데 사용)
     *
     * @param bookId 책 ID
     * @return 조건에 맞는 에피소드의 개수
     */
    @Query("SELECT count(e) FROM Episode e WHERE e.book.bookId = :bookId AND e.content IS NOT NULL AND e.content <> ''")
    long countCompletedEpisodesByBookId(@Param("bookId") Long bookId);


    List<Episode> findByBookBookIdOrderByEpisodeOrderAsc(Long bookId);

}
