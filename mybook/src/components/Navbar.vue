<template>
  <nav class="navbar navbar-expand-lg fixed-top"
       :class="{ 'navbar-content-hidden': isIntroActive, 'navbar-dark': isHome, 'navbar-light': !isHome }">
    <div class="container-fluid">
      <router-link class="navbar-brand" to="/?from=home">
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
                <i class="fas fa-bell"></i>
                <span v-if="unreadNotifications.length > 0" class="badge">{{ unreadNotifications.length }}</span>
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
        <ul v-if="unreadNotifications.length > 0">
          <li v-for="notification in unreadNotifications" :key="notification.id">
            <span>{{ notification.message }}</span>
            <button @click="markAsRead(notification)" class="confirm-button">확인</button>
          </li>
        </ul>
        <p v-else>새로운 알림이 없습니다.</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';

defineProps({
  isIntroActive: Boolean,
  isHome: Boolean
});

const isLoggedIn = ref(true); // Always logged in for verification
const showNotificationModal = ref(false);

const notifications = ref([
  { id: 1, message: "'코드 마스터' 그룹에서 당신을 초대했습니다.", read: false },
  { id: 2, message: "'Vue 정복' 그룹에서 당신을 초대했습니다.", read: false },
  { id: 3, message: "'타입스크립트 고수' 그룹에서 당신을 초대했습니다.", read: false },
]);

const unreadNotifications = computed(() => {
  return notifications.value.filter(n => !n.read);
});

const toggleNotificationModal = () => {
  showNotificationModal.value = !showNotificationModal.value;
};

const markAsRead = (notification: any) => {
  notification.read = true;
};

const logout = () => {
  isLoggedIn.value = false;
  alert('로그아웃 되었습니다.');
};
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

.confirm-button {
  background: none;
  border: none;
  color: #8B4513; /* Brown color */
  cursor: pointer;
  padding: 5px;
  font-weight: bold;
  flex-shrink: 0;
}
</style>
