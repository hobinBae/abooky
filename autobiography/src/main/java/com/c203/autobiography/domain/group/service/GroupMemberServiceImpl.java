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
                .map(gm -> GroupResponse.from(gm.getGroup()))
                .collect(Collectors.toList());
    }
}
