<template>
  <div class="group-timeline-page">
    <div v-if="group" class="page-layout">
      <aside class="member-sidebar">
        <h2 class="sidebar-title">그룹 멤버</h2>
        <ul class="sidebar-member-list">
          <li v-for="member in group.members" :key="member" class="sidebar-member-item">
            <router-link :to="`/member/${member}`" class="member-link" :title="`${member} 페이지로 이동`">
              <span class="member-name">{{ member }}</span>
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
            <p>우리 그룹의 소중한 순간들과 이야기의 흐름을 확인해보세요.</p>
          </div>
          <button @click="openGroupSettings" class="btn btn-secondary settings-btn">
            <i class="bi bi-gear-fill"></i> 그룹 설정
          </button>
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
              <span class="timeline-date">{{ new Date(event.date).toLocaleDateString() }}</span>
              <h4 class="timeline-title">{{ event.title }}</h4>
              <p class="timeline-description">{{ event.description }}</p>
              <router-link v-if="event.episodeLink" :to="event.episodeLink" class="episode-link">
                관련 에피소드 보기
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="loading-message">
      <p>타임라인 정보를 불러오는 중입니다...</p>
    </div>

    <div v-if="isGroupSettingsModalVisible" class="modal-backdrop">
      <div class="modal-content modal-lg">
        <button @click="closeGroupSettingsModal" class="close-button" title="닫기">
          <i class="bi bi-x-lg"></i>
        </button>
        <div v-if="selectedGroup">
          <h2 class="modal-title">그룹 관리: {{ selectedGroup.groupName }}</h2>
          <div class="settings-section">
            <h3 class="settings-section-title">멤버 관리</h3>
            <ul class="member-list">
              <li v-for="member in selectedGroup.members" :key="member" class="member-item">
                <span class="member-name">{{ member }}</span>
                <span class="member-role">{{ getMemberRole(selectedGroup, member) }}</span>
                <div class="member-actions">
                  <button v-if="isGroupOwner(selectedGroup) && member !== selectedGroup.ownerId" @click="toggleManager(selectedGroup, member)" class="btn btn-sm">
                    {{ isManager(selectedGroup, member) ? '매니저 해제' : '매니저 임명' }}
                  </button>
                  <button v-if="canRemoveMember(selectedGroup, member)" @click="removeMember(selectedGroup, member)" class="btn btn-sm btn-danger">
                    내보내기
                  </button>
                </div>
              </li>
            </ul>
          </div>
          <div v-if="canInvite(selectedGroup)" class="settings-section">
            <h3 class="settings-section-title">멤버 초대</h3>
            <div class="invite-form">
              <input type="text" v-model="inviteSearchQuery" placeholder="초대할 멤버의 아이디 또는 닉네임으로 검색" class="form-control">
              <button @click="searchMember" class="btn btn-primary">검색</button>
            </div>
            <div v-if="inviteSearchResult" class="search-result">
              <p>검색 결과: {{ inviteSearchResult }}</p>
              <button @click="inviteMember" class="btn btn-success">초대하기</button>
            </div>
          </div>
          <div v-if="isGroupOwner(selectedGroup)" class="settings-section danger-zone">
            <h3 class="settings-section-title">그룹 삭제</h3>
            <p>그룹을 삭제하면 모든 관련 데이터가 영구적으로 삭제됩니다. 이 작업은 되돌릴 수 없습니다.</p>
            <button @click="deleteGroup(selectedGroup.id)" class="btn btn-danger">그룹 삭제</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// --- Interfaces ---
interface Group {
  id: string;
  groupName: string;
  ownerId: string;
  managers: string[];
  members: string[];
}
interface TimelineEvent {
  id: string;
  date: string;
  title: string;
  description: string;
  icon?: string;
  episodeLink?: string;
  groupId: string;
}


// --- Router & User ---
const route = useRoute();
const router = useRouter();
const groupId = computed(() => route.params.id as string);
const currentUserNickname = ref('김작가');

// --- Reactive State ---
const group = ref<Group | null>(null);
const timeline = ref<TimelineEvent[]>([]);
const newEvent = ref({
  date: new Date().toISOString().split('T')[0],
  title: '',
  description: '',
});
const isGroupSettingsModalVisible = ref(false);
const selectedGroup = ref<Group | null>(null);
const inviteSearchQuery = ref('');
const inviteSearchResult = ref<string | null>(null);

// --- Computed Properties ---
const sortedTimeline = computed(() => {
  return [...timeline.value].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
});

// --- Functions ---
async function fetchGroupInfo() {
  try {
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 그룹 정보를 API에서 가져오기', groupId.value);
    // const response = await groupService.getGroupInfo(groupId.value);
    // group.value = response.data;
  } catch (error) {
    console.error('그룹 정보 조회 실패:', error);
  }
}

async function setupTimelineListener() {
  try {
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 타임라인 이벤트를 API에서 가져오기', groupId.value);
    // const response = await timelineService.getTimelineEvents(groupId.value);
    // timeline.value = response.data;
  } catch (error) {
    console.error('타임라인 조회 실패:', error);
  }
}
async function addTimelineEvent() {
  if (!newEvent.value.title || !newEvent.value.description) {
    alert('이벤트 제목과 내용을 모두 입력해주세요.');
    return;
  }
  
  try {
    const event: TimelineEvent = {
      id: `t${Date.now()}`,
      groupId: groupId.value,
      date: newEvent.value.date,
      title: newEvent.value.title,
      description: newEvent.value.description,
      icon: 'bi-plus-circle',
    };
    
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 타임라인 이벤트 추가 API 호출', event);
    // await timelineService.addTimelineEvent(event);
    
    timeline.value.unshift(event);
    newEvent.value = { date: new Date().toISOString().split('T')[0], title: '', description: '' };
    alert('이벤트가 추가되었습니다.');
  } catch (error) {
    console.error('이벤트 추가 실패:', error);
    alert('이벤트 추가에 실패했습니다.');
  }
}
function openGroupSettings() {
  if (!group.value) return;
  selectedGroup.value = JSON.parse(JSON.stringify(group.value));
  isGroupSettingsModalVisible.value = true;
}
function closeGroupSettingsModal() {
  isGroupSettingsModalVisible.value = false;
  selectedGroup.value = null;
}

// --- Group Settings Functions ---
const isGroupOwner = (g: Group) => g.ownerId === currentUserNickname.value;
const isManager = (g: Group, member: string) => g.managers.includes(member);
const canInvite = (g: Group) => isGroupOwner(g) || isManager(g, currentUserNickname.value);
const canRemoveMember = (g: Group, member: string) => {
  if (member === g.ownerId) return false;
  const amIOwner = isGroupOwner(g);
  const amIManager = isManager(g, currentUserNickname.value);
  const isTargetManager = isManager(g, member);
  if (amIOwner) return true;
  if (amIManager && !isTargetManager) return true;
  return false;
};
const getMemberRole = (g: Group, member: string) => {
  if (g.ownerId === member) return '그룹장';
  if (isManager(g, member)) return '매니저';
  return '멤버';
};
const getRoleClass = (role: string) => {
  if (role === '그룹장') return 'role-owner';
  if (role === '매니저') return 'role-manager';
  return 'role-member';
};
const toggleManager = (g: Group, member: string) => {
  if (!selectedGroup.value) return;
  const managers = selectedGroup.value.managers;
  const index = managers.indexOf(member);
  if (index > -1) managers.splice(index, 1);
  else managers.push(member);
};
const removeMember = (g: Group, member: string) => {
  if (!selectedGroup.value) return;
  selectedGroup.value.members = selectedGroup.value.members.filter(m => m !== member);
  const managerIndex = selectedGroup.value.managers.indexOf(member);
  if (managerIndex > -1) selectedGroup.value.managers.splice(managerIndex, 1);
};
const searchMember = async () => {
  try {
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 멤버 검색 API 호출', inviteSearchQuery.value);
    // const response = await memberService.searchMember(inviteSearchQuery.value);
    // inviteSearchResult.value = response.data.username;
    inviteSearchResult.value = '검색된멤버';
  } catch (error) {
    console.error('멤버 검색 실패:', error);
  }
};

const inviteMember = async () => {
  if (!selectedGroup.value || !inviteSearchResult.value) return;
  
  try {
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 멤버 초대 API 호출', selectedGroup.value.id, inviteSearchResult.value);
    // await groupService.inviteMember(selectedGroup.value.id, inviteSearchResult.value);
    
    selectedGroup.value.members.push(inviteSearchResult.value);
    alert(`'${inviteSearchResult.value}'님을 초대했습니다.`);
    inviteSearchResult.value = null;
    inviteSearchQuery.value = '';
  } catch (error) {
    console.error('멤버 초대 실패:', error);
    alert('멤버 초대에 실패했습니다.');
  }
};
const deleteGroup = async (id: string) => {
  if (!confirm('정말로 그룹을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    return;
  }
  
  try {
    // TODO: 실제 API 호출로 교체
    console.log('TODO: 그룹 삭제 API 호출', id);
    // await groupService.deleteGroup(id);
    
    alert('그룹이 삭제되었습니다.');
    router.push('/my-library');
  } catch (error) {
    console.error('그룹 삭제 실패:', error);
    alert('그룹 삭제에 실패했습니다.');
  }
};
onMounted(() => {
  fetchGroupInfo();
  setupTimelineListener();
});
</script>

<style scoped>
@import '../../styles/group-timeline.css';
</style>