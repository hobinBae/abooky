package com.c203.autobiography.domain.groupbook.episode.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 에피소드 답변 교정 응답 DTO")
public class GroupAnswerCorrectionResponse {

    @Schema(description = "원본 답변", example = "5살 때 부모님과 함께 간 제주도 여행이 가장 기억에 남아요...")
    private String originalAnswer;

    @Schema(description = "교정된 답변", 
            example = "어린 시절 가장 소중한 추억으로, 5세 때 부모님과 함께 떠났던 제주도 여행을 꼽을 수 있습니다...")
    private String correctedAnswer;

    @Schema(description = "교정 사유/개선점", 
            example = "문체를 보다 자연스럽게 다듬고, 감정적 표현을 강화하여 독자의 몰입도를 높였습니다.")
    private String correctionReason;

    @Schema(description = "추천 추가 질문", 
            example = "그 여행에서 가장 인상 깊었던 장소나 경험이 있다면 자세히 말씀해 주시겠어요?")
    private String suggestedFollowUpQuestion;
}