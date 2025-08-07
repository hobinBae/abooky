<template>
  <div class="auth-page">
    <div class="auth-container">
      <h1 class="auth-title">로그인</h1>
      <p class="auth-subtitle">다시 만나서 반가워요! 당신의 이야기를 이어가세요.</p>

      <form @submit.prevent="handleLogin">
        <div class="mb-3">
          <label for="email" class="form-label">이메일</label>
          <input type="email" v-model="email" class="form-control" id="email" placeholder="you@example.com" required>
        </div>
        <div class="mb-3">
          <label for="password" class="form-label">비밀번호</label>
          <input type="password" v-model="password" class="form-control" id="password" placeholder="••••••••" required>
        </div>

        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

        <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isLoading ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <div class="auth-links">
        <router-link to="/signup">회원가입</router-link>
        <router-link to="/password-reset">비밀번호 찾기</router-link>
      </div>

      <div class="social-login-divider">소셜 계정으로 로그인</div>

      <div class="social-login-buttons">
        <button @click="handleSocialLogin('google')" class="btn btn-social btn-google">
          <i class="bi bi-google"></i> Google로 로그인
        </button>
        <!-- 다른 소셜 로그인 버튼 추가 가능 -->
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { AxiosError } from 'axios';

// --- Store ---
const authStore = useAuthStore();

// --- Router ---
const router = useRouter();
const route = useRoute();

// --- Reactive State ---
const email = ref('');
const password = ref('');
const errorMessage = ref('');
const isLoading = ref(false);

// --- Functions ---
async function handleLogin() {
  if (!email.value || !password.value) {
    errorMessage.value = '이메일과 비밀번호를 모두 입력해주세요.';
    return;
  }
  isLoading.value = true;
  errorMessage.value = '';

  try {
    await authStore.login({
      email: email.value,
      password: password.value,
    });

    // 성공 시 이전 페이지 또는 홈으로 리디렉션
    const redirectPath = route.query.redirect as string || '/';
    router.push(redirectPath);
  } catch (error) {
    if (error instanceof AxiosError && error.response) {
      errorMessage.value = error.response.data.message || '아이디 또는 비밀번호를 확인해주세요.';
    } else if (error instanceof Error) {
      errorMessage.value = error.message;
    } else {
      errorMessage.value = '로그인 중 알 수 없는 오류가 발생했습니다.';
    }
  } finally {
    isLoading.value = false;
  }
}

function handleSocialLogin(provider: 'google') {
  // 백엔드의 소셜 로그인 URL로 리디렉션
  window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
}

</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5DC;
  padding: 2rem;
}

.auth-container {
  width: 100%;
  max-width: 420px;
  background-color: #FFFFFF;
  padding: 2.5rem;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0,0,0,0.1);
}

.auth-title {
  font-size: 2.2rem;
  font-weight: 700;
  text-align: center;
  color: #3D2C20;
  margin-bottom: 0.5rem;
}

.auth-subtitle {
  text-align: center;
  color: #5C4033;
  margin-bottom: 2rem;
}

.form-label {
  font-weight: 600;
  color: #3D2C20;
}

.form-control {
  background-color: #F5F5DC;
  border: 1px solid #B8860B;
  padding: 0.75rem 1rem;
}
.form-control:focus {
    background-color: #fff;
    box-shadow: 0 0 0 0.25rem rgba(184, 134, 11, 0.25);
}

.btn-auth {
  padding: 0.75rem;
  font-size: 1rem;
  font-weight: 600;
  background-color: #B8860B;
  border-color: #B8860B;
  transition: background-color 0.3s;
}

.btn-auth:hover {
  background-color: #DAA520;
}

.auth-links {
  display: flex;
  justify-content: space-between;
  margin-top: 1rem;
}

.auth-links a {
  color: #8B4513;
  text-decoration: none;
  font-size: 0.9rem;
}

.social-login-divider {
  text-align: center;
  margin: 2rem 0;
  color: #8B4513;
  font-size: 0.9rem;
  position: relative;
}

.social-login-divider::before, .social-login-divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: 35%;
  height: 1px;
  background-color: #D2B48C;
}

.social-login-divider::before {
  left: 0;
}

.social-login-divider::after {
  right: 0;
}

.social-login-buttons .btn-social {
  width: 100%;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem;
}

.btn-google {
  background-color: #fff;
  color: #4285F4;
  border: 1px solid #4285F4;
}

.btn-google:hover {
  background-color: #f8f9fa;
}
</style>
