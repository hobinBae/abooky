package com.c203.autobiography.domain.groupbook.episode.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupEpisodeServiceImpl implements GroupEpisodeService {

    private final GroupBookRepository groupBookRepository;
    private final GroupEpisodeRepository episodeRepository;
    private final GroupEpisodeGuideStateRepository stateRepository;
    private final GroupEpisodeImageRepository imageRepository;
    private final GuideResolverService guideResolver;
    private final EditorService editorService;
    private final FileStorageService fileStorageService;


    @Override @Transactional
    public GroupEpisodeResponse create(Long groupId, Long groupBookId, GroupEpisodeCreateRequest req, Long memberId) {
        GroupBook gb = groupBookRepository.findById(groupBookId)
                .orElseThrow(() -> new IllegalArgumentException("GroupBook not found: " + groupBookId));

        // 순서 번호 계산
        Integer orderNo = (req.getOrderNo() == null)
                ? (int) (episodeRepository.findByGroupBook_GroupBookIdOrderByOrderNoAscCreatedAtAsc(groupBookId).size() + 1)
                : req.getOrderNo();

        // 에피소드 생성
        GroupEpisode ep = episodeRepository.save(GroupEpisode.toEntity(gb, req.getTitle(), orderNo, req.getTemplate()));

        // 첫 질문 준비
        GuideQuestion firstQuestion = guideResolver.resolveFirst(gb.getGroupType(), req.getTemplate());

        // 🎯 첫 번째 답변 편집
        String editedFirstAnswer = editorService.polish(req.getFirstAnswer(), "");

        // 🎯 첫 번째 가이드 상태 저장 (완료된 상태로)
        GroupEpisodeGuideState firstState = GroupEpisodeGuideState.builder()
                .groupEpisode(ep)
                .stepNo(1)
                .guideKey(firstQuestion.key())
                .question(firstQuestion.question())
                .userAnswer(req.getFirstAnswer())
                .editedParagraph(editedFirstAnswer)
                .isFinal(true)  // 🎯 이미 완료된 상태
                .build();
        stateRepository.save(firstState);

        // 🎯 에피소드에 첫 번째 편집 내용 추가
        ep.appendEdited(editedFirstAnswer);
        ep.nextStep(); // currentStep = 2가 됨
        ep.setStatus(GroupEpisodeStatus.IN_PROGRESS);

        // 🎯 두 번째 질문 준비
        Optional<GuideQuestion> secondQuestionOpt = guideResolver.resolveNext(
                gb.getGroupType(),
                req.getTemplate(),
                1, // 첫 번째 단계 완료
                req.getFirstAnswer()
        );

        String nextQuestion = null;

        if (secondQuestionOpt.isPresent()) {
            GuideQuestion secondQuestion = secondQuestionOpt.get();
            nextQuestion = secondQuestion.question();
            GroupEpisodeGuideState secondState = GroupEpisodeGuideState.builder()
                    .groupEpisode(ep)
                    .stepNo(2)
                    .guideKey(secondQuestion.key())
                    .question(secondQuestion.question())
                    .userAnswer(null)
                    .editedParagraph(null)
                    .isFinal(false)
                    .build();
            stateRepository.save(secondState);
        } else {
            // 질문이 더 없으면 리뷰 상태로
            ep.setStatus(GroupEpisodeStatus.REVIEW);
        }

        return GroupEpisodeResponse.builder()
                .id(ep.getGroupEpisodeId())
                .groupBookId(ep.getGroupBook().getGroupBookId())
                .title(ep.getTitle())
                .orderNo(ep.getOrderNo())
                .status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep())
                .editedContent(ep.getEditedContent())
                .currentQuestion(nextQuestion) // 🎯 이 부분이 핵심!
                .build();
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
}
