package com.c203.autobiography.domain.groupbook.episode.service;

/**
 * 가이드 진행 중 하나의 질문을 표현하는 DTO
 * @param key : 템플릿/단계 식별용 키 (예 : "INTRO_FAMILY_1")
 * @param question : 사용자에게 표시할 질문 문장
 */
public record GuideQuestion(String key, String question) {
}
