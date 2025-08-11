package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeCreateRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextRequest;
import com.c203.autobiography.domain.groupbook.episode.dto.StepNextResponse;
import com.c203.autobiography.domain.groupbook.episode.entity.GroupEpisode;
import com.c203.autobiography.domain.groupbook.episode.repository.EpisodeGuideStateRepository;
import com.c203.autobiography.domain.groupbook.episode.repository.GroupEpisodeRepository;
import com.c203.autobiography.domain.groupbook.repository.GroupBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupEpisodeServiceImpl implements GroupEpisodeService {

    private final GroupBookRepository groupBookRepository;
    private final GroupEpisodeRepository episodeRepository;
    private final EpisodeGuideStateRepository stateRepository;
    private final GuideResolverService guideResolver;
    private final EditorService editorService;


    @Override @Transactional
    public EpisodeResponse create(Long groupId, Long groupBookId, EpisodeCreateRequest req, Long memberId) {
        GroupBook gb = groupBookRepository.findById(groupBookId)
                .orElseThrow(() -> new IllegalArgumentException("GroupBook not found: " + groupBookId));
        Integer orderNo = (req.getOrderNo() == null)
                ? (int) (episodeRepository.findByGroupBook_IdOrderByOrderNoAscCreatedAtAsc(groupBookId).size() + 1)
                : req.getOrderNo();
        GroupEpisode ep = episodeRepository.save(GroupEpisode.create(gb, req.getTitle(), orderNo));

        // 첫 질문 준비
        String firstQuestion = guideResolver.firstQuestion(gb, req.getTemplate());
        return EpisodeResponse.builder()
                .id(ep.getId()).groupBookId(gb.getId()).title(ep.getTitle()).orderNo(ep.getOrderNo())
                .status(ep.getStatus().name()).currentStep(ep.getCurrentStep()).editedContent(ep.getEditedContent())
                .build();
    }

    @Override @Transactional
    public StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        if (ep.getStatus() == EpisodeStatus.DRAFT) ep.setStatus(EpisodeStatus.IN_PROGRESS);

        int nextStepNo = (req.getLastStepNo() == null ? 0 : req.getLastStepNo()) + 1;

        // 1) 현재 질문 로드 (guideKey/question)
        GuideQuestion q = guideResolver.questionForStep(ep, nextStepNo);

        // 2) 사용자 답변 저장 & 편집
        String edited = editorService.polishPlain(q.question(), req.getUserAnswer(), ep.getEditedContent());

        stateRepository.save(EpisodeGuideState.builder()
                .episode(ep).stepNo(nextStepNo).guideKey(q.key()).question(q.question())
                .userAnswer(req.getUserAnswer()).editedParagraph(edited).isFinal(true).build());

        ep.appendRaw(req.getUserAnswer());
        ep.appendEdited(edited);
        ep.nextStep();

        // 3) 다음 질문 결정
        GuideQuestion nextQ = guideResolver.nextQuestion(ep, nextStepNo + 1);
        boolean completed = (nextQ == null);
        if (completed) ep.setStatus(EpisodeStatus.REVIEW);

        return StepNextResponse.builder()
                .stepNo(nextStepNo)
                .editedParagraph(edited)
                .question(completed ? null : nextQ.question())
                .completed(completed)
                .build();
    }

    @Override
    public EpisodeResponse get(Long groupId, Long groupBookId, Long episodeId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        return EpisodeResponse.builder()
                .id(ep.getId()).groupBookId(ep.getGroupBook().getId()).title(ep.getTitle())
                .orderNo(ep.getOrderNo()).status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep()).editedContent(ep.getEditedContent())
                .build();
    }

    @Override @Transactional
    public EpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId) {
        GroupEpisode ep = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new IllegalArgumentException("Episode not found: " + episodeId));
        ep.setStatus(EpisodeStatus.COMPLETE);
        return EpisodeResponse.builder()
                .id(ep.getId()).groupBookId(ep.getGroupBook().getId()).title(ep.getTitle())
                .orderNo(ep.getOrderNo()).status(ep.getStatus().name())
                .currentStep(ep.getCurrentStep()).editedContent(ep.getEditedContent())
                .build();
    }
}
