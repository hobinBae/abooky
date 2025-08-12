package com.c203.autobiography.domain.communityBook.dto;

import com.c203.autobiography.domain.book.entity.Tag;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "커뮤니티 책 태그 응답")
public class CommunityBookTagResponse {
    private Long tagId;
    private String tagName;

    // Tag 엔티티로부터 생성
    public static CommunityBookTagResponse from(Tag tag) {
        return CommunityBookTagResponse.builder()
                .tagId(tag.getTagId())
                .tagName(tag.getTagName())
                .build();
    }

    // 생성자 직접 사용
    public static CommunityBookTagResponse of(Long tagId, String tagName) {
        return CommunityBookTagResponse.builder()
                .tagId(tagId)
                .tagName(tagName)
                .build();
    }
}
