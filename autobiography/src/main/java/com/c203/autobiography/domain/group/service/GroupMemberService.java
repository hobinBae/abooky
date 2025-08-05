package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupMemberResponse;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.entity.GroupMember;

import java.util.List;

public interface GroupMemberService {
    List<GroupMemberResponse> listGroupMembers(Long groupId);

    /** 그룹원 강퇴 (관리자 전용) **/
    void removeMember(Long groupId, Long targetId, Long actorId);

    /** 그룹 탈퇴 **/
    void leaveGroup(Long groupId, Long memberId);

    /** 내가 속한 그룹 보기 **/
    List<GroupResponse> listMyGroups(Long memberId);

}
