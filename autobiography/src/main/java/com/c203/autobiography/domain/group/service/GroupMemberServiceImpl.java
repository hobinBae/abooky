package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupMemberResponse;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupRole;
import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupMemberServiceImpl implements GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public void verifyMember(Long groupId, Long memberId) {
        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .isPresent();
        if(!isMember) {
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }

    }

    @Transactional(readOnly = true)
    @Override
    public List<GroupMemberResponse> listGroupMembers(Long groupId) {
        List<GroupMember> members = groupMemberRepository.findByGroupIdAndDeletedAtIsNull(groupId);
        return members.stream()
                .map(gm -> GroupMemberResponse.from(gm, gm.getMember()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, Long targetId, Long actorId) {
        GroupMember actor = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, actorId)
                .orElseThrow(()-> new ApiException(ErrorCode.GROUP_ACCESS_DENIED));

        if(!(actor.getRole() == GroupRole.LEADER || actor.getRole() == GroupRole.MANAGER)){
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }

        GroupMember target = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, targetId)
                .orElseThrow(()-> new ApiException(ErrorCode.USER_NOT_FOUND));
        target.softDelete();

    }

    @Override
    @Transactional
    public void leaveGroup(Long groupId, Long memberId) {
        GroupMember gm = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, memberId)
                .orElseThrow(()-> new ApiException(ErrorCode.USER_NOT_FOUND));
        gm.softDelete();
    }

    @Override
    public List<GroupResponse> listMyGroups(Long memberId) {
        return groupMemberRepository.findByMemberIdAndDeletedAtIsNull(memberId).stream()
                .map(GroupMember::getGroup)
                .filter(group -> group.getDeletedAt() == null) // 그룹이 삭제된 경우도 제외해야함
                .map(GroupResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void changeGroupMemberRole(Long groupId, Long targetMemberId, GroupRole newRole, Long actorId) {
        // 액션을 수행하는 사용자가 그룹의 리더인지 확인
        GroupMember actor = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, actorId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_ACCESS_DENIED));

        if (actor.getRole() != GroupRole.LEADER) {
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }

        // 대상 멤버 조회
        GroupMember target = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, targetMemberId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 리더는 역할 변경 불가
        if (target.getRole() == GroupRole.LEADER) {
            throw new ApiException(ErrorCode.GROUP_LEADER_ROLE_CANNOT_BE_CHANGED);
        }

        // 역할 변경
        target.changeRole(newRole);
    }
}
