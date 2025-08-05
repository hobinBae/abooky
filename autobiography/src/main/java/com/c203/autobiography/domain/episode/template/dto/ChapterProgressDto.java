package com.c203.autobiography.domain.episode.template.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterProgressDto {
    
    private String sessionId;
    private String currentChapterId;
    private String currentChapterName;
    private String currentTemplateId;
    private String currentStageName;
    private int currentChapterOrder;
    private int currentTemplateOrder;
    private int totalChapters;
    private int totalTemplatesInCurrentChapter;
    private int chapterProgress; // 현재 챕터 내 진행률 (%)
    private int overallProgress; // 전체 진행률 (%)
    private List<ChapterSummaryDto> chapterSummaries;
    private int totalAnswerTokens; // 현재까지 답변한 총 토큰 수
    private boolean canCreateEpisode; // 에피소드 생성 가능 여부
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChapterSummaryDto {
    private String chapterId;
    private String chapterName;
    private boolean completed;
    private int progress; // 해당 챕터 진행률 (%)
} 