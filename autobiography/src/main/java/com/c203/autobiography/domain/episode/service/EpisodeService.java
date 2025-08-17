package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.episode.dto.EpisodeImageResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeImageUploadRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.member.entity.Member;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EpisodeService {

    EpisodeResponse createEpisode(Long memberId, Long bookId);

    EpisodeResponse getEpisode(Long episodeId);

    EpisodeResponse updateEpisode(Long memberId, Long bookId, Long episodeId, EpisodeUpdateRequest request);

    Void deleteEpisode(Long memberId, Long bookId, Long episodeId);

    EpisodeResponse createEpisodeFromCurrentWindow(Episode episode, String sessionId) throws JsonProcessingException;

    // 이미지 관련 메서드
    EpisodeImageResponse uploadImage(Long bookId, Long episodeId, MultipartFile file, 
                                   EpisodeImageUploadRequest request, Long memberId);
    
    List<EpisodeImageResponse> getImages(Long bookId, Long episodeId, Long memberId);
    
    void deleteImage(Long bookId, Long episodeId, Long imageId, Long memberId);
    EpisodeResponse createEpisodeBySessionId(String sessionId) throws JsonProcessingException;

}
