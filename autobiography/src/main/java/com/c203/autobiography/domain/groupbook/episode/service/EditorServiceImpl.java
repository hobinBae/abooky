package com.c203.autobiography.domain.groupbook.episode.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class EditorServiceImpl implements EditorService {
    @Override
    public String polish(String rawAnswer, String priorContext) {
        if (rawAnswer == null || rawAnswer.isBlank()) return "";

        // 1) 공백/이모지/과도한 기호 정리
        String text = normalize(rawAnswer);

        // 2) 문장 단위로 깔끔하게 마침표/띄어쓰기 보정 (매우 가벼운 규칙)
        String[] sentences = text.split("(?<=[.!?])\\s+|\\n+");
        String normalized = Arrays.stream(sentences)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(this::capitalizeIfNeeded)
                .map(this::ensurePeriod)
                .collect(Collectors.joining(" "));

        // 3) 담백한 톤: 과장 표현/중복 강조 줄이기
        normalized = normalized
                .replace("!!!", ".")
                .replace("..", ".")
                .replace("??", "?")
                .replace("!!", ".");

        // 4) 맥락과 자연스럽게 이어붙이기 (여기서는 단순 연결)
        if (priorContext != null && !priorContext.isBlank()) {
            return normalized;
        }
        return normalized;
    }

    private String normalize(String s) {
        // 이모지/특수문자 간략 제거, 연속 공백 축소
        String t = s.replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", ""); // 간이 이모지 제거
        t = t.replaceAll("[“”]", "\"").replaceAll("[‘’]", "'");
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private String capitalizeIfNeeded(String s) {
        if (s.isEmpty()) return s;
        // 한국어는 대문자 개념이 약하므로 영문만 첫 글자 대문자 처리
        char c = s.charAt(0);
        if (c >= 'a' && c <= 'z') {
            return Character.toUpperCase(c) + s.substring(1);
        }
        return s;
    }

    private String ensurePeriod(String s) {
        // 문장 끝에 마침표/물음표/느낌표가 없으면 마침표를 붙여 담백한 마감
        char last = s.charAt(s.length() - 1);
        if (".?!".indexOf(last) >= 0) return s;
        return s + ".";
    }
}
