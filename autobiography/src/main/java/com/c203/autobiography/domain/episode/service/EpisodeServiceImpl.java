package com.c203.autobiography.domain.episode.service;


import com.c203.autobiography.domain.ai.client.AiClient;
import com.c203.autobiography.domain.ai.dto.ChatCompletionResponse;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.repository.BookRepository;
import com.c203.autobiography.domain.episode.dto.EpisodeImageResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeImageUploadRequest;
import com.c203.autobiography.domain.episode.dto.EpisodeResponse;
import com.c203.autobiography.domain.episode.dto.EpisodeUpdateRequest;
import com.c203.autobiography.domain.episode.entity.ConversationMessage;
import com.c203.autobiography.domain.episode.entity.ConversationSession;
import com.c203.autobiography.domain.episode.entity.Episode;
import com.c203.autobiography.domain.episode.entity.EpisodeImage;
import com.c203.autobiography.domain.episode.entity.EpisodeImageId;
import com.c203.autobiography.domain.episode.repository.ConversationMessageRepository;
import com.c203.autobiography.domain.episode.repository.ConversationSessionRepository;
import com.c203.autobiography.domain.episode.repository.EpisodeImageRepository;
import com.c203.autobiography.domain.episode.repository.EpisodeRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EpisodeServiceImpl implements EpisodeService {

    private final ConversationMessageRepository conversationMessageRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeImageRepository episodeImageRepository;
    private final AiClient aiClient;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final FileStorageService fileStorageService;

    /**
     * 에피소드 생성
     *
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
     *
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

        episode.updateEpisode(request.getTitle(), request.getEpisodeDate(), request.getEpisodeOrder(),
                request.getContent(), request.getAudioUrl());

        return EpisodeResponse.of(episode);
    }

    /**
     * 에피소드 삭제
     *
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
    public EpisodeResponse createEpisodeFromCurrentWindow(Episode episode, String sessionId)
            throws JsonProcessingException {

        var session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INPUT_VALUE));
        Integer startNo = session.getEpisodeStartMessageNo();

        if (startNo == null) {
            startNo = 1;
        }

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
        String prompt = """
                다음 JSON 포맷으로만 응답하세요:
                {"title": "...", "content": "..."}
                아래 대화를 바탕으로 에피소드 제목과 본문을 작성하세요.

                %s
                """.formatted(dialog);

        String rawApiResponse = aiClient.generateEpisode(prompt);

        String innerJsonContent = extractAssistantText(rawApiResponse); // 헬퍼 메소드 사용
        if (innerJsonContent == null) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }
        String cleaned = cleanInnerJsonText(innerJsonContent);
        ObjectMapper om = new ObjectMapper();
        JsonNode finalNode = om.readTree(cleaned);
        String title = finalNode.path("title").asText(null);
        String content = finalNode.path("content").asText(null);

        // 4. 파싱된 결과를 가지고 DB에 저장하는 '트랜잭션 메소드'를 호출
        return saveEpisodeWithAiResult(episode, title, content);

    }
    /**
     * [신규] AI 결과를 DB에 저장하는 역할만 담당하는 새로운 트랜잭션 메소드
     * 이 메소드는 매우 빠르게 실행되므로 커넥션을 오래 차지하지 않습니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EpisodeResponse saveEpisodeWithAiResult(Episode episode, String title, String content) {
        if (title == null || content == null) {
            log.error("AI 결과에서 제목 또는 콘텐츠가 누락되었습니다.");
            throw new ApiException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // DB에서 최신 상태의 Episode 엔티티를 다시 가져오는 것이 더 안전합니다.
        Episode managedEpisode = episodeRepository.findById(episode.getEpisodeId())
                .orElseThrow(() -> new ApiException(ErrorCode.EPISODE_NOT_FOUND));

        managedEpisode.updateEpisode(
                title,
                managedEpisode.getEpisodeDate(),
                managedEpisode.getEpisodeOrder(),
                content,
                managedEpisode.getAudioUrl()
        );
        // save는 더티 체킹에 의해 필요 없을 수 있지만, 명시적으로 호출해도 괜찮습니다.
        episodeRepository.save(managedEpisode);


        // 세션의 다음 에피소드 시작점 업데이트 로직이 필요하다면 여기에 추가
        // ...

        return EpisodeResponse.of(managedEpisode);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }

    // 문자열 전처리 헬퍼 메소드
    private static String cleanInnerJsonText(String text) {
        if (text == null) {
            return null;
        }
        // ```json ... ``` 만 제거 (본문만 정리)
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```\\s*$", "");
        }
        return trimmed.trim();
    }

    private static String extractAssistantText(String raw) throws JsonProcessingException {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();

        // 0) 백틱 코드블록 제거(있다면)
        if (t.startsWith("```")) {
            t = cleanInnerJsonText(t);
        }

        // 0-1) 이미 {"title": "...", "content": "..."} 형태로 온 경우 바로 통과
        if (t.startsWith("{") && t.endsWith("}")) {
            try {
                ObjectMapper om = new ObjectMapper();
                JsonNode node = om.readTree(t);
                if (node.has("title") && node.has("content")) {
                    return t; // 이미 최종 JSON
                }
            } catch (Exception ignore) {
            }
        }

        // 0-2) 응답이 이중 인코딩된 JSON 문자열인 경우 처리 ( "{"title":".."}" )
        if (t.startsWith("\"") && t.endsWith("\"")) {
            try {
                ObjectMapper om = new ObjectMapper();
                JsonNode asTextNode = om.readTree(t);
                if (asTextNode.isTextual()) {
                    String unquoted = asTextNode.asText().trim();
                    if (unquoted.startsWith("{") && unquoted.endsWith("}")) {
                        JsonNode node2 = om.readTree(unquoted);
                        if (node2.has("title") && node2.has("content")) {
                            return unquoted;
                        }
                        // 래퍼일 수도 있으니 아래 일반 흐름으로 계속 진행
                        t = unquoted;
                    }
                }
            } catch (Exception ignore) {}
        }



        // 1) OpenAI chat 랩퍼에서 추출
        ObjectMapper om = new ObjectMapper();
        JsonNode root = om.readTree(t);

        JsonNode msg = root.path("choices").path(0).path("message");

        // 1-1) content가 문자열
        JsonNode content = msg.get("content");
        if (content != null && !content.isNull()) {
            if (content.isTextual()) {
                return content.asText();
            }

            // 1-2) content가 배열(Responses 스타일)
            if (content.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode part : content) {
                    if (part.hasNonNull("text")) {
                        sb.append(part.get("text").asText());
                    } else if (part.hasNonNull("output_text")) {
                        sb.append(part.get("output_text").asText());
                    }
                }
                if (sb.length() > 0) {
                    return sb.toString();
                }
            }
        }
        // 1-3) content가 객체 { type, text } 형태인 경우
        if (content != null && content.isObject()) {
            if (content.hasNonNull("text")) {
                return content.get("text").asText();
            }
            if (content.hasNonNull("output_text")) {
                return content.get("output_text").asText();
            }
        }

        // 2) tool_calls(function.arguments)에 JSON이 담긴 경우
        JsonNode toolArgs = msg.path("tool_calls").path(0).path("function").path("arguments");
        if (!toolArgs.isMissingNode() && !toolArgs.isNull()) {
            return toolArgs.isTextual() ? toolArgs.asText() : toolArgs.toString();
        }

        return null;
    }


    /**
     * 공통 검증 로직: 1) 회원 존재 2) 책 존재 + 논리삭제 체크 + 권한 확인 3) 에피소드 존재 + 논리삭제 체크
     */

    private Episode validateAndGetEpisode(Long memberId, Long bookId, Long episodeId) {
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

    // ======== 이미지 관련 메서드 ========

    @Override
    @Transactional
    public EpisodeImageResponse uploadImage(Long bookId, Long episodeId, MultipartFile file, 
                                          EpisodeImageUploadRequest request, Long memberId) {
        // 1. 책 및 에피소드 존재 확인
        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);
        
        // 2. 파일 업로드
        String imageUrl = fileStorageService.store(file, "episode");
        
        // 3. 순서 번호 결정 (요청에 없으면 자동 부여)
        Integer orderNo = request.getOrderNo();
        if (orderNo == null) {
            Integer maxOrder = episodeImageRepository.findMaxOrderNoByEpisodeId(episodeId);
            orderNo = (maxOrder == null ? 0 : maxOrder) + 1;
        }
        
        // 4. 이미지 ID 생성 (타임스탬프 기반)
        Long imageId = System.currentTimeMillis();
        
        // 5. 이미지 엔티티 생성 및 저장
        EpisodeImage image = EpisodeImage.create(
                episode, imageId, imageUrl, orderNo, request.getDescription());
        
        EpisodeImage savedImage = episodeImageRepository.save(image);
        
        return EpisodeImageResponse.from(savedImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeImageResponse> getImages(Long bookId, Long episodeId, Long memberId) {
        // 1. 책 및 에피소드 존재 확인
        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);
        
        // 2. 이미지 목록 조회
        List<EpisodeImage> images = episodeImageRepository
                .findByEpisode_EpisodeIdAndDeletedAtIsNullOrderByOrderNoAscCreatedAtAsc(episodeId);
        
        return images.stream()
                .map(EpisodeImageResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteImage(Long bookId, Long episodeId, Long imageId, Long memberId) {
        // 1. 책 및 에피소드 존재 확인
        Episode episode = validateAndGetEpisode(memberId, bookId, episodeId);
        
        // 2. 이미지 조회 및 삭제
        EpisodeImageId imageEntityId = EpisodeImageId.of(episodeId, imageId);
        EpisodeImage image = episodeImageRepository.findByIdAndDeletedAtIsNull(imageEntityId)
                .orElseThrow(() -> new ApiException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 3. 소프트 삭제
        image.softDelete();
    }
}
