package com.c203.autobiography.domain.groupbook.episode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_episode_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class GroupEpisodeImage {
    @EmbeddedId
    private GroupEpisodeImageId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("groupEpisodeId")
    @JoinColumn(
            name = "group_episode_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_group_episode_image_episode")
    )
    private GroupEpisode groupEpisode;

    @Column(name = "image_url", length = 500, nullable = false)
    @NotBlank
    private String imageUrl;

    @Column(name = "order_no")
    private Integer orderNo;

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * URL과 순서 업데이트
     */
    public void updateImage(String imageUrl, Integer orderNo, String description) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            this.imageUrl = imageUrl;
        }
        if (orderNo != null) {
            this.orderNo = orderNo;
        }
        if (description != null) {
            this.description = description;
        }
    }

    /**
     * 소프트 삭제
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 팩토리 메서드: GroupEpisode 엔티티와 imageId, URL을 받아 객체 생성
     */
    public static GroupEpisodeImage create(GroupEpisode groupEpisode, Long imageId, String imageUrl, Integer orderNo, String description) {
        GroupEpisodeImageId pk = GroupEpisodeImageId.of(groupEpisode.getGroupEpisodeId(), imageId);
        return GroupEpisodeImage.builder()
                .id(pk)
                .groupEpisode(groupEpisode)
                .imageUrl(imageUrl)
                .orderNo(orderNo)
                .description(description)
                .build();
    }
}