package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupApplyRequest;
import com.c203.autobiography.domain.group.dto.GroupApplyResponse;
import com.c203.autobiography.domain.group.entity.*;
import com.c203.autobiography.domain.group.repository.GroupApplyRepository;
import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.domain.group.repository.GroupRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupApplyServiceImpl implements GroupApplyService {

    private final GroupApplyRepository groupApplyRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    @Override
    public GroupApplyResponse createInvite(Long groupId, GroupApplyRequest groupApplyRequest) {
        Member receiver = memberRepository.findByEmail(groupApplyRequest.getReceiverEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // 중복 초대 방지
        boolean isMember = groupMemberRepository.findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, receiver.getMemberId())
                .isPresent();
        if (isMember) {
            throw new ApiException(ErrorCode.GROUP_MEMBER_ALREADY_EXISTS);
        }
        boolean exists = groupApplyRepository.existsByGroup_GroupIdAndReceiverIdAndStatus(
                groupId, receiver.getMemberId(), ApplyStatus.PENDING
        );
        if(exists) {
            throw new ApiException(ErrorCode.INVITE_ALREADY_EXISTS);
        }
        Group gg = groupRepository.getById(groupId);
        GroupApply entity = groupApplyRequest.toEntity(gg, receiver.getMemberId());
        GroupApply saved = groupApplyRepository.save(entity);

        Group g = groupRepository.findByGroupIdAndDeletedAtIsNull(saved.getGroupId())
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        Member leader = memberRepository.findById(g.getLeaderId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        return GroupApplyResponse.from(saved, g, leader);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupApplyResponse> listInvites(Long groupId) {
        List<GroupApply> pendingList = groupApplyRepository.findByGroup_GroupIdAndStatus(groupId, ApplyStatus.PENDING);
        return pendingList.stream()
                .map(ga -> {
                    Group g = groupRepository.findById(ga.getGroupId())
                            .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
                    Member leader = memberRepository.findById(g.getLeaderId())
                            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
                    return GroupApplyResponse.from(ga, g, leader);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupApplyResponse> listReceivedInvites(Long memberId) {
        List<GroupApply> invites = groupApplyRepository.findByReceiverIdAndStatus(memberId, ApplyStatus.PENDING);
        return invites.stream()
                .map(ga -> {
                    Group g = groupRepository.findById(ga.getGroupId())
                            .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
                    Member leader = memberRepository.findById(g.getLeaderId())
                            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
                    return GroupApplyResponse.from(ga, g, leader);
                })
                .collect(Collectors.toList());
    }

    @Override
    public GroupApplyResponse updateInviteStatus(Long groupId, Long groupApplyId, ApplyStatus status, Long actorId) {
        //기존 초대 내역 조회
        GroupApply apply =  groupApplyRepository.findById(groupApplyId)
                .filter(groupApply -> groupApply.getGroupId().equals(groupId))
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if(!apply.getReceiverId().equals(actorId))  {
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }
        apply.changeStatus(status);
        groupApplyRepository.save(apply);

        if(status == ApplyStatus.ACCEPTED) {
            boolean exists = groupMemberRepository
                    .findByGroupIdAndMemberIdAndDeletedAtIsNull(groupId, apply.getReceiverId())
                    .isPresent();
            if(!exists) {
                GroupMember newMember = GroupMember.builder()
                        .groupId(groupId)
                        .memberId(apply.getReceiverId())
                        .role(GroupRole.MEMBER)
                        .build();
                groupMemberRepository.save(newMember);
            }
        }
        Group g = groupRepository.findById(apply.getGroupId())
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        Member leader = memberRepository.findById(g.getLeaderId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return GroupApplyResponse.from(apply, g, leader);
    }
}
