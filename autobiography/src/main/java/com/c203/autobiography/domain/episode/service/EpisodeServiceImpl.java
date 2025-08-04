package com.c203.autobiography.domain.episode.service;

import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.book.Entity.Book;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
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
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpisodeServiceImpl implements EpisodeService{

    private final ConversationMessageRepository conversationMessageRepository;
    private final EpisodeRepository episodeRepository;
    private final AiClient aiClient;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    @Override
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
}
