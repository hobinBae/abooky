package com.c203.autobiography.domain.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "book_category",
        uniqueConstraints = @UniqueConstraint(
                columnNames = "category_name",
                name = "uk_book_category_name"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookCategory {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "book_category_id")
        private Long bookCategoryId;

        @NotBlank
        @Size(max = 50)
        @Column(name = "category_name", nullable = false, length = 50)
        private String categoryName;
        /**
         * 카테고리 이름을 변경합니다.
         */
        public void updateName(String categoryName) {
                this.categoryName = categoryName;
        }
        public static BookCategory of(String categoryName) {
                return BookCategory.builder()
                        .categoryName(categoryName)
                        .build();
        }
}
