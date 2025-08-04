package com.c203.autobiography.domain.group.repository;

import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupMemberId;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface GroupMemberRepository extends CrudRepository<GroupMember, GroupMemberId> {
    List<GroupMember> findByGroupIdAndDeletedAtIsNull(Long groupId);
}
