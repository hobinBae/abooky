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
          <div v-if="loading" class="loading-message">
            <div class="spinner"></div>
            <p>그룹 목록을 불러오는 중...</p>
          </div>
          <div v-else-if="myGroups.length === 0" class="no-groups-message">
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
              v-for="group in myGroups" 
              :key="group.groupId"
              class="group-item"
              @click="selectGroup(group)"
            >
              <div class="group-avatar" :style="{ backgroundColor: group.themeColor || '#42b983' }">
                <img 
                  v-if="group.groupImageUrl" 
                  :src="group.groupImageUrl" 
                  :alt="group.groupName"
                  class="group-image"
                />
                <i v-else class="bi bi-people-fill"></i>
              </div>
              <div class="group-info">
                <h3>{{ group.groupName }}</h3>
                <p v-if="group.description" class="group-description">{{ group.description }}</p>
                <div class="group-details">
                  <span class="leader-info">
                    <i class="bi bi-crown-fill me-1"></i>
                    방장: {{ group.leaderNickname }}
                  </span>
                  <span class="created-date">{{ formatDate(group.createdAt) }} 생성</span>
                </div>
                <div v-if="group.leaderId === currentUserId" class="group-badge owner-badge">
                  <i class="bi bi-crown-fill"></i>
                  <span>방장</span>
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
          <div v-if="loadingSessions" class="loading-message">
            <div class="spinner"></div>
            <p>활성화된 세션을 불러오는 중...</p>
          </div>
          <div v-else-if="availableGroupBookSessions.length === 0" class="no-sessions-message">
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

interface ApiResponse<T> {
  success: boolean;
  status: number;
  message: string;
  data: {
    content: T[];
    pageable: {
      page: number;
      size: number;
      totalElements: number;
      totalPages: number;
    };
  };
  timestamp: string;
  path: string;
}

interface Group {
  groupId: number;
  groupName: string;
  description: string;
  themeColor: string;
  groupImageUrl: string;
  leaderId: number;
  leaderNickname: string;
  createdAt: string;
  updatedAt: string;
  // 클라이언트 필터링을 위한 멤버 정보 추가
  members?: string[]; // 그룹 상세 정보에서 가져올 멤버 리스트
}

interface ActiveSession {
  groupId: number;
  groupName: string;
  hostName: string;
  startedAt: Date;
  participantCount: number;
}

const router = useRouter();

// 모달 상태
const showGroupModal = ref(false);
const showJoinModal = ref(false);

// 로딩 상태
const loading = ref(false);
const loadingSessions = ref(false);

// 현재 사용자 정보 (실제로는 인증 상태에서 가져와야 함)
const currentUserId = ref(1001);
const currentUserNickname = ref('김싸피123');

// 그룹 데이터 (기본 정보 + 상세 정보)
const myGroups = ref<Group[]>([]);

// 전체 활성화된 그룹책 세션 (모든 그룹의 세션)
const allActiveGroupBookSessions = ref<ActiveSession[]>([]);

// API 호출 함수
const fetchMyGroups = async () => {
  loading.value = true;
  try {
    // 1. 기본 그룹 정보 가져오기
    const response = await fetch('/api/v1/groups/me', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const apiResponse: ApiResponse<Group> = await response.json();
    
    if (apiResponse.success) {
      const basicGroups = apiResponse.data.content;
      
      // 2. 각 그룹의 상세 정보 가져오기 (멤버 리스트 포함)
      const groupsWithDetails = await Promise.all(
        basicGroups.map(async (group) => {
          try {
            const detailResponse = await fetch(`/api/v1/groups/${group.groupId}/details`, {
              headers: {
                'Authorization': `Bearer ${getAccessToken()}`,
                'Content-Type': 'application/json'
              }
            });
            
            if (detailResponse.ok) {
              const detailData = await detailResponse.json();
              // 기본 정보 + 멤버 정보 병합
              return {
                ...group,
                members: detailData.data.members || [] // 멤버 리스트 추가
              };
            } else {
              // 상세 정보 가져오기 실패시 기본 정보만 반환
              console.warn(`그룹 ${group.groupId} 상세 정보 조회 실패`);
              return group;
            }
          } catch (error) {
            console.error(`그룹 ${group.groupId} 상세 정보 조회 오류:`, error);
            return group;
          }
        })
      );
      
      myGroups.value = groupsWithDetails;
    } else {
      console.error('API 응답 실패:', apiResponse.message);
    }
  } catch (error) {
    console.error('그룹 목록 조회 실패:', error);
    
    // 개발용 더미 데이터 (실제 배포시에는 제거)
    myGroups.value = [
      {
        groupId: 1,
        groupName: "우리 가족",
        description: "가족들과 추억을 기록하는 공간",
        themeColor: "#FFCC00",
        groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
        leaderId: 1001,
        leaderNickname: "김싸피123",
        createdAt: "2025-07-22T10:00:00",
        updatedAt: "2025-07-22T11:00:00",
        members: ["김싸피123", "엄마", "아빠"] // 더미 멤버 데이터
      },
      {
        groupId: 2,
        groupName: "대학 동기",
        description: "대학 동기들과 추억을 기록하는 공간",
        themeColor: "#FFFFFF",
        groupImageUrl: "https://your-bucket.s3.ap-northeast-2.amazonaws.com/profiles/550e8400-e29b-41d4-a716-446655440000.jpg",
        leaderId: 5001,
        leaderNickname: "이싸피123",
        createdAt: "2025-07-22T10:00:00",
        updatedAt: "2025-07-22T11:00:00",
        members: ["김싸피123", "이싸피123", "박싸피456"] // 더미 멤버 데이터
      }
    ];
  } finally {
    loading.value = false;
  }
};

// 전체 활성화된 그룹책 세션 조회
const fetchAllActiveGroupBookSessions = async () => {
  loadingSessions.value = true;
  try {
    // 모든 활성화된 그룹책 세션을 가져오기
    const response = await fetch('/api/v1/group-books/active-sessions', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const apiResponse: ApiResponse<ActiveSession> = await response.json();
    
    if (apiResponse.success) {
      allActiveGroupBookSessions.value = apiResponse.data.content;
    } else {
      console.error('API 응답 실패:', apiResponse.message);
      allActiveGroupBookSessions.value = [];
    }
  } catch (error) {
    console.error('활성화된 세션 조회 실패:', error);
    
    // 개발용 더미 데이터 (실제 배포시에는 제거)
    // 전체 활성화된 세션 데이터 (내가 속하지 않은 그룹도 포함)
    allActiveGroupBookSessions.value = [
      {
        groupId: 2,
        groupName: '대학 동기',
        hostName: '이싸피123',
        startedAt: new Date(),
        participantCount: 1
      },
      {
        groupId: 99, // 내가 속하지 않은 그룹
        groupName: '다른 사람 그룹',
        hostName: '타인123',
        startedAt: new Date(),
        participantCount: 3
      }
    ];
  } finally {
    loadingSessions.value = false;
  }
};

// 내가 참여 가능한 활성화된 그룹책 세션만 필터링
const availableGroupBookSessions = computed(() => {
  return allActiveGroupBookSessions.value.filter(session => {
    // 내가 속한 그룹의 ID 목록
    const myGroupIds = myGroups.value.map(group => group.groupId);
    
    // 해당 세션의 그룹이 내가 속한 그룹 중 하나인지 확인
    return myGroupIds.includes(session.groupId);
  });
});

// 토큰 가져오기 함수 (실제 인증 로직에 맞게 구현)
const getAccessToken = (): string => {
  return localStorage.getItem('accessToken') || '';
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchMyGroups();
});

const closeModal = () => {
  showGroupModal.value = false;
  showJoinModal.value = false;
};

const selectGroup = (group: Group) => {
  console.log('선택된 그룹:', group);
  
  try {
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: group.groupId.toString(), 
        groupName: group.groupName 
      }
    });
    closeModal();
  } catch (error) {
    console.error('라우터 네비게이션 오류:', error);
    window.location.href = `/group-book-creation?groupId=${group.groupId}&groupName=${encodeURIComponent(group.groupName)}`;
  }
};

const goToJoin = () => {
  showJoinModal.value = true;
  fetchAllActiveGroupBookSessions(); // 전체 세션 데이터 가져오기
};

const joinGroupBookSession = (session: ActiveSession) => {
  console.log('참여할 세션:', session);
  
  try {
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: session.groupId.toString(), 
        groupName: session.groupName,
        mode: 'join'
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

// 날짜 포맷 함수
const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
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
@import '../../styles/group-book-lobby.css';
</style>