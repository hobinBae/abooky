package com.c203.autobiography.global.config;

import com.c203.autobiography.domain.episode.template.service.ChapterDataInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final ChapterDataInitService chapterDataInitService;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("서버 시작시 챕터 데이터 초기화 시작");
            chapterDataInitService.initializeChapterData();
            log.info("챕터 데이터 초기화 완료");
        } catch (Exception e) {
            log.warn("챕터 데이터 초기화 중 오류 발생 (기존 데이터가 있을 수 있음): {}", e.getMessage());
        }
    }
} 