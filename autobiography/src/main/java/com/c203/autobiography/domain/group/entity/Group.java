package com.c203.autobiography.domain.group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "`group`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "theme_color", length=20)
    private String themeColor;

    @Column(name = "group_image_url", length=255)
    private String groupImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** 그룹 정보 수정을 위한 메서드 **/
    public void updateInfo(String groupName, String description, String themeColor, String groupImageUrl) {
        if(groupName != null) this.groupName = groupName;
        if(description != null) this.description = description;
        if(themeColor != null) this.themeColor = themeColor;
        if(groupImageUrl != null) this.groupImageUrl = groupImageUrl;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

}
