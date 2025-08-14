package com.c203.autobiography.domain.episode.entity;

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
public class EpisodeImageId implements Serializable {
    @Column(name = "episode_id", nullable = false)
    private Long episodeId;

    @Column(name = "image_id", nullable = false)
    private Long imageId;
}
