package com.c203.autobiography.domain.episode.repository;

import com.c203.autobiography.domain.episode.entity.EpisodeImage;
import com.c203.autobiography.domain.episode.entity.EpisodeImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeImageRepository extends JpaRepository<EpisodeImage, EpisodeImageId> {
    
    /**
     * 특정 에피소드의 삭제되지 않은 이미지들을 순서대로 조회
     */
    List<EpisodeImage> findByEpisode_EpisodeIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(Long episodeId);
    
    /**
     * 특정 에피소드의 이미지 개수 조회 (삭제되지 않은 것만)
     */
    long countByEpisode_EpisodeIdAndDeletedAtIsNull(Long episodeId);
    
    /**
     * 특정 이미지 조회 (삭제되지 않은 것만)
     */
    Optional<EpisodeImage> findByIdAndDeletedAtIsNull(EpisodeImageId id);
    
    /**
     * 특정 에피소드의 최대 순서 번호 조회
     */
    @Query("SELECT COALESCE(MAX(ei.orderNo), 0) FROM EpisodeImage ei " +
           "WHERE ei.episode.episodeId = :episodeId AND ei.deletedAt IS NULL")
    Integer findMaxOrderNoByEpisodeId(@Param("episodeId") Long episodeId);
}
