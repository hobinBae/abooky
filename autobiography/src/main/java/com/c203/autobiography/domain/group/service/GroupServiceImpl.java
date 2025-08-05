package com.c203.autobiography.domain.group.service;

import com.c203.autobiography.domain.group.dto.GroupCreateRequest;
import com.c203.autobiography.domain.group.dto.GroupResponse;
import com.c203.autobiography.domain.group.dto.GroupUpdateRequest;
import com.c203.autobiography.domain.group.entity.Group;
import com.c203.autobiography.domain.group.entity.GroupMember;
import com.c203.autobiography.domain.group.entity.GroupRole;
import com.c203.autobiography.domain.group.repository.GroupMemberRepository;
import com.c203.autobiography.domain.group.repository.GroupRepository;
import com.c203.autobiography.domain.member.entity.Member;
import com.c203.autobiography.domain.member.repository.MemberRepository;
import com.c203.autobiography.global.exception.ApiException;
import com.c203.autobiography.global.exception.ErrorCode;
import com.c203.autobiography.global.s3.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;

    private final FileStorageService fileStorageService;
    private static final String DEFAULT_IMAGE_URL = "https://ssafytrip.s3.ap-northeast-2.amazonaws.com/userProfile/default.png";

    @Transactional
    @Override
    public GroupResponse createGroup(Long leaderId, GroupCreateRequest request, MultipartFile file) {
        // 이미지 업로드
        String imageUrl = null;
        if(file != null && !file.isEmpty()) {
            imageUrl = fileStorageService.store(file, "groupProfiles");
            request.setGroupImageUrl(imageUrl);
        } else {
            request.setGroupImageUrl(DEFAULT_IMAGE_URL);
        }

        Group group = request.toEntity(leaderId);
        Group saved = groupRepository.save(group);

        // 생성하는 멤버도 GroupMember에 추가
        Member leader = memberRepository.findById(leaderId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        GroupMember leaderEntry = GroupMember.builder()
                .groupId(saved.getGroupId())
                .memberId(leaderId)
                .role(GroupRole.LEADER)
                .build();
        groupMemberRepository.save(leaderEntry);
        return GroupResponse.from(saved);
    }

    @Override
    public GroupResponse getGroup(Long groupId) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        return GroupResponse.from(group);
    }

    @Override
    public List<GroupResponse> listGroups(Long currentUserId) {
        return null;
    }

    @Override
    public GroupResponse updateGroup(Long leaderId, Long groupId, GroupUpdateRequest request, MultipartFile file) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        // 권한 체크
        if(!group.getLeaderId().equals(leaderId)){
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }

        // 이미지 교체 로직
        if(file != null && !file.isEmpty()) {
            // 기존 이미지 URL있고 디폴트 이미지가 아니라면 삭제
            String currentImageUrl = group.getGroupImageUrl();
            if(currentImageUrl != null && !currentImageUrl.isEmpty() && !currentImageUrl.equals(DEFAULT_IMAGE_URL)) {
                fileStorageService.delete(currentImageUrl);
            }
            String newImageUrl = fileStorageService.store(file, "groupProfiles");;
            request.setGroupImageUrl(newImageUrl);
        }

        group.updateInfo(request.getGroupName(), request.getDescription(), request.getThemeColor(), request.getGroupImageUrl());
        return GroupResponse.from(group);
    }

    @Transactional
    @Override
    public void deleteGroup(Long leaderId, Long groupId) {
        Group group = groupRepository.findByGroupIdAndDeletedAtIsNull(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        // 권한 체크
        if(!group.getLeaderId().equals(leaderId)){
            throw new ApiException(ErrorCode.GROUP_ACCESS_DENIED);
        }

        group.softDelete();
    }
}
