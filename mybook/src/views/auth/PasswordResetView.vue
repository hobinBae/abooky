<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <div class="auth-image-section">
        <img :src="currentImage.src" alt="Auth Illustration" class="auth-image" />
        <h2 class="image-section-title" v-html="currentImage.title"></h2>
        <p class="image-section-subtitle" v-html="currentImage.subtitle"></p>
        <div class="pagination-controls">
          <button @click="prevImage" class="pagination-button"><</button>
          <div class="dots">
            <span v-for="(image, index) in images" :key="index" class="dot"
              :class="{ active: index === currentImageIndex }"></span>
          </div>
          <button @click="nextImage" class="pagination-button">></button>
        </div>
      </div>
      <div class="auth-container">
        <h1 class="auth-title">비밀번호 재설정</h1>
        <p class="auth-subtitle">가입 시 사용한 이메일 주소를 입력하시면, 비밀번호 재설정 링크를 보내드립니다.</p>

        <form v-if="!isEmailSent" @submit.prevent="handlePasswordReset">
          <div class="mb-3">
            <label for="email" class="form-label">이메일</label>
            <input type="email" v-model="email" class="form-control" id="email" placeholder="you@example.com" required>
          </div>

          <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            {{ isLoading ? '전송 중...' : '재설정 이메일 보내기' }}
          </button>
        </form>

        <div v-else class="alert alert-success">
          <p><strong>{{ email }}</strong>으로 비밀번호 재설정 이메일을 보냈습니다.</p>
          <p>이메일을 확인하여 비밀번호를 재설정해주세요. 스팸 메일함도 확인해보세요.</p>
        </div>

        <!-- CustomAlert Component -->
        <CustomAlert ref="customAlert" @alert-opened="handleAlertOpened" @alert-closed="handleAlertClosed" />

        <div class="auth-links">
          <router-link to="/login">로그인으로 돌아가기</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import apiClient from '@/api';
import CustomAlert from '@/components/common/CustomAlert.vue';

// --- Image Slider State ---
const images = ref([
  {
    src: '/images/man.png',
    title: '누구나 작가가 될 수 있어요',
    subtitle: 'AI 도우미 아띠와 함께 <br />나만의 이야기로 책을 만들어 보세요'
  },
  {
    src: '/images/woman.png',
    title: '당신의 이야기가 시작되는 곳',
    subtitle: '세상에 단 하나뿐인 당신의 책을 만들어보세요<br />지금 바로 작가가 되어 첫 글을 공유할 수 있습니다'
  },
  {
    src: '/images/group.png',
    title: '함께 쓰는 즐거움',
    subtitle: '친구들과 함께 책을 만들고<br />우리 그룹의 책꽂이에 책을 모아 보세요'
  }
]);
const currentImageIndex = ref(0);
const currentImage = computed(() => images.value[currentImageIndex.value]);

function nextImage() {
  currentImageIndex.value = (currentImageIndex.value + 1) % images.value.length;
}

function prevImage() {
  currentImageIndex.value = (currentImageIndex.value - 1 + images.value.length) % images.value.length;
}

// --- Automatic Slider ---
let slideInterval: number;

onMounted(() => {
  slideInterval = window.setInterval(() => {
    nextImage();
  }, 5000); // 5초로 변경
});

onUnmounted(() => {
  clearInterval(slideInterval);
});


// --- Router ---
const router = useRouter();

// --- Reactive State ---
const email = ref('');
const isLoading = ref(false);
const isEmailSent = ref(false);

// --- Refs for CustomAlert ---
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);

// --- Functions ---
async function handleAlertOpened() {
  document.body.style.overflow = 'hidden';
}

async function handleAlertClosed() {
  document.body.style.overflow = ''; // Reset to default
}

async function handlePasswordReset() {
  if (!email.value) {
    if (customAlert.value) {
      customAlert.value.showAlert({ message: '이메일 주소를 입력해주세요.', title: '입력 오류' });
    }
    return;
  }
  isLoading.value = true;

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1500)); 

  // Simulate success or failure based on email for testing
  if (email.value.includes('@') && email.value.includes('.') && !email.value.includes('fail')) {
    isEmailSent.value = true;
    if (customAlert.value) {
      customAlert.value.showAlert({ message: '비밀번호 재설정 이메일을 보냈습니다. 이메일을 확인해주세요.', title: '이메일 전송 완료' });
    }
  }
  else {
    if (customAlert.value) {
      customAlert.value.showAlert({ message: '이메일 전송에 실패했습니다. 유효한 이메일을 입력해주세요. (더미)', title: '전송 실패' });
    }
  }
  isLoading.value = false;
}
</script>

<style scoped>
.auth-page {
  padding: 1.6rem; /* 2rem * 0.8 */
  background-color: var(--background-color);
  color: var(--primary-text-color);
  min-height: calc(100vh - 56px);
  font-family: 'SCDream4', sans-serif;
  display: flex;
  min-height: 80vh;
  align-items: center;
  justify-content: center;
}

.auth-wrapper {
  display: flex;
  width: 100%;
  max-width: 768px; /* 960px * 0.8 */
  min-height: 544px; /* 680px * 0.8 */
  background-color: #fff;
  border-radius: 10px; /* 12px * 0.8 */
  border: 2px solid #657143; /* 3px * 0.8 */
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); /* 10px, 30px * 0.8 */
  overflow: hidden;
}

.auth-image-section {
  display: none;
  position: relative;
}

.auth-container {
  width: 100%;
  border-radius: 10px; /* 12px * 0.8 */
}

/* Apply two-column layout on larger screens */
@media (min-width: 992px) {
  .auth-image-section {
    flex: 0 0 50%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 2rem; /* 2.5rem * 0.8 */
    background-color: #fff;
  }

  .auth-container {
    flex: 0 0 50%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 2.4rem; /* 3rem * 0.8 */
    border-radius: 0;
  }
}

.auth-image {
  max-width: 100%;
  max-height: 256px; /* 320px * 0.8 */
  object-fit: contain;
  margin-bottom: 1.6rem; /* 2rem * 0.8 */
}

.image-section-title {
  font-size: 1.44rem; /* 1.8rem * 0.8 */
  font-weight: 700;
  color: #333;
  margin-bottom: 0.8rem; /* 1rem * 0.8 */
  text-align: center;
}

.image-section-subtitle {
  font-size: 0.8rem; /* 1rem * 0.8 */
  color: #666;
  text-align: center;
  line-height: 1.6;
}

.auth-title {
  font-size: 16px; /* 20px * 0.8 */
  font-weight: 600;
  color: #333;
  margin-bottom: 0.4rem; /* 0.5rem * 0.8 */
  text-align: left;
}

.auth-subtitle {
  color: #666;
  margin-bottom: 48px; /* 60px * 0.8 */
  font-size: 12px; /* 15px * 0.8 */
  text-align: left;
}

.form-label {
  font-weight: 600;
  color: #555;
}

.form-control {
  background-color: #f4f3e8;
  border: 1px solid #e0e0d1;
  padding: 0.68rem 0.88rem; /* 0.85rem * 0.8, 1.1rem * 0.8 */
  border-radius: 6px; /* 8px * 0.8 */
  transition: background-color 0.2s, border-color 0.2s;
}

.form-control:-webkit-autofill,
.form-control:-webkit-autofill:hover,
.form-control:-webkit-autofill:focus,
.form-control:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px #f4f3e8 inset !important;
  box-shadow: 0 0 0 30px #f4f3e8 inset !important;
}

.form-control:focus {
  background-color: #fff;
  border-color: #8A9A5B;
  box-shadow: 0 0 0 0.16rem rgba(138, 154, 91, 0.25); /* 0.2rem * 0.8 */
}

.btn-auth {
  padding: 0.68rem; /* 0.85rem * 0.8 */
  font-size: 0.8rem; /* 1rem * 0.8 */
  font-weight: 600;
  background-color: #8A9A5B;
  border-color: #8A9A5B;
  border-radius: 6px; /* 8px * 0.8 */
  transition: background-color 0.3s;
}

.btn-auth:hover {
  background-color: #6F7D48;
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: 1.2rem; /* 1.5rem * 0.8 */
  margin-top: 1.2rem; /* 1.5rem * 0.8 */
}

.auth-links a {
  color: #868e96;
  text-decoration: none;
  font-size: 0.72rem; /* 0.9rem * 0.8 */
  transition: color 0.2s;
  font-weight: 600;
}

.auth-links a:hover {
  color: #495057;
}

.social-login-divider {
  text-align: center;
  margin: 2rem 0; /* 2.5rem * 0.8 */
  color: #adb5bd;
  font-size: 0.68rem; /* 0.85rem * 0.8 */
  position: relative;
}

.social-login-divider::before,
.social-login-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 30%;
  height: 1px;
  background-color: #e9ecef;
}

.social-login-divider::before {
  left: 0;
}

.social-login-divider::after {
  right: 0;
}

.social-login-buttons .btn-social {
  width: 100%;
  margin-bottom: 0.6rem; /* 0.75rem * 0.8 */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem; /* 0.75rem * 0.8 */
  padding: 0.68rem; /* 0.85rem * 0.8 */
  border-radius: 6px; /* 8px * 0.8 */
  font-weight: 500;
}

.btn-google {
  background-color: #fff;
  color: #495057;
  border: 1px solid #dee2e6;
  transition: background-color 0.2s, border-color 0.2s;
}

.btn-google:hover {
  background-color: #f8f9fa;
  border-color: #ced4da;
}

.pagination-controls {
  position: absolute;
  bottom: 2.4rem; /* 3rem * 0.8 */
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1.2rem; /* 1.5rem * 0.8 */
  align-items: center;
}

.pagination-button {
  background: none;
  border: none;
  color: #adb5bd;
  width: 29px; /* 36px * 0.8 */
  height: 29px; /* 36px * 0.8 */
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2rem; /* 1.5rem * 0.8 */
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.pagination-button:hover {
  color: #495057;
}

.dots {
  display: flex;
  gap: 6px; /* 8px * 0.8 */
}

.dot {
  width: 8px; /* 10px * 0.8 */
  height: 8px; /* 10px * 0.8 */
  border-radius: 50%;
  background-color: #dee2e6;
  transition: background-color 0.3s ease;
}

.dot.active {
  background-color: #8A9A5B;
}
</style>