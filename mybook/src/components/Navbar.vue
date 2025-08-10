<template>
  <nav class="navbar navbar-expand-lg fixed-top"
       :class="{ 'navbar-content-hidden': isIntroActive, 'navbar-dark': isHome, 'navbar-light': !isHome }">
    <div class="container-fluid">
      <router-link class="navbar-brand" to="/">
        <span style="font-size: 1.5rem; font-weight: bold;">MyBook</span>
      </router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <router-link class="nav-link" to="/bookstore">서점</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/create-book">책만들기</router-link>
          </li>
          <li class="nav-item">
            <router-link class="nav-link" to="/my-library">내서재</router-link>
          </li>
        </ul>
        <ul class="navbar-nav">
          <template v-if="!isLoggedIn">
            <li class="nav-item">
              <router-link class="nav-link" to="/login">로그인</router-link>
            </li>
            <li class="nav-item">
              <router-link class="nav-link" to="/signup">회원가입</router-link>
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
              <router-link class="nav-link" to="/my-page">마이페이지</router-link>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="#" @click.prevent="logout">로그아웃</a>
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
import { ref, onMounted, watch } from 'vue';
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
  background-color: transparent !important;
  z-index: 1030;
  transition: background-color 0.5s ease;
}

.navbar-content-hidden .navbar-brand,
.navbar-content-hidden .navbar-toggler,
.navbar-content-hidden .nav-link {
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.5s ease, visibility 0.5s ease;
}

.navbar-brand,
.navbar-toggler,
.nav-link {
  opacity: 1;
  visibility: visible;
  transition: opacity 0.5s ease, visibility 0.5s ease, color 0.5s ease;
  font-size: 18px !important;
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
  background-color: red;
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
.btn-accept:hover {
  background-color: #28a745;
  color: white;
}

.btn-reject {
  color: #dc3545;
  border-color: #dc3545;
}
.btn-reject:hover {
  background-color: #dc3545;
  color: white;
}
</style>
