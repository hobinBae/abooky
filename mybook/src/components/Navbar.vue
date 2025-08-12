<template>
  <nav class="navbar navbar-expand-lg"
       :class="{ 'navbar-content-hidden': isIntroActive, 'navbar-dark': isHome, 'navbar-light': !isHome, 'scrolled': isScrolled }">
    <div class="container-fluid position-relative">
      <router-link class="navbar-brand" to="/">
        <div class="logo-container">
          <span class="logo-large">아</span><span class="logo-small">띠와&nbsp;</span>
          <span class="logo-large">북</span><span class="logo-small">적북적&nbsp;</span>
          <span class="logo-large">이</span><span class="logo-small">야길</span>
        </div>
      </router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
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
            <router-link class="nav-link" to="/my-library">서재</router-link>
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
        <h2>알림</h2>
        <button @click="toggleNotificationModal" class="close-button">&times;</button>
      </div>
      <div class="modal-body">
        <ul v-if="myInvites.length > 0">
          <li v-for="invite in myInvites" :key="invite.groupApplyId">
            <span>'{{ invite.groupTitle }}' 그룹에서 초대했습니다.</span>
            <div class="invite-actions">
              <button @click="handleInvite(invite, 'ACCEPT')" class="btn-invite btn-accept">수락</button>
              <button @click="handleInvite(invite, 'REJECT')" class="btn-invite btn-reject">거절</button>
            </div>
          </li>
        </ul>
        <p v-else>새로운 초대가 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { storeToRefs } from 'pinia';
import apiClient from '@/api';

interface Invite {
  groupApplyId: number;
  groupId: number;
  groupTitle: string;
}

const authStore = useAuthStore();
const { isLoggedIn } = storeToRefs(authStore);
const { logout } = authStore;

defineProps({
  isIntroActive: Boolean,
  isHome: Boolean
});

const isScrolled = ref(false);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

onMounted(() => {
  window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});

const showNotificationModal = ref(false);
const myInvites = ref<Invite[]>([]);

const toggleNotificationModal = () => {
  showNotificationModal.value = !showNotificationModal.value;
  if (showNotificationModal.value) {
    fetchMyInvites();
  }
};

const fetchMyInvites = async () => {
  if (!isLoggedIn.value) return;
  try {
    const response = await apiClient.get('/api/v1/members/me/invites');
    myInvites.value = response.data.data;
  } catch (error) {
    console.error('Failed to fetch invites:', error);
  }
};

const handleInvite = async (invite: Invite, status: 'ACCEPT' | 'REJECT') => {
  try {
    await apiClient.patch(`/api/v1/groups/${invite.groupId}/invites/${invite.groupApplyId}`, { status });
    alert(`초대를 ${status === 'ACCEPT' ? '수락' : '거절'}했습니다.`);
    fetchMyInvites(); // 목록 새로고침
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
  z-index: 1030;
  transition: background-color 0.5s ease;
  padding-top: 20px; /* 상단 여백 추가 */
  padding-bottom: 20px; /* 하단 여백 추가 */
}

.navbar.scrolled {
  background-color: rgb(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
}

.navbar-content-hidden .navbar-brand,
.navbar-content-hidden .navbar-toggler,
.navbar-content-hidden .nav-link {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.5s ease, visibility 0.5s ease;
}

.navbar-brand {
  padding-left: 2rem; /* Add left padding to the logo */
}

.navbar-brand,
.navbar-toggler,
.nav-link {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.5s ease, visibility 0.5s ease, color 0.5s ease, transform 0.2s ease-in-out;
  font-size: 18px !important;
}

.nav-link:hover {
  transform: scale(1.1);
}

.notification-item {
  position: relative;
}

.notification-item .badge {
  position: absolute;
  top: 5px;
  right: 0px;
  padding: 3px 6px;
  border-radius: 50%;
  background-color: rgb(163, 24, 24);
  color: white;
  font-size: 10px;
}

.notification-modal {
  position: absolute;
  top: 60px; /* Adjust this value based on your navbar's height */
  right: 10px;
  width: 450px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 15px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 1055;
}

.modal-content {
  padding: 15px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 10px;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
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
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.modal-body li span {
  flex-grow: 1;
  margin-right: 15px;
}

.modal-body li:last-child {
  border-bottom: none;
}

.invite-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.btn-invite {
  background: none;
  border: 1px solid;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
}

.btn-accept {
  color: #28a745;
  border-color: #28a745;
}

.btn-reject {
  color: #dc3545;
  border-color: #dc3545;
}

.auth-button {
  position: relative;
  overflow: hidden;
  z-index: 1;
  display: inline-block;
  border: 1px solid #dee2e6 !important;
  border-radius: 20px !important;
  margin-left: 1rem !important;
  margin-right: 1rem !important;
  padding: 0.2rem 0.7rem !important;
  font-size: 14px !important;
  white-space: nowrap;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  transition: color 0.4s ease;
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
  transform-origin: top;
  transform: scaleY(1);
}

.logo-container {
  display: flex;
  align-items: baseline;
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-weight: normal;
  white-space: nowrap;
  min-width: 250px; /* Adjust as needed to fit the full text */
}

.logo-large {
  font-size: 2rem;
  transition: margin-right 0.4s ease-in-out;
  margin-right: 1rem; /* Default spacing for "아 북 이" */
}

.logo-small {
  font-size: 1rem;
  max-width: 0;
  opacity: 0;
  overflow: hidden;
  transition: max-width 0.4s ease-in-out, opacity 0.3s ease-in-out;
  padding-right: 0.5rem; /* Spacing for "아띠와 북적북적 이야길" */
}

.logo-container:hover .logo-large {
  margin-right: 0;
}

.logo-container:hover .logo-small {
  max-width: 100px; /* A reasonable max width to expand to */
  opacity: 1;
}

#navbarNav {
  position: static !important;
}

.center-nav {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  list-style: none;
  padding-left: 0;
  gap: 4rem; /* 메뉴 간격 조정 */
}

.center-nav .nav-link {
  font-family: 'EBSHunminjeongeumSaeronL', sans-serif;
  font-size: 20px !important; /* 글씨 크기 조정 */
  font-weight: bold !important; /* 항상 두껍게 */
}

.navbar-dark .center-nav .nav-link {
  color: rgba(255, 255, 255, 0.8) !important;
}

.navbar-light .center-nav .nav-link {
  color: #000000 !important;
}

</style>
