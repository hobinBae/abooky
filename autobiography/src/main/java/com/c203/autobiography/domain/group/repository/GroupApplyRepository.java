package com.c203.autobiography.domain.group.repository;

import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.GroupApply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupApplyRepository extends JpaRepository<GroupApply, Long> {
    List<GroupApply> findByGroup_GroupIdAndStatus(Long groupId, ApplyStatus status);
    List<GroupApply> findByReceiverIdAndStatus(Long receiverId, ApplyStatus status);
    boolean existsByGroup_GroupIdAndReceiverIdAndStatus(Long groupId, Long receiverId, ApplyStatus status);
}
