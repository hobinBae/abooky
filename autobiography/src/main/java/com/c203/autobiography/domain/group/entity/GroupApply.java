package com.c203.autobiography.domain.group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_apply")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_apply_id")
    private Long groupApplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @CreationTimestamp
    @Column(name = "invited_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime invitedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplyStatus status;

    /** 편의 getter **/
    public Long getGroupId() {
        return group.getGroupId();
    }

    /** 초대 상태 변경 메서드 **/
    public void changeStatus(ApplyStatus newStatus) {
        this.status = newStatus;
    }
}
