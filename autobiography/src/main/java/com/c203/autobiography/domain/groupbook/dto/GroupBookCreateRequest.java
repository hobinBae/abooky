package com.c203.autobiography.domain.groupbook.dto;

import com.c203.autobiography.domain.book.dto.BookType;
import com.c203.autobiography.domain.book.entity.Book;
import com.c203.autobiography.domain.book.entity.BookCategory;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.groupbook.entity.GroupBook;
import com.c203.autobiography.domain.groupbook.entity.GroupType;
import com.c203.autobiography.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "그룹책 생성 요청 DTO")
public class GroupBookCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 가능합니다.")
    private String title;

    private String summary;

    @Schema(description = "책 타입 (선택사항, 기본값: AUTO)", example = "FREE_FORM")
    private BookType bookType;

    private GroupType groupType;

    private String coverImageUrl;

    /** null 허용: 카테고리를 지정하지 않을 수도 있습니다. */
    private Long categoryId;


    // 필요 시 서비스 계층에서 사용
    public GroupBook toEntity(Member member, Group group, BookCategory category, String coverImageUrl) {

        GroupType resolvedType = (groupType != null) ? groupType : GroupType.OTHER;
        BookType resolvedBookType = (bookType != null) ? bookType : BookType.AUTO;
        return GroupBook.builder()
            .member(member)
            .group(group)
            .title(title)
            .coverImageUrl(coverImageUrl)
            .summary(summary)
            .bookType(resolvedBookType)
            .groupType(resolvedType)
            .category(category)
            .build();
    }

}
