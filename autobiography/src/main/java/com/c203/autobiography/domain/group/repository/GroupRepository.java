package com.c203.autobiography.domain.group.repository;

import com.c203.autobiography.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    // 전체 그룹 조회 (deletedAt IS NULL)
    List<Group> findByDeletedAtIsNull();

    // 단일 그룹 조회 (ID와 deletedAt IS NULL)
    Optional<Group> findByGroupIdAndDeletedAtIsNull(Long groupId);

    // 리더가 생성한 그룹 조회
    List<Group> findByLeaderIdAndDeletedAtIsNull(Long leaderId);
}
