package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.domain.group.service.GroupMemberService;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupType;
import com.c203.autobiography.domain.groupbook.episode.dto.*;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeGuideState;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeStatus;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeImage;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeImageId;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeGuideStateRepository;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeImageRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import com.c203.autobiography.domain.sse.service.SseService;
import com.c203.autobiography.domain.episode.template.dto.QuestionResponse;
import com.c203.autobiography.domain.ai.client.AiClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Getter
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GroupEpisodeServiceImpl implements GroupEpisodeService {


    private final GroupBookRepository groupBookRepository;
    private final GroupEpisodeRepository episodeRepository;
    private final GroupEpisodeGuideStateRepository stateRepository;
    private final GroupEpisodeImageRepository imageRepository;
    private final GuideResolverService guideResolver;
    private final EditorService editorService;
    private final FileStorageService fileStorageService;
    private final SseService sseService;
    private final AiClient aiClient;
    private final GroupMemberService groupMemberService;

    // 대화 세션 관리를 위한 메모리 저장소
    private final ConcurrentHashMap<String, GroupConversationSession> activeSessions = new ConcurrentHashMap<>();

    // 그룹 대화 세션 내부 클래스
    private static class GroupConversationSession {
        private final String sessionId;
        private final Long memberId;
        private final Long groupId;
        private final Long groupBookId;
        private Long currentEpisodeId; // 현재 활성 에피소드 ID (변경 가능)
        private final GroupType groupType;
        private String currentTemplate;
        private int currentStep;

        public GroupConversationSession(String sessionId, Long memberId, Long groupId, 
                                      Long groupBookId, Long initialEpisodeId, GroupType groupType, String initialTemplate) {
            this.sessionId = sessionId;
            this.memberId = memberId;
            this.groupId = groupId;
            this.groupBookId = groupBookId;
            this.currentEpisodeId = initialEpisodeId;
            this.groupType = groupType;
            this.currentTemplate = initialTemplate;
            this.currentStep = 0;
        }

        // getters
        public String getSessionId() { return sessionId; }
        public Long getMemberId() { return memberId; }
        public Long getGroupId() { return groupId; }
        public Long getGroupBookId() { return groupBookId; }
        public Long getCurrentEpisodeId() { return currentEpisodeId; } // 이름 변경
        public GroupType getGroupType() { return groupType; }
        public String getCurrentTemplate() { return currentTemplate; }
        public int getCurrentStep() { return currentStep; }

        public void nextStep() { this.currentStep++; }
    }


    @Override @Transactional
    public GroupEpisodeResponse create(Long groupId, Long groupBookId, GroupEpisodeCreateRequest req, Long memberId) {
        // 1. 그룹책 확인
        GroupBook gb = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 2. 그룹 멤버 권한 확인 (필요시 추가)
        
        // 3. 순서 번호 계산
        Integer orderNo = (req.getOrderNo() == null)
                ? (int) (episodeRepository.findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(groupBookId).size() + 1)
                : req.getOrderNo();

        // 4. 간단한 에피소드 생성 (template는 기본값 INTRO 사용)
        GroupEpisode ep = episodeRepository.save(
            GroupEpisode.toEntity(gb, req.getTitle(), orderNo, "INTRO")
        );

        // 5. 간단한 응답 반환
        return GroupEpisodeResponse.of(ep);
    }

    // 🔧 현재 에피소드의 템플릿을 guide_key에서 추출하는 메서드
    private String getCurrentTemplateFromGuideStates(Long episodeId) {
        List<GroupEpisodeGuideState> states = stateRepository
                .findByGroupEpisode_GroupEpisodeIdOrderByStepNoAsc(episodeId);

        if (!states.isEmpty()) {
            String guideKey = states.get(0).getGuideKey(); // 첫 번째 guide_key 확인
            // "STORY_FRIENDS_1" → "STORY" 추출
            if (guideKey != null && guideKey.contains("_")) {
                return guideKey.split("_")[0]; // STORY, INTRO, REFLECTION 등
            }
        }

        return "STORY"; // 기본값
    }

    @Override @Transactional
    public StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        GroupBook gb = ep.getGroupBook();
        if (ep.getStatus() == GroupEpisodeStatus.DRAFT) ep.setStatus(GroupEpisodeStatus.IN_PROGRESS);

        int currentStepNo = (req.getLastStepNo() == null ? 1 : req.getLastStepNo());

        // 1) 현재 스텝 사용자 답변을 편집
        String editedParagraph = editorService.polish(req.getUserAnswer(), ep.getEditedContent(), "FORMAL");

        // 2) 현재 스텝의 가이드 상태 업데이트 (사용자 답변과 편집 결과 저장)
        Optional<GroupEpisodeGuideState> currentStateOpt = stateRepository
                .findByGroupEpisode_GroupEpisodeIdAndStepNo(episodeId, currentStepNo);

        if(currentStateOpt.isPresent()) {
            GroupEpisodeGuideState currentState = currentStateOpt.get();
            // 기존 상태 업데이트
            GroupEpisodeGuideState updatedState = GroupEpisodeGuideState.builder()
                    .id(currentState.getId())
                    .groupEpisode(ep)
                    .stepNo(currentStepNo)
                    .guideKey(currentState.getGuideKey())
                    .question(currentState.getQuestion())
                    .userAnswer(req.getUserAnswer())
                    .editedParagraph(editedParagraph)
                    .isFinal(true)
                    .build();
            stateRepository.save(updatedState);
        }

        // 3) 에피소드의 편집된 내용 추가
        ep.appendEdited(editedParagraph);
        ep.nextStep();

        // 4) 다음 질문 결정
        int nextStepNo = currentStepNo + 1;
        String currentTemplate = getCurrentTemplateFromGuideStates(episodeId);
        Optional<GuideQuestion> nextQuestionOpt = guideResolver.resolveNext(
                gb.getGroupType(),
                currentTemplate,
                currentStepNo,
                req.getUserAnswer()
        );

        boolean completed = nextQuestionOpt.isEmpty();

        if(completed) {
            ep.setStatus(GroupEpisodeStatus.REVIEW);
        } else {
            // 다음 질문을 위한 가이드 생성
            GuideQuestion nextQuestion = nextQuestionOpt.get();
            GroupEpisodeGuideState nextState = GroupEpisodeGuideState.builder()
                    .groupEpisode(ep)
                    .stepNo(nextStepNo)
                    .guideKey(nextQuestion.key())
                    .question(nextQuestion.question())
                    .userAnswer(null)
                    .editedParagraph(null)
                    .isFinal(false)
                    .build();
            stateRepository.save(nextState);
        }

        return StepNextResponse.builder()
                .stepNo(currentStepNo)
                .editedParagraph(editedParagraph)
                .question(completed ? null : nextQuestionOpt.get().question())
                .completed(completed)
                .build();
    }

    @Override
    public GroupEpisodeResponse get(Long groupId, Long groupBookId, Long episodeId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        return GroupEpisodeResponse.of(ep);
    }

    @Override @Transactional
    public GroupEpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        ep.setStatus(GroupEpisodeStatus.COMPLETE);

        // 🎯 다음 에피소드를 위한 질문 준비
        GroupBook groupBook = ep.getGroupBook();
        String nextTemplate = determineNextTemplate(ep.getTemplate()); // INTRO → STORY
        GuideQuestion nextQuestion = guideResolver.resolveFirst(groupBook.getGroupType(), nextTemplate);
        String recommendedTitle = generateRecommendedTitle(nextTemplate, groupBook.getGroupType());

        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .currentQuestion(null) // 완료된 에피소드는 질문 없음
                // 🎯 다음 에피소드 정보
                .nextEpisodeQuestion(nextQuestion.question())
                .nextQuestionKey(nextQuestion.key())
                .recommendedTemplate(nextTemplate)
                .recommendedTitle(recommendedTitle)
                .build();
    }

    private String determineNextTemplate(String currentTemplate) {
        // 템플릿 순서: INTRO → STORY → REFLECTION → OUTRO
        return switch (currentTemplate) {
            case "INTRO" -> "STORY";
            case "STORY" -> "REFLECTION";
            case "REFLECTION" -> "OUTRO";
            default -> "STORY";
        };
    }

    private String generateRecommendedTitle(String template, GroupType groupType) {
        return switch (template) {
            case "STORY" -> switch (groupType) {
                case FRIENDS -> "기억에 남는 순간";
                case FAMILY -> "특별한 하루";
                case COUPLE -> "소중한 추억";
                case TEAM -> "함께한 도전";
                default -> "우리의 이야기";
            };
            case "REFLECTION" -> "돌아보며";
            case "OUTRO" -> "앞으로의 다짐";
            default -> "다음 이야기";
        };
    }

    @Override
    public List<GroupEpisodeResponse> getEpisodeList(Long groupId, Long groupBookId, Long memberId) {
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new IllegalArgumentException("GroupBook not found: " + groupBookId));

        List<GroupEpisode> episodes = episodeRepository.findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(groupBookId);

        return episodes.stream()
                .map(GroupEpisodeResponse::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupEpisodeResponse update(Long groupId, Long groupBookId, Long episodeId, GroupEpisodeUpdateRequest request, Long memberId) {
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new IllegalArgumentException("GroupBook not found: " + groupBookId));

        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));

        // 에피소드가 해당 그룹책에 속하는지 확인
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new IllegalArgumentException("에피소드가 해당 그룹책에 속하지 않습니다.");
        }

        episode.updateEpisodeWithContent(
                request.getTitle(),
                request.getOrderNo(),
                request.getTemplate(),
                request.getEditedContent()
        );

        return GroupEpisodeResponse.of(episode);
    }

    @Override
    @Transactional
    public void delete(Long groupId, Long groupBookId, Long episodeId, Long memberId) {
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new IllegalArgumentException("GroupBook not found: " + groupBookId));

        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));

        // 에피소드가 해당 그룹책에 속하는지 확인
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new IllegalArgumentException("에피소드가 해당 그룹책에 속하지 않습니다.");
        }

        episode.softDelete();
    }

    // ======== 이미지 관련 메서드 ========

    @Override
    @Transactional
    public GroupEpisodeImageResponse uploadImage(Long groupId, Long groupBookId, Long episodeId, 
                                               MultipartFile file, GroupEpisodeImageUploadRequest request, Long memberId) {
        // 1. 그룹책 및 에피소드 존재 확인
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        
        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        
        // 2. 에피소드가 해당 그룹책에 속하는지 확인
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new ApiException(ErrorCode.EPISODE_NOT_FOUND);
        }
        
        // 3. 파일 업로드
        String imageUrl = fileStorageService.store(file, "group-episode");
        
        // 4. 순서 번호 결정 (요청에 없으면 자동 부여)
        Integer orderNo = request.getOrderNo();
        if (orderNo == null) {
            Integer maxOrder = imageRepository.findMaxOrderNoByGroupEpisodeId(episodeId);
            orderNo = (maxOrder == null ? 0 : maxOrder) + 1;
        }
        
        // 5. 이미지 ID 생성 (타임스탬프 기반)
        Long imageId = System.currentTimeMillis();
        
        // 6. 이미지 엔티티 생성 및 저장
        GroupEpisodeImage image = GroupEpisodeImage.create(
                episode, imageId, imageUrl, orderNo, request.getDescription());
        
        GroupEpisodeImage savedImage = imageRepository.save(image);
        
        return GroupEpisodeImageResponse.from(savedImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupEpisodeImageResponse> getImages(Long groupId, Long groupBookId, Long episodeId, Long memberId) {
        // 1. 그룹책 및 에피소드 존재 확인
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        
        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        
        // 2. 에피소드가 해당 그룹책에 속하는지 확인
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new ApiException(ErrorCode.EPISODE_NOT_FOUND);
        }
        
        // 3. 이미지 목록 조회
        List<GroupEpisodeImage> images = imageRepository
                .findByGroupEpisode_GroupEpisodeIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(episodeId);
        
        return images.stream()
                .map(GroupEpisodeImageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteImage(Long groupId, Long groupBookId, Long episodeId, Long imageId, Long memberId) {
        // 1. 그룹책 및 에피소드 존재 확인
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        
        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        
        // 2. 에피소드가 해당 그룹책에 속하는지 확인
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new ApiException(ErrorCode.EPISODE_NOT_FOUND);
        }
        
        // 3. 이미지 조회 및 삭제
        GroupEpisodeImageId imageEntityId = GroupEpisodeImageId.of(episodeId, imageId);
        GroupEpisodeImage image = imageRepository.findByIdAndDeletedAtIsNull(imageEntityId)
                .orElseThrow(() -> new ApiException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 4. 소프트 삭제
        image.softDelete();
    }

    // ========== 대화 세션 관련 메서드 (개인 book 로직과 동일) ==========

    @Override
    public String startNewConversation(Long memberId, Long groupId, Long groupBookId, Long episodeId) {
        // 1. 그룹책 및 에피소드 검증
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        
        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new ApiException(ErrorCode.EPISODE_NOT_FOUND);
        }

        // 2. 세션 ID 생성
        String sessionId = UUID.randomUUID().toString();
        
        // 3. 에피소드의 현재 템플릿 가져오기
        String currentTemplate = episode.getTemplate() != null ? episode.getTemplate() : "INTRO";
        
        // 4. 세션 생성 및 저장
        GroupConversationSession session = new GroupConversationSession(
                sessionId, memberId, groupId, groupBookId, episodeId, groupBook.getGroupType(), currentTemplate
        );
        activeSessions.put(sessionId, session);
        
        log.info("새로운 그룹 대화 세션 생성: sessionId={}, groupId={}, groupBookId={}, episodeId={}", 
                sessionId, groupId, groupBookId, episodeId);
        
        return sessionId;
    }

    @Override
    public SseEmitter establishConversationStream(String sessionId, Long groupId, Long groupBookId, Long episodeId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        try {
            // 1. 세션 확인
            GroupConversationSession session = activeSessions.get(sessionId);
            if (session == null) {
                throw new ApiException(ErrorCode
                        .SESSION_NOT_FOUND);
            }
            
            // 2. SSE 서비스에 등록
            sseService.register(sessionId, emitter);
            
            // 3. 첫 번째 질문 전송
            GuideQuestion firstQuestion = guideResolver.resolveFirst(session.getGroupType(), session.getCurrentTemplate());
            
            // 4. 클라이언트에게 연결 성공 메시지 전송
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("대화 세션이 연결되었습니다."));
            
            // 5. 첫 질문 전송
            QuestionResponse questionResponse = QuestionResponse.builder()
                    .text(firstQuestion.question())
                    .currentChapter(session.getCurrentTemplate())
                    .currentStage("step-" + session.getCurrentStep())
                    .build();
                    
            sseService.pushQuestion(sessionId, questionResponse);
            
            log.info("SSE 스트림 연결 및 첫 질문 전송 완료: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("SSE 스트림 설정 중 에러 발생. sessionId={}", sessionId, e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("스트림 연결 중 서버 오류가 발생했습니다: " + e.getMessage()));
            } catch (IOException ex) {
                log.warn("SSE 에러 이벤트 전송 실패. sessionId={}", sessionId, ex);
            }
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    @Override
    public void getNextQuestion(Long memberId, Long groupId, Long groupBookId, Long episodeId, String sessionId) {
        try {
            // 1. 세션 확인
            GroupConversationSession session = activeSessions.get(sessionId);
            if (session == null) {
                throw new ApiException(ErrorCode.SESSION_NOT_FOUND);
            }
            
            // 2. 다음 질문 생성
            session.nextStep();
            
            Optional<GuideQuestion> nextQuestion = guideResolver.resolveNext(
                    session.getGroupType(), 
                    session.getCurrentTemplate(), 
                    session.getCurrentStep(), 
                    "" // 마지막 답변 (필요시 구현)
            );
            
            if (nextQuestion.isPresent()) {
                // 3. 질문이 있으면 SSE로 전송
                GuideQuestion question = nextQuestion.get();
                QuestionResponse questionResponse = QuestionResponse.builder()
                        .text(question.question())
                        .currentChapter(session.getCurrentTemplate())
                        .currentStage("step-" + session.getCurrentStep())
                        .build();
                        
                sseService.pushQuestion(sessionId, questionResponse);
                
                log.info("다음 질문 전송 완료: sessionId={}, step={}", sessionId, session.getCurrentStep());
                
            } else {
                // 4. 질문이 없으면 템플릿 완료 상태로 처리
                String nextTemplate = getNextTemplate(session.getCurrentTemplate());
                if (nextTemplate != null) {
                    // 템플릿 완료 메시지 전송 (자동 생성하지 않고 사용자 액션 대기)
                    QuestionResponse templateCompleteResponse = QuestionResponse.builder()
                            .text("'" + getTemplateKoreanName(session.getCurrentTemplate()) + "' 주제에 대한 질문이 완료되었습니다. 지금까지의 답변을 확인하고 저장해주세요.")
                            .currentChapter(session.getCurrentTemplate())
                            .currentStage("template_completed")
                            .isTemplateCompleted(true)
                            .build();
                            
                    sseService.pushQuestion(sessionId, templateCompleteResponse);
                    
                    log.info("템플릿 완료 - 사용자 액션 대기: sessionId={}, template={}, nextTemplate={}", 
                            sessionId, session.getCurrentTemplate(), nextTemplate);
                    
                } else {
                    // 5. 모든 질문 완료 - QuestionResponse로 완료 메시지 전송
                    QuestionResponse completeResponse = QuestionResponse.builder()
                            .text("모든 질문이 완료되었습니다. 수고하셨습니다!")
                            .currentChapter("COMPLETE")
                            .currentStage("finished")
                            .isLastQuestion(true)
                            .build();
                            
                    sseService.pushQuestion(sessionId, completeResponse);
                    log.info("모든 질문 완료: sessionId={}", sessionId);
                }
            }
            
        } catch (Exception e) {
            log.error("다음 질문 생성 중 오류 발생: sessionId={}", sessionId, e);
            // 에러도 QuestionResponse로 전송
            QuestionResponse errorResponse = QuestionResponse.builder()
                    .text("질문 생성 중 오류가 발생했습니다: " + e.getMessage())
                    .currentChapter("ERROR")
                    .currentStage("error")
                    .build();
            sseService.pushQuestion(sessionId, errorResponse);
        }
    }

    @Override
    public void closeSseStream(String sessionId) {
        try {
            // 1. SSE 연결 종료
            sseService.closeConnection(sessionId);
            
            // 2. 세션 정리
            activeSessions.remove(sessionId);
            
            log.info("SSE 스트림 연결 종료: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("SSE 스트림 종료 중 오류 발생: sessionId={}", sessionId, e);
        }
    }

    @Override
    @Transactional
    public void submitAnswer(Long memberId, Long groupId, Long groupBookId, Long episodeId, String sessionId, GroupAnswerRequest request) {
        try {
            log.info("사용자 답변 제출 시작: sessionId={}, memberId={}", sessionId, memberId);
            
            // 1. 세션 확인
            GroupConversationSession session = activeSessions.get(sessionId);
            if (session == null) {
                throw new ApiException(ErrorCode.SESSION_NOT_FOUND);
            }
            
            // 2. 세션 소유자 확인
            if (!session.getMemberId().equals(memberId)) {
                throw new ApiException(ErrorCode.FORBIDDEN);
            }
            
            // 3. 세션의 현재 활성 에피소드 조회 (템플릿별 분리)
            Long currentEpisodeId = session.getCurrentEpisodeId();
            GroupEpisode episode = episodeRepository.findById(currentEpisodeId)
                    .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
                    
            log.info("답변 저장 대상 에피소드: sessionEpisodeId={}, currentTemplate={}", 
                    currentEpisodeId, session.getCurrentTemplate());
            
            // 4. AI를 통한 답변 교정
            String originalAnswer = request.getAnswer();
            String correctedAnswer;
            
            try {
                // 이전 컨텍스트 (현재 에피소드의 기존 내용)
                String priorContext = episode.getEditedContent() != null ? episode.getEditedContent() : "";
                
                // AI로 답변 교정 (tone을 FORMAL로 고정, 필요시 요청에서 받을 수 있음)
                correctedAnswer = aiClient.editText(originalAnswer, priorContext, "FORMAL");
                
                log.info("AI 답변 교정 완료: sessionId={}, 원본 길이={}, 교정본 길이={}", 
                        sessionId, originalAnswer.length(), correctedAnswer.length());
                        
            } catch (Exception e) {
                log.warn("AI 답변 교정 실패, 원본 답변 사용: sessionId={}, error={}", sessionId, e.getMessage());
                correctedAnswer = originalAnswer;
            }
            
            // 5. 현재 질문 텍스트 가져오기
            String currentQuestionText = getCurrentQuestionText(session);
            
            // 6. 현재 단계의 가이드 상태 저장/업데이트
            String currentQuestionKey = generateQuestionKey(session.getCurrentTemplate(), session.getCurrentStep());
            
            // 기존 답변이 있는지 확인 (현재 템플릿의 에피소드에서)
            Optional<GroupEpisodeGuideState> existingState = stateRepository
                    .findByGroupEpisode_GroupEpisodeIdAndGuideKey(currentEpisodeId, currentQuestionKey);
            
            GroupEpisodeGuideState guideState;
            if (existingState.isPresent()) {
                // 기존 답변 업데이트
                guideState = existingState.get();
                guideState.setUserAnswer(originalAnswer);
                guideState.setEditedAnswer(correctedAnswer);
                log.info("기존 답변 업데이트: questionKey={}", currentQuestionKey);
            } else {
                // 새 답변 생성
                guideState = GroupEpisodeGuideState.builder()
                        .groupEpisode(episode)
                        .guideKey(currentQuestionKey)
                        .stepNo(session.getCurrentStep())
                        .question(currentQuestionText) // 질문 텍스트 추가
                        .userAnswer(originalAnswer)
                        .editedAnswer(correctedAnswer)
                        .isFinal(false) // 기본값 설정
                        .build();
                log.info("새 답변 생성: questionKey={}, question={}", currentQuestionKey, 
                        currentQuestionText.length() > 50 ? currentQuestionText.substring(0, 50) + "..." : currentQuestionText);
            }
            
            stateRepository.save(guideState);
            
            // 6. 에피소드 전체 내용 업데이트 (모든 교정된 답변들을 합침)
            updateEpisodeContent(episode);
            
            // 7. SSE로 답변 처리 완료 알림
            QuestionResponse responseNotification = QuestionResponse.builder()
                    .text("답변이 성공적으로 저장되었습니다.")
                    .currentChapter(session.getCurrentTemplate())
                    .currentStage("answer-saved")
                    .build();
            
            sseService.pushQuestion(sessionId, responseNotification);
            
            log.info("사용자 답변 제출 완료: sessionId={}, questionKey={}", sessionId, currentQuestionKey);
            
        } catch (Exception e) {
            log.error("사용자 답변 제출 중 오류 발생: sessionId={}", sessionId, e);
            
            // 에러 메시지를 SSE로 전송
            QuestionResponse errorResponse = QuestionResponse.builder()
                    .text("답변 저장 중 오류가 발생했습니다: " + e.getMessage())
                    .currentChapter("ERROR")
                    .currentStage("error")
                    .build();
            
            sseService.pushQuestion(sessionId, errorResponse);
            
            if (e instanceof ApiException) {
                throw e;
            } else {
                throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * 새로운 템플릿용 에피소드 생성
     */
    @Transactional
    public Long createNewEpisodeForTemplate(GroupConversationSession session, String templateName) {
        try {
            // 1. 그룹북 조회
            GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(session.getGroupBookId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
            
            // 2. 현재 그룹북의 에피소드 개수로 orderNo 계산
            int nextOrderNo = (int) episodeRepository.findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(
                    session.getGroupBookId()).size() + 1;
            
            // 3. 템플릿별 제목 생성
            String episodeTitle = generateEpisodeTitleByTemplate(templateName, session.getGroupType());
            
            // 4. 새 에피소드 생성
            GroupEpisode newEpisode = episodeRepository.save(
                    GroupEpisode.toEntity(groupBook, episodeTitle, nextOrderNo, templateName)
            );
            
            log.info("새 템플릿 에피소드 생성 완료: episodeId={}, template={}, title={}", 
                    newEpisode.getGroupEpisodeId(), templateName, episodeTitle);
            
            return newEpisode.getGroupEpisodeId();
            
        } catch (Exception e) {
            log.error("새 템플릿 에피소드 생성 실패: sessionId={}, template={}, error={}", 
                    session.getSessionId(), templateName, e.getMessage());
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 템플릿별 에피소드 제목 생성
     */
    private String generateEpisodeTitleByTemplate(String template, GroupType groupType) {
        String groupTypeKorean = getGroupTypeKorean(groupType);
        
        return switch (template) {
            case "INTRO" -> groupTypeKorean + " - 소개";
            case "STORY" -> groupTypeKorean + " - 이야기"; 
            case "REFLECTION" -> groupTypeKorean + " - 회상";
            case "FUTURE" -> groupTypeKorean + " - 미래";
            default -> groupTypeKorean + " - " + template;
        };
    }
    
    /**
     * GroupType을 한글로 변환
     */
    private String getGroupTypeKorean(GroupType groupType) {
        return switch (groupType) {
            case FAMILY -> "가족";
            case FRIENDS -> "친구들";
            case COUPLE -> "커플";
            case TEAM -> "팀";
            case OTHER -> "기타";
        };
    }

    /**
     * 템플릿을 한글로 변환
     */
    private String getTemplateKoreanName(String template) {
        return switch (template) {
            case "INTRO" -> "소개";
            case "STORY" -> "이야기";
            case "REFLECTION" -> "회상";
            case "FUTURE" -> "미래";
            default -> template;
        };
    }

    /**
     * 현재 세션의 질문 텍스트 가져오기
     */
    private String getCurrentQuestionText(GroupConversationSession session) {
        try {
            // GuideResolver를 사용해서 현재 단계의 질문을 가져옴
            Optional<GuideQuestion> currentQuestion = guideResolver.resolveNext(
                    session.getGroupType(), 
                    session.getCurrentTemplate(), 
                    session.getCurrentStep(), 
                    ""
            );
            
            if (currentQuestion.isPresent()) {
                return currentQuestion.get().question();
            } else {
                // 첫 번째 질문일 경우
                GuideQuestion firstQuestion = guideResolver.resolveFirst(session.getGroupType(), session.getCurrentTemplate());
                return firstQuestion.question();
            }
        } catch (Exception e) {
            log.warn("현재 질문 텍스트 가져오기 실패: sessionId={}, error={}", session.getSessionId(), e.getMessage());
            return "질문을 가져올 수 없습니다."; // 기본값
        }
    }
    
    /**
     * 질문 키 생성 (예: "INTRO_FAMILY_1", "STORY_FRIENDS_2")
     */
    private String generateQuestionKey(String template, int step) {
        return String.format("%s_%s_%d", template, "QUESTION", step);
    }
    
    /**
     * 에피소드의 전체 내용을 모든 교정된 답변으로 업데이트
     */
    private void updateEpisodeContent(GroupEpisode episode) {
        List<GroupEpisodeGuideState> allStates = stateRepository
                .findByGroupEpisode_GroupEpisodeIdOrderByStepNoAsc(episode.getGroupEpisodeId());
        
        StringBuilder contentBuilder = new StringBuilder();
        for (GroupEpisodeGuideState state : allStates) {
            if (state.getEditedAnswer() != null && !state.getEditedAnswer().trim().isEmpty()) {
                if (contentBuilder.length() > 0) {
                    contentBuilder.append("\n\n");
                }
                contentBuilder.append(state.getEditedAnswer());
            }
        }
        
        String updatedContent = contentBuilder.toString();
        episode.updateEpisodeWithContent(episode.getTitle(), episode.getOrderNo(), episode.getTemplate(), updatedContent);
        episodeRepository.save(episode);
        
        log.info("에피소드 전체 내용 업데이트 완료: episodeId={}, contentLength={}", 
                episode.getGroupEpisodeId(), updatedContent.length());
    }

    // 템플릿 순서 관리 헬퍼 메서드
    private String getNextTemplate(String currentTemplate) {
        return switch (currentTemplate) {
            case "INTRO" -> "STORY";
            case "STORY" -> "REFLECTION";
            case "REFLECTION" -> "FUTURE";
            case "FUTURE" -> null; // 종료
            default -> null;
        };
    }

    @Override
    @Transactional
    public GroupEpisodeResponse createNextTemplateEpisode(Long groupId, Long groupBookId, String currentTemplate, Long memberId) {
        // 1. 다음 템플릿 확인
        String nextTemplate = getNextTemplate(currentTemplate);
        if (nextTemplate == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }

        // 2. 그룹책 확인
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 3. 현재 그룹북의 에피소드 개수로 orderNo 계산
        int nextOrderNo = (int) episodeRepository.findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(groupBookId).size() + 1;

        // 4. 템플릿별 제목 생성
        String episodeTitle = generateEpisodeTitleByTemplate(nextTemplate, groupBook.getGroupType());

        // 5. 새 에피소드 생성
        GroupEpisode newEpisode = episodeRepository.save(
                GroupEpisode.toEntity(groupBook, episodeTitle, nextOrderNo, nextTemplate)
        );

        log.info("사용자 요청으로 다음 템플릿 에피소드 생성: groupId={}, groupBookId={}, currentTemplate={}, nextTemplate={}, newEpisodeId={}", 
                groupId, groupBookId, currentTemplate, nextTemplate, newEpisode.getGroupEpisodeId());

        return GroupEpisodeResponse.of(newEpisode);
    }

    @Override
    public GroupAnswerCorrectionResponse correctAnswer(Long groupId, Long groupBookId, Long episodeId, 
                                                     GroupAnswerCorrectionRequest request, Long memberId) {
        
        // 1. 그룹 멤버 권한 확인
        groupMemberService.verifyMember(groupId, memberId);
        
        // 2. 그룹책 및 에피소드 존재 확인
        GroupBook groupBook = groupBookRepository.findByGroupBookIdAndDeletedAtIsNull(groupBookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        
        GroupEpisode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));
        
        if (!episode.getGroupBook().getGroupBookId().equals(groupBookId)) {
            throw new ApiException(ErrorCode.EPISODE_NOT_FOUND);
        }
        
        // 3. 그룹 타입 정보 가져오기
        String groupType = groupBook.getGroupType().name();
        
        // 4. AI 교정 요청
        try {
            String aiResponse = aiClient.correctAnswerWithContext(
                    request.getQuestion(),
                    request.getAnswer(),
                    request.getCurrentTemplate(),
                    groupType,
                    request.getCorrectionStyle() != null ? request.getCorrectionStyle() : "CASUAL"
            );
            
            // 5. AI 응답 파싱 (JSON 형태로 반환되므로 파싱 필요)
            // 간단한 파싱 로직 - 실제로는 JSON 라이브러리 사용 권장
            return parseAiCorrectionResponse(aiResponse, request.getAnswer());
            
        } catch (Exception e) {
            log.error("AI 답변 교정 실패: groupId={}, episodeId={}, error={}", groupId, episodeId, e.getMessage());
            
            // 임시: API 키 문제로 인한 실패 시 샘플 교정 응답 반환
            if (e.getMessage().contains("401") || e.getMessage().contains("UNAUTHORIZED")) {
                return GroupAnswerCorrectionResponse.builder()
                        .originalAnswer(request.getAnswer())
                        .correctedAnswer(request.getAnswer() + "라는 소중한 기억이 마음 깊이 새겨져 있습니다. 그 순간의 감정과 경험이 오늘의 저를 만들어주었다고 생각합니다.")
                        .correctionReason("현재 AI 서비스 연결에 문제가 있어 샘플 교정을 제공합니다. 원본 답변을 보다 서정적이고 감동적인 문체로 개선했습니다.")
                        .suggestedFollowUpQuestion("그 경험을 통해 얻은 가장 큰 깨달음이나 교훈이 있다면 무엇인가요?")
                        .build();
            }
            
            // 일반적인 AI 교정 실패 시 원본 답변 반환
            return GroupAnswerCorrectionResponse.builder()
                    .originalAnswer(request.getAnswer())
                    .correctedAnswer(request.getAnswer())
                    .correctionReason("AI 교정 서비스에 일시적인 문제가 발생했습니다.")
                    .suggestedFollowUpQuestion(null)
                    .build();
        }
    }
    
    private GroupAnswerCorrectionResponse parseAiCorrectionResponse(String aiResponse, String originalAnswer) {
        try {
            // JSON 파싱 로직 (간단한 구현)
            // 실제로는 ObjectMapper 등을 사용하는 것이 좋음
            
            // 기본값 설정
            String correctedAnswer = originalAnswer;
            String correctionReason = "교정이 완료되었습니다.";
            String suggestedFollowUpQuestion = null;
            
            if (aiResponse.contains("correctedAnswer")) {
                int start = aiResponse.indexOf("\"correctedAnswer\":") + 18;
                int end = aiResponse.indexOf("\",", start);
                if (end == -1) end = aiResponse.indexOf("\"}", start);
                if (start > 17 && end > start) {
                    correctedAnswer = aiResponse.substring(start + 1, end);
                }
            }
            
            if (aiResponse.contains("correctionReason")) {
                int start = aiResponse.indexOf("\"correctionReason\":") + 19;
                int end = aiResponse.indexOf("\",", start);
                if (end == -1) end = aiResponse.indexOf("\"}", start);
                if (start > 18 && end > start) {
                    correctionReason = aiResponse.substring(start + 1, end);
                }
            }
            
            if (aiResponse.contains("suggestedFollowUpQuestion")) {
                int start = aiResponse.indexOf("\"suggestedFollowUpQuestion\":") + 28;
                int end = aiResponse.indexOf("\"}", start);
                if (end == -1) end = aiResponse.indexOf("\",", start);
                if (start > 27 && end > start && !aiResponse.substring(start, end).contains("null")) {
                    suggestedFollowUpQuestion = aiResponse.substring(start + 1, end);
                }
            }
            
            return GroupAnswerCorrectionResponse.builder()
                    .originalAnswer(originalAnswer)
                    .correctedAnswer(correctedAnswer)
                    .correctionReason(correctionReason)
                    .suggestedFollowUpQuestion(suggestedFollowUpQuestion)
                    .build();
                    
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", e.getMessage());
            return GroupAnswerCorrectionResponse.builder()
                    .originalAnswer(originalAnswer)
                    .correctedAnswer(originalAnswer)
                    .correctionReason("응답 처리 중 오류가 발생했습니다.")
                    .suggestedFollowUpQuestion(null)
                    .build();
        }
    }
}
