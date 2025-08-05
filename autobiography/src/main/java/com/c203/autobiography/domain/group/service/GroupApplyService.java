package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupApplyRequest;
import com.c203.autobiography.domain.group.dto.GroupApplyResponse;

import java.util.List;

public interface GroupApplyService {
    GroupApplyResponse createInvite(Long groupId, GroupApplyRequest groupApplyRequest);
    List<GroupApplyResponse> listInvites(Long groupId);
    List<GroupApplyResponse> listReceivedInviteds(Long memberId);
}
