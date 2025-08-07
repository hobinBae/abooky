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
import { useRoute, useRouter, RouterLink } from 'vue-router';

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

// --- Dummy Data ---
const DUMMY_GROUPS: Group[] = [
  { id: 'group1', groupName: '독서 토론 모임', ownerId: '김작가', managers: ['이영희'], members: ['김작가', '이영희', '박철수'] },
  { id: 'group2', groupName: '글쓰기 동호회', ownerId: '최수진', managers: [], members: ['최수진', '김작가'] },
  { id: 'group3', groupName: '여행 에세이 클럽', ownerId: '정민준', managers: ['김작가'], members: ['정민준', '김작가', '하은지'] },
  { id: 'group4', groupName: '코딩 스터디', ownerId: '박개발', managers: [], members: ['박개발', '이코딩'] },
];
const DUMMY_TIMELINE_EVENTS: TimelineEvent[] = [
  { id: 't1', groupId: 'group1', date: '2025-01-01', title: '그룹 결성', description: '새로운 독서 토론 모임이 시작되었습니다.', icon: 'bi-star-fill' },
  { id: 't2', groupId: 'group1', date: '2025-01-15', title: '첫 토론 주제 선정', description: `'데미안'을 첫 토론 도서로 선정했습니다.`, icon: 'bi-lightbulb-fill', episodeLink: '/book-detail/b1' },
  { id: 't3', groupId: 'group2', date: '2025-02-01', title: '글쓰기 워크숍', description: `'나만의 에세이 쓰기' 워크숍을 진행했습니다.`, icon: 'bi-pencil-square' },
  { id: 't4', groupId: 'group1', date: '2025-03-10', title: '정기 모임', description: `'나의 첫 유럽 여행기'에 대한 심도 깊은 토론을 진행했습니다.`, icon: 'bi-book-fill', episodeLink: '/book-detail/b1' },
  { id: 't5', groupId: 'group3', date: '2025-04-05', title: '여행 계획 공유', description: '다음 여행지인 제주도에 대한 계획을 공유했습니다.', icon: 'bi-geo-alt-fill' },
];

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
function fetchGroupInfo() {
  const foundGroup = DUMMY_GROUPS.find(g => g.id === groupId.value);
  if (foundGroup) {
    group.value = foundGroup;
  }
}
function setupTimelineListener() {
  timeline.value = DUMMY_TIMELINE_EVENTS.filter(event => event.groupId === groupId.value);
}
function addTimelineEvent() {
  if (!newEvent.value.title || !newEvent.value.description) {
    alert('이벤트 제목과 내용을 모두 입력해주세요.');
    return;
  }
  const event: TimelineEvent = {
    id: `t${Date.now()}`,
    groupId: groupId.value,
    date: newEvent.value.date,
    title: newEvent.value.title,
    description: newEvent.value.description,
    icon: 'bi-plus-circle',
  };
  timeline.value.unshift(event);
  DUMMY_TIMELINE_EVENTS.push(event);
  newEvent.value = { date: new Date().toISOString().split('T')[0], title: '', description: '' };
  alert('이벤트가 추가되었습니다.');
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
const searchMember = () => { inviteSearchResult.value = '검색된멤버'; };
const inviteMember = () => {
  if (!selectedGroup.value || !inviteSearchResult.value) return;
  alert(`'${inviteSearchResult.value}'님을 초대했습니다. (더미 기능)`);
  selectedGroup.value.members.push(inviteSearchResult.value);
  inviteSearchResult.value = null;
  inviteSearchQuery.value = '';
};
const deleteGroup = (id: string) => {
  if (confirm('정말로 그룹을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    alert('그룹이 삭제되었습니다.');
    router.push('/my-library');
  }
};
onMounted(() => {
  fetchGroupInfo();
  setupTimelineListener();
});
</script>

<style scoped>
/* Google Fonts & Icons */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;700&family=Pretendard:wght@400;600&display=swap');
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

/* --- 전역 변수만 사용하도록 수정 --- */

/* .group-timeline-page 등에서 base.css의 전역 변수 사용 */
.group-timeline-page {
  padding: 80px 2rem 2rem;
  background-color: var(--color-background);
  color: var(--color-text);
  min-height: calc(100vh - 56px);
  font-family: 'Pretendard', sans-serif;
}

.page-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 2.5rem;
  max-width: 1300px;
  margin: 0 auto;
  align-items: start;
}

/* --- Left Member Sidebar --- */
.member-sidebar {
  background: transparent;
  padding: 1.5rem;
  border-radius: 12px;
  position: sticky;
  top: 80px;
}
.sidebar-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--color-border);
  color: var(--color-heading);
}
.sidebar-member-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.sidebar-member-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1rem;
}
.member-link {
  text-decoration: none;
  color: var(--color-text);
  transition: color 0.2s;
}
.member-link:hover {
  color: var(--color-heading);
}
.sidebar-member-item .member-name {
  font-weight: 600;
}
.sidebar-member-item .member-role {
  font-size: 0.8rem;
  padding: 0.3rem 0.7rem;
  border-radius: 12px;
  color: #fff;
  font-weight: 600;
  flex-shrink: 0;
}
.member-role.role-owner { background-color: #8C4332; }
.member-role.role-manager { background-color: #8C6A56; }
.member-role.role-member { background-color: #A9A9A9; }

/* --- Right Timeline Content --- */
.timeline-container { max-width: 100%; }
.timeline-header {
  margin-bottom: 3rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background-color: var(--color-background-soft);
  border-radius: 12px;
  border: 1px solid var(--color-border);
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
}
.header-content { text-align: left; }
.timeline-header h1 {
  font-family: 'Noto Serif KR', serif;
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-heading);
}
.timeline-header p {
  font-size: 1.1rem;
  color: var(--color-border-hover);
  margin-top: 0.5rem;
}
.settings-btn { flex-shrink: 0; }

.add-event-section {
  background: var(--color-background-soft);
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 3rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.05);
  border: 1px solid var(--color-border);
}
.add-event-section h3 {
  text-align: center;
  margin-bottom: 1.5rem;
  font-family: 'Noto Serif KR', serif;
  color: var(--color-heading);
}

.event-form {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 1rem;
}
.event-form textarea {
  grid-column: 1 / -1;
  height: 100px;
}
.event-form button {
  grid-column: 1 / -1;
}

.timeline {
  position: relative;
  padding: 1rem 0;
}
.timeline::before {
  content: '';
  position: absolute;
  top: 0;
  left: 18px;
  height: 100%;
  width: 4px;
  background: var(--color-border);
  border-radius: 2px;
}
.timeline-item {
  position: relative;
  margin-bottom: 2rem;
}
.timeline-item:last-child { margin-bottom: 0; }
.timeline-icon {
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #8C6A56;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 1.5rem;
  border: 3px solid var(--color-background);
}
.timeline-content {
  margin-left: 60px;
  background: var(--color-background-soft);
  padding: 1.5rem;
  border-radius: 8px;
  border: 1px solid var(--color-border);
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.timeline-date {
  font-size: 0.9rem;
  color: var(--color-border-hover);
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: block;
}
.timeline-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--color-heading);
}
.episode-link {
  margin-top: 1rem;
  display: inline-block;
  color: #8C4332;
  font-weight: bold;
  text-decoration: none;
}

/* General Form & Button Styles */
.form-control {
  background-color: #fff;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 0.75rem 1rem;
  width: 100%;
  box-sizing: border-box;
  font-size: 1rem;
  color: var(--color-text);
}
.form-control:focus {
  outline: none;
  border-color: #8C6A56;
  box-shadow: 0 0 0 3px rgba(140, 106, 86, 0.2);
}
.btn {
  border: 1px solid #8C6A56;
  background-color: #fff;
  color: var(--color-text);
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  font-size: 0.95rem;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}
.btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.08);
}
.btn-primary {
  background-color: #8C4332;
  border-color: #8C4332;
  color: #fff;
}
.btn-secondary {
  background-color: #fff;
  border-color: #8C6A56;
  color: var(--color-text);
}

/* --- Modal Styles --- */
.modal-backdrop {
  position: fixed;
  z-index: 1000;
  inset: 0;
  background-color: rgba(64, 48, 35, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background-color: var(--color-background-soft);
  margin: 1rem;
  padding: 2rem 2.5rem;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.3);
  position: relative;
  border: 1px solid var(--color-border);
}
.modal-content.modal-lg { max-width: 800px; }
.close-button {
  color: var(--color-text);
  position: absolute;
  top: 1rem;
  right: 1rem;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  padding: 0.5rem;
}
.close-button:hover { color: var(--color-heading); }
.modal-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.8rem;
  font-weight: 700;
  margin-bottom: 0.75rem;
  text-align: center;
  color: var(--color-heading);
}
.settings-section {
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
  text-align: left;
}
.settings-section:last-child {
  margin-bottom: 0;
  border-bottom: none;
  padding-bottom: 0;
}
.settings-section-title {
  font-family: 'Noto Serif KR', serif;
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 1.2rem;
  color: var(--color-heading);
}
.member-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 250px;
  overflow-y: auto;
}
.member-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.75rem;
  background-color: var(--color-background-mute);
  border-radius: 8px;
}
.member-item .member-name { font-weight: 600; flex-grow: 1; }
.member-item .member-role {
  font-size: 0.85rem;
  padding: 0.25rem 0.6rem;
  border-radius: 12px;
  color: #fff;
  flex-shrink: 0;
  background-color: var(--color-border-hover);
}
.member-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
.btn-sm { padding: 0.3rem 0.8rem; font-size: 0.85rem; }
.btn-danger { background-color: #d9534f; border-color: #d43f3a; color: #fff; }
.btn-danger:hover { background-color: #c9302c; border-color: #ac2925; }
.btn-success { background-color: #5cb85c; border-color: #4cae4c; color: #fff; }
.btn-success:hover { background-color: #449d44; border-color: #398439; }
.invite-form { display: flex; gap: 1rem; margin-bottom: 1rem; }
.search-result { display: flex; justify-content: space-between; align-items: center; background-color: var(--color-background-mute); padding: 0.75rem; border-radius: 8px; }
.danger-zone { background-color: rgba(217, 83, 79, 0.05); padding: 1.5rem; border-radius: 8px; border: 1px solid rgba(217, 83, 79, 0.3); }
.danger-zone .settings-section-title { color: #c9302c; }
.danger-zone p { font-size: 0.95rem; line-height: 1.6; }
</style>