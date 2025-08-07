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
        name = "tag",
        uniqueConstraints = @UniqueConstraint(columnNames = "tag_name", name = "uk_tag_name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @NotBlank
    @Size(max = 50)
    @Column(name = "tag_name", length = 50, nullable = false)
    private String tagName;

    /**
     * 태그 이름을 변경할 때 사용
     */
    public void updateName(String newName){
        this.tagName = newName;
    }
}
