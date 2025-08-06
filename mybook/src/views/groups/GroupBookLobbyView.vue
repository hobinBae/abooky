<template>
  <div class="lobby-container">
    <div class="lobby-content">
      <div class="lobby-header">
        <h1>그룹 책 만들기</h1>
        <p>그룹 사람들과 새로운 책을 만들거나 이 여정에 참여해 보세요.</p>
      </div>
      <div class="button-container">
        <button class="lobby-button create-button" @click="showGroupModal = true">
          <h2>그룹책 방 만들기</h2>
          <p>새로운 그룹 책을 만들어보세요.</p>
        </button>
        <button class="lobby-button join-button" @click="goToJoin">
          <h2>그룹책 방 참여하기</h2>
          <p>활성화된 나의 그룹 책에 참여하세요.</p>
        </button>
      </div>
    </div>

    <!-- 그룹 선택 모달 -->
    <div v-if="showGroupModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>내가 속한 그룹 선택</h2>
          <button class="close-button" @click="closeModal">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="filteredGroups.length === 0" class="no-groups-message">
            <div class="no-groups-icon">
              <i class="bi bi-people"></i>
            </div>
            <h3>참여한 그룹이 없습니다</h3>
            <p>먼저 '나의 서재'에서 그룹을 생성하거나 다른 그룹에 참여해보세요.</p>
            <button class="btn btn-secondary" @click="goToMyLibrary">
              나의 서재로 이동
            </button>
          </div>
          <div v-else class="group-list">
            <div 
              v-for="group in filteredGroups" 
              :key="group.id"
              class="group-item"
              @click="selectGroup(group)"
            >
              <div class="group-avatar">
                <i class="bi bi-people-fill"></i>
              </div>
              <div class="group-info">
                <h3>{{ group.groupName }}</h3>
                <p>멤버 {{ group.members.length }}명</p>
                <div class="group-details">
                  <span class="member-list">{{ group.members.join(', ') }}</span>
                  <span class="book-count">{{ group.books.length }}권의 책</span>
                </div>
                <div v-if="group.ownerId === currentUserNickname" class="group-badge owner-badge">
                  <i class="bi bi-crown-fill"></i>
                  <span>방장</span>
                </div>
                <div v-else-if="group.managers.includes(currentUserNickname)" class="group-badge manager-badge">
                  <i class="bi bi-shield-fill"></i>
                  <span>관리자</span>
                </div>
              </div>
              <div class="group-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 그룹책 참여 모달 -->
    <div v-if="showJoinModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>활성화된 그룹책 만들기</h2>
          <button class="close-button" @click="closeModal">
            <i class="bi bi-x"></i>
          </button>
        </div>
        <div class="modal-body">
          <div v-if="availableGroupBookSessions.length === 0" class="no-sessions-message">
            <div class="no-sessions-icon">
              <i class="bi bi-book"></i>
            </div>
            <h3>참여할 수 있는 그룹책 만들기가 없습니다</h3>
            <p>현재 진행 중인 그룹책 만들기가 없습니다.<br>직접 그룹책 방을 만들어보세요.</p>
            <button class="btn btn-primary" @click="closeModal">
              그룹책 방 만들기
            </button>
          </div>
          <div v-else class="session-list">
            <div 
              v-for="session in availableGroupBookSessions" 
              :key="session.groupId"
              class="session-item"
              @click="joinGroupBookSession(session)"
            >
              <div class="session-avatar">
                <i class="bi bi-book-fill"></i>
              </div>
              <div class="session-info">
                <h3>{{ session.groupName }}</h3>
                <p class="session-host">방장: {{ session.hostName }}</p>
                <div class="session-details">
                  <span class="participant-count">
                    <i class="bi bi-people-fill me-1"></i>
                    {{ session.participantCount }}명 참여 중
                  </span>
                  <span class="session-time">{{ getTimeAgo(session.startedAt) }}</span>
                </div>
                <div class="session-status">
                  <span class="status-badge active-badge">
                    <i class="bi bi-circle-fill"></i>
                    활성화
                  </span>
                </div>
              </div>
              <div class="session-arrow">
                <i class="bi bi-chevron-right"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';

interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName?: string;
}

interface Group {
  id: string;
  groupName: string;
  ownerId: string;
  managers: string[];
  members: string[];
  books: Book[];
  createdAt: Date;
}

const router = useRouter();

// 모달 상태
const showGroupModal = ref(false);
const showJoinModal = ref(false);

// 현재 사용자 (MyLibraryView.vue와 동일)
const currentUserNickname = ref('김작가');

// MyLibraryView.vue와 동일한 그룹 데이터
const myGroups = ref<Group[]>([
  { 
    id: 'group1', 
    groupName: '독서 토론 모임', 
    ownerId: '김작가', 
    managers: ['이영희'], 
    members: ['김작가', '이영희', '박철수'], 
    books: [{ id: 'mybook1', title: '나의 어린 시절 이야기', authorId: 'dummyUser1', authorName: '김작가' }], 
    createdAt: new Date() 
  },
  { 
    id: 'group2', 
    groupName: '글쓰기 동호회', 
    ownerId: '김작가', 
    managers: [], 
    members: ['김작가', '최수진'], 
    books: [], 
    createdAt: new Date() 
  },
  { 
    id: 'group3', 
    groupName: '여행 에세이 클럽', 
    ownerId: '정민준', 
    managers: [], 
    members: ['정민준', '김작가', '하은지'], 
    books: [{ id: 'mybook3', title: '여행의 기록', authorId: 'dummyUser1', authorName: '김작가' }], 
    createdAt: new Date() 
  },
]);

// 활성화된 그룹책 만들기 세션 (실제로는 서버에서 가져올 데이터)
const activeGroupBookSessions = ref([
  {
    groupId: 'group3',
    groupName: '여행 에세이 클럽',
    hostName: '정민준',
    startedAt: new Date(),
    participantCount: 1
  }
  // 예시: group1에서도 세션이 활성화된 경우
  // {
  //   groupId: 'group1',
  //   groupName: '독서 토론 모임',
  //   hostName: '이영희',
  //   startedAt: new Date(),
  //   participantCount: 2
  // }
]);

// 내가 속한 그룹만 필터링
const filteredGroups = computed(() => {
  return myGroups.value.filter(group => group.members.includes(currentUserNickname.value));
});

// 참여 가능한 활성화된 그룹책 만들기 세션 필터링
const availableGroupBookSessions = computed(() => {
  return activeGroupBookSessions.value.filter(session => {
    // 해당 그룹의 멤버인지 확인
    const group = myGroups.value.find(g => g.id === session.groupId);
    return group && group.members.includes(currentUserNickname.value);
  });
});

const closeModal = () => {
  showGroupModal.value = false;
  showJoinModal.value = false;
};

const selectGroup = (group: Group) => {
  console.log('선택된 그룹:', group);
  
  try {
    // router.push 방식으로 변경
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: group.id, 
        groupName: group.groupName 
      }
    });
    closeModal();
  } catch (error) {
    console.error('라우터 네비게이션 오류:', error);
    // 대체 방법으로 직접 경로 이동
    window.location.href = `/group-book-creation?groupId=${group.id}&groupName=${encodeURIComponent(group.groupName)}`;
  }
};

const goToJoin = () => {
  showJoinModal.value = true;
};

const joinGroupBookSession = (session: any) => {
  console.log('참여할 세션:', session);
  
  try {
    // 활성화된 그룹책 만들기 세션에 참여
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: session.groupId, 
        groupName: session.groupName,
        mode: 'join' // 참여 모드로 구분
      }
    });
    closeModal();
  } catch (error) {
    console.error('그룹책 세션 참여 오류:', error);
    window.location.href = `/group-book-creation?groupId=${session.groupId}&groupName=${encodeURIComponent(session.groupName)}&mode=join`;
  }
};

const goToMyLibrary = () => {
  closeModal();
  router.push({ path: '/my-library' });
};

// 시간 경과 표시 함수
const getTimeAgo = (date: Date) => {
  const now = new Date();
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
  
  if (diffInMinutes < 1) return '방금 전';
  if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
  
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours}시간 전`;
  
  const diffInDays = Math.floor(diffInHours / 24);
  return `${diffInDays}일 전`;
};
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css");

.lobby-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px); /* Navbar and Footer height */
  background-color: #f4f4f9;
  padding: 2rem;
}

.lobby-content {
  text-align: center;
  background-color: white;
  padding: 3rem;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  max-width: 900px;
  width: 100%;
}

.lobby-header {
  margin-bottom: 3rem;
}

.lobby-header h1 {
  font-size: 2.5rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.5rem;
}

.lobby-header p {
  font-size: 1.2rem;
  color: #666;
}

.button-container {
  display: flex;
  justify-content: space-around;
  gap: 2rem;
}

.lobby-button {
  flex: 1;
  padding: 2rem;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  background-color: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.lobby-button:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.15);
}

.lobby-button h2 {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.lobby-button p {
  font-size: 1rem;
  color: #555;
}

.create-button {
  border: 2px solid #42b983;
  color: #42b983;
}

.create-button:hover {
  background-color: #42b983;
  color: white;
}

.join-button {
  border: 2px solid #3498db;
  color: #3498db;
}

.join-button:hover {
  background-color: #3498db;
  color: white;
}

/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 15px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    transform: translateY(-50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0.25rem;
  border-radius: 50%;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.close-button:hover {
  background-color: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 0;
}

.group-list {
  max-height: 400px;
  overflow-y: auto;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.group-item:hover {
  background-color: #f8f9fa;
}

.group-item:last-child {
  border-bottom: none;
}

.group-avatar {
  width: 50px;
  height: 50px;
  background-color: #42b983;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.group-avatar i {
  color: white;
  font-size: 1.5rem;
}

.group-info {
  flex-grow: 1;
  text-align: left;
}

.group-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.group-info p {
  margin: 0;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
}

.group-details {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.member-list {
  font-size: 0.85rem;
  color: #888;
  font-style: italic;
}

.book-count {
  font-size: 0.8rem;
  color: #42b983;
  font-weight: 600;
}

.group-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-top: 0.5rem;
}

.owner-badge {
  background-color: #ffd700;
  color: #8b6914;
}

.manager-badge {
  background-color: #42b983;
  color: white;
}

.no-groups-message {
  text-align: center;
  padding: 2rem 1rem;
  color: #666;
}

.no-groups-icon {
  font-size: 3rem;
  color: #ddd;
  margin-bottom: 1rem;
}

.no-groups-message h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  color: #333;
}

.no-groups-message p {
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

.btn {
  padding: 0.6rem 1.2rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s ease;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #5a6268;
}

.group-arrow {
  color: #ccc;
  font-size: 1rem;
  margin-left: 1rem;
  flex-shrink: 0;
}

.group-item:hover .group-arrow {
  color: #42b983;
}

/* 그룹책 참여 모달 스타일 */
.no-sessions-message {
  text-align: center;
  padding: 2rem 1rem;
  color: #666;
}

.no-sessions-icon {
  font-size: 3rem;
  color: #ddd;
  margin-bottom: 1rem;
}

.no-sessions-message h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1.2rem;
  color: #333;
}

.no-sessions-message p {
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  line-height: 1.5;
}

.session-list {
  max-height: 400px;
  overflow-y: auto;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s ease;
}

.session-item:hover {
  background-color: #f8f9fa;
}

.session-item:last-child {
  border-bottom: none;
}

.session-avatar {
  width: 50px;
  height: 50px;
  background-color: #3498db;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  flex-shrink: 0;
}

.session-avatar i {
  color: white;
  font-size: 1.5rem;
}

.session-info {
  flex-grow: 1;
  text-align: left;
}

.session-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.1rem;
  font-weight: 600;
  color: #333;
}

.session-host {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
}

.session-details {
  margin-bottom: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.participant-count {
  font-size: 0.85rem;
  color: #3498db;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.session-time {
  font-size: 0.8rem;
  color: #888;
  font-style: italic;
}

.session-status {
  display: flex;
  align-items: center;
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.active-badge {
  background-color: #d4edda;
  color: #155724;
}

.active-badge i {
  font-size: 0.6rem;
  color: #28a745;
}

.session-arrow {
  color: #ccc;
  font-size: 1rem;
  margin-left: 1rem;
  flex-shrink: 0;
}

.session-item:hover .session-arrow {
  color: #3498db;
}

.btn-primary {
  background-color: #42b983;
  color: white;
}

.btn-primary:hover {
  background-color: #369870;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .button-container {
    flex-direction: column;
  }
  
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .group-item {
    padding: 0.75rem 1rem;
  }
  
  .group-avatar {
    width: 40px;
    height: 40px;
    margin-right: 0.75rem;
  }
  
  .group-avatar i {
    font-size: 1.25rem;
  }
  
  .group-info h3 {
    font-size: 1rem;
  }
  
  .group-info p {
    font-size: 0.85rem;
  }
}
</style>