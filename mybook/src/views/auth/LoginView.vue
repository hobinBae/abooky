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
        <h1 class="auth-title">아북이 시작하기</h1>
        <p class="auth-subtitle">만나서 반가워요! 당신의 이야기를 이어가세요.</p>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label for="email" class="form-label">이메일</label>
            <input type="email" v-model="email" class="form-control" id="email" placeholder="you@example.com" required>
          </div>
          <div class="mb-3">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" v-model="password" class="form-control" id="password" placeholder="••••••••"
              required>
          </div>

          <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            {{ isLoading ? '로그인 중...' : '로그인' }}
          </button>
        </form>

        <!-- CustomAlert Component -->
        <CustomAlert ref="customAlert" @alert-opened="handleAlertOpened" @alert-closed="handleAlertClosed" />

        <div class="auth-links">
          <router-link to="/signup">회원가입</router-link>
          <router-link to="/password-reset">비밀번호 찾기</router-link>
        </div>

        <div class="social-login-divider">소셜 계정으로 로그인</div>

        <div class="social-login-buttons">
          <button @click="handleSocialLogin('google')" class="btn btn-social btn-google">
            <i class="bi bi-google"></i> Google로 로그인
          </button>
          <!-- <button @click="handleSocialLogin('naver')" class="btn btn-social btn-naver">
            <span class="naver-icon">N</span> 네이버로 로그인
          </button>
          <button @click="handleSocialLogin('kakao')" class="btn btn-social btn-kakao">
            <span class="kakao-icon">K</span> 카카오로 로그인
          </button> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { AxiosError } from 'axios';
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


// --- Store ---
const authStore = useAuthStore();

// --- Router ---
const router = useRouter();
const route = useRoute();

// --- Reactive State ---
const email = ref('');
const password = ref('');
const isLoading = ref(false);

// --- Refs for CustomAlert ---
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);

// --- Functions ---
async function handleLogin() {
  if (!email.value || !password.value) {
    if (customAlert.value) {
      customAlert.value.showAlert({ message: '이메일과 비밀번호를 모두 입력해주세요.', title: '입력 오류' });
    }
    return;
  }
  isLoading.value = true;

  try {
    await authStore.login({
      email: email.value,
      password: password.value,
    });

    const redirectPath = route.query.redirect as string || '/';
    router.push(redirectPath);
  } catch (error) {
    let alertMessage = '로그인 중 알 수 없는 오류가 발생했습니다.';
    const alertTitle = '로그인 오류';

    if (error instanceof AxiosError && error.response) {
      alertMessage = error.response.data.message || '아이디 또는 비밀번호를 확인해주세요.';
    } else if (error instanceof Error) {
      alertMessage = error.message;
    }

    if (customAlert.value) {
      customAlert.value.showAlert({ message: alertMessage, title: alertTitle });
    }
  } finally {
    isLoading.value = false;
  }
}

function handleSocialLogin(provider: 'google' | 'naver' | 'kakao') {
  const baseURL = apiClient.defaults.baseURL;
  
  // 현재 페이지나 리다이렉트 경로를 저장
  const redirectPath = route.query.redirect as string || '/';
  sessionStorage.setItem('socialLoginRedirect', redirectPath);
  
  // 백엔드 OAuth2 엔드포인트로 리다이렉트
  // 백엔드에서는 성공 시 /auth/callback?token=... 로 리다이렉트해야 함
  window.location.href = `${baseURL}/oauth2/authorization/${provider}`;
}

function handleAlertOpened() {
  document.body.style.overflow = 'hidden';
}

function handleAlertClosed() {
  document.body.style.overflow = ''; // Reset to default
}
</script>

<style scoped>
.auth-page {
  padding: 2rem 2rem 2rem 2rem;
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
  max-width: 960px; /* 고정 너비 설정 */
  min-height: 680px; /* 고정 최소 높이 설정 */
  background-color: #fff;
  border-radius: 12px;
  border: 3px solid #657143;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.auth-image-section {
  display: none;
  position: relative;
  /* 모바일에서는 숨김 */
}

.auth-container {
  width: 100%;
  border-radius: 12px;
}

/* Apply two-column layout on larger screens */
@media (min-width: 992px) {
  .auth-image-section {
    flex: 0 0 50%; /* 너비를 50%로 고정 */
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    padding: 2.5rem;
    background-color: #fff;
  }

  .auth-container {
    flex: 0 0 50%; /* 너비를 50%로 고정 */
    display: flex;
    flex-direction: column;
    justify-content: center;
    padding: 3rem;
    border-radius: 0;
  }
}

.auth-image {
  max-width: 100%;
  max-height: 320px;
  object-fit: contain;
  margin-bottom: 2rem;
}

.image-section-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 1rem;
  text-align: center;
}

.image-section-subtitle {
  font-size: 1rem;
  color: #666;
  text-align: center;
  line-height: 1.6;
}

.auth-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
  text-align: left;
}

.auth-subtitle {
  color: #666;
  margin-bottom:60px; /* 회원가입과 동일하게 여백 조정 */
  font-size: 15px;
  text-align: left;
}

.form-label {
  font-weight: 600;
  color: #555;
}

.form-control {
  background-color: #f4f3e8; /* 연한 올리브 배경 */
  border: 1px solid #e0e0d1; /* 연한 올리브 테두리 */
  padding: 0.85rem 1.1rem;
  border-radius: 8px;
  transition: background-color 0.2s, border-color 0.2s;
}

/* 브라우저 자동 완성 스타일 덮어쓰기 */
.form-control:-webkit-autofill,
.form-control:-webkit-autofill:hover,
.form-control:-webkit-autofill:focus,
.form-control:-webkit-autofill:active {
  -webkit-box-shadow: 0 0 0 30px #f4f3e8 inset !important;
  box-shadow: 0 0 0 30px #f4f3e8 inset !important;
}

.form-control:focus {
  background-color: #fff;
  border-color: #8A9A5B; /* 올리브색 */
  box-shadow: 0 0 0 0.2rem rgba(138, 154, 91, 0.25); 
}

.btn-auth {
  padding: 0.85rem;
  font-size: 1rem;
  font-weight: 600;
  background-color: #8A9A5B; /* 올리브색 */
  border-color: #8A9A5B; /* 올리브색 */
  border-radius: 8px;
  transition: background-color 0.3s;
}

.btn-auth:hover {
  background-color: #6F7D48; /* 어두운 올리브색 */
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.auth-links a {
  color: #868e96;
  text-decoration: none;
  font-size: 0.9rem;
  transition: color 0.2s;
  font-weight: 600;
}

.auth-links a:hover {
  color: #495057;
}

.social-login-divider {
  text-align: center;
  margin: 2.5rem 0;
  color: #adb5bd;
  font-size: 0.85rem;
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
  margin-bottom: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 0.85rem;
  border-radius: 8px;
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

.btn-naver {
  background-color: #03C75A;
  color: white;
  border: 1px solid #03C75A;
  transition: background-color 0.2s, border-color 0.2s;
}

.btn-naver:hover {
  background-color: #02B350;
  border-color: #02B350;
  color: white;
}

.btn-kakao {
  background-color: #FEE500;
  color: #191919;
  border: 1px solid #FEE500;
  transition: background-color 0.2s, border-color 0.2s;
}

.btn-kakao:hover {
  background-color: #FDD700;
  border-color: #FDD700;
  color: #191919;
}

.naver-icon, .kakao-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 3px;
  font-weight: bold;
  font-size: 14px;
}

.naver-icon {
  background-color: white;
  color: #03C75A;
}

.kakao-icon {
  background-color: #191919;
  color: #FEE500;
  border-radius: 50%;
}

.pagination-controls {
  position: absolute;
  bottom: 3rem;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.pagination-button {
  background: none;
  border: none; /* 테두리 제거 */
  color: #adb5bd;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.5rem; /* 아이콘 크기 살짝 키움 */
  display: flex;
  justify-content: center;
  align-items: center;
  transition: all 0.3s ease;
}

.pagination-button:hover {
  color: #495057; /* 호버 시 색상만 살짝 어둡게 변경 */
}

.dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #dee2e6;
  transition: background-color 0.3s ease;
}

.dot.active {
  background-color: #8A9A5B; /* 올리브색 */
}
</style>
