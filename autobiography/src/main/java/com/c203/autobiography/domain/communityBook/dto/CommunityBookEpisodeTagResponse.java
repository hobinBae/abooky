package com.c203.autobiography.domain.communityBook.dto;

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
public class CommunityBookEpisodeTagResponse {
    private Long communityBookEpisodeTagId;
    private String communityBookEpisodeTagName;
}
