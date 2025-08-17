package com.c203.autobiography.domain.groupbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class GroupBookTagId implements Serializable {

    @Column(name = "group_book_id", nullable = false)
    private Long groupBookId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
