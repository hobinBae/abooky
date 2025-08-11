package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EpisodeServiceImpl implements EpisodeService{

    private final ConversationMessageRepository conversationMessageRepository;
    private final EpisodeRepository episodeRepository;
    private final AiClient aiClient;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ConversationSessionRepository conversationSessionRepository;

    /**
     * 에피소드 생성
     * @param memberId
     * @param bookId
     * @return
     */
    @Override
    @Transactional
    public EpisodeResponse createEpisode(Long memberId, Long bookId) {
        // 1) 사용자 확인
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2) 책 확인
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        // 3) 권한 체크: 책 소유자만 생성 가능
        if (!book.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }

        // 4) 다음 순서 계산 (soft-delete 제외)
        long count = episodeRepository.countByBookBookIdAndDeletedAtIsNull(bookId);
        int nextOrder = (int) count + 1;

        // 5) "빈 에피소드" 생성: 제목 기본값, 본문/오디오/날짜는 비워둠
        Episode episode = Episode.builder()
                .book(book)
                .title(nextOrder + "번째 이야기")  // 기본 제목
                .content("")                      // 비어 있는 상태
                .episodeOrder(nextOrder)          // 정렬 순서
                .episodeDate(null)                // 날짜 미지정
                .audioUrl(null)                   // 오디오 미지정
                .build();

        Episode saved = episodeRepository.save(episode);
        return EpisodeResponse.of(saved);
    }

    @Override
    public EpisodeResponse getEpisode(Long episodeId) {
        return null;
    }

    /**
     * 에피소드 수정
     * @param memberId
     * @param bookId
     * @param episodeId
     * @param request
     * @return
     */
    @Override
    @Transactional
    public EpisodeResponse updateEpisode(Long memberId, Long bookId, Long episodeId, EpisodeUpdateRequest request) {

        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);

        episode.updateEpisode(request.getTitle(), request.getEpisodeDate(), request.getEpisodeOrder(), request.getContent(), request.getAudioUrl());

        return EpisodeResponse.of(episode);
    }

    /**
     * 에피소드 삭제
     * @param memberId
     * @param bookId
     * @param episodeId
     * @return
     */
    @Override
    @Transactional
    public Void deleteEpisode(Long memberId, Long bookId, Long episodeId) {

        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);

        episode.softDelete();
        return null;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EpisodeResponse createEpisodeFromCurrentWindow(Episode episode, String sessionId)
            throws JsonProcessingException {


        var session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));
        Integer startNo = session.getEpisodeStartMessageNo();

        if (startNo == null) startNo = 1;

        Integer endNo = conversationMessageRepository.findMaxMessageNo(sessionId);

        if (endNo == null || endNo < startNo) {
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE); // 포함할 메시지가 없음
        }
        List<ConversationMessage> history = conversationMessageRepository
                .findBySessionIdAndMessageNoBetweenOrderByMessageNo(sessionId, startNo, endNo);

        // 대화 텍스트 합치기
        StringBuilder dialog = new StringBuilder();
        for (ConversationMessage m : history) {
            dialog.append(m.getMessageType()).append(": ").append(m.getContent()).append("\n");
        }

        // JSON 강제 포맷 요청 (기존 로직 그대로 사용 가능)
        String prompt = String.format("""
      다음 JSON 포맷으로 응답해주세요:
      {
        "title": "여기에 에피소드 제목을 적어주세요",
        "content": "여기에 에피소드 본문을 적어주세요"
      }
      위와 같은 형태로, 아래 대화 내용을 바탕으로 에피소드 제목과 본문을 작성해 주세요:

      %s
      """, dialog);

        String episodeJsonContent = aiClient.generateEpisode(prompt);
        ObjectMapper om = new ObjectMapper();
        JsonNode node = om.readTree(episodeJsonContent);
        String title   = node.get("title").asText();
        String content = node.get("content").asText();

         episode.updateEpisode(
                 title,
                 episode.getEpisodeDate(),
                 episode.getEpisodeOrder(),
                 content,
                 episode.getAudioUrl()
         );
        episodeRepository.save(episode);

//        // 세션에 연결(선택) + 다음 에피소드 구간 시작점 갱신
//        // 다음 장부터는 다음 메시지부터 새 에피소드로 묶임
//        ConversationSession updated = session.toBuilder()
//                .episodeId(episode.getEpisodeId()) // 필요 없으면 제거 가능
//                .episodeStartMessageNo(endNo + 1)  // ✅ 다음 구간 시작점 점프
//                .build();
//        conversationSessionRepository.save(updated);

        return EpisodeResponse.of(episode);
    }


    /**
     * 공통 검증 로직:
     * 1) 회원 존재
     * 2) 책 존재 + 논리삭제 체크 + 권한 확인
     * 3) 에피소드 존재 + 논리삭제 체크
     */

    private Episode validateAndGetEpisode(Long memberId, Long bookId, Long episodeId){
        // 1) 회원
        memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 2) 책 & 권한
        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));
        if (!book.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.FORBIDDEN);
        }
        // 3) 에피소드 & 소속
        Episode episode = episodeRepository.findByEpisodeIdAndDeletedAtIsNull(episodeId)
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));

        return episode;
    }
}
