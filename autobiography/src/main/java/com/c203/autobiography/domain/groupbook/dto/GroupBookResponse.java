package com.c203.autobiography.domain.groupbook.dto;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.episode.dto.GroupEpisodeResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹 책 응답 DTO")
public class GroupBookResponse {
    private Long groupBookId;
    private Long groupId;
    private Long memberId;
    private String name;
    private String email;
    private String nickname;
    private String title;
    private String coverImageUrl;
    private String summary;
    private BookType bookType;
    private Long categoryId;
    private String categoryName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

//    private Integer likeCount;
//    private Integer viewCount;
//    private BigDecimal averageRating; // 소수점 한 자리: "4.5" 형태
    private Boolean completed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    //책에 속한 에피소드 목록
    private List<GroupEpisodeResponse> episodes;
    private List<String> tags;

    private String firstEpisodeQuestion;  // 첫 에피소드 질문
    private String questionKey;           // 질문 식별 키

    public static GroupBookResponse of(GroupBook book, List<GroupEpisodeResponse> episodes, List<String> tags) {
        return GroupBookResponse.builder()
                .groupBookId(book.getGroupBookId())
                .groupId(book.getGroup().getGroupId())  // 누락된 groupId 추가
                .memberId(book.getMember().getMemberId())
                .name(book.getMember().getName())
                .email(book.getMember().getEmail())
                .nickname(book.getMember().getNickname())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .summary(book.getSummary())
                .bookType(book.getBookType())
                .categoryId(book.getCategory() != null ? book.getCategory().getBookCategoryId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getCategoryName() : null)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
//                .likeCount(book.getLikeCount())
//                .viewCount(book.getViewCount())
//                .averageRating(book.getAverageRating())
                .completed(book.getCompleted())
                .completedAt(book.getCompletedAt())
                .episodes(episodes)
                .tags(tags)
                .build();
    }
    // 🎯 첫 질문과 함께 생성하는 정적 메서드
    public static GroupBookResponse ofWithFirstQuestion(
            GroupBook book,
            List<GroupEpisodeResponse> episodes,
            List<String> tags,
            String firstQuestion,
            String questionKey
    ) {
        return GroupBookResponse.builder()
                .groupBookId(book.getGroupBookId())
                .groupId(book.getGroup().getGroupId())  // 누락된 groupId 추가
                .memberId(book.getMember().getMemberId())
                .name(book.getMember().getName())
                .email(book.getMember().getEmail())
                .nickname(book.getMember().getNickname())
                .title(book.getTitle())
                .coverImageUrl(book.getCoverImageUrl())
                .summary(book.getSummary())
                .bookType(book.getBookType())
                .categoryId(book.getCategory() != null ? book.getCategory().getBookCategoryId() : null)
                .categoryName(book.getCategory() != null ? book.getCategory().getCategoryName() : null)
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .completed(book.getCompleted())
                .completedAt(book.getCompletedAt())
                .episodes(episodes)
                .tags(tags)
                .firstEpisodeQuestion(firstQuestion)  // 🎯 첫 질문 추가
                .questionKey(questionKey)             // 🎯 질문 키 추가
                .build();
    }
    public static GroupBookResponse of(
            GroupBook book,
            List<GroupEpisodeResponse> episodes
    ) {
        return of(book, episodes, List.of());
    }

    public static GroupBookResponse of(GroupBook book){
        return of(book, List.of(), List.of());
    }
}
