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
          @select="selectGroup"
        />
      </div>
    </SimpleModal>

    <!-- 그룹책 참여 모달 -->
    <SimpleModal 
      :is-visible="showJoinModal" 
      title="활성화된 그룹책 만들기" 
      @close="closeJoinModal"
    >
      <div v-if="loadingSessions" style="padding: 2rem; text-align: center;">
        <LoadingSpinner message="활성화된 세션을 불러오는 중..." />
      </div>
      
      <div v-else-if="availableGroupBookSessions.length === 0" style="padding: 2rem;">
        <EmptyState 
          icon-class="bi bi-book"
          title="참여할 수 있는 그룹책 만들기가 없습니다"
          description="현재 진행 중인 그룹책 만들기가 없습니다.\n직접 그룹책 방을 만들어보세요."
          action-text="그룹책 방 만들기"
          action-class="btn-primary"
          @action="closeJoinModal"
        />
      </div>
      
      <div v-else class="session-list">
        <SessionItem 
          v-for="session in availableGroupBookSessions" 
          :key="session.groupId"
          :session="session"
          @join="joinGroupBookSession"
        />
      </div>
    </SimpleModal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
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
  return allActiveGroupBookSessions.value.filter(session => {
    const myGroupIds = myGroups.value.map(group => group.groupId);
    return myGroupIds.includes(session.groupId);
  });
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

// 이벤트 핸들러들
const closeGroupModal = () => {
  showGroupModal.value = false;
  loading.value = false;
};

const closeJoinModal = () => {
  showJoinModal.value = false;
  loadingSessions.value = false;
};

const selectGroup = async (group: Group) => {
  console.log('선택된 그룹:', group);
  
  try {
    // 그룹책 세션 시작
    await groupService.startGroupBookSession(group.groupId, group.groupName);
    
    router.push({
      path: '/group-book-creation',
      query: { 
        groupId: group.groupId.toString(), 
        groupName: group.groupName 
      }
    });
    closeGroupModal();
  } catch (error) {
    console.error('라우터 네비게이션 오류:', error);
    window.location.href = `/group-book-creation?groupId=${group.groupId}&groupName=${encodeURIComponent(group.groupName)}`;
  }
};

const goToJoin = () => {
  showJoinModal.value = true;
  fetchAllActiveGroupBookSessions();
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

const goToMyLibrary = () => {
  closeGroupModal();
  router.push({ path: '/my-library' });
};

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  fetchMyGroups();
});
</script>

<style scoped>
@import '../../styles/group-book-lobby.css';
</style>