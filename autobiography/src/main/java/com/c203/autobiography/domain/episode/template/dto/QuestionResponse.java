package com.c203.autobiography.domain.episode.template.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI가 생성한 질문 응답 DTO")
public class QuestionResponse {

    @Schema(description = "AI 질문 텍스트", example = "당신의 출생은 언제입니까?")
    private String text;
    
    @Schema(description = "현재 챕터 정보")
    private String currentChapter;

    @Schema(description = "챕터 이름")
    private String currentChapterName;

    @Schema(description = "스테이지 이름")
    private String currentStageName;

    @Schema(description = "현재 단계 정보")
    private String currentStage;
    
    @Schema(description = "질문 유형", example = "MAIN, FOLLOWUP_STATIC, FOLLOWUP_DYNAMIC")
    private String questionType;
    
    @Schema(description = "전체 진행률 (0-100)")
    private Integer overallProgress;
    
    @Schema(description = "챕터 진행률 (0-100)")
    private Integer chapterProgress;
    
    @Schema(description = "마지막 질문 여부")
    private Boolean isLastQuestion;
    
    @Schema(description = "템플릿 완료 여부 (사용자가 답변 확인 및 저장 후 다음 단계로 진행)")
    private Boolean isTemplateCompleted;
}
