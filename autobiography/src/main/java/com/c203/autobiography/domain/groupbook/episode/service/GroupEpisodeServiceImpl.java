package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.episode.dto.*;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeGuideState;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisodeStatus;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeGuideStateRepository;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final GuideResolverService guideResolver;
    private final EditorService editorService;


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

        if (secondQuestionOpt.isPresent()) {
            GuideQuestion secondQuestion = secondQuestionOpt.get();
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

        return GroupEpisodeResponse.of(ep);
    }

    @Override @Transactional
    public StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        GroupBook gb = ep.getGroupBook();
        if (ep.getStatus() == GroupEpisodeStatus.DRAFT) ep.setStatus(GroupEpisodeStatus.IN_PROGRESS);

        int currentStepNo = (req.getLastStepNo() == null ? 1 : req.getLastStepNo());

        // 1) 현재 스텝 사용자 답변을 편집
        String editedParagraph = editorService.polish(req.getUserAnswer(), ep.getEditedContent());

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
        Optional<GuideQuestion> nextQuestionOpt = guideResolver.resolveNext(
                gb.getGroupType(),
                "INTRO",
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
        return GroupEpisodeResponse.of(ep);
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
}
