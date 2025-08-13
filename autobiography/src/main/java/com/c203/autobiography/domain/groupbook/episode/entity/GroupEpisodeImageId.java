package com.c203.autobiography.domain.groupbook.episode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class GroupEpisodeImageId implements Serializable {
    @Column(name = "group_episode_id", nullable = false)
    private Long groupEpisodeId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;
}