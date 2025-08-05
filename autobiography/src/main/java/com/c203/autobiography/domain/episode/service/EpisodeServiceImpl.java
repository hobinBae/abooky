package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.book.Entity.Book;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForInteger;
import org.springframework.stereotype.Service;
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
    private final DecimalMaxValidatorForInteger decimalMaxValidatorForInteger;

    /**
     * 에피소드 생성
     * @param memberId
     * @param bookId
     * @param sessionId
     * @return
     */
    @Override
    @Transactional
    public EpisodeResponse createEpisode(Long memberId, Long bookId, String sessionId) {
        Member member = memberRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Book book = bookRepository.findByBookIdAndDeletedAtIsNull(bookId)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOK_NOT_FOUND));

        List<ConversationMessage> history = conversationMessageRepository
                .findBySessionIdOrderByMessageNo(sessionId);

        // 2) 메시지를 하나의 텍스트로 합치기
        StringBuilder dialog = new StringBuilder();
        for (ConversationMessage m : history) {
            dialog.append(m.getMessageType())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n");
        }

        // 3) AI에게 “정돈된 에피소드 본문” 생성 요청
        String prompt = String.format(
                "위 대화 내용을 바탕으로, 사용자 자서전의 하나의 에피소드 본문을 자연스럽게 작성해주세요:\n\n%s",
                dialog.toString()
        );
        String episodeContent = aiClient.generateEpisode(prompt);
        Episode episode = Episode.builder()
                .book(book)
                .content(episodeContent)
                .createdAt(LocalDateTime.now())
                .build();
        episodeRepository.save(episode);

        return EpisodeResponse.of(episode);
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

        episode.updateEpisode(request.getTitle(), request.getEpisodeDate(), request.getEpisodeOrder(), request.getContent());

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
    public Void deleteEpisode(Long memberId, Long bookId, Long episodeId) {

        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);

        episode.softDelete();
        return null;
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
