package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupApplyRequest;
import com.c203.autobiography.domain.group.dto.GroupApplyResponse;
import com.c203.autobiography.domain.group.entity.ApplyStatus;
import com.c203.autobiography.domain.group.entity.GroupApply;
import com.c203.autobiography.domain.group.repository.GroupApplyRepository;
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

    @Transactional
    @Override
    public GroupApplyResponse createInvite(Long groupId, GroupApplyRequest groupApplyRequest) {
        Member receiver = memberRepository.findById(groupApplyRequest.getReceiverId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        GroupApply saved = groupApplyRepository.save(groupApplyRequest.toEntity(groupId));
        return GroupApplyResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupApplyResponse> listInvites(Long groupId) {
        List<GroupApply> pendingList = groupApplyRepository.findByGroupIdAndStatus(groupId, ApplyStatus.PENDING);
        return pendingList.stream()
                .map(GroupApplyResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupApplyResponse> listReceivedInviteds(Long memberId) {
        List<GroupApply> invites = groupApplyRepository.findByReceiverIdAndStatus(memberId, ApplyStatus.PENDING);
        return invites.stream()
                .map(GroupApplyResponse::from)
                .collect(Collectors.toList());
    }
}
