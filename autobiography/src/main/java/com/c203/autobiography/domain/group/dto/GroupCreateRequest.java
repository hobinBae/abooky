package com.c203.autobiography.domain.group.dto;

import com.c203.autobiography.domain.group.entity.Group;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupCreateRequest {

    @Schema(description = "그룹 이름", example = "가족")
    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String groupName;

    @Schema(description = "그룹 설명", example = "우리 가족 아카이브")
    private String description;

    @Schema(description = "그룹 테마색 설정", example = "#FFFFFF")
    @Pattern(
            regexp = "^#([A-Fa-f0-9]{6})$",
            message = "올바른 HEX 컬러여야 합니다."
    )
    private String themeColor;

    @Schema(description = "그룹 커버 이미지", example ="https://your-bucket.s3.ap-northeast-2.amazonaws.com/group/gid.jpg")
    @URL(message = "유효한 URL 형식이어야 합니다.")
    private String groupImageUrl;

    public Group toEntity(Long leaderId) {
        return Group.builder()
                .groupName(groupName)
                .leaderId(leaderId)
                .description(description)
                .themeColor(themeColor)
                .groupImageUrl(groupImageUrl)
                .build();
    }
}
