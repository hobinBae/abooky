<template>
  <nav class="navbar navbar-expand-lg" ref="navbarRef" :class="{
    'navbar-content-hidden': isIntroActive,
    'navbar-dark': isHome && !isMenuOpen,
    'navbar-light': !isHome || isMenuOpen,
    'scrolled': isScrolled,
    'menu-open': isMenuOpen
  }">
    <div class="container-fluid position-relative">
      <a class="navbar-brand" href="#" @click.prevent="goHome">
        <div class="logo-container">
          <span class="logo-large">아</span><span class="logo-small">띠와&nbsp;</span>
          <span class="logo-large">북</span><span class="logo-small">적북적&nbsp;</span>
          <span class="logo-large">이</span><span class="logo-small">야길</span>
        </div>
      </a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
        aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav center-nav">
          <li class="nav-item mx-3">
            <router-link class="nav-link" to="/bookstore">서점</router-link>
          </li>
          <li class="nav-item mx-3">
            <router-link class="nav-link" to="/create-book">책 집필</router-link>
          </li>
          <li class="nav-item mx-3">
            <router-link class="nav-link" to="/my-library">내 서재</router-link>
          </li>
          <li v-if="isLoggedIn" class="nav-item mx-3">
            <router-link class="nav-link" to="/my-page">내 정보</router-link>
          </li>
        </ul>
        <ul class="navbar-nav ms-auto align-items-center flex-nowrap">
          <template v-if="!isLoggedIn">
            <li class="nav-item">
              <router-link class="nav-link auth-button" to="/login">로그인</router-link>
            </li>
          </template>
          <template v-else>
            <li class="nav-item notification-item">
              <a class="nav-link" href="#" @click.prevent="toggleNotificationModal">
                <i class="bi bi-bell-fill"></i>
                <span v-if="myInvites.length > 0" class="badge">{{ myInvites.length }}</span>
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link auth-button" href="#" @click.prevent="logout">로그아웃</a>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>

  <!-- Notification Modal -->
  <div v-if="showNotificationModal" class="notification-modal">
    <div class="modal-content">
      <div class="modal-header">
        <h2>그룹 초대 알림</h2>
      </div>
      <button @click="toggleNotificationModal" class="close-button"><i class="bi bi-x-lg"></i></button>

      <div class="modal-body">
        <ul v-if="myInvites.length > 0">
          <li v-for="invite in myInvites" :key="invite.groupApplyId">
            <span>'{{ invite.groupName }}' 그룹에서 초대했습니다.</span>
            <div class="invite-actions">
              <button @click="handleInvite(invite, 'ACCEPTED')" class="btn-invite btn-accept">수락</button>
              <button @click="handleInvite(invite, 'DENIED')" class="btn-invite btn-reject">거절</button>
            </div>
          </li>
        </ul>
        <p v-else>새로운 초대가 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, defineEmits } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import { groupService, type GroupInvite } from '@/services/groupService';

const authStore = useAuthStore();
const { isLoggedIn } = storeToRefs(authStore);
const { logout } = authStore;

const emit = defineEmits(['go-home']);

defineProps({
  isIntroActive: Boolean,
  isHome: Boolean
});

const goHome = () => {
  emit('go-home');
};

const isScrolled = ref(false);
const isMenuOpen = ref(false);
const navbarRef = ref<HTMLElement | null>(null);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  const navCollapse = document.getElementById('navbarNav');
  if (navCollapse) {
    navCollapse.addEventListener('show.bs.collapse', () => isMenuOpen.value = true);
    navCollapse.addEventListener('hide.bs.collapse', () => isMenuOpen.value = false);
  }
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  // Note: Technically, you should also remove the collapse event listeners,
  // but since the navbar is persistent, it's less critical.
});

const showNotificationModal = ref(false);
const myInvites = ref<GroupInvite[]>([]);

const toggleNotificationModal = () => {
  showNotificationModal.value = !showNotificationModal.value;
  if (showNotificationModal.value) {
    document.body.style.overflow = 'hidden';
    fetchMyInvites();
  } else {
    document.body.style.overflow = '';
  }
};

const fetchMyInvites = async () => {
  if (!isLoggedIn.value) return;
  try {
    myInvites.value = await groupService.fetchMyInvites();
  } catch (error) {
    console.error('Failed to fetch invites:', error);
  }
};

const handleInvite = async (invite: GroupInvite, status: 'ACCEPTED' | 'DENIED') => {
  try {
    const result = await groupService.handleInvite(String(invite.groupId), invite.groupApplyId, status);
    if (result) {
      alert(`초대를 ${status === 'ACCEPTED' ? '수락' : '거절'}했습니다.`);
      fetchMyInvites(); // 목록 새로고침
    } else {
      alert('초대 처리에 실패했습니다.');
    }
  } catch (error) {
    console.error('Failed to handle invite:', error);
    alert('초대 처리 중 오류가 발생했습니다.');
  }
};

watch(isLoggedIn, (newValue) => {
  if (newValue) {
    fetchMyInvites();
  } else {
    myInvites.value = [];
  }
});

onMounted(() => {
  fetchMyInvites();
});
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  background-color: transparent !important;
  z-index: 999;
  /* MyLibraryView 모달보다 낮게 설정 */
  transition: background-color 0.5s ease;
  padding-top: 18px;
  padding-bottom: 18px;
}

.navbar.scrolled {
  background-color: rgba(152, 164, 115, 0.3) !important;
  backdrop-filter: blur(9px);
}

.navbar.menu-open:not(.scrolled) {
  background-color: #fff !important;
}

.navbar.scrolled.menu-open {
  background-color: rgba(152, 164, 115, 0.95) !important;
}


.navbar-content-hidden .navbar-brand,
.navbar-content-hidden .navbar-toggler,
.navbar-content-hidden .nav-link {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.5s ease, visibility 0.5s ease;
}

.navbar-brand {
  padding-left: 1.8rem;
}

.navbar-brand,
.navbar-toggler,
.nav-link {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.5s ease, visibility 0.5s ease, color 0.5s ease, transform 0.2s ease-in-out;
  font-size: 16px !important;
}

.nav-link:hover {
  transform: scale(1.1);
}

.notification-item {
  position: relative;
}

.notification-item .badge {
  position: absolute;
  top: 4px;
  right: 0px;
  padding: 3px 5.5px;
  border-radius: 50%;
  background-color: rgb(208, 42, 42);
  color: white;
  font-size: 9px;
}

.notification-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(38, 30, 23, 0.6);
  backdrop-filter: blur(4px);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
  user-select: none;
}

.notification-modal .modal-content {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.notification-modal .modal-body {
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 9px;
  margin-bottom: 9px;
}

.modal-header h2 {
  font-family: 'SCDream4', sans-serif;
  font-size: 1.2rem;
  /* 글씨 크기 축소 */
  font-weight: 600;
}

.close-button {
  color: #594C40;
  position: absolute;
  top: 0.9rem;
  right: 0.9rem;
  cursor: pointer;
  background: none;
  border: none;
  font-size: 1.35rem;
  line-height: 1;
  padding: 0.45rem;
}

.close-button:hover {
  color: #261E17;
}

.modal-body ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.modal-body li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 0;
  border-bottom: 1px solid #eee;
}

.modal-body li span {
  flex-grow: 1;
  margin-right: 13px;
  font-family: 'SCDream4', sans-serif;
  font-size: 0.85rem;
  /* 글씨 크기 축소 */
}

.modal-body li:last-child {
  border-bottom: none;
}

.invite-actions {
  display: flex;
  gap: 0.45rem;
  flex-shrink: 0;
}

.btn-invite {
  border: 2px solid transparent;
  padding: 0.4rem 0.8rem;
  border-radius: 16px;
  cursor: pointer;
  font-size: 0.7rem;
  font-weight: 600;
  color: white;
}

.btn-accept {
  background-color: #7f924c;
}

.btn-accept:hover {
  background-color: #546127;
}

.btn-reject {
  background-color: #d87777;
}

.btn-reject:hover {
  background-color: #823232;
}

.auth-button {
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: inline-block;
  border: 1px solid #dee2e6 !important;
  border-radius: 18px !important;
  margin-left: 0.9rem !important;
  margin-right: 0.9rem !important;
  padding: 0.2rem 0.6rem !important;
  font-size: 13px !important;
  white-space: nowrap;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  transition: color 0.5s ease;
}

.auth-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(138, 154, 91, 0.4);
  transform-origin: top;
  transform: scaleY(0);
  transition: transform 0.4s ease-in-out;
  z-index: -1;
}

.auth-button:hover::before {
  transform-origin: bottom;
  transform: scaleY(1);
}

.auth-button:hover {
  color: white !important;
}

.logo-container {
  display: flex;
  align-items: baseline;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-weight: normal;
  white-space: nowrap;
  min-width: 225px;
}

.logo-large {
  font-size: 1.8rem;
  transition: margin-right 0.4s ease-in-out;
  margin-right: 0.9rem;
}

.logo-small {
  font-size: 0.9rem;
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  transition: max-width 0.4s ease-in-out, opacity 0.3s ease-in-out;
  padding-right: 0.45rem;
}

.logo-container:hover .logo-large {
  margin-right: 0;
}

.logo-container:hover .logo-small {
  max-width: 90px;
  opacity: 1;
}

#navbarNav {
  position: static !important;
}

/* --- [변경] 모바일과 데스크톱 스타일 분리 --- */

/* 기본 (모바일) 스타일 */
.center-nav {
  display: flex;
  flex-direction: column;
  /* 세로 정렬 */
  align-items: center;
  /* 가운데 정렬 */
  list-style: none;
  padding: 2rem 0;
  /* 위아래 여백 추가 */
  gap: 1.5rem;
  /* 모바일에 맞는 적절한 간격 */
  margin-left: 0;
  /* 중앙 정렬을 위해 margin 초기화 */
  width: 100%;
  /* 너비 100% 차지 */
}

.navbar-nav.ms-auto {
  width: 100%;
  justify-content: center;
  /* 버튼 중앙 정렬 */
  padding-bottom: 1.5rem;
  /* 하단 여백 */
}

/* 데스크톱 (화면이 992px 이상일 때) 스타일 */
@media (min-width: 992px) {
  .center-nav {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    flex-direction: row;
    /* 가로 정렬 */
    padding: 0;
    gap: 3.6rem;
    width: auto;
    /* 너비 자동 */
  }

  .navbar-nav.ms-auto {
    width: auto;
    /* 너비 자동 */
    padding-bottom: 0;
  }
}

.center-nav .nav-link {
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-size: 18px !important;
}

.navbar-dark .center-nav .nav-link {
  color: rgba(255, 255, 255, 0.8) !important;
}

.navbar-light .center-nav .nav-link {
  color: #000000 !important;
}
</style>
