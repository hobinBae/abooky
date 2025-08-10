package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.StartConversationResponse;
import com.c203.autobiography.domain.episode.template.dto.FollowUpType;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.domain.episode.template.entity.Chapter;
import com.c203.autobiography.domain.episode.template.entity.ChapterTemplate;
import com.c203.autobiography.domain.episode.template.entity.FollowUpQuestion;
import com.c203.autobiography.domain.episode.template.repository.ChapterRepository;
import com.c203.autobiography.domain.episode.template.repository.ChapterTemplateRepository;
import com.c203.autobiography.domain.episode.template.repository.FollowUpQuestionRepository;
import com.c203.autobiography.domain.episode.template.dto.NextQuestionDto;
import com.c203.autobiography.domain.episode.dto.ConversationMessageUpdateRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionRequest;
import com.c203.autobiography.domain.episode.dto.ConversationSessionResponse;
import com.c203.autobiography.domain.episode.dto.ConversationSessionUpdateRequest;
import com.c203.autobiography.domain.episode.dto.MessageType;
import com.c203.autobiography.domain.episode.dto.SessionStatus;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * ConversationService 구현체: 세션과 메시지를 관리하며, 질문 템플릿 인덱스를 자동으로 증가시킵니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private final EpisodeService episodeService;
    private final ConversationSessionRepository sessionRepo;
    private final ConversationMessageRepository messageRepo;
    private final ChapterRepository chapterRepo;
    private final ChapterTemplateRepository templateRepo;
    private final FollowUpQuestionRepository followUpRepo;
    private final AiClient aiClient;
    private final SseService sseService;

    // == 인메모리 상태 관리 ==
    // 세션별 질문 큐 관리 (0부터 시작)
    private final ConcurrentHashMap<String, Deque<String>> dynamicFollowUpQueues = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Deque<String>> questionQueueMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> lastQuestionMap = new ConcurrentHashMap<>();


    @Override
    @Transactional
    public ConversationSessionResponse createSession(ConversationSessionRequest request) {
        // 여기 에러 코드 추가
        ConversationSession existing = sessionRepo.findById(request.getSessionId()).orElse(null);
        if (existing != null) {
            return ConversationSessionResponse.from(existing);
        }
        // 기존 세션이 있으면, episodeStartMessageNo를 요청받은 값으로 "업데이트"합니다.
        Integer startNo = request.getEpisodeStartMessageNo() != null
                ? request.getEpisodeStartMessageNo()
                : 1;
        ConversationSession session = ConversationSession.builder()
                .sessionId(request.getSessionId())
                //여기 나중에 바꿔야함
                .bookId(1L)
                .episodeId(request.getEpisodeId())
                .status(SessionStatus.OPEN)
                .tokenCount(0L)
                .templateIndex(0)
                .episodeStartMessageNo(startNo)
                .build();
        sessionRepo.save(session);
        return ConversationSessionResponse.from(session);
    }

    @Override
    public String startNewConversation() {
        String sessionId = UUID.randomUUID().toString();
        // 2. 새로운 세션 객체 생성 및 저장
        ConversationSession newSession = ConversationSession.builder()
                .sessionId(sessionId)
                .bookId(1L) // TODO: 실제 bookId를 동적으로 할당해야 함
                .status(SessionStatus.OPEN)
                .tokenCount(0L)
                .templateIndex(0)
                .episodeStartMessageNo(1)
                .build();
        sessionRepo.save(newSession);

//        // 3. 챕터 기반 대화 초기화 및 첫 질문 가져오기
//        NextQuestionDto firstQuestion = initializeSession(sessionId); // 이전 답변에서 통합한 메서드
//
//        // 4. 첫 질문을 DB에 저장
//        createMessage(
//                ConversationMessageRequest.builder()
//                        .sessionId(sessionId)
//                        .messageType(MessageType.QUESTION)
//                        .content(firstQuestion.getQuestionText())
//                        .build()
//        );

        // 5. 컨트롤러로 전달할 응답 DTO 생성
        //오직 생성된 sessionId만 반환
        return sessionId;
    }

    @Override
    public SseEmitter establishConversationStream(String sessionId) {
        // 1. 세션 존재 여부 확인 (없으면 예외 발생)
        ConversationSession session = getSessionEntity(sessionId);

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onTimeout(() -> sseService.remove(sessionId));
        emitter.onCompletion(() -> sseService.remove(sessionId));
        emitter.onError(e -> sseService.remove(sessionId));
        sseService.register(sessionId, emitter);

        // 세션 상태를 확인하여 첫 연결인지 재연결인지 판단
        if(session.getCurrentChapterId() == null){
            // [첫 연결 시나리오]
            log.info("첫 연결입니다. 대화를 초기화합니다. SessionId: {}", sessionId);

            // 1. 대화 초기화 및 첫 질문 가져오기
            NextQuestionDto firstQuestion = initializeSession(sessionId);

            // 2. 첫 질문을 DB에 저장
            createMessage(
                    ConversationMessageRequest.builder()
                            .sessionId(sessionId)
                            .messageType(MessageType.QUESTION)
                            .content(firstQuestion.getQuestionText())
                            .build()
            );
            QuestionResponse.QuestionResponseBuilder responseBuilder = QuestionResponse.builder()
                    .text(firstQuestion.getQuestionText());
            // 3. SSE 채널을 통해 "첫 질문"을 전송
            sseService.pushQuestion(sessionId, responseBuilder.build());

        }else{
            // [재연결 시나리오]
            log.info("기존 대화에 재연결합니다. SessionId: {}", sessionId);

            // 클라이언트가 대화를 이어갈 수 있도록 마지막 질문을 다시 보내줌
            String lastQuestion = getLastQuestion(sessionId);
            if (lastQuestion != null && !lastQuestion.isEmpty()) {
                // TODO: 마지막 질문에 대한 전체 DTO를 만들어서 보내주면 더 좋습니다.
                sseService.pushQuestion(sessionId, QuestionResponse.builder().text(lastQuestion).build());
            }
        }
        return emitter;
    }

    @Override
    @Transactional
    public ConversationSessionResponse updateSession(ConversationSessionUpdateRequest request) {
        ConversationSession session = sessionRepo.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + request.getSessionId()));

        ConversationSession updated = session.toBuilder()
                .status(request.getStatus() != null ? request.getStatus() : session.getStatus())
                .templateIndex(
                        request.getTemplateIndex() != null ? request.getTemplateIndex() : session.getTemplateIndex())
                .episodeStartMessageNo(
                        request.getEpisodeStartMessageNo() != null
                                ? request.getEpisodeStartMessageNo()
                                : session.getEpisodeStartMessageNo()
                )
                .build();

        sessionRepo.save(updated);


        // 상태가 CLOSED면 레거시 큐 정리(권장)
        if (request.getStatus() == SessionStatus.CLOSE) {
            questionQueueMap.remove(request.getSessionId());
            lastQuestionMap.remove(request.getSessionId());
        }

        return ConversationSessionResponse.from(updated);
    }


    @Override
    public ConversationSessionResponse getSession(String sessionId) {
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
        return ConversationSessionResponse.from(session);
    }

    @Override
    public ConversationSession getSessionEntity(String sessionId) {
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
    }

    @Override
    @Transactional
    public ConversationMessageResponse createMessage(ConversationMessageRequest request) {
        int lastNo = messageRepo.findTopBySessionIdOrderByMessageNoDesc(request.getSessionId())
                .map(ConversationMessage::getMessageNo)
                .orElse(0);

        ConversationMessage msg = ConversationMessage.builder()
                .sessionId(request.getSessionId())
                .messageType(request.getMessageType())
                .chunkIndex(request.getChunkIndex())
                .content(request.getContent())
                .messageNo(lastNo + 1)
                .build();
        messageRepo.save(msg);

        if (request.getMessageType() == MessageType.ANSWER) {
            long add = estimateTokens(request.getContent());
            sessionRepo.findById(request.getSessionId()).ifPresent(s -> {
                ConversationSession updated = s.toBuilder()
                        .tokenCount((s.getTokenCount() == null ? 0L : s.getTokenCount()) + add)
                        // toBuilder가 알아서  복제
                        .build();
                sessionRepo.save(updated);
            });
        }
        return ConversationMessageResponse.from(msg);
    }

    @Override
    public ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request) {
        ConversationMessage msg = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + request.getMessageId()));
        msg = ConversationMessage.builder()
                .messageId(msg.getMessageId())
                .sessionId(msg.getSessionId())
                .messageType(msg.getMessageType())
                .chunkIndex(msg.getChunkIndex())
                .content(request.getContent())
                .messageNo(msg.getMessageNo())
                .build();
        messageRepo.save(msg);
        return ConversationMessageResponse.from(msg);
    }

    @Override
    public List<ConversationMessageResponse> getHistory(String sessionId) {
        return messageRepo.findBySessionIdOrderByMessageNo(sessionId).stream()
                .map(ConversationMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public String getLastAnswer(String sessionId) {
        return messageRepo
                .findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(sessionId, MessageType.ANSWER)
                .map(ConversationMessage::getContent)
                .orElse("");
    }
    @Override
    public String getLastQuestion(String sessionId) {
        return lastQuestionMap.getOrDefault(sessionId, "");
    }

    // ChapterBasedQuestion에서 가져온 코드

    @Override
    public NextQuestionDto initializeSession(String sessionId) {
        // 로직 동일
        Chapter firstChapter = chapterRepo.findByChapterOrder(1)
                .orElseThrow(() -> new IllegalStateException("첫 번째 챕터를 찾을 수 없습니다."));
        ChapterTemplate firstTemplate = templateRepo.findByChapterOrderAndTemplateOrder(1, 1)
                .orElseThrow(() -> new IllegalStateException("첫 번째 질문 템플릿을 찾을 수 없습니다."));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        ConversationSession updatedSession = session.toBuilder()
                .currentChapterId(firstChapter.getChapterId())
                .currentTemplateId(firstTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentChapterOrder(1)
                .currentTemplateOrder(1)
                .build();
        sessionRepo.save(updatedSession);

        return createNextQuestionDto(updatedSession, firstTemplate.getMainQuestion(), "MAIN", firstTemplate);
    }


    @Override
    public NextQuestionDto getNextQuestion(String sessionId, String userAnswer) {
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // 1. 원본 코드의 방식을 따라, fetch join이 적용된 메서드로 현재 템플릿을 조회합니다. (성능 최적화)
        ChapterTemplate currentTemplate = templateRepo.findByTemplateIdWithFollowUps(session.getCurrentTemplateId())
                .orElseThrow(() -> new IllegalStateException("현재 템플릿을 찾을 수 없습니다."));

        lastQuestionMap.put(sessionId, currentTemplate.getMainQuestion());

        String followUpQuestion = processFollowUpQuestions(session, currentTemplate, userAnswer);
        if (followUpQuestion != null) {
            lastQuestionMap.put(sessionId, followUpQuestion);
            return createNextQuestionDto(session, followUpQuestion, "FOLLOWUP", currentTemplate);
        }

        return moveToNextMainQuestion(session);
    }

    // sse 관련 메서드




    // == 내부 헬퍼 메서드들 (구. ChapterBasedQuestionService) ==

    private NextQuestionDto moveToNextMainQuestion(ConversationSession session) {
        ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                session.getCurrentChapterOrder(),
                session.getCurrentTemplateOrder() + 1
        ).orElse(null);

        if (nextTemplate == null) { // 챕터 종료
            return handleChapterTransition(session);
        } else { // 다음 템플릿으로
            return moveToNextTemplate(session, nextTemplate);
        }
    }

    private NextQuestionDto handleChapterTransition(ConversationSession session) {


        // 1. "방금 끝난" 챕터의 에피소드를 생성합니다.
        try {
            log.info("챕터 종료! 에피소드 생성을 시작합니다. SessionId: {}", session.getSessionId());
            episodeService.createEpisodeFromCurrentWindow(session.getBookId(), session.getSessionId());
            log.info("✅ 챕터 에피소드 생성 완료");
        } catch (Exception ex) {
            log.warn("챕터 에피소드 생성 실패(다음 챕터 진행은 계속): {}", ex.getMessage());
        }

        Chapter nextChapter = chapterRepo.findByChapterOrder(session.getCurrentChapterOrder() + 1)
                .orElse(null);

        // 모든 대화 종료
        if (nextChapter == null) {
            ConversationSession finalSession = sessionRepo.findById(session.getSessionId()).get();
            return createCompletionQuestion(finalSession);
        }

        // 다음 챕터로 넘어가기 위한 상태 초기화
        // 다음 챕터의 첫 템플릿을 찾는다.
        ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                nextChapter.getChapterOrder(), 1).orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        // 다음 에피소드의 시작점을 "현재까지의 마지막 메시지 번호 + 1"로 리셋한다.
        Integer nextEpisodeStartNo = messageRepo.findMaxMessageNo(session.getSessionId()) + 1;

        // 세션의 상트를 다음 챕터 기준으로 완전히 새로 설정하여 저장합니다.
        ConversationSession sessionForNextChapter = sessionRepo.save(
                session.toBuilder()
                        .currentChapterId(nextChapter.getChapterId())
                        .currentTemplateId(nextTemplate.getTemplateId())
                        .followUpQuestionIndex(0)
                        .currentChapterOrder(nextChapter.getChapterOrder())
                        .currentTemplateOrder(1)
                        .episodeStartMessageNo(nextEpisodeStartNo) // ★ 상태 리셋 ★
                        .build()

        );


        dynamicFollowUpQueues.remove(session.getSessionId());
        return createNextQuestionDto(sessionForNextChapter, nextTemplate.getMainQuestion(), "MAIN", nextTemplate);
    }

    private NextQuestionDto moveToNextTemplate(ConversationSession session, ChapterTemplate nextTemplate) {
        ConversationSession updatedSession = session.toBuilder()
                .currentTemplateId(nextTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentTemplateOrder(session.getCurrentTemplateOrder() + 1)
                .build();

        sessionRepo.save(updatedSession);

        dynamicFollowUpQueues.remove(session.getSessionId());
        return createNextQuestionDto(updatedSession, nextTemplate.getMainQuestion(), "MAIN", nextTemplate);
    }

//    private ConversationSession autoCreateEpisodeSafely(ConversationSession session) {
//        try {
//            log.info("에피소드 자동 생성을 시작합니다. SessionId: {}", session.getSessionId());
//            episodeService.createEpisodeFromCurrentWindow(session.getBookId(), session.getSessionId());
//            log.info("✅ 에피소드 자동 생성 완료");
//        } catch (Exception ex) {
//            log.warn("에피소드 자동 생성 실패(다음 챕터 진행은 계속): {}", ex.getMessage());
//        }
//
//        // ★ 중요: DB에 반영된 최신 상태의 세션을 다시 불러와서 반환
//        return sessionRepo.findById(session.getSessionId())
//                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));
//    }

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

        if (currentIndex < followUps.size()) {
            // 이 부분은 개선의 여지가 있지만, 일단 기존 로직을 유지합니다.
            // updateFollowUpIndex가 세션을 업데이트하고 DB에 저장하지만, 현재 컨텍스트의 'session' 객체는 변경되지 않습니다.
            updateFollowUpIndex(session, currentIndex + 1);
            return followUps.get(currentIndex).getQuestionText();
        }
        return null;
    }

    private String processDynamicFollowUp(ConversationSession session, ChapterTemplate template, String userAnswer) {
        Deque<String> dynamicQueue = dynamicFollowUpQueues.get(session.getSessionId());

        if (dynamicQueue == null && session.getFollowUpQuestionIndex() == 0) {
            String sectionKey = template.getDynamicPromptTemplate();
            String generatedQuestions = null;
            try {
                generatedQuestions = aiClient.generateDynamicFollowUpBySection(sectionKey, userAnswer);
            } catch (Exception e) {
                log.warn("동적 후속질문 생성 실패(sectionKey={}):", sectionKey, e.getMessage(), e);
            }
            dynamicQueue = parseAndCreateDynamicQueue(generatedQuestions);
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

    private void updateFollowUpIndex(ConversationSession session, int newIndex) {
        // 이 메서드는 호출한 쪽의 'session' 객체 상태를 직접 바꾸지 않으므로 주의가 필요합니다.
        ConversationSession fresh = sessionRepo.findById(session.getSessionId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        ConversationSession updated = fresh.toBuilder()
                .followUpQuestionIndex(newIndex)
                .build();
        sessionRepo.save(updated);
    }

    private NextQuestionDto createNextQuestionDto(ConversationSession session, String questionText, String questionType,
                                                  ChapterTemplate template) {
        Chapter currentChapter = chapterRepo.findById(session.getCurrentChapterId())
                .orElseThrow(() -> new IllegalStateException("현재 챕터를 찾을 수 없습니다."));

        return NextQuestionDto.builder()
                .questionText(questionText)
                .questionType(questionType)
                .currentChapterId(currentChapter.getChapterId())
                .currentChapterName(currentChapter.getChapterName())
                .currentTemplateId(template.getTemplateId())
                .currentStageName(template.getStageName())
                .chapterProgress(calculateChapterProgress(session))
                .overallProgress(calculateOverallProgress(session))
                .isLastQuestion(false)
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
                .build();
    }

    private int calculateChapterProgress(ConversationSession session) {
        List<ChapterTemplate> templatesInChapter = templateRepo.findByChapterChapterIdOrderByTemplateOrderAsc(
                session.getCurrentChapterId());
        if (templatesInChapter.isEmpty()) {
            return 0;
        }
        return (session.getCurrentTemplateOrder() * 100) / templatesInChapter.size();
    }

    private int calculateOverallProgress(ConversationSession session) {
        List<Chapter> allChapters = chapterRepo.findAllByOrderByChapterOrderAsc();
        if (allChapters.isEmpty()) {
            return 0;
        }
        return (session.getCurrentChapterOrder() * 100) / allChapters.size();
    }


    private long estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
// 한글 기준 대략 2~3자당 1토큰으로 가정
        return Math.max(1, text.length() / 3);
    }

}
