package com.c203.autobiography.global.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private PageInfo pageable;

    public static <T> PageResponse<T> from(Page<T> page) {
        PageInfo info = new PageInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
        return new PageResponse<>(page.getContent(), info);
    }
}

