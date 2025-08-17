package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupCreateRequest;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.dto.GroupUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupService {
    public GroupResponse createGroup(Long leaderId, GroupCreateRequest request, MultipartFile file);
    public GroupResponse getGroup(Long groupId);
    List<GroupResponse> listGroups(Long memberId);
    public GroupResponse updateGroup(Long leaderId, Long groupId, GroupUpdateRequest request, MultipartFile file);
    public void deleteGroup(Long leaderId, Long groupId);
}
