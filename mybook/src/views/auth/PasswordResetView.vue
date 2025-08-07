<template>
  <div class="auth-page">
    <div class="auth-container">
      <h1 class="auth-title">비밀번호 재설정</h1>
      <p class="auth-subtitle">가입 시 사용한 이메일 주소를 입력하시면, 비밀번호 재설정 링크를 보내드립니다.</p>
      
      <form v-if="!isEmailSent" @submit.prevent="handlePasswordReset">
        <div class="mb-3">
          <label for="email" class="form-label">이메일</label>
          <input type="email" v-model="email" class="form-control" id="email" placeholder="you@example.com" required>
        </div>
        
        <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
        
        <button type="submit" class="btn btn-primary w-100 btn-auth" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
          {{ isLoading ? '전송 중...' : '재설정 이메일 보내기' }}
        </button>
      </form>

      <div v-else class="alert alert-success">
        <p><strong>{{ email }}</strong>으로 비밀번호 재설정 이메일을 보냈습니다.</p>
        <p>이메일을 확인하여 비밀번호를 재설정해주세요. 스팸 메일함도 확인해보세요.</p>
      </div>

      <div class="auth-links">
        <router-link to="/login">로그인으로 돌아가기</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter, RouterLink } from 'vue-router';

// --- Router ---
const router = useRouter();

// --- Reactive State ---
const email = ref('');
const errorMessage = ref('');
const isLoading = ref(false);
const isEmailSent = ref(false);

// --- Functions ---
async function handlePasswordReset() {
  if (!email.value) {
    errorMessage.value = '이메일 주소를 입력해주세요.';
    return;
  }
  isLoading.value = true;
  errorMessage.value = '';

  // Simulate API call
  await new Promise(resolve => setTimeout(resolve, 1500)); 

  // Simulate success or failure based on email for testing
  if (email.value.includes('@') && email.value.includes('.') && !email.value.includes('fail')) {
    isEmailSent.value = true;
  } else {
    errorMessage.value = '이메일 전송에 실패했습니다. 유효한 이메일을 입력해주세요. (더미)';
  }
  isLoading.value = false;
}

</script>

<style scoped>
/* Using the same styles as LoginView for consistency */
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
  text-align: center;
  margin-top: 1.5rem;
}

.auth-links a {
  color: #8B4513;
  text-decoration: none;
  font-size: 0.9rem;
}
</style>
