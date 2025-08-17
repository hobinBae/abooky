package com.c203.autobiography.domain.groupbook.episode.service;

import com.c203.autobiography.domain.groupbook.episode.dto.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface GroupEpisodeService {
    GroupEpisodeResponse create(Long groupId, Long groupBookId, GroupEpisodeCreateRequest request, Long memberId);

    StepNextResponse next(Long groupId, Long groupBookId, Long episodeId, StepNextRequest req, Long memberId);

    GroupEpisodeResponse get(Long groupId, Long groupBookId, Long episodeId);

    GroupEpisodeResponse finalizeEpisode(Long groupId, Long groupBookId, Long episodeId);

    List<GroupEpisodeResponse> getEpisodeList(Long groupId, Long groupBookId, Long memberId);

    GroupEpisodeResponse update(Long groupId, Long groupBookId, Long episodeId, GroupEpisodeUpdateRequest request, Long memberId);

    void delete(Long groupId, Long groupBookId, Long episodeId, Long memberId);
    
    // 이미지 관련 메서드
    GroupEpisodeImageResponse uploadImage(Long groupId, Long groupBookId, Long episodeId, 
                                         MultipartFile file, GroupEpisodeImageUploadRequest request, Long memberId);
    
    List<GroupEpisodeImageResponse> getImages(Long groupId, Long groupBookId, Long episodeId, Long memberId);
    
    void deleteImage(Long groupId, Long groupBookId, Long episodeId, Long imageId, Long memberId);
    
    // 대화 세션 관련 메서드 (개인 book과 동일한 구조)
    String startNewConversation(Long memberId, Long groupId, Long groupBookId, Long episodeId);
    
    SseEmitter establishConversationStream(String sessionId, Long groupId, Long groupBookId, Long episodeId);
    
    void getNextQuestion(Long memberId, Long groupId, Long groupBookId, Long episodeId, String sessionId);
    
    void closeSseStream(String sessionId);
    
    // 답변 처리 메서드
    void submitAnswer(Long memberId, Long groupId, Long groupBookId, Long episodeId, String sessionId, GroupAnswerRequest request);
    
    // 다음 템플릿 에피소드 생성
    GroupEpisodeResponse createNextTemplateEpisode(Long groupId, Long groupBookId, String currentTemplate, Long memberId);
}
