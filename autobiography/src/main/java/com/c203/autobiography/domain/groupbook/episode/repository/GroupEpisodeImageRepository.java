package com.c203.autobiography.domain.groupbook.episode.repository;

import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeImage;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeImageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupEpisodeImageRepository extends JpaRepository<GroupEpisodeImage, GroupEpisodeImageId> {
    
    /**
     * 특정 그룹 에피소드의 삭제되지 않은 이미지들을 순서대로 조회
     */
    List<GroupEpisodeImage> findByGroupEpisode_GroupEpisodeIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(Long groupEpisodeId);
    
    /**
     * 특정 그룹 에피소드의 이미지 개수 조회 (삭제되지 않은 것만)
     */
    long countByGroupEpisode_GroupEpisodeIdAndDeletedAtIsNull(Long groupEpisodeId);
    
    /**
     * 특정 이미지 조회 (삭제되지 않은 것만)
     */
    Optional<GroupEpisodeImage> findByIdAndDeletedAtIsNull(GroupEpisodeImageId id);
    
    /**
     * 특정 그룹 에피소드의 최대 순서 번호 조회
     */
    @Query("SELECT COALESCE(MAX(gei.orderNo), 0) FROM GroupEpisodeImage gei " +
           "WHERE gei.groupEpisode.groupEpisodeId = :groupEpisodeId AND gei.deletedAt IS NULL")
    Integer findMaxOrderNoByGroupEpisodeId(@Param("groupEpisodeId") Long groupEpisodeId);
}