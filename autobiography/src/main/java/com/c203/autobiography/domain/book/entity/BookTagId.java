package com.c203.autobiography.domain.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(staticName = "of")
public class BookTagId implements Serializable {

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "tag_id", nullable = false)
    private Long tagId;
}
