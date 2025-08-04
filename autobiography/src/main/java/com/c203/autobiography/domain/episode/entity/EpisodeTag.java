package com.c203.autobiography.domain.episode.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "episode_tags",
        indexes = @Index(name = "idx_episode_tags_tag_id", columnList = "tag_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder
@EqualsAndHashCode(of = "id")
public class EpisodeTag {
    @EmbeddedId
    private EpisodeTagId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("episodeId")
    @JoinColumn(
            name = "episode_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_tag_episode")
    )
    private Episode episode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tagId")
    @JoinColumn(
            name = "tag_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_episode_tag_tag")
    )
    private Tag tag;


    /**
     * 관계를 생성합니다.
     */
    public static EpisodeTag of(Episode episode, Tag tag) {
        return EpisodeTag.builder()
                .id(EpisodeTagId.of(episode.getEpisodeId(), tag.getTagId()))
                .episode(episode)
                .tag(tag)
                .build();
    }
}
