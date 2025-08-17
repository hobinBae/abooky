<template>
  <CustomAlert ref="customAlert" />
  <div class="group-timeline-page">
    <div v-if="group" class="page-layout">
      <aside class="member-sidebar">
        <div class="sidebar-header">
          <h2 class="sidebar-title">그룹 멤버</h2>
          <button v-if="canManageGroup(group)" @click="openMemberManagement" class="btn btn-sm btn-secondary">
            <i class="bi bi-people-fill"></i> 관리
          </button>
        </div>
        <ul class="sidebar-member-list">
          <li v-for="member in group.members" :key="member.memberId" class="sidebar-member-item">
            <router-link :to="`/author/${member.memberId}`" class="member-link" :title="`${member.nickname} 페이지로 이동`">
              <span class="member-name">{{ member.nickname }}</span>
            </router-link>
            <span class="member-role" :class="getRoleClass(getMemberRole(group, member))">
              {{ getMemberRole(group, member) }}
            </span>
          </li>
        </ul>
      </aside>

      <div class="timeline-container">
        <header class="timeline-header">
          <div class="header-content">
            <h1>{{ group.groupName }} 타임라인</h1>
            <p>{{ group.description || '그룹 설명이 없습니다.' }}</p>
          </div>
          <div class="header-actions">
            <button v-if="isGroupOwner(group)" @click="openGroupSettings" class="btn btn-secondary settings-btn">
              <i class="bi bi-gear-fill"></i> 그룹 설정
            </button>
            <button v-if="!isGroupOwner(group)" @click="leaveGroupHandler(group.id)" class="btn btn-danger settings-btn">
              <i class="bi bi-box-arrow-right"></i> 그룹 탈퇴
            </button>
          </div>
        </header>

        <section class="add-event-section">
          <h3>새로운 타임라인 이벤트 추가</h3>
          <div class="event-form">
            <input type="date" v-model="newEvent.date" class="form-control">
            <input type="text" v-model="newEvent.title" placeholder="이벤트 제목" class="form-control">
            <textarea v-model="newEvent.description" placeholder="이벤트 내용" class="form-control"></textarea>
            <button @click="addTimelineEvent" class="btn btn-primary">이벤트 추가</button>
          </div>
        </section>

        <div class="timeline">
          <div v-for="event in sortedTimeline" :key="event.id" class="timeline-item">
            <div class="timeline-icon">
              <i :class="event.icon || 'bi bi-calendar-event'"></i>
            </div>
            <div class="timeline-content">
              <button v-if="event.isCustom" @click="deleteTimelineEvent(event.id)" class="btn-delete-event" title="이벤트 삭제">
                <i class="bi bi-trash"></i>
              </button>
              <span class="timeline-date">{{ new Date(event.date).toLocaleDateString() }}</span>
              <h4 class="timeline-title">{{ event.title }}</h4>
              <p class="timeline-description">{{ event.description }}</p>
              <router-link v-if="event.episodeLink" :to="event.episodeLink" class="episode-link">
                『{{ event.title }}』 보러가기
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="loading-message">
      <p>타임라인 정보를 불러오는 중입니다...</p>
    </div>

    <!-- 그룹 정보 수정/삭제 모달 -->
    <div v-if="isGroupInfoModalVisible" class="modal-backdrop">
      <div class="modal-content">
        <button @click="closeGroupSettingsModal" class="close-button" title="닫기">
          <i class="bi bi-x-lg"></i>
        </button>
        <div v-if="selectedGroup && isGroupOwner(selectedGroup)">
          <h2 class="modal-title">그룹 정보 설정</h2>
          <div class="settings-section">
            <div class="form-group">
              <label for="group-name" class="modal-label">그룹 이름</label>
              <input type="text" id="group-name" v-model="groupInfoForEdit.groupName" class="form-control">
            </div>
            <div class="form-group">
              <label for="group-description" class="modal-label">그룹 설명</label>
              <textarea id="group-description" v-model="groupInfoForEdit.description" class="form-control" rows="3"></textarea>
            </div>
            <div class="form-actions">
              <button @click="saveGroupSettings" class="btn btn-primary">변경사항 저장</button>
            </div>
          </div>
          <div class="settings-section danger-zone">
            <h3 class="settings-section-title">그룹 삭제</h3>
            <p>그룹을 삭제하면 모든 관련 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.</p>
            <button @click="deleteGroup(selectedGroup.id)" class="btn btn-danger">그룹 삭제</button>
          </div>
        </div>
        <div v-else>
          <h2 class="modal-title">그룹 설정</h2>
          <p>그룹장만 그룹 정보를 수정할 수 있습니다.</p>
        </div>
      </div>
    </div>

    <!-- 멤버 관리 모달 -->
    <div v-if="isMemberManagementModalVisible" class="modal-backdrop">
      <div class="modal-content modal-lg">
        <button @click="closeMemberManagementModal" class="close-button" title="닫기">
          <i class="bi bi-x-lg"></i>
        </button>
        <div v-if="selectedGroup">
          <h2 class="modal-title">그룹 멤버 관리</h2>
          <div class="settings-section">
            <h3 class="settings-section-title">멤버 목록</h3>
            <ul class="member-list">
              <li v-for="member in selectedGroup.members" :key="member.memberId" class="member-item">
                <span class="member-name">{{ member.nickname }}</span>
                <span class="member-role" :class="getRoleClass(getMemberRole(selectedGroup, member))">
                  {{ getMemberRole(selectedGroup, member) }}
                </span>
                <div class="member-actions">
                  <button v-if="isGroupOwner(selectedGroup) && String(member.memberId) !== selectedGroup.ownerId" @click="toggleManager(selectedGroup, member)" class="btn btn-sm">
                    {{ isManager(selectedGroup, member) ? '매니저 해제' : '매니저 임명' }}
                  </button>
                  <button v-if="canRemoveMember(selectedGroup, member)" @click="removeMember(selectedGroup, member)" class="btn btn-sm btn-danger">
                    내보내기
                  </button>
                </div>
              </li>
            </ul>
          </div>
          <div v-if="canManageGroup(selectedGroup)" class="settings-section">
            <h3 class="settings-section-title">멤버 초대</h3>
            <div class="invite-form">
              <input type="email" v-model="inviteEmail" placeholder="초대할 멤버의 이메일" class="form-control">
              <button @click="inviteMemberHandler" class="btn btn-primary">초대 요청</button>
            </div>
          </div>
          <div class="settings-section">
            <h3 class="settings-section-title">보낸 초대 목록</h3>
            <ul v-if="sentInvites.length > 0" class="invite-list">
              <li v-for="invite in sentInvites" :key="invite.groupApplyId" class="invite-item">
                <span>{{ invite.receiverNickname }}님에게 보낸 초대: {{ getInviteStatusText(invite.status) }}</span>
                <button v-if="invite.status !== 'PENDING'" @click="removeInviteFromList(invite.groupApplyId)" class="btn-remove-invite" title="목록에서 제거">
                  &times;
                </button>
              </li>
            </ul>
            <p v-else>보낸 초대가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { groupService, type Group as GroupResponse, type GroupMember, type GroupInvite } from '@/services/groupService';
import apiClient from '@/api';
import { useAuthStore } from '@/stores/auth';
import CustomAlert from '@/components/common/CustomAlert.vue';
import '@/styles/group-timeline.css';

// --- Interfaces ---
interface Group extends GroupResponse {
  id: string;
  ownerId: string;
  managers: GroupMember[];
  members: GroupMember[];
}
interface TimelineEvent {
  id: string;
  date: string;
  title: string;
  description: string;
  icon?: string;
  episodeLink?: string;
  groupId: string;
  isCustom?: boolean; // 사용자가 직접 추가한 이벤트인지 여부
}


// --- Router & User ---
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const groupId = computed(() => route.params.id as string);
const currentUser = computed(() => authStore.user);

// --- Reactive State ---
const group = ref<Group | null>(null);
const timeline = ref<TimelineEvent[]>([]);
const newEvent = ref({
  date: new Date().toISOString().split('T')[0],
  title: '',
  description: '',
});
const isGroupInfoModalVisible = ref(false);
const isMemberManagementModalVisible = ref(false);
const selectedGroup = ref<Group | null>(null);
const groupInfoForEdit = ref({ groupName: '', description: '' });
const inviteEmail = ref('');
const sentInvites = ref<GroupInvite[]>([]);
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);

// --- Watchers ---
watch([isGroupInfoModalVisible, isMemberManagementModalVisible], ([infoVisible, memberVisible]) => {
  if (infoVisible || memberVisible) {
    document.body.style.overflow = 'hidden';
  } else {
    document.body.style.overflow = '';
  }
});

// --- Computed Properties ---
const sortedTimeline = computed(() => {
  // 최신 날짜가 위로 오도록 내림차순 정렬
  return [...timeline.value].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
});

// const isOwnerOrManager = computed(() => {
//   if (!group.value || !currentUser.value) return false;
//   const currentUserIsOwner = String(group.value.leaderId) === String(currentUser.value.memberId);
//   // managers 배열이 GroupMember 객체 배열이라고 가정하고 수정합니다.
//   const currentUserIsManager = group.value.managers.some(m => String(m.memberId) === String(currentUser.value!.memberId));
//   return currentUserIsOwner || currentUserIsManager;
// });

// --- Functions ---
async function fetchGroupInfo() {
  const details = await groupService.fetchGroupDetails(groupId.value);
  if (details) {
    const members = await groupService.fetchGroupMembers(groupId.value);
    group.value = {
      ...details,
      id: String(details.groupId),
      ownerId: String(details.leaderId),
      managers: [], // TODO: 백엔드에서 매니저 정보를 별도로 제공하지 않으므로 임시로 빈 배열을 할당합니다.
      members: members,
    };
  }
}

async function setupTimelineListener() {
  if (!group.value) return;

  const events: TimelineEvent[] = [];

  // 1. 그룹 생성 이벤트 추가
  events.push({
    id: `group-creation-${group.value.id}`,
    groupId: group.value.id,
    date: group.value.createdAt,
    title: '그룹 결성',
    description: `${group.value.groupName} 그룹이 생성되었습니다.`,
    icon: 'bi-star-fill',
  });

  // 2. 멤버 가입 이벤트 추가 (그룹장 제외)
  group.value.members.forEach(member => {
    if (String(member.memberId) !== group.value!.ownerId && member.joinedAt) {
      events.push({
        id: `member-joined-${member.memberId}`,
        groupId: group.value!.id,
        date: member.joinedAt,
        title: '새 멤버 참여',
        description: `${member.nickname}님이 그룹에 추가되었습니다.`,
        icon: 'bi-person-plus-fill',
      });
    }
  });

  // 3. 생일 이벤트 추가 (이번 달 생일자만)
  const today = new Date();
  const currentMonth = today.getMonth();
  const currentYear = today.getFullYear();

  group.value.members.forEach(member => {
    if (member.birthDate) {
      const birthDate = new Date(member.birthDate);
      if (birthDate.getMonth() === currentMonth) {
        const birthdayThisYear = new Date(currentYear, birthDate.getMonth(), birthDate.getDate());
        events.push({
          id: `birthday-${member.memberId}`,
          groupId: group.value!.id,
          date: birthdayThisYear.toISOString(),
          title: '생일 축하',
          description: `🎂 ${member.nickname}님의 생일을 축하합니다!`,
          icon: 'bi-cake2-fill',
        });
      }
    }
  });

  // 4. 그룹 책 추가 이벤트 생성
  try {
    if (!group.value) return;
    const currentGroup = group.value;
    const books = await groupService.fetchGroupBooks(currentGroup.id);
    console.log('API Response for Group Books:', books); // 응답 데이터 구조 확인용
    books.forEach(book => {
      const authorName = book.nickname || '멤버'; // nickname 필드로 변경
      events.push({
        id: `book-added-${book.groupBookId}`,
        groupId: currentGroup.id,
        date: book.createdAt,
        title: book.title, // title 필드가 있다고 가정
        description: `${authorName}님이 『${book.title}』을(를) 그룹에 추가했습니다.`,
        icon: 'bi-book-fill',
        episodeLink: `/group-book-detail/${currentGroup.id}/${book.groupBookId}`,
      });
    });
  } catch (error) {
    console.error('그룹 책 목록 조회 실패:', error);
  }

  timeline.value = events;
}
async function addTimelineEvent() {
  if (!newEvent.value.title || !newEvent.value.description) {
    customAlert.value?.showAlert({ message: '이벤트 제목과 내용을 모두 입력해주세요.' });
    return;
  }

  try {
    const event: TimelineEvent = {
      id: `t${Date.now()}`,
      groupId: groupId.value,
      date: newEvent.value.date,
      title: newEvent.value.title,
      description: newEvent.value.description,
      icon: 'bi-check-lg',
      isCustom: true, // 사용자 추가 이벤트로 표시
    };

    // TODO: 실제 API 호출로 교체
    console.log('TODO: 타임라인 이벤트 추가 API 호출', event);
    // await timelineService.addTimelineEvent(event);

    timeline.value.unshift(event);
    newEvent.value = { date: new Date().toISOString().split('T')[0], title: '', description: '' };
    customAlert.value?.showAlert({ message: '이벤트가 추가되었습니다.' });
  } catch (error) {
    console.error('이벤트 추가 실패:', error);
    customAlert.value?.showAlert({ message: '이벤트 추가에 실패했습니다.' });
  }
}

async function deleteTimelineEvent(eventId: string) {
  const confirmed = await customAlert.value?.showConfirm({ message: '이 이벤트를 정말 삭제하시겠습니까?' });
  if (confirmed) {
    timeline.value = timeline.value.filter(event => event.id !== eventId);
  }
}
function openGroupSettings() {
  if (!group.value) return;
  selectedGroup.value = JSON.parse(JSON.stringify(group.value));
  if (selectedGroup.value) {
    groupInfoForEdit.value.groupName = selectedGroup.value.groupName;
    groupInfoForEdit.value.description = selectedGroup.value.description || '';
  }
  isGroupInfoModalVisible.value = true;
}

function closeGroupSettingsModal() {
  isGroupInfoModalVisible.value = false;
  selectedGroup.value = null;
}

function openMemberManagement() {
  if (!group.value) return;
  selectedGroup.value = JSON.parse(JSON.stringify(group.value));
  isMemberManagementModalVisible.value = true;
  fetchSentInvites();
}

function closeMemberManagementModal() {
  isMemberManagementModalVisible.value = false;
  selectedGroup.value = null;
}

// --- Group Settings Functions ---
const isGroupOwner = (g: Group) => currentUser.value && String(g.leaderId) === String(currentUser.value.memberId);
const isManager = (g: Group, member: GroupMember) => {
  return member.role === 'MANAGER';
} 
const canManageGroup = (g: Group) => {
  if(isGroupOwner(g)) return true;
  if(!currentUser.value || !g.members) return false;
  const currentMember = g.members.find(m => m.memberId === currentUser.value!.memberId);
  return currentMember?.role === 'MANAGER';
}
// const canInvite = (g: Group) => isGroupOwner(g) || (currentUser.value && g.managers.some(m => m.memberId === currentUser.value!.memberId));
const canToggleManager = (g: Group, member: GroupMember) => {
  if(!isGroupOwner(g)) {
    customAlert.value?.showAlert({ message: '그룹장만 매니저를 임명하거나 해제할 수 있습니다.' });
    return false;
  }
  if(String(g.leaderId) === String(member.memberId)) return false;
  return true;
};
const canRemoveMember = (g: Group, member: GroupMember) => {
  if (String(g.leaderId) === String(member.memberId)) return false;
  const amIOwner = isGroupOwner(g);
  const amIManager = currentUser.value && g.managers.some(m => m.memberId === currentUser.value!.memberId);
  const isTargetManager = isManager(g, member);
  if (amIOwner) return true;
  if (amIManager && !isTargetManager) return true;
  return false;
};
const getMemberRole = (g: Group, member: GroupMember) => {
  if (String(g.leaderId) === String(member.memberId)) return '그룹장';
  if (isManager(g, member)) return '매니저';
  return '멤버';
};
const getRoleClass = (role: string) => {
  if (role === '그룹장') return 'role-owner';
  if (role === '매니저') return 'role-manager';
  return 'role-member';
};

const getInviteStatusText = (status: 'PENDING' | 'ACCEPTED' | 'DENIED') => {
  if (status === 'PENDING') return '초대 수락 대기 중';
  if (status === 'ACCEPTED') return '초대 수락됨';
  if (status === 'DENIED') return '초대 거절됨';
  return '';
};

const removeInviteFromList = (groupApplyId: number) => {
  sentInvites.value = sentInvites.value.filter(invite => invite.groupApplyId !== groupApplyId);
};

const toggleManager = async (g: Group, member: GroupMember) => {
  // TODO: 매니저 임명/해제 API 연동 필요
  if (!selectedGroup.value) return;
  if(!canToggleManager(g, member)) {
    customAlert.value?.showAlert({ message: '매니저 임명/해제 권한이 없습니다.' });
    return;
  }
  try {
    const managers = selectedGroup.value.managers || [];
    const members = selectedGroup.value.members || [];
    const isCurrentlyManager = managers.some(m => m.memberId === member.memberId);
    const newRole = isCurrentlyManager ? 'MEMBER' : 'MANAGER';

    await groupService.changeGroupMemberRole(String(g.groupId), member.memberId, newRole);

    //API 호출 성공 시에만 UI 상태 즉시 업데이트
    const memberIndex = members.findIndex(m => m.memberId === member.memberId);
    if(memberIndex > -1) {
      members[memberIndex].role = newRole;
    }
    const managerIndex = managers.findIndex(m => m.memberId === member.memberId);
    if(isCurrentlyManager) {
      if(managerIndex > -1) {
        managers.splice(managerIndex, 1);
      }
      customAlert.value?.showAlert({ message: `${member.nickname}님이 매니저에서 멤버로 변경되었습니다.` });
    } else {
      if(managerIndex === -1) {
        const updatedMember = members.find(m => m.memberId === member.memberId);
        if(updatedMember) {
          managers.push(updatedMember);
        }
      }
      customAlert.value?.showAlert({ message: `${member.nickname}님이 매니저로 임명되었습니다.` });
    }
    selectedGroup.value = { ...selectedGroup.value };

  } catch(error) {
    console.error('역할 변경 실패:', error);
    customAlert.value?.showAlert({ message: error instanceof Error ? error.message : '역할 변경 중 오류가 발생했습니다.' });
  }
};

const removeMember = async (g: Group, member: GroupMember) => {
  if (!selectedGroup.value) return;
  const confirmed = await customAlert.value?.showConfirm({ message: `'${member.nickname}'님을 그룹에서 내보내시겠습니까?` });
  if (confirmed) {
    const success = await groupService.kickMember(g.id, member.memberId);
    if (success) {
      customAlert.value?.showAlert({ message: `${member.nickname}님을 내보냈습니다.` });
      // 멤버 목록 새로고침
      selectedGroup.value.members = selectedGroup.value.members.filter(m => m.memberId !== member.memberId);
      if (group.value) {
        group.value.members = group.value.members.filter(m => m.memberId !== member.memberId);
      }
    } else {
      customAlert.value?.showAlert({ message: '멤버를 내보내는 데 실패했습니다.' });
    }
  }
};

const inviteMemberHandler = async () => {
  if (!selectedGroup.value || !inviteEmail.value) {
    customAlert.value?.showAlert({ message: '초대할 멤버의 이메일을 입력해주세요.' });
    return;
  }
  const result = await groupService.inviteMember(selectedGroup.value.id, inviteEmail.value);
  if (result) {
    customAlert.value?.showAlert({ message: '초대를 보냈습니다.' });
    sentInvites.value.push(result);
    inviteEmail.value = '';
  } else {
    customAlert.value?.showAlert({ message: '초대 보내기에 실패했습니다.' });
  }
};

const fetchSentInvites = async () => {
  if (!group.value) return;
  sentInvites.value = await groupService.fetchSentInvites(group.value.id);
};

const deleteGroup = async (groupIdToDelete: string) => {
  const confirmed = await customAlert.value?.showConfirm({ message: '정말로 그룹을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.' });
  if (confirmed) {
    try {
      await apiClient.delete(`/api/v1/groups/${groupIdToDelete}`);
      customAlert.value?.showAlert({ message: '그룹이 삭제되었습니다.' });
      router.push('/my-library');
    } catch (error) {
      console.error('그룹 삭제 실패:', error);
      customAlert.value?.showAlert({ message: '그룹 삭제 중 오류가 발생했습니다.' });
    }
  }
};

const leaveGroupHandler = async (groupIdToLeave: string) => {
  const confirmed = await customAlert.value?.showConfirm({ message: '정말로 그룹을 탈퇴하시겠습니까?' });
  if (confirmed) {
    const success = await groupService.leaveGroup(groupIdToLeave);
    if (success) {
      customAlert.value?.showAlert({ message: '그룹에서 탈퇴했습니다.' });
      router.push('/my-library');
    } else {
      customAlert.value?.showAlert({ message: '그룹 탈퇴에 실패했습니다.' });
    }
  }
};

async function saveGroupSettings() {
  if (!selectedGroup.value) return;

  const formData = new FormData();
  formData.append('groupName', groupInfoForEdit.value.groupName);
  formData.append('description', groupInfoForEdit.value.description || '');
  // TODO: 이미지 파일 변경 로직 추가 필요

  const updatedGroup = await groupService.updateGroup(selectedGroup.value.id, formData);
  if (updatedGroup) {
    customAlert.value?.showAlert({ message: '그룹 정보가 성공적으로 수정되었습니다.' });
    await fetchGroupInfo(); // 현재 페이지 정보 새로고침
    closeGroupSettingsModal();
  } else {
    customAlert.value?.showAlert({ message: '그룹 정보 수정에 실패했습니다.' });
  }
}
onMounted(async () => {
  await fetchGroupInfo();

  if (group.value && currentUser.value) {
    const isMember = group.value.members.some(member => member.memberId === currentUser.value!.memberId);
    if (!isMember) {
      customAlert.value?.showAlert({ message: '이 그룹의 멤버가 아닙니다.' });
      router.go(-1);
      return;
    }
  } else if (!group.value) {
    // 그룹 정보 로드 실패 시 (예: 존재하지 않는 그룹)
    customAlert.value?.showAlert({ message: '존재하지 않는 그룹입니다.' });
    router.go(-1);
    return;
  }

  fetchSentInvites();
  // setupTimelineListener는 타임라인 API 연동 시 함께 수정합니다.
  setupTimelineListener();
});
</script>

<style scoped>
/* 이제 이 파일의 스타일은 @/styles/group-timeline.css에서 관리됩니다. */
</style>
