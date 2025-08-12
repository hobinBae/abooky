package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditorServiceImpl implements EditorService {

    private final AiClient aiClient;

    @Value("${app.editor.use-gpt:true}")
    private boolean useGpt;

    @Override
    public String polish(String rawAnswer, String priorContext) {
        return polish(rawAnswer, priorContext, "PLAIN");
    }

    @Override
    public String polish(String rawAnswer, String priorContext, String tone) {
        if (rawAnswer == null || rawAnswer.isBlank()) return "";

        // GPT 사용 못하면 기존 로직 사용
        if(!useGpt) {
            return basicPolish(rawAnswer, priorContext);
        }

        try {
            String gptResult = aiClient.editText(rawAnswer, priorContext, tone);

            if(gptResult == null || gptResult.isBlank() ||
            gptResult.length() > rawAnswer.length() * 3) {
                log.warn("GPT 편집 결과가 부적절합니다. 기본 편집을 사용합니다.");
                return basicPolish(rawAnswer, priorContext);
            }
            return gptResult;
        } catch (Exception e) {
            log.error("GPT 편집 중 오류 발생 기본 편집으로 풀백 : {}", e.getMessage());
            return basicPolish(rawAnswer, priorContext);
        }


    }

    private String basicPolish(String rawAnswer, String priorContext) {
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
