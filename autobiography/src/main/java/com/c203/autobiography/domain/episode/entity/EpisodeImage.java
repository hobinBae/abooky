package com.c203.autobiography.domain.episode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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

@Entity
@Table(name = "episode_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class EpisodeImage {
    @Embeddable
    private EpisodeImageId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("episodeId")
    @JoinColumn(
            name = "episode_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_image_episode")
    )
    private Episode episode;


    @Column(name = "image_url", length = 255, nullable = false)
    @NotBlank
    private String imageUrl;

    /**
     * URL만 갱신할 때 사용합니다.
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * 팩토리 메서드: Episode 엔티티와 imageId, URL을 받아 객체 생성
     */
    public static EpisodeImage create(Episode episode, Long imageId, String imageUrl) {
        EpisodeImageId pk = EpisodeImageId.of(episode.getEpisodeId(), imageId);
        return EpisodeImage.builder()
                .id(pk)
                .episode(episode)
                .imageUrl(imageUrl)
                .build();
    }

}
