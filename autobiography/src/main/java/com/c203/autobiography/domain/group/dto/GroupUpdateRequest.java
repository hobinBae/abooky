package com.c203.autobiography.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUpdateRequest {

    @Schema(description = "그룹 이름", example = "가족")
    private String groupName;

    @Schema(description = "그룹 설명", example = "우리 가족 아카이브")
    private String description;

    @Schema(description = "그룹 테마색 설정", example = "#FFFFFF")
    private String themeColor;

    @Schema(description = "그룹 커버 이미지", example ="https://your-bucket.s3.ap-northeast-2.amazonaws.com/group/gid.jpg")
    @URL(message = "유효한 URL 형식이어야 합니다.")
    private String groupImageUrl;

}
