package com.c203.autobiography.domain.groupbook.episode.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "episode_guide_state", indexes = @Index(name = "idx_egs_episode_step", columnList="group_episode_id, step_no"))
public class GroupEpisodeGuideState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_episode_id", nullable = false)
    private GroupEpisode groupEpisode;

    @Column(name = "step_no", nullable = false)
    private Integer stepNo;

    @Column(name = "guide_key", length = 100)
    private String guideKey;

    @Lob
    @Column(nullable = false)
    private String question;

    @Lob
    private String userAnswer;

    @Lob
    private String editedParagraph; // AI 편집 결과

    @Column(name = "is_final")
    private Boolean isFinal;
}
