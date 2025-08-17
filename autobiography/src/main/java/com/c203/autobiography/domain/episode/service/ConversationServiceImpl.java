package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.episode.dto.ConversationMessageRequest;
import com.c203.autobiography.domain.episode.dto.ConversationMessageResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.StartConversationResponse;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
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
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final EpisodeRepository episodeRepository;
    private static final Set<String> COMMENT_PROMPT_KEYS = Set.of(
            "PROMPT_ACKNOWLEDGE_NAME",
            "PROMPT_FACTS_CONNECT"
    );

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
    @Transactional
    public String startNewConversation(Long memberId, Long bookId, Long episodeId) {
        // 1) 사용자/책/에피소드 검증 + 소유권 체크
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        if (!book.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        Episode episode = episodeRepository.findByEpisodeIdAndBookBookIdAndDeletedAtIsNull(episodeId, bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));

        String sessionId = UUID.randomUUID().toString();
        // 2. 새로운 세션 객체 생성 및 저장
        ConversationSession newSession = ConversationSession.builder()
                .sessionId(sessionId)
                .bookId(bookId) // TODO: 실제 bookId를 동적으로 할당해야 함
                .episodeId(episode.getEpisodeId())
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
    public void establishConversationStream(String sessionId, Long bookId, SseEmitter emitter) {
        // 1. 세션 존재 여부 확인 (없으면 예외 발생)
        ConversationSession session = getSessionEntity(sessionId);

        emitter.onTimeout(() -> sseService.remove(sessionId));
        emitter.onCompletion(() -> sseService.remove(sessionId));
        emitter.onError(e -> sseService.remove(sessionId));
        sseService.register(sessionId, emitter);

        processSseConnectionAsync(sessionId, bookId, emitter);

//        // 세션 상태를 확인하여 첫 연결인지 재연결인지 판단
//        if (session.getCurrentChapterId() == null) {
//            // [첫 연결 시나리오]
//            log.info("첫 연결입니다. 대화를 초기화합니다. SessionId: {}", sessionId);
//
//            // 1. 대화 초기화 및 첫 질문 가져오기
//            NextQuestionDto firstQuestion = initializeSession(sessionId, bookId);
//
//            // 2. 첫 질문을 DB에 저장
//            createMessage(
//                    ConversationMessageRequest.builder()
//                            .sessionId(sessionId)
//                            .messageType(MessageType.QUESTION)
//                            .content(firstQuestion.getQuestionText())
//                            .build()
//            );
//            QuestionResponse.QuestionResponseBuilder responseBuilder = QuestionResponse.builder()
//                    .text(firstQuestion.getQuestionText());
//            // 3. SSE 채널을 통해 "첫 질문"을 전송
//            sseService.pushQuestion(sessionId, responseBuilder.build());
//
//        } else {
//            // [재연결 시나리오]
//            log.info("기존 대화에 재연결합니다. SessionId: {}", sessionId);
//
//            // 클라이언트가 대화를 이어갈 수 있도록 마지막 질문을 다시 보내줌
//            String lastQuestion = getLastQuestion(sessionId);
//            if (lastQuestion != null && !lastQuestion.isEmpty()) {
//                // TODO: 마지막 질문에 대한 전체 DTO를 만들어서 보내주면 더 좋습니다.
//                log.info("퀘스천리스판스 : {}",String.valueOf(QuestionResponse.builder().text(lastQuestion).build()));
//                sseService.pushQuestion(sessionId, QuestionResponse.builder().text(lastQuestion).build());
//            }
//        }

    }

    @Override
    @Transactional
    public NextQuestionDto skipCurrentQuestion(Long memberId, Long bookId, Long episodeId, String sessionId) {
        // 0) 세션/권한 검증 (기존 getNextQuestion와 동일한 검증 절차)
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        Episode episode = episodeRepository.findByEpisodeIdAndDeletedAtIsNull(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // 2) 다음 메인 템플릿으로 전진 (없다면 챕터 종료 처리)
        ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                session.getCurrentChapterOrder(),
                session.getCurrentTemplateOrder() + 1
        ).orElse(null);


        // 2) 다음 템플릿이 없다면, 현재가 마지막 질문이므로 예외를 발생시킵니다.
        if (nextTemplate == null) {
            throw new ApiException(ErrorCode.CANNOT_SKIP_LAST_QUESTION);
        }


        // 1) 화면에 떠 있던 '마지막 QUESTION'은 DB에서 제거
        messageRepo.findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(sessionId, MessageType.QUESTION)
                .ifPresent(messageRepo::delete);

        // 팔로업 중이었다면 다 버리고, 다음 메인으로 가야 하므로 동적 큐/인덱스 초기화
        dynamicFollowUpQueues.remove(sessionId);
        updateFollowUpIndex(session, 0); // 내부에서 fresh 로딩해 저장

        //다음 메인 템플릿으로 상태를 전진
        NextQuestionDto nextQuestionDto = moveToNextTemplate(session, nextTemplate);

        // 3) 새 질문을 DB에 '저장' + SSE 푸시 (여기서 저장되는 건 '건너뛰기 후의 질문'뿐)
        createMessage(
                ConversationMessageRequest.builder()
                        .sessionId(sessionId)
                        .messageType(MessageType.QUESTION)
                        .content(nextQuestionDto.getQuestionText())
                        .build()
        );

        QuestionResponse response = QuestionResponse.builder()
                .text(nextQuestionDto.getQuestionText())
                .questionType(nextQuestionDto.getQuestionType())
                .currentChapterName(nextQuestionDto.getCurrentChapterName())
                .currentStageName(nextQuestionDto.getCurrentStageName())
                .chapterProgress(nextQuestionDto.getChapterProgress())
                .overallProgress(nextQuestionDto.getOverallProgress())
                .isLastQuestion(nextQuestionDto.isLastQuestion())
                .build();
        sseService.pushQuestion(sessionId, response);

        return nextQuestionDto;
    }

    @Async
    public void processSseConnectionAsync(String sessionId, Long bookId, SseEmitter emitter) {
        try {
            ConversationSession session = getSessionEntity(sessionId); // 여기서는 트랜잭션 없이 단순 조회
            // 1. 세션의 상태(status)를 가장 먼저 확인합니다.
            if (session.getStatus() == SessionStatus.CLOSE) {
                log.warn("이미 종료된 세션(SessionId: {})에 대한 SSE 연결 시도입니다. 연결을 거부하고 종료합니다.", sessionId);

                // 2. 프론트엔드에 '이미 완료된 세션'임을 알리는 에러 이벤트를 보냅니다.
                //    이를 통해 프론트엔드는 오래된 세션 ID를 사용하지 않고 새 인터뷰를 시작하도록 유도할 수 있습니다.
                emitter.send(SseEmitter.event().name("error").data("이미 완료된 인터뷰 세션입니다. 새로운 인터뷰를 시작해주세요."));

                // 3. 서버 측에서도 SSE 연결을 즉시 종료합니다.
                emitter.complete();

                // 4. 더 이상 진행하지 않고 메소드를 종료합니다.
                return;
            }

            if (session.getCurrentChapterId() == null) {
                // 첫 연결 시나리오: DB 작업은 별도의 트랜잭션 메소드에서 수행
                initializeAndPushFirstQuestion(sessionId, bookId);
            } else {
                // 재연결 시나리오: DB 작업은 별도의 트랜잭션 메소드에서 수행
                pushLastQuestion(sessionId);
            }
        } catch (Exception e) {
            log.error("SSE 비동기 처리 중 에러 발생, sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event().name("error").data("대화 처리 중 오류 발생: " + e.getMessage()));
            } catch (IOException ex) {
                log.warn("SSE 에러 이벤트 전송 실패", ex);
            }
            emitter.completeWithError(e);
        }
    }

    /**
     * [신규] 첫 질문 생성 및 전송을 담당하는 트랜잭션 메소드
     */
    @Transactional
    public void initializeAndPushFirstQuestion(String sessionId, Long bookId) {
        log.info("첫 연결 대화 초기화 및 질문 전송. SessionId: {}", sessionId);
        NextQuestionDto firstQuestion = initializeSession(sessionId, bookId);
        createMessage(
                ConversationMessageRequest.builder()
                        .sessionId(sessionId)
                        .messageType(MessageType.QUESTION)
                        .content(firstQuestion.getQuestionText())
                        .build()
        );
        // ... (QuestionResponse 빌드 및 sseService.pushQuestion 호출)
        QuestionResponse response = QuestionResponse.builder()
                .text(firstQuestion.getQuestionText())
                .currentChapterName(firstQuestion.getCurrentChapterName())
                // ... 등등
                .build();
        sseService.pushQuestion(sessionId, response);
        log.info("질문 전송 {}", response);
    }

    /**
     * [신규] 마지막 질문 전송을 담당하는 읽기 전용 트랜잭션 메소드
     */
    @Transactional(readOnly = true)
    public void pushLastQuestion(String sessionId) {
        log.info("기존 대화 재연결, 마지막 질문 전송. SessionId: {}", sessionId);
        String lastQuestion = getLastQuestion(sessionId); // DB 조회
        if (lastQuestion != null && !lastQuestion.isEmpty()) {
            sseService.pushQuestion(sessionId, QuestionResponse.builder().text(lastQuestion).build());
        }
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
    @Transactional
    public ConversationMessageResponse updateMessage(ConversationMessageUpdateRequest request) {
        ConversationMessage msg = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new IllegalArgumentException("메시지를 찾을 수 없습니다: " + request.getMessageId()));
        msg.updateContent(request.getContent());
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
        // 세션 ID와 메시지 타입이 'QUESTION'인 메시지들 중에서
        // messageNo가 가장 큰(가장 최신인) 메시지 1개를 찾아서 내용을 반환합니다.
        return messageRepo
                .findFirstBySessionIdAndMessageTypeOrderByMessageNoDesc(sessionId, MessageType.QUESTION)
                .map(ConversationMessage::getContent)
                .orElse(null); // 마지막 질문이 없으면 null 반환
    }

    // ChapterBasedQuestion에서 가져온 코드

    @Override
    public NextQuestionDto initializeSession(String sessionId, Long bookId) {
        // 로직 동일
        long completedChapterCount = episodeRepository.countByBookBookIdAndContentIsNotNullAndContentIsNot(bookId, "");
        int nextChapterOrder = (int) completedChapterCount + 1;

        log.info("새 인터뷰 시작. 완료된 챕터 수: {}, 다음 챕터: {}", completedChapterCount, nextChapterOrder);

        Chapter firstChapter = chapterRepo.findByChapterOrder(nextChapterOrder)
                .orElseThrow(() -> new IllegalStateException("첫 번째 챕터를 찾을 수 없습니다."));
        ChapterTemplate firstTemplate = templateRepo.findByChapterOrderAndTemplateOrder(nextChapterOrder, 1)
                .orElseThrow(() -> new IllegalStateException("첫 번째 질문 템플릿을 찾을 수 없습니다."));

        ConversationSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));

        // 1. 현재 세션의 최대 메시지 번호를 조회합니다.
        Integer maxMessageNo = messageRepo.findMaxMessageNo(sessionId);

        // 2. maxMessageNo가 null이면 (메시지가 없으면) 1을, 아니면 maxMessageNo + 1을 사용합니다.
        Integer nextEpisodeStartNo = (maxMessageNo == null) ? 1 : maxMessageNo + 1;

        // 세션을 새로 시작하는 챕터의 정보로 업데이트
        ConversationSession updatedSession = session.toBuilder()
                .currentChapterId(firstChapter.getChapterId())
                .currentTemplateId(firstTemplate.getTemplateId())
                .followUpQuestionIndex(0)
                .currentChapterOrder(firstChapter.getChapterOrder())
                .currentTemplateOrder(1)
                .episodeStartMessageNo(nextEpisodeStartNo) // 새 시작점
                .build();
        sessionRepo.save(updatedSession);

        return createNextQuestionDto(updatedSession, firstTemplate.getMainQuestion(), "MAIN", firstTemplate);
    }


    @Override
    public NextQuestionDto getNextQuestion(Long memberId, Long bookId, Long episodeId, String sessionId,
                                           String userAnswer) {
        // 1) 사용자 확인
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2) 책 확인
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        Episode episode = episodeRepository.findByEpisodeIdAndDeletedAtIsNull(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));

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

        return moveToNextMainQuestion(member, book, episode, session);
    }

    // sse 관련 메서드

    // == 내부 헬퍼 메서드들 (구. ChapterBasedQuestionService) ==

    private @Nullable NextQuestionDto moveToNextMainQuestion(Member member, Book book, Episode episode,
                                                             ConversationSession session) {
        ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                session.getCurrentChapterOrder(),
                session.getCurrentTemplateOrder() + 1
        ).orElse(null);

        if (nextTemplate == null) { // 챕터 종료
            return handleChapterTransition(member, book, episode, session);
        } else { // 다음 템플릿으로
            return moveToNextTemplate(session, nextTemplate);
        }
    }

    private NextQuestionDto handleChapterTransition(Member member, Book book, Episode episode,
                                                    ConversationSession session) {

        // 1. "방금 끝난" 챕터의 에피소드를 생성합니다.
        try {
            log.info("챕터 종료! 에피소드 생성을 시작합니다. SessionId: {}", session.getSessionId());
            EpisodeResponse savedEpisode = episodeService.createEpisodeFromCurrentWindow(episode,
                    session.getSessionId());
            log.info("✅ 챕터 에피소드 생성 완료");
            sseService.pushEpisode(session.getSessionId(), savedEpisode);
            log.info("에피소드 생성 완료. content: {}", savedEpisode.getContent());
        } catch (Exception ex) {
            log.warn("챕터 에피소드 생성 실패(다음 챕터 진행은 계속): {}", ex.getMessage());
        }

        log.info("인터뷰 세션을 종료 처리합니다. SessionId: {}", session.getSessionId());
        ConversationSession updatedSession = session.toBuilder()
                .status(SessionStatus.CLOSE) // 상태를 CLOSED로 변경
                .build();
        sessionRepo.save(updatedSession);

        Chapter nextChapter = chapterRepo.findByChapterOrder(session.getCurrentChapterOrder() + 1)
                .orElse(null);

        Chapter currentChapter = chapterRepo.findById(session.getCurrentChapterId())
                .orElseThrow(() -> new IllegalStateException("현재 챕터 정보를 찾을 수 없습니다: " + session.getCurrentChapterId()));

        // 모든 대화 종료
        if (nextChapter != null) {
            // 다음 질문 대신, 인터뷰가 한 단락 끝났음을 알리는 특별한 DTO를 반환합니다.
            String completionMessage = String.format(
                    "챕터 %d이(가) 완료되었습니다. 왼쪽 목차에 새 에피소드를 추가하고 'AI 인터뷰 시작'을 눌러 다음 챕터를 시작해주세요.",
                    session.getCurrentChapterOrder()
            );

            QuestionResponse chapterCompleteResponse = QuestionResponse.builder()
                    .text(completionMessage)
                    .questionType("CHAPTER_COMPLETE")
                    .isLastQuestion(true)
                    .currentChapterName(currentChapter.getChapterName())
                    .currentStageName("챕터 완료")
                    .chapterProgress(100)
                    .overallProgress(calculateOverallProgress(session))
                    .build();
            sseService.pushQuestion(session.getSessionId(), chapterCompleteResponse);
            return null;
        } else {
            // 모든 챕터가 끝났을 때의 로직은 그대로 유지
            ConversationSession finalSession = sessionRepo.findById(session.getSessionId()).get();
            return createCompletionQuestion(finalSession);
        }


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
        String sectionKey = template.getDynamicPromptTemplate();

        // AI에게 '어떤 재료'를 줄지 결정하기 위해 키를 확인합니다.
        if (COMMENT_PROMPT_KEYS.contains(sectionKey)) {
            // --- 재료: [사용자 답변] + [다음 본 질문] ---

            ChapterTemplate nextTemplate = templateRepo.findByChapterOrderAndTemplateOrder(
                    session.getCurrentChapterOrder(),
                    session.getCurrentTemplateOrder() + 1
            ).orElse(null);

            if (nextTemplate == null) {
                return null; // 다음 질문 없으면 종료
            }

            String combinedResponse = aiClient.generateDynamicFollowUpBySection(
                    sectionKey,
                    userAnswer,
                    nextTemplate.getMainQuestion()
            );

            // ★ AI가 다음 질문까지 만들어줬으니, 우리 시스템의 상태도 다음으로 넘겨줍니다.
            advanceSessionToTemplate(session, nextTemplate);

            return combinedResponse; // "코멘트\n다음 본 질문" 텍스트를 반환

        } else {
            // --- 재료: [사용자 답변] --- (기존 로직)
            Deque<String> dynamicQueue = dynamicFollowUpQueues.get(session.getSessionId());

            if (dynamicQueue == null && session.getFollowUpQuestionIndex() == 0) {
                String generatedQuestions = aiClient.generateDynamicFollowUpBySection(sectionKey, userAnswer, null);
                dynamicQueue = parseAndCreateDynamicQueue(generatedQuestions);
                dynamicFollowUpQueues.put(session.getSessionId(), dynamicQueue);
            }

            if (dynamicQueue != null && !dynamicQueue.isEmpty()) {
                updateFollowUpIndex(session, session.getFollowUpQuestionIndex() + 1);
                return dynamicQueue.poll();
            }

            return null;
        }
    }

    /**
     * 세션 상태를 다음 템플릿으로 업데이트하는 헬퍼 메소드
     */
    private void advanceSessionToTemplate(ConversationSession session, ChapterTemplate nextTemplate) {
        ConversationSession freshSession = sessionRepo.findById(session.getSessionId())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));

        ConversationSession updatedSession = freshSession.toBuilder()
                .currentTemplateId(nextTemplate.getTemplateId())
                .currentTemplateOrder(nextTemplate.getTemplateOrder())
                .followUpQuestionIndex(0)
                .build();

        sessionRepo.save(updatedSession);
        dynamicFollowUpQueues.remove(session.getSessionId());
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
