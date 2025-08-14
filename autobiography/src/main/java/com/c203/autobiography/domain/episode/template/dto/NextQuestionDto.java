package com.c203.autobiography.domain.episode.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NextQuestionDto {
    
    private String questionText;
    private String questionType; // MAIN, FOLLOWUP_STATIC, FOLLOWUP_DYNAMIC
    private String currentChapterId;
    private String currentChapterName;
    private String currentTemplateId;
    private String currentStageName;
    private int chapterProgress; // 현재 챕터에서의 진행률 (%)
    private int overallProgress; // 전체 진행률 (%)
    private boolean isLastQuestion; // 마지막 질문 여부
    private boolean shouldCreateEpisode; // 에피소드 생성 필요 여부
} 