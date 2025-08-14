<template>
  <div class="create-book-page">
    <section class="initial-choice-section">
      <h2 class="section-title">마음을 잇는 이야기</h2>
      <p class="section-subtitle1">각자의 시선으로 바라본 같은 순간들이</p>
      <p class="section-subtitle2">어떻게 다르고 또 같은지 발견하는 즐거움.</p>
      <p class="section-subtitle3">함께 쓰는 책, 함께 만드는 추억</p>


      <section class="choice-section">
        <div class="choice-cards">
          <div class="choice-card" @click="openGroupModal">
            <div class="card-icon"><i class="bi bi-door-open"></i></div>
            <h3 class="card-title">그룹책 방 입장하기</h3>
            <p class="card-description">내가 속한 그룹에서 새로운 책을 만들거나<br>활성화된 방에 참여하세요.</p>
          </div>
          <div class="choice-card" @click="openCreateModal">
            <div class="card-icon"><i class="bi bi-people"></i></div>
            <h3 class="card-title">그룹책 만들기</h3>
            <p class="card-description">새로운 그룹을 생성하고<br>멤버들과 책을 만들어보세요.</p>
          </div>
        </div>
      </section>

    </section>

    <!-- 그룹 선택 모달 -->
    <SimpleModal 
      :is-visible="showGroupModal" 
      title="내가 속한 그룹 선택" 
      @close="closeGroupModal"
    >
      <div v-if="loading" style="padding: 2rem; text-align: center;">
        <LoadingSpinner message="그룹 목록을 불러오는 중..." />
      </div>
      
      <div v-else-if="myGroups.length === 0" style="padding: 2rem;">
        <EmptyState 
          icon-class="bi bi-people"
          title="참여한 그룹이 없습니다"
          description="먼저 '나의 서재'에서 그룹을 생성하거나 다른 그룹에 참여해보세요."
          action-text="나의 서재로 이동"
          action-class="btn-secondary"
          @action="goToMyLibrary"
        />
      </div>
      
      <div v-else class="group-list">
        <GroupItem 
          v-for="group in myGroups" 
          :key="group.groupId"
          :group="group"
          :current-user-id="currentUserId"
          :is-active="isGroupActive(group.groupId)"
          @select="selectGroup"
        />
      </div>
    </SimpleModal>

    <!-- 그룹책 참여 모달 -->
    <SimpleModal 
      :key="joinModalKey"
      :is-visible="showJoinModal" 
      title="내가 속한 그룹 선택" 
      @close="closeJoinModal"
    >
      <div v-if="loadingSessions" style="padding: 2rem; text-align: center;">
        <LoadingSpinner message="활성화된 그룹책 방을 확인하는 중..." />
      </div>
      
      <div v-else-if="activeGroupsForJoin.length === 0" style="padding: 2rem;">
        <EmptyState 
          icon-class="bi bi-book"
          title="활성화된 그룹책 방이 없습니다"
          description="활성화된 그룹책 방이 없습니다.\n그룹책 방 만들기를 이용해 활성화 시켜주세요."
          action-text="그룹책 방 만들기"
          action-class="btn-primary"
          @action="handleCreateFromJoin"
        />
      </div>
      
      <div v-else class="group-list">
        <GroupItem 
          v-for="group in activeGroupsForJoin" 
          :key="group.groupId"
          :group="group"
          :current-user-id="currentUserId"
          @select="joinExistingGroupBookSession"
        />
      </div>
    </SimpleModal>

    <!-- 그룹책 만들기 모달 -->
    <SimpleModal 
      :is-visible="showCreateModal" 
      title="그룹책을 만들 그룹 선택" 
      @close="closeCreateModal"
    >
      <div v-if="loading" style="padding: 2rem; text-align: center;">
        <LoadingSpinner message="그룹 목록을 불러오는 중..." />
      </div>
      
      <div v-else-if="myGroups.length === 0" style="padding: 2rem;">
        <EmptyState 
          icon-class="bi bi-people"
          title="참여한 그룹이 없습니다"
          description="먼저 '나의 서재'에서 그룹을 생성하거나 다른 그룹에 참여해보세요."
          action-text="나의 서재로 이동"
          action-class="btn-secondary"
          @action="goToMyLibrary"
        />
      </div>
      
      <div v-else class="group-list">
        <GroupItem 
          v-for="group in myGroups" 
          :key="group.groupId"
          :group="group"
          :current-user-id="currentUserId"
          @select="selectGroupForCreate"
        />
      </div>
    </SimpleModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import SimpleModal from '@/components/common/SimpleModal.vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import EmptyState from '@/components/common/EmptyState.vue';
import GroupItem from '@/components/groups/GroupItem.vue';
import SessionItem from '@/components/groups/SessionItem.vue';
import { groupService, type Group, type ActiveSession } from '@/services/groupService';

const router = useRouter();

// 모달 상태
const showGroupModal = ref(false);
const showJoinModal = ref(false);
const showCreateModal = ref(false);
const joinModalKey = ref(0); // 강제 재렌더링용

// 로딩 상태
const loading = ref(false);
const loadingSessions = ref(false);

// 현재 사용자 정보 (localStorage에서 가져오기)
const currentUserId = computed(() => {
  const userId = localStorage.getItem('userId');
  return userId ? parseInt(userId) : 1001;
});

// 그룹 데이터
const myGroups = ref<Group[]>([]);

// 전체 활성화된 그룹책 세션
const allActiveGroupBookSessions = ref<ActiveSession[]>([]);

// 내가 참여 가능한 활성화된 그룹책 세션만 필터링
const availableGroupBookSessions = computed(() => {
  const filtered = allActiveGroupBookSessions.value.filter(session => {
    const myGroupIds = myGroups.value.map(group => group.groupId);
    return myGroupIds.includes(session.groupId);
  });
  console.log('🔍 availableGroupBookSessions computed 실행됨, 결과:', filtered.length);
  return filtered;
});

// 참여하기용: 활성화된 세션이 있는 그룹들만 필터링
const activeGroupsForJoin = computed(() => {
  const activeSessionGroupIds = availableGroupBookSessions.value.map(session => session.groupId);
  const activeGroups = myGroups.value.filter(group => 
    activeSessionGroupIds.includes(group.groupId)
  );
  console.log('🔍 activeGroupsForJoin computed 실행됨, 결과:', activeGroups.length);
  return activeGroups;
});

// API 호출 함수들
const fetchMyGroups = async () => {
  loading.value = true;
  try {
    myGroups.value = await groupService.fetchMyGroups();
  } catch (error) {
    console.error('그룹 목록 조회 실패:', error);
  } finally {
    loading.value = false;
  }
};

const fetchAllActiveGroupBookSessions = async () => {
  loadingSessions.value = true;
  try {
    allActiveGroupBookSessions.value = await groupService.fetchActiveGroupBookSessions();
  } catch (error) {
    console.error('활성화된 세션 조회 실패:', error);
  } finally {
    loadingSessions.value = false;
  }
};

// 모달 열기 함수들
const openGroupModal = async () => {
  showGroupModal.value = true;
  await Promise.all([
    fetchMyGroups(),
    fetchAllActiveGroupBookSessions()
  ]);
};

const openCreateModal = async () => {
  showCreateModal.value = true;
  await fetchMyGroups();
};

const openJoinModal = async () => {
  showJoinModal.value = true;
  loadingSessions.value = true;
  
  try {
    // 그룹 데이터와 활성 세션을 모두 가져옴 (최신 상태로 업데이트)
    console.log('🔍 참여하기 모달 열기 - 최신 세션 상태 확인');
    await Promise.all([
      fetchMyGroups(),
      fetchAllActiveGroupBookSessions()
    ]);
    
    console.log('🔍 현재 활성 세션:', allActiveGroupBookSessions.value.length);
    console.log('🔍 참여 가능한 그룹:', activeGroupsForJoin.value.length);
  } catch (error) {
    console.error('데이터 로딩 실패:', error);
  } finally {
    loadingSessions.value = false;
  }
};

// 모달 닫기 함수들
const closeGroupModal = () => {
  console.log('=== 그룹 모달 닫기 시작 ===');
  
  // 1단계: 모든 상태 강제 초기화
  showGroupModal.value = false;
  showJoinModal.value = false;
  showCreateModal.value = false;
  loading.value = false;
  loadingSessions.value = false;
  
  // 2단계: 비동기로 다시 한 번 확인
  setTimeout(() => {
    showGroupModal.value = false;
    console.log('그룹 모달 완전 닫기 완료');
  }, 10);
  
  console.log('=== 그룹 모달 닫기 완료 ===');
};

const closeCreateModal = () => {
  showCreateModal.value = false;
  showGroupModal.value = false;
  showJoinModal.value = false;
  loading.value = false;
};

const closeJoinModal = () => {
  console.log('🔥🔥🔥 부모 컴포넌트에서 closeJoinModal 호출됨!');
  console.log('호출 전 showJoinModal 값:', showJoinModal.value);
  
  // 1단계: 상태 변경 전 로그
  showJoinModal.value = false;
  console.log('showJoinModal.value = false 설정 후:', showJoinModal.value);
  
  showGroupModal.value = false;
  showCreateModal.value = false;
  loading.value = false;
  loadingSessions.value = false;
  
  // 2단계: 강제 재렌더링
  joinModalKey.value = Date.now();
  console.log('joinModalKey 업데이트:', joinModalKey.value);
  
  // 3단계: nextTick으로 DOM 업데이트 대기
  nextTick(() => {
    console.log('nextTick에서 showJoinModal 값:', showJoinModal.value);
    if (showJoinModal.value === true) {
      console.error('❌ nextTick에서도 모달이 여전히 true입니다!');
    }
  });
  
  // 4단계: 추가 안전장치
  setTimeout(() => {
    showJoinModal.value = false;
    console.log('setTimeout에서 최종 확인:', showJoinModal.value);
  }, 10);
  
  console.log('🔥🔥🔥 closeJoinModal 함수 완료');
};

const selectGroup = async (group: Group) => {
  console.log('선택된 그룹:', group);
  
  try {
    const isGroupAlreadyActive = isGroupActive(group.groupId);
    
    if (isGroupAlreadyActive) {
      // 활성화된 그룹인 경우 바로 참여
      router.push({
        path: '/group-book-creation',
        query: { 
          groupId: group.groupId.toString(), 
          groupName: group.groupName,
          mode: 'join'
        }
      });
    } else {
      // 비활성화된 그룹인 경우 새로 세션 시작
      await groupService.startGroupBookSession(group.groupId, group.groupName);
      
      router.push({
        path: '/group-book-creation',
        query: { 
          groupId: group.groupId.toString(), 
          groupName: group.groupName 
        }
      });
    }
    closeGroupModal();
  } catch (error) {
    console.error('라우터 네비게이션 오류:', error);
    const modeParam = isGroupActive(group.groupId) ? '&mode=join' : '';
    window.location.href = `/group-book-creation?groupId=${group.groupId}&groupName=${encodeURIComponent(group.groupName)}${modeParam}`;
  }
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
    closeJoinModal();
  } catch (error) {
    console.error('그룹책 세션 참여 오류:', error);
    window.location.href = `/group-book-creation?groupId=${session.groupId}&groupName=${encodeURIComponent(session.groupName)}&mode=join`;
  }
};

const joinExistingGroupBookSession = (group: Group) => {
  console.log('활성화된 그룹책 방에 참여:', group);
  
  try {
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: group.groupId.toString(), 
        groupName: group.groupName,
        mode: 'join'
      }
    });
    closeJoinModal();
  } catch (error) {
    console.error('그룹책 세션 참여 오류:', error);
    window.location.href = `/group-book-creation?groupId=${group.groupId}&groupName=${encodeURIComponent(group.groupName)}&mode=join`;
  }
};

const goToMyLibrary = () => {
  closeGroupModal();
  router.push({ path: '/my-library' });
};

const handleCreateFromJoin = () => {
  closeJoinModal();
  openGroupModal();
};

const selectGroupForCreate = (group: Group) => {
  console.log('그룹책 만들기용 그룹 선택:', group);
  
  try {
    router.push({
      path: '/group-book-editor',
      query: { 
        groupId: group.groupId.toString(), 
        groupName: group.groupName
      }
    });
    closeCreateModal();
  } catch (error) {
    console.error('그룹책 에디터 이동 오류:', error);
    window.location.href = `/group-book-editor?groupId=${group.groupId}&groupName=${encodeURIComponent(group.groupName)}`;
  }
};

const goToGroupCreate = () => {
  router.push('/group-book-editor');
};

// 그룹이 활성화되어 있는지 확인하는 함수
const isGroupActive = (groupId: number) => {
  return allActiveGroupBookSessions.value.some(session => session.groupId === groupId);
};

// showJoinModal 변경 감지
watch(showJoinModal, (newValue, oldValue) => {
  console.log(`🔍 showJoinModal 변경 감지: ${oldValue} → ${newValue}`);
  const stack = new Error().stack;
  console.log('변경된 곳의 호출 스택:', stack);
}, { immediate: true });

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchMyGroups();
});
</script>

<style scoped>
/* --- Google Fonts Import --- */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+KR:wght@400;600;700&family=Pretendard:wght@400;500;700&display=swap');

/* --- 색상 변수 --- */
:root {
  --background-color: #F5F5DC;
  --surface-color: #FFFFFF;
  --primary-text-color: #3D2C20;
  --secondary-text-color: #6c757d;
  --accent-color: #8B4513;
  --border-color: #EAE0D5;
  --shadow-color: rgba(0, 0, 0, 0.06);
}

.create-book-page {
  padding: 2em 2rem 2rem 2rem;
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  /* font-family: 'Pretendard', sans-serif; */
}

.section-title {
  font-family: 'SCDream3', serif;
  font-size: 4rem;
  font-weight: 700;
  color: var(--primary-text-color);
  margin-bottom: 0rem;
  margin-left: 3rem;
  margin-right: auto;
}

.section-subtitle1 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(116, 125, 76, 0.9);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: -0.5rem;
}

.section-subtitle2 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(141, 153, 109, 0.7);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: -0.5rem;

}

.section-subtitle3 {
  font-family: 'SCDream4', serif;
  font-size: 3rem;
  color: rgba(147, 161, 89, 0.4);
  margin-left: 3.5rem;
  margin-right: auto;
  margin-bottom: 5rem;
}

.initial-choice-section {
  max-width: 1200px;
  margin: 0 auto;
}

.choice-section {
   max-width: 1200px;
}

.choice-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 420px)); /* 2개의 열, 각 열의 최대 너비 420px */
  gap: 3.5rem; /* 카드 사이 간격 조정 */
  justify-content: center; /* 카드들을 중앙에 정렬 */
}

.choice-card {
  background: var(--surface-color);
  border-radius: 50px;
  padding: 2.5rem;
  border: 3px solid #657143;
  box-shadow: 0 4px 15px var(--shadow-color);
  cursor: pointer;
  text-align: center;
  transition: color 0.4s ease, box-shadow 0.3s;
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.choice-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(138, 154, 91, 0.4);
  transform-origin: top;
  transform: scaleY(0);
  transition: transform 0.5s ease-in-out;
  z-index: -1;
}

.choice-card:hover::before {
  transform-origin: bottom;
  transform: scaleY(1);
}

.choice-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  color: white;
  /* 텍스트 색상을 변경하여 가독성 확보 */
}

.card-icon {
  font-size: 3rem;
  color: var(--accent-color);
  margin-bottom: 1rem;
  line-height: 1;
}

.card-title {
  font-family: 'EBSHunminjeongeumSaeronL', serif;
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 0.9rem;
}

.card-description {
  font-family: 'SCDream4', serif;
  color: var(--secondary-text-color);
  line-height: 1.6;
}

/* --- 반응형 디자인 --- */
@media (max-width: 1200px) {
  .section-title {
    font-size: 3.5rem;
    margin-left: 2rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 2.5rem;
    margin-left: 2.5rem;
  }
  
  .choice-cards {
    grid-template-columns: repeat(3, minmax(0, 300px));
    gap: 2rem;
  }
}

@media (max-width: 992px) {
  .create-book-page {
    padding: 1.5rem 1.5rem 1.5rem 1.5rem;
  }
  
  .section-title {
    font-size: 3rem;
    margin-left: 1.5rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 2rem;
    margin-left: 2rem;
    margin-bottom: -0.3rem;
  }
  
  .section-subtitle3 {
    margin-bottom: 3rem;
  }
  
  .choice-cards {
    grid-template-columns: repeat(2, minmax(0, 280px));
    gap: 1.5rem;
  }
  
  .choice-card {
    padding: 2rem;
  }
}

@media (max-width: 768px) {
  .create-book-page {
    padding: 1rem;
  }
  
  .section-title {
    font-size: 2.5rem;
    margin-left: 1rem;
    text-align: center;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 1.5rem;
    margin-left: 1rem;
    text-align: center;
  }
  
  .choice-cards {
    grid-template-columns: 1fr;
    gap: 1.5rem;
    max-width: 400px;
    margin: 0 auto;
  }
  
  .choice-card {
    padding: 1.5rem;
  }
  
  .card-icon {
    font-size: 2.5rem;
  }
  
  .card-title {
    font-size: 1.5rem;
  }
  
  .card-description {
    font-size: 0.9rem;
  }
}

@media (max-width: 480px) {
  .section-title {
    font-size: 2rem;
  }
  
  .section-subtitle1,
  .section-subtitle2,
  .section-subtitle3 {
    font-size: 1.2rem;
  }
  
  .choice-card {
    padding: 1rem;
    border-radius: 25px;
  }
  
  .card-icon {
    font-size: 2rem;
  }
  
  .card-title {
    font-size: 1.3rem;
  }
  
  .card-description {
    font-size: 0.85rem;
  }
}
</style>