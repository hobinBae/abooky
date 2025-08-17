<template>
  <div class="auth-page">
    <div class="auth-wrapper">
      <div class="auth-image-section">
        <img :src="currentImage.src" alt="Auth Illustration" class="auth-image" />
        <h2 class="image-section-title" v-html="currentImage.title"></h2>
        <p class="image-section-subtitle" v-html="currentImage.subtitle"></p>
        <div class="pagination-controls">
          <button @click="prevImage" class="pagination-button">
          </button>
          <div class="dots">
            <span v-for="(image, index) in images" :key="index" class="dot"
              :class="{ active: index === currentImageIndex }"></span>
          </div>
          <button @click="nextImage" class="pagination-button">></button>
        </div>
      </div>
      <div class="auth-container">
        <h1 class="auth-title">회원가입</h1>
        <p class="auth-subtitle">당신의 이야기, 지금 바로 시작해보세요.</p>

        <form @submit.prevent="handleSignup">
          <div class="row">
            <div class="col-md-6 mb-1">
              <label for="name" class="form-label">이름</label>
              <input type="text" v-model="form.name" class="form-control" id="name" placeholder="홍길동" required>
            </div>
            <div class="col-md-6 mb-1">
              <label for="nickname" class="form-label">닉네임 (필명)</label>
              <input type="text" v-model="form.nickname" class="form-control" id="nickname" placeholder="멋진작가" required>
            </div>
          </div>

          <div class="mb-1">
            <label for="email" class="form-label">이메일 (ID)</label>
            <input type="email" v-model="form.email" class="form-control" id="email" placeholder="you@example.com"
              required>
          </div>

          <div class="mb-1">
            <label for="password" class="form-label">비밀번호</label>
            <input type="password" v-model="form.password" class="form-control" id="password" placeholder="••••••••"
              required>
          </div>

          <div class="mb-1">
            <label for="confirmPassword" class="form-label">비밀번호 확인</label>
            <input type="password" v-model="form.confirmPassword" class="form-control" id="confirmPassword"
              placeholder="••••••••" required>
          </div>

          <div class="mb-1">
            <label for="birthdate" class="form-label">생년월일</label>
            <input type="date" v-model="form.birthdate" class="form-control" id="birthdate" required>
          </div>

          <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

          <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
            {{ isLoading ? '가입하는 중...' : '회원가입' }}
          </button>
        </form>
        <CustomAlert ref="customAlert" @alert-closed="handleAlertClosed" />

        <div class="auth-links">
          <span>이미 계정이 있으신가요?</span>
          <router-link to="/login">로그인</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter, RouterLink } from 'vue-router';
import apiClient from '@/api';
import { AxiosError } from 'axios';
import CustomAlert from '@/components/common/CustomAlert.vue';

// --- Image Slider State ---
const images = ref([
  {
    src: '/images/man.png',
    title: '누구나 쉽게 작가가 될 수 있어요',
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
  }, 5000);
});

onUnmounted(() => {
  clearInterval(slideInterval);
});


// --- Router ---
const router = useRouter();

// --- Reactive State ---
const form = ref({
  name: '',
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  birthdate: '',
});
const errorMessage = ref('');
const isLoading = ref(false);
const customAlert = ref<InstanceType<typeof CustomAlert> | null>(null);
// --- Functions ---
async function handleSignup() {
  if (Object.values(form.value).some(v => !v)) {
    errorMessage.value = '모든 필드를 입력해주세요.';
    return;
  }
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.';
    return;
  }
  if (form.value.password.length < 8 || form.value.password.length > 20) {
    errorMessage.value = '비밀번호는 8자 이상, 20자 이하이어야 합니다.';
    return;
  }

  isLoading.value = true;
  errorMessage.value = '';

  try {
    const formData = new FormData();
    formData.append('name', form.value.name);
    formData.append('nickname', form.value.nickname);
    formData.append('email', form.value.email);
    formData.append('password', form.value.password);
    formData.append('birthdate', form.value.birthdate);

    await apiClient.post('/api/v1/members/register', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (customAlert.value) {
      customAlert.value.showAlert({
        message: '회원가입에 성공했습니다! 로그인 페이지로 이동합니다.',
        title: '회원가입 완료'
      });
    }

  } catch (error) {
    if (error instanceof AxiosError && error.response) {
      errorMessage.value = error.response.data.message || '회원가입 중 오류가 발생했습니다.';
    } else if (error instanceof Error) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = '회원가입 중 알 수 없는 오류가 발생했습니다.';
    }
  } finally {
    isLoading.value = false;
  }
}
function handleAlertClosed() {
  router.push('/login');
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
  padding: 2rem; /* 2.5rem * 0.8 */
  border-radius: 10px; /* 12px * 0.8 */
}

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
  margin-bottom: 0.96rem; /* 1.2rem * 0.8 */
  font-size: 12px; /* 15px * 0.8 */
  text-align: left;
}

.form-label {
  font-weight: 600;
  color: #555;
  font-size: 11px; /* 14px * 0.8 */
  margin-bottom: 0.2rem; /* 0.25rem * 0.8 */
}

.form-control {
  background-color: #f4f3e8;
  border: 1px solid #e0e0d1;
  padding: 0.52rem 0.8rem; /* 0.65rem * 0.8, 1rem * 0.8 */
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
  margin-top: 0.8rem; /* 1rem * 0.8 */
}

.btn-auth:hover {
  background-color: #6F7D48;
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: 0.4rem; /* 0.5rem * 0.8 */
  margin-top: 1.2rem; /* 1.5rem * 0.8 */
  font-size: 0.72rem; /* 0.9rem * 0.8 */
  color: #868e96;
}

.auth-links a {
  color: #8A9A5B;
  text-decoration: none;
  font-weight: 600;
}

.auth-links a:hover {
  text-decoration: underline;
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
