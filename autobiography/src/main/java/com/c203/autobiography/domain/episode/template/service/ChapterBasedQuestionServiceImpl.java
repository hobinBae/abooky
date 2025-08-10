package com.c203.autobiography.domain.episode.template.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.episode.service.EpisodeService;
import com.c203.autobiography.domain.episode.template.dto.ChapterProgressDto;
import com.c203.autobiography.domain.episode.template.dto.FollowUpType;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import com.c203.autobiography.domain.episode.template.entity.*;
import com.c203.autobiography.domain.episode.template.repository.*;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChapterBasedQuestionServiceImpl implements ChapterBasedQuestionService {

    private final ChapterRepository chapterRepo;
    private final ChapterTemplateRepository templateRepo;
    private final FollowUpQuestionRepository followUpRepo;
    private final ConversationSessionRepository sessionRepo;
    private final ConversationMessageRepository messageRepo;
    private final AiClient aiClient;
    private final EpisodeService episodeService;
    private EntityManager em;
    // 세션별 동적 후속질문 큐 관리
    private final ConcurrentHashMap<String, Deque<String>> dynamicFollowUpQueues = new ConcurrentHashMap<>();

    private static final int EPISODE_TOKEN_THRESHOLD = 1000; // 에피소드 생성 기준 토큰 수

    @Override
    public NextQuestionDto initializeSession(String sessionId) {
        // 첫 번째 챕터의 첫 번째 템플릿 찾기
        Chapter firstChapter = chapterRepo.findByChapterOrder(1)
                .orElseThrow(() -> new IllegalStateException("첫 번째 챕터를 찾을 수 없습니다."));

        ChapterTemplate firstTemplate = templateRepo.findByChapterOrderAndTemplateOrder(1, 1)
                .orElseThrow(() -> new IllegalStateException("첫 번째 질문 템플릿을 찾을 수 없습니다."));

        // 세션 업데이트
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        ConversationSession updatedSession = session.toBuilder()
                .currentChapterId(firstChapter.getChapterId())
                .currentTemplateId(firstTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentChapterOrder(1)
                .currentTemplateOrder(1)
                // episodeStartMessageNo 유지됨 (toBuilder로 상속)
                .build();
        sessionRepo.save(updatedSession);

        return NextQuestionDto.builder()
                .questionText(firstTemplate.getMainQuestion())
                .questionType("MAIN")
                .currentChapterId(firstChapter.getChapterId())
                .currentChapterName(firstChapter.getChapterName())
                .currentTemplateId(firstTemplate.getTemplateId())
                .currentStageName(firstTemplate.getStageName())
                .chapterProgress(0)
                .overallProgress(0)
                .isLastQuestion(false)
                .shouldCreateEpisode(false)
                .build();
    }

    @Override
    public NextQuestionDto getNextQuestion(String sessionId, String userAnswer) {
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // 현재 템플릿 조회
        ChapterTemplate currentTemplate = templateRepo.findByTemplateIdWithFollowUps(session.getCurrentTemplateId())
                .orElseThrow(() -> new IllegalStateException("현재 템플릿을 찾을 수 없습니다."));

        // 후속질문 처리
        String nextQuestion = processFollowUpQuestions(session, currentTemplate, userAnswer);
        if (nextQuestion != null) {
            return createNextQuestionDto(session, nextQuestion, "FOLLOWUP", currentTemplate);
        }

        // 다음 메인 질문으로 이동
        return moveToNextMainQuestion(session);
    }

    private String processFollowUpQuestions(ConversationSession session, ChapterTemplate template, String userAnswer) {
        if (template.getFollowUpType() == FollowUpType.STATIC) {
            return processStaticFollowUp(session, template, userAnswer);
        } else if (template.getFollowUpType() == FollowUpType.DYNAMIC) {
            return processDynamicFollowUp(session, template, userAnswer);
        }
        return null;
    }

    private String processStaticFollowUp(ConversationSession session, ChapterTemplate template, String userAnswer) {
        List<FollowUpQuestion> followUps = followUpRepo.findByChapterTemplateTemplateIdOrderByQuestionOrderAsc(
                template.getTemplateId());

        int currentIndex = session.getFollowUpQuestionIndex();

        // 첫 번째 후속질문일 때 답변 분석 수행
        if (currentIndex == 0 && userAnswer != null && !userAnswer.trim().isEmpty()) {
            log.info("후속질문 분석 시작 - 사용자 답변: {}", userAnswer);

            // 남은 질문들을 문자열로 변환
            StringBuilder questionList = new StringBuilder();
            for (int i = 0; i < followUps.size(); i++) {
                questionList.append(String.format("%d. %s\n", i + 1, followUps.get(i).getQuestionText()));
            }

            try {
                // AI로 답변 분석
                String analysis = aiClient.analyzeAnsweredQuestions(userAnswer, questionList.toString());
                log.info("AI 분석 결과: {}", analysis);

                if ("NONE".equalsIgnoreCase(analysis.trim())) {
                    // 모든 질문이 답변됨 - 다음 템플릿으로
                    log.info("모든 후속질문이 답변됨, 다음 템플릿으로 이동");
                    return null;
                }

                // 아직 답변되지 않은 질문 번호들 파싱
                if (analysis != null && !analysis.trim().isEmpty() && !analysis.trim().equalsIgnoreCase("NONE")) {
                    String[] remainingIndexes = analysis.split(",");
                    for (String indexStr : remainingIndexes) {
                        try {
                            int questionIndex = Integer.parseInt(indexStr.trim()) - 1; // 1-based to 0-based
                            if (questionIndex >= 0 && questionIndex < followUps.size()) {
                                updateFollowUpIndex(session, questionIndex + 1);
                                log.info("답변되지 않은 질문 발견: {}", followUps.get(questionIndex).getQuestionText());
                                return followUps.get(questionIndex).getQuestionText();
                            }
                        } catch (NumberFormatException e) {
                            log.warn("질문 인덱스 파싱 오류: {}", indexStr);
                        }
                    }
                }

                // 파싱 실패시 다음 템플릿으로
                log.info("분석 결과 파싱 실패, 다음 템플릿으로 이동");
                return null;

            } catch (Exception e) {
                log.warn("답변 분석 중 오류 발생, 기본 로직 사용: {}", e.getMessage());
            }
        }

        // 기본 로직: 순서대로 진행
        if (currentIndex < followUps.size()) {
            updateFollowUpIndex(session, currentIndex + 1);
            return followUps.get(currentIndex).getQuestionText();
        }
        return null;
    }

    private String processDynamicFollowUp(ConversationSession session, ChapterTemplate template, String userAnswer) {
        Deque<String> dynamicQueue = dynamicFollowUpQueues.get(session.getSessionId());

        // 첫 번째 답변이면 AI로 후속질문 생성
        if (dynamicQueue == null && session.getFollowUpQuestionIndex() == 0) {
            String sectionKey = template.getDynamicPromptTemplate();
            String generatedQuestions = null;
            try {
                generatedQuestions = aiClient.generateDynamicFollowUpBySection(sectionKey, userAnswer);
            } catch (Exception e) {
                log.warn("동적 후속질문 생성 실패(sectionKey={}):", sectionKey, e.getMessage(), e);
            }

//            String generatedQuestions = aiClient.generateDynamicFollowUp(template.getDynamicPromptTemplate(), userAnswer);

            // AI 응답을 파싱하여 질문들을 큐에 추가
            dynamicQueue = parseAndCreateDynamicQueue(generatedQuestions);
            log.info(String.valueOf(dynamicQueue) + "질문 큐");
            dynamicFollowUpQueues.put(session.getSessionId(), dynamicQueue);
        }

        if (dynamicQueue != null && !dynamicQueue.isEmpty()) {
            updateFollowUpIndex(session, session.getFollowUpQuestionIndex() + 1);
            return dynamicQueue.poll();
        }
        return null;
    }

    private Deque<String> parseAndCreateDynamicQueue(String aiResponse) {
        Deque<String> queue = new ConcurrentLinkedDeque<>();
        if (aiResponse != null && !aiResponse.trim().isEmpty()) {
            // AI 응답을 줄바꿈으로 분리하여 질문들 추출
            String[] questions = aiResponse.split("\n");
            for (String question : questions) {
                String cleanQuestion = question.trim().replaceAll("^[0-9]+[.)\\-]\\s*", "");
                if (!cleanQuestion.isEmpty()) {
                    queue.offer(cleanQuestion);
                }
            }
        }
        return queue;
    }


    private NextQuestionDto moveToNextMainQuestion(ConversationSession session) {
        // 다음 템플릿 찾기
        ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                session.getCurrentChapterOrder(),
                session.getCurrentTemplateOrder() + 1
        ).orElse(null);

        final ConversationSession finalSessionToUse; // 최종적으로 사용할 세션 객체

        if (nextTemplate == null) {
            // 현재 챕터의 다음 템플릿이 없으면 다음 챕터로
            Chapter nextChapter = chapterRepo.findByChapterOrder(session.getCurrentChapterOrder() + 1)
                    .orElse(null);

            if (nextChapter == null) {
                // 모든 질문 완료
                autoCreateEpisodeSafely(session);
                return createCompletionQuestion(session);
            }

            try{
                log.info("챕터 전환 전 에피소드 자동 생성을 시작합니다. SessionId: {}", session.getSessionId());
                episodeService.createEpisodeFromCurrentWindow(session.getBookId(), session.getSessionId());
                log.info("✅ 챕터 종료 자동 에피소드 생성 완료");

            }catch(Exception e){
                log.warn("에피소드 자동 생성 실패(다음 챕터 진행은 계속): {}", e.getMessage());
            }

            //  DB에 반영된 최신 상태의 세션을 다시 불러옵니다.
            // 이 객체는 이제 올바른 episodeStartMessageNo (예: 47)를 가지고 있습니다.
            ConversationSession sessionAfterEpisodeCreation = sessionRepo.findById(session.getSessionId())
                    .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

            nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                    nextChapter.getChapterOrder(), 1
            ).orElseThrow(() -> new IllegalStateException("다음 챕터의 첫 템플릿을 찾을 수 없습니다."));

            finalSessionToUse = sessionRepo.save(
                    sessionAfterEpisodeCreation.toBuilder()
                            .currentChapterId(nextChapter.getChapterId())
                            .currentTemplateId(nextTemplate.getTemplateId())
                            .followUpQuestionIndex(0)
                            .currentChapterOrder(nextChapter.getChapterOrder())
                            .currentTemplateOrder(1)
                            .build()
            );
            // 챕터 변경
        } else {
            // 같은 챕터 내 다음 템플릿으로
            finalSessionToUse = updateSessionForNextTemplate(session, nextTemplate);

        }

        // 동적 후속질문 큐 초기화
        dynamicFollowUpQueues.remove(session.getSessionId());

        // 6. 모든 업데이트가 끝난 최종 세션 객체로 DTO를 생성합니다.
        return createNextQuestionDto(finalSessionToUse, nextTemplate.getMainQuestion(), "MAIN", nextTemplate);
    }


    private void autoCreateEpisodeSafely(ConversationSession sessionParam) {
        try {
            em.flush();
            em.clear();

            ConversationSession session = sessionRepo.findById(sessionParam.getSessionId())
                    .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

            Integer startNo = (session.getEpisodeStartMessageNo() != null) ? session.getEpisodeStartMessageNo() : 1;
            Integer endNo = messageRepo.findMaxMessageNo(session.getSessionId());

            log.info("[AUTO-EP] window start={}, end={}", startNo, endNo);

            if (endNo == null || endNo < startNo) {
                log.info("에피소드 생성 스킵: 포함할 메시지 없음 (start={}, end={})", startNo, endNo);
                return;
            }
            episodeService.createEpisodeFromCurrentWindow(session.getBookId(), session.getSessionId());

            em.flush();
            em.clear();

            ConversationSession after = sessionRepo.findById(session.getSessionId())
                            .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));
            log.info("[AUTO-EP] next window start(after)={}", after.getEpisodeStartMessageNo());
        } catch (Exception ex) {
            log.warn("에피소드 자동 생성 실패(다음 챕터 진행은 계속): {}", ex.getMessage());
        }
    }

    private ConversationSession updateFollowUpIndex(ConversationSession session, int newIndex) {
        ConversationSession fresh = sessionRepo.findById(session.getSessionId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        ConversationSession updated = fresh.toBuilder()
                .followUpQuestionIndex(newIndex)
                .build();
        return sessionRepo.save(updated);
    }

    private ConversationSession updateSessionForNewChapter(ConversationSession session, Chapter newChapter,
                                                           ChapterTemplate newTemplate) {
        ConversationSession fresh = sessionRepo.findById(session.getSessionId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        ConversationSession updated = fresh.toBuilder()
                .currentChapterId(newChapter.getChapterId())
                .currentTemplateId(newTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentChapterOrder(newChapter.getChapterOrder())
                .currentTemplateOrder(fresh.getCurrentTemplateOrder() + 1)
                .build();
        return sessionRepo.save(updated);
    }

    private ConversationSession updateSessionForNextTemplate(ConversationSession session, ChapterTemplate newTemplate) {
        ConversationSession fresh = sessionRepo.findById(session.getSessionId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        ConversationSession updated = fresh
                .toBuilder()
                .currentTemplateId(newTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentTemplateOrder(fresh.getCurrentTemplateOrder() + 1)
                .build();
        return sessionRepo.save(updated);
    }

    private NextQuestionDto createNextQuestionDto(ConversationSession session, String questionText, String questionType,
                                                  ChapterTemplate template) {
        Chapter currentChapter = chapterRepo.findById(session.getCurrentChapterId())
                .orElseThrow(() -> new IllegalStateException("현재 챕터를 찾을 수 없습니다."));

        // 진행률 계산
        int chapterProgress = calculateChapterProgress(session);
        int overallProgress = calculateOverallProgress(session);

        return NextQuestionDto.builder()
                .questionText(questionText)
                .questionType(questionType)
                .currentChapterId(currentChapter.getChapterId())
                .currentChapterName(currentChapter.getChapterName())
                .currentTemplateId(template.getTemplateId())
                .currentStageName(template.getStageName())
                .chapterProgress(chapterProgress)
                .overallProgress(overallProgress)
                .isLastQuestion(false)
                .shouldCreateEpisode(shouldCreateEpisode(session.getSessionId()))
                .build();
    }

    private NextQuestionDto createCompletionQuestion(ConversationSession session) {
        Chapter currentChapter = chapterRepo.findById(session.getCurrentChapterId())
                .orElseThrow(() -> new IllegalStateException("현재 챕터를 찾을 수 없습니다."));

        return NextQuestionDto.builder()
                .questionText("모든 질문이 완료되었습니다. 자서전 작성을 마치시겠습니까?")
                .questionType("COMPLETION")
                .currentChapterId(currentChapter.getChapterId())
                .currentChapterName(currentChapter.getChapterName())
                .currentTemplateId(null)
                .currentStageName("완료")
                .chapterProgress(100)
                .overallProgress(100)
                .isLastQuestion(true)
                .shouldCreateEpisode(true)
                .build();
    }

    private int calculateChapterProgress(ConversationSession session) {
        // 현재 챕터의 총 템플릿 수 대비 진행률 계산
        List<ChapterTemplate> templatesInChapter = templateRepo.findByChapterChapterIdOrderByTemplateOrderAsc(
                session.getCurrentChapterId());
        if (templatesInChapter.isEmpty()) {
            return 0;
        }

        return (session.getCurrentTemplateOrder() * 100) / templatesInChapter.size();
    }

    private int calculateOverallProgress(ConversationSession session) {
        // 전체 챕터 대비 진행률 계산
        List<Chapter> allChapters = chapterRepo.findAllByOrderByChapterOrderAsc();
        if (allChapters.isEmpty()) {
            return 0;
        }

        return (session.getCurrentChapterOrder() * 100) / allChapters.size();
    }

    @Override
    public ChapterProgressDto getSessionProgress(String sessionId) {
        // 구현 생략 - 필요시 추가
        return null;
    }

    @Override
    public List<String> getChapterTemplates(String chapterId) {
        return templateRepo.findByChapterChapterIdOrderByTemplateOrderAsc(chapterId)
                .stream()
                .map(ChapterTemplate::getMainQuestion)
                .toList();
    }

    @Override
    public void resetSessionToChapter(String sessionId, String chapterId) {
        // 구현 생략 - 필요시 추가
    }

    @Override
    public boolean shouldCreateEpisode(String sessionId) {
        ConversationSession session = sessionRepo.findById(sessionId).orElse(null);
        return session != null && session.getTokenCount() >= EPISODE_TOKEN_THRESHOLD;
    }

    @Override
    public String generateEpisodeContent(String sessionId) {
        // AI를 사용하여 대화 내용을 에피소드로 정리
        // 구현 생략 - 필요시 추가
        return "에피소드 내용 생성 기능은 추후 구현 예정입니다.";
    }
} 