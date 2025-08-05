package com.c203.autobiography.domain.group.repository;

import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupMemberId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends CrudRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findByGroupIdAndDeletedAtIsNull(Long groupId);
    List<GroupMember> findByMemberIdAndDeletedAtIsNull(Long memberId);
    Optional<GroupMember> findByGroupIdAndMemberIdAndDeletedAtIsNull(Long groupId, Long memberId);
}
